# Decisions

A record of the notable engineering decisions in this project and the reasoning behind them. It
describes the system as built.

## Architecture: single-module clean architecture + MVI

Three layers (`data` / `domain` / `presentation`) in one module, with dependencies pointing inward
to a pure `domain` core. A single module is proportionate for an app of this size — multi-module
build wiring would be ceremony without payoff here — while the layer separation keeps fetching,
mapping, business logic, and UI in their own places and makes the core unit-testable without Android.

Presentation follows MVI: each screen exposes one immutable `StateFlow<UiState>`, takes a sealed
`Intent`, and renders as a pure function of state, with one-shot events modelled as a separate
`Effect` stream. Modelling state as a sealed type makes Loading / Content / Empty / Error mutually
exclusive by construction, which removes a class of "spinner and error showing at once" bugs.

## Tolerant boundary, strict core

DTOs are fully nullable and the `Json` is lenient (`ignoreUnknownKeys`, `coerceInputValues`), so
parsing never fails on an absent/unknown/coercible field. Mappers are the single place that turns
this into strict, non-null, validated domain models. The benefit is that no defensive null-checking
or clamping leaks into the ViewModels or UI — downstream code can trust the model.

This is also where the per-item failure modes live: an item missing essential fields (id, usable
name, parseable price) is dropped so the rest of the list still renders, and corrupt values
(out-of-range rating, negative price) are clamped/defaulted via the pure converters.

## Serialization: kotlinx.serialization

Chosen over Moshi. It is Kotlin-first and compiler-plugin based, avoiding reflection and a separate
annotation-processing step, and is the current default for new Kotlin projects.

## Persistence: in-memory cache

A last-good in-memory cache is used rather than a persistent store (e.g. Room). Nothing in the
current feature needs to survive process death, and the remote data (notably exchange rates) is
expected to change frequently. The cache exists to avoid surfacing an error on a transient refresh
failure, and to let the detail screen reuse list data. Repository interfaces leave room to introduce
a persistent cache later as a localised change, should offline-across-launches become a requirement.

## Detail screen reuses cached data (no refetch)

The property list already contains everything the detail screen needs, so the detail flow does not
re-fetch the property. `PropertyRepository.cachedProperty(id)` returns the already-loaded model, and
the domain `Property` carries the detail extras (district, address, rating breakdown) and the
denormalised city/country directly. The only network call the detail flow can make is for exchange
rates. A not-found id (e.g. after process death clears the cache) folds into the detail Error state,
which offers retry — there is intentionally no separate Empty state for a single item.

## Exchange-rate caching and freshness

Property prices are returned in EUR with the property list, so the list never needs the rates API.
Exchange rates are required only for the currency toggle on the detail screen, so they are fetched on
demand when a detail screen needs them — not pre-fetched.

A rates repository owns an in-memory, timestamped cache with a short time-to-live (`RATES_CACHE_TTL`
= 5 minutes). Within the TTL the cached rates are reused; once they expire they are re-fetched. If a
fetch fails, the last-good cached value is used; if nothing has ever been cached, the screen degrades
gracefully to showing the EUR price only. Because the rates and the prices share the same base
currency (EUR), conversion is a direct multiply (the converter still implements the general
cross-rate form for safety). The clock used for freshness is injected so expiry is unit-testable
without real delays.

Considered / deferred: a background warm-up of the rates just after the list loads — guarded by a
single-flight mechanism (`Mutex`) to prevent duplicate concurrent fetches — was considered to make
the first currency toggle feel instant. It was deferred because the current scope does not justify
the added concurrency complexity; the repository interface leaves room to add it later as a localised
change.

## Currency display: single-currency segmented toggle

The detail screen shows **one** currency at a time and switches between EUR / USD / GBP via a
Material 3 `SingleChoiceSegmentedButtonRow`. This was chosen as the final display form over
showing all three prices at once: a toggle is the cleaner, more scannable presentation and matches
the brief's "toggle the lowest price" intent. The ViewModel retains the fetched `Rates`, so switches
are instant and need no refetch. When only EUR is available (rates unavailable), the toggle is
hidden and the price simply shows in EUR — the show-all-three fallback was not needed.

## List card imagery

Each list card shows a fixed-size square (92dp) leading thumbnail, loaded with Coil and vertically
centred so the card stays balanced. A 1:1 crop sits naturally with the landscape source photos (no
stretch) and keeps the card compact (~5 per screen), preserving the scannability the brand is built
on — the thumbnail is a visual anchor, not a hero. The rating tier and count sit on a caption line
so the graded pill and the prominent price share one tidy bottom row.

The API exposes an `imagesGallery` array per property but **no cover/hero field** — nothing marks
which image best represents the listing. We therefore use the **first gallery image**
(`imagesGallery[0]`) as the representative thumbnail; this is a documented assumption, not a derived
truth. The production Cloudinary gallery is used, not the `images[]` array (a staging host that may
not resolve). The thumbnail's box reserves its space with a neutral background, so there is no layout
shift: the neutral fill shows while loading and on any failure (missing URL, network error) — never
a broken-image icon.

## Detail hero image

The detail screen shows a single hero image (`imagesGallery[0]`) in a fixed 3:2 frame with reserved
space (no layout shift) and a neutral placeholder/fallback, reusing Coil. A carousel, indicators, and
tap-to-expand were deliberately left out of scope. The hero is also the seam for a future
shared-element list→detail transition.

## Detail extras: included and omitted

Included, because they are unambiguous and add real value: the address (`address1` + `address2`
joined), the district name when present, and a compact rating breakdown (the sub-scores run through
the same `RatingConverter`, one labelled `/10` row each).

Omitted, with reason: `distance` (the payload names no reference point, so "km from what?" is
ambiguous) and `starRating` (17 of 20 properties have `0`; only the two hotels carry it, and a star
rating risks being confused with the `/10` quality pill).

## Rating display

A small number of records carry an `overallRating.overall` of 0, which represents the absence of a
meaningful score rather than a genuine "zero" rating. These are rendered as "No rating" rather than
"0.0", so the UI does not imply a property was rated zero. All other values convert from the source
1–100 scale to a one-decimal `/10` value (HALF_UP). The score also drives a quality-graded pill
(Option A): the tier (`RatingTier.forRating`) sets the pill's colour and label, giving an at-a-glance
trust signal that is the convention in accommodation-booking apps.

## Theme: fixed brand palette, no dynamic colour

A vibrant orange is used sparingly as an accent (featured badge, price, rating highlight) on neutral
light/dark surfaces, with explicit light and dark schemes. Material You **dynamic colour is disabled**
on purpose: the brand identity is a specific orange-on-neutral look, and recolouring it from the
device wallpaper would undermine that consistency.

## API endpoint URLs

The upstream gist URLs were pinned to a specific commit of an account that has since been renamed
(`PedroTrabuloHostelworld` → `pedrotrabulo-hw`), so those exact commit-pinned URLs return 404. The
gist IDs and data are unchanged. The app therefore targets the commit-less raw URLs for the same
gists, which always serve the latest revision and do not break when a gist is edited.

## Network performance reporting

Request durations are measured client-side and reported to a stats endpoint after each network call.
Reporting is isolated in a dedicated `StatsReporter`, runs off the critical path on a long-lived
application scope (so it survives the originating screen), and fails silently so it can never degrade
the experience it measures. Cache hits make no network call and so report nothing; the reporter never
times itself.

Both outcomes are reported, under distinct action labels — `load` / `load-failed` for the property
fetch and `load-details` / `load-details-failed` for the rates fetch — so success and failure
latencies are not conflated. The duration is measured to the point the call resolved (time-to-parsed
on success, time-to-failure on failure). A failed data call still propagates its
`NetworkError`/`ParseError` to the caller; the telemetry is purely a side effect. The label is the
only outcome information carried: there is intentionally no retry, no severity, and no error taxonomy.
The provided endpoint accepts a GET; a production pipeline would POST a batched payload that carries a
structured outcome/error type rather than encoding it in the label string.

## Data-boundary error handling (defensive catch)

The repository (the data boundary) catches defensively so an unforeseen exception degrades to an
error state for the user rather than crashing the app. This was hardened after an uncaught
`SecurityException` (missing `INTERNET` permission) surfaced on device: it was a `RuntimeException`,
not an `IOException`, so it bypassed the network/parse handling entirely and killed the process.

Handling order at the boundary: re-throw `CancellationException` first (preserve structured
concurrency); map the anticipated `SerializationException` → parse error and `IOException` /
`HttpException` → network error; and for any *other* exception take an "unexpected" branch. That
branch fails soft for the user (degrades to the generic error state) but loud for the developer: it
logs at `ERROR` with the full stack trace — a caught exception does not auto-dump to logcat the way a
crash does, so the explicit log is what restores visibility — and reports a **distinct** telemetry
label (`load-failed-unexpected` / `load-details-failed-unexpected`) so unanticipated failures are
their own signal rather than being swallowed among normal network failures. This is still only the
expected-vs-unexpected distinction: no error taxonomy, severity, or retry. In production, the
unexpected branch is also where the exception would be recorded as a non-fatal to a crash-reporting
tool (e.g. `Crashlytics.recordException`). The broad catch lives at the data boundary only.

## HTML unescaping

Property `overview` text contains HTML entities (e.g. `&#039;`). They are decoded by a small pure
`HtmlUnescape` object (decimal and hex numeric entities plus a short named-entity map), not
`android.text.Html.fromHtml`. Keeping it framework-agnostic makes it JVM-unit-testable and avoids
pulling Android text handling into the mapper; `&amp;` is resolved last so escaped sequences are not
double-decoded.

## Testing approach: JVM-only suite

The entire test suite runs on the JVM with no device or emulator. Pure logic and mappers are tested
directly; repositories are tested against fake API services (success / network failure / unparsable /
partial data, plus rates TTL and last-good fallback using a fake `TimeProvider`); ViewModels are
tested with Turbine over their state/effect streams including error, empty, and refresh paths. The
list card and detail screen are exercised as render tests via **Robolectric** (with a Coil fake image
engine) rather than on-device instrumented tests, so they run fast and in CI alongside the rest of
the suite. Tests use captured JSON fixtures and fakes, never the live API, so the four failure modes
are proven deterministically even though the live data is currently clean.
