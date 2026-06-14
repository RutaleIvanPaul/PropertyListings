# Decisions

A lightweight record of notable engineering decisions and their reasoning. Expanded as the
project develops.

## Serialization: kotlinx.serialization

Chosen over Moshi. It is Kotlin-first and compiler-plugin based, avoiding reflection and an
annotation-processing step, and is the current default for new Kotlin projects.

## Persistence: in-memory cache

A last-good in-memory cache is used rather than a persistent store (e.g. Room). Nothing in
the current feature needs to survive process death, and the remote data (notably exchange
rates) is expected to change frequently. The cache exists to avoid surfacing an error on a
transient refresh failure. Repository interfaces leave room to introduce a persistent cache
later as a localised change, should offline-across-launches become a requirement.

## Network performance reporting

Request durations are measured client-side and reported to a stats endpoint after each
network call. Reporting is isolated, runs off the critical path, and fails silently so it
can never degrade the experience it measures. The provided endpoint accepts a GET; a
production telemetry pipeline would more appropriately POST a batched payload.
