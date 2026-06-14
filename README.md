# Property Listings

An Android app that fetches hostel property data from a remote API and presents it as a
scrollable list with a detail view, including live currency conversion of nightly prices.

> Status: in active development.

## Tech stack

- Kotlin
- Jetpack Compose (Material 3)
- MVI presentation pattern
- Coroutines + Flow
- Hilt (dependency injection)
- Retrofit + OkHttp + kotlinx.serialization

## Building

Requires Android Studio (latest stable) and JDK 17.
./gradlew assembleDebug

To run the unit tests:
./gradlew test

## Architecture

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the structure and
[docs/DECISIONS.md](docs/DECISIONS.md) for notable trade-offs.

## Telemetry

The app measures the client-perceived duration of each real data request (timed from request start
through to the fully-parsed response) and reports it to the stats endpoint. Reporting is isolated
in a dedicated `StatsReporter`, runs off the critical path on its own coroutine, and fails silently
so it can never block or degrade the experience it measures. Cache hits make no network call and so
report nothing.

The report is sent as a `GET` with query parameters because that is the shape the provided endpoint
accepts, and the response is ignored. A production telemetry pipeline would instead **POST a batched
payload** — multiple measurements in a structured body, with retry and back-pressure — rather than
one fire-and-forget `GET` per request. This is also noted in code on `StatsApi`.
