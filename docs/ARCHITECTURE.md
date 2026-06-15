# Architecture

This document describes the system as built.

## Overview

A single-module app in proportionate clean architecture, with three layers and dependencies that
point strictly inward:

```
presentation  ──►  domain  ◄──  data
   (UI, MVI)      (pure core)   (API, mappers, repos)
```

- **domain** — framework-agnostic models, repository interfaces, and the pure business logic
  (rating and currency conversion). No Android dependencies.
- **data** — Retrofit APIs, nullable DTOs, mappers, and the repository implementations that fulfil
  the domain interfaces.
- **presentation** — Compose UI and ViewModels following an MVI pattern.

The domain layer never depends on `data` or `presentation`; `data` and `presentation` both depend
only on the domain abstractions. Hilt supplies the concrete `data` implementations to the
presentation layer at runtime, so the dependency rule holds at compile time.

## Package structure

```
io.github.rutaleivanpaul.propertylistings
├─ MainActivity                     // single Activity, edge-to-edge, hosts the nav graph
├─ PropertyListingsApp              // @HiltAndroidApp
├─ domain
│  ├─ DataResult                    // sealed: Success | NetworkError | ParseError
│  ├─ model                         // Property, Money, Currency, Rates, PropertyType,
│  │                                //   RatingScore/RatingCategory
│  ├─ converter                     // RatingConverter, CurrencyConverter (pure objects)
│  ├─ repository                    // PropertyRepository, RatesRepository (interfaces)
│  └─ time                          // TimeProvider (injected clock)
├─ data
│  ├─ remote/api                    // PropertyApi, RatesApi, StatsApi (Retrofit)
│  ├─ remote/dto                    // nullable DTOs (tolerant boundary)
│  ├─ mapper                        // PropertyMapper, RatesMapper, HtmlUnescape
│  ├─ repository                    // PropertyRepositoryImpl, RatesRepositoryImpl
│  └─ stats                         // StatsReporter
├─ di                               // NetworkModule, RepositoryModule, CoroutinesModule, Qualifiers
└─ presentation
   ├─ theme                         // Color, Theme, Type (brand orange, no dynamic colour)
   ├─ common                        // MoneyFormatter, PropertyTypeLabel, RatingCategoryLabel
   ├─ list                          // ListUiState/Intent/Effect, ListViewModel, ListScreen,
   │                                //   PropertyCard, RatingPill, RatingTier
   ├─ detail                        // DetailUiState/Intent, DetailViewModel, DetailScreen
   └─ navigation                    // Destinations, AppNavHost
```

## Domain

**Models** are strict: non-null, validated, framework-agnostic. `Property` carries everything both
screens need — including denormalised `city`/`country` (the API holds these once at response level)
and the detail extras (`district`, `address`, `ratingBreakdown`) — so the detail screen is
self-contained and needs no second network call. `Property.hasRating` distinguishes a genuine score
from the `0` "no rating" sentinel. `Money` pairs an amount with its `Currency`; `Rates` is a base
currency plus a `Map<Currency, Double>` of units-per-base.

**`DataResult<T>`** is the repository outcome: `Success`, `NetworkError`, or `ParseError`. These are
the two failure modes the UI reacts to distinctly; per-item failures are absorbed earlier (see Data).

**Pure logic** lives in two stateless objects so it is trivially unit-testable with no Android:
- `RatingConverter` — clamps the raw rating to `0..100`, divides by 10, rounds HALF_UP to one
  decimal; `null` → `0.0` (rendered as "No rating").
- `CurrencyConverter` — converts a `Money` to a target currency via the base rate; returns `null` if
  a required rate is missing, leaving the missing-rate policy to the caller.

**`TimeProvider`** is a `fun interface` over the wall clock, injected wherever elapsed time matters
(rates cache freshness, request timing) so those behaviours are testable with a fake clock.

## Data — tolerant boundary, strict core

DTOs have **every field nullable** and the `Json` is configured with `ignoreUnknownKeys = true`
(the real payloads carry ~40 fields we discard) and `coerceInputValues = true`. Parsing therefore
never fails on an absent, unknown, or coercible field.

Mappers turn the tolerant DTOs into strict domain models and are where validation and the per-item
failure modes are handled:
- **Missing essential fields** (no id / blank name / unparseable price) → the item is dropped; the
  rest of the list still renders.
- **Corrupt values** (out-of-range rating, negative price) → clamped/defaulted via the converters.
- `HtmlUnescape` decodes the HTML entities present in `overview` (it is a small pure decoder, not
  `android.text.Html`, so it stays JVM-testable).

**Repositories** own in-memory caches:
- `PropertyRepositoryImpl` keeps the last-good list. `getProperties(forceRefresh)` returns the cache
  instantly when not forced; `cachedProperty(id)` lets the detail screen reuse already-loaded data
  with no refetch.
- `RatesRepositoryImpl` keeps a **timestamped** cache with a 5-minute TTL (`RATES_CACHE_TTL`). Within
  TTL it returns the cache (no network); when expired it refetches; on failure it returns last-good;
  if nothing was ever cached it returns `null` and the toggle degrades to EUR-only.

Both repositories run network + mapping on the injected IO dispatcher and apply a **defensive catch
at the data boundary**: re-throw `CancellationException`; map `SerializationException` → `ParseError`
and `IOException`/`HttpException` → `NetworkError`; and for any *other* exception degrade to an error
state while logging the full stack trace and reporting a distinct `*-unexpected` telemetry label.

## Telemetry

`StatsReporter` reports the client-perceived duration of each real request on a long-lived
application scope (`SupervisorJob` on the IO dispatcher). It is fire-and-forget and silent-failing,
never blocks the UI, and never times itself. Labels distinguish success/failure and expected/
unexpected (`load`, `load-failed`, `load-failed-unexpected`, and the `load-details` equivalents).

## Presentation (MVI)

Each screen exposes a single immutable `StateFlow<UiState>` and receives user actions as a sealed
`Intent`; the UI is a pure function of the current state. State is a sealed type so Loading /
Content / Empty / Error are mutually exclusive by construction.

- **List** — `ListUiState` (`Loading` / `Content(isRefreshing)` / `Empty` / `Error`), `ListIntent`
  (`Load` / `Refresh` / `Retry` / `SelectProperty`), and one-shot `ListEffect`
  (`NavigateToDetail` / `ShowRefreshError`). A failed pull-to-refresh keeps the last-good content on
  screen and surfaces a transient error effect rather than discarding the list.
- **Detail** — `DetailUiState` (`Loading` / `Content` / `Error`; there is no Empty — a not-found id
  folds into Error), `DetailIntent` (`Load` / `Retry` / `SelectCurrency`). The ViewModel reads the
  property from the cache, fetches rates once, computes the available currencies (EUR always, plus
  any with a live rate), retains the `Rates` so currency switches need no refetch, and converts the
  displayed price reactively.

The graded **rating pill** (Option A) is driven by `RatingTier.forRating(...)`, a pure mapping from
the `/10` score to a tier (colour + label); `RatingPill` collapses its content into a single
semantic node so screen readers announce e.g. "Rated 8.7 out of 10, Fabulous".

## Navigation

A flat two-destination Navigation-Compose graph (`AppNavHost`): `list` and `detail/{propertyId}`.
The list emits a property id on tap, the host builds the typed route, and the detail ViewModel reads
the id back from `SavedStateHandle`. Back is the default pop.

## Theme

A fixed brand palette (vibrant orange used sparingly on neutral light/dark surfaces) with explicit
light and dark schemes. Material You **dynamic colour is deliberately disabled** so the brand
identity is consistent across devices.

## Dependency injection

Hilt wires everything from `SingletonComponent`:
- `NetworkModule` — the tolerant `Json`, `OkHttpClient` (with a BASIC logging interceptor),
  `Retrofit` (base `https://gist.githubusercontent.com/`), and the three API interfaces.
- `RepositoryModule` — `@Binds` the repository interfaces to their implementations.
- `CoroutinesModule` — the `@IoDispatcher`, the `@ApplicationScope` (for fire-and-forget telemetry),
  and the real `TimeProvider`.
- `Qualifiers` — `@IoDispatcher` and `@ApplicationScope`.

ViewModels depend only on the repository interfaces, so any implementation (real or fake) is
substitutable — which is exactly how the tests inject fakes.

## Testing strategy

The whole suite runs on the JVM (no device needed). Pure logic and mappers are tested directly;
repositories are tested against fake API services covering success, network failure, unparsable
bodies, and partial data, plus the rates TTL and last-good fallback (with a fake `TimeProvider`).
ViewModels are tested with Turbine over their `StateFlow`/effects, including error, empty, and
refresh paths. The list card and detail screen are exercised as **Robolectric** render tests with a
Coil fake image engine. Tests use captured JSON fixtures and fakes, never the live API.
