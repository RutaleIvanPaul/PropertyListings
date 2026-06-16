# Property Listings

A small Android app that fetches property data from a remote API and presents it as a
scrollable list and a detail screen, with live currency conversion of the nightly price.

- **List** — every property with its name, featured status, a quality-graded `/10` rating pill,
  and the lowest price per night.
- **Detail** — hero image, key information (type, location, address, overview, rating breakdown),
  and a currency toggle that converts the price across **EUR / USD / GBP** using live exchange rates.
- **Stateful and resilient** — distinct Loading / Content / Empty / Error states, pull-to-refresh,
  an in-memory last-good cache, and graceful handling of every failure mode at the data boundary.

## Tech stack

- **Kotlin**, **Jetpack Compose** + **Material 3**
- **MVI** presentation (immutable `UiState`, sealed `Intent`, one-shot `Effect`)
- **Coroutines + Flow**
- **Hilt** for dependency injection
- **Retrofit + OkHttp + kotlinx.serialization** for networking
- **Coil** for image loading
- Tests: **JUnit4**, **MockK**, **Turbine**, **Robolectric** (JVM-backed Compose render tests)

Single module, proportionate clean architecture (`data` / `domain` / `presentation`). See
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the structure and
[docs/DECISIONS.md](docs/DECISIONS.md) for the notable trade-offs and why they were made.

## Building and running

Requirements: **Android Studio** (latest stable) and **JDK 17**. The Android SDK location is read
from `local.properties` (`sdk.dir=...`) or the `ANDROID_HOME` environment variable.

```bash
# Build the debug APK
./gradlew assembleDebug

# Install and run on a connected device / emulator (minSdk 24)
./gradlew installDebug
```

Or open the project in Android Studio and run the `app` configuration on a device or emulator.
The app needs network access to reach the data endpoints (the `INTERNET` permission is declared).

## Running the tests

All business logic and the ViewModels are covered by JVM unit tests (the Compose render tests run
on the JVM via Robolectric, so the whole suite runs without a device):

```bash
./gradlew test
```

The suite covers the pure converters (rating and currency, including clamp / null / missing-rate),
the mappers (including per-item degrade and HTML unescaping), the repositories against fake
services (success / network failure / unparsable / partial data, plus rates TTL and last-good
fallback), the telemetry reporter, both ViewModels (including error, empty and refresh paths), and
the key composables (list card, detail screen) as render tests.

## Architecture in brief

Dependencies point inward: `presentation → domain ← data`. The `domain` layer is pure Kotlin with
no Android dependencies and owns the models, the repository interfaces, and the pure rating /
currency logic. The `data` layer implements those interfaces — a **tolerant boundary** (nullable
DTOs) that mappers turn into a **strict core** (validated, clamped, non-null domain models), so a
single bad record never breaks a screen. The `presentation` layer is MVI: each screen exposes a
single immutable `StateFlow<UiState>`, receives `Intent`s, and the UI is a pure function of state.

The four failure modes are handled distinctly: network/timeout/5xx → error + retry; unparsable
body → error; per-item missing fields → that item degrades (the list still renders); corrupt values
→ clamped/defaulted. Live data is clean, so these paths are proven by unit tests with fakes and
fixtures rather than relying on the live API.

## Telemetry

The app measures the client-perceived duration of each real data request (from request start to
fully-parsed) and reports it to the stats endpoint. Reporting is isolated in a dedicated
`StatsReporter`, runs off the critical path on a long-lived application scope, and fails silently,
so it can never block or degrade the experience it measures. Cache hits make no network call and so
report nothing.

Both outcomes are reported under distinct action labels — `load` / `load-failed` for the property
fetch and `load-details` / `load-details-failed` for the rates fetch — so success and failure
latencies are not conflated. Unexpected failures get their own `*-unexpected` labels. The duration
is measured to the moment the call resolved; a failed call still returns its error to the caller,
with the report sent only as a side effect.

The report is sent as a `GET` with query parameters because that is the shape the provided endpoint
accepts, and the response is ignored. A production pipeline would instead **POST a batched payload**
that carries a structured outcome/error type rather than encoding it in the label string, with
retry and back-pressure. This is also noted in code on `StatsApi`.

## Note on the data source

The endpoint URLs in the original brief were pinned to a specific commit of GitHub gists owned by an
account that has since been **renamed** (`PedroTrabuloHostelworld` → `pedrotrabulo-hw`). Those exact
commit-pinned raw URLs now return **404**, although the gist **IDs and data are unchanged**. The app
therefore targets the **commit-less raw URLs** for the same gists, which always serve the latest
revision and do not break when a gist is edited. This is a deliberate, documented change from the
brief's literal URLs; the data itself is the same.

## With more time

In rough priority order, the things I would do next:

- **Standout animations** — a shimmer/skeleton loading state matching the card layout, an animated
  price transition on the currency toggle, and a shared-element list→detail transition (the list
  thumbnail and detail hero are already in place as seams for it).
- **Rates warm-up + single-flight** — pre-warm exchange rates just after the list loads, guarded by
  a `Mutex` so concurrent detail opens don't duplicate the fetch, making the first toggle instant.
  Considered and deliberately deferred for the current scope (see DECISIONS.md).
- **Persistence** — an offline-across-launches cache (e.g. Room or DataStore) if the product needed
  to survive process death; the repository interfaces leave room for it as a localised change.
- **Image polish** — a detail-screen gallery/carousel and blur-hash placeholders.
- **End-to-end instrumented tests** for navigation and rotation on a real device, alongside the
  current JVM suite.
