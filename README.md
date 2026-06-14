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
