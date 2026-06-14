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

## Exchange-rate caching and freshness

Property prices are returned in EUR with the property list, so the list never needs the rates
API. Exchange rates are required only for the currency toggle on the detail screen, so they are
fetched on demand when a detail screen needs them — not pre-fetched.

A rates repository owns an in-memory, timestamped cache with a short time-to-live (TTL). While
the cached rates are within the TTL they are reused; once they expire they are re-fetched. If a
fetch fails, the last-good cached value is used; if nothing has ever been cached, the screen
degrades gracefully to showing the EUR price only. Because the rates and the prices share the
same base currency (EUR), conversion is a direct multiply. The clock used for freshness is
injected so expiry is unit-testable without real delays.

Considered / deferred: a background warm-up of the rates just after the list loads — guarded by
a single-flight mechanism to prevent duplicate concurrent fetches — was considered to make the
first currency toggle feel instant. It was deferred because the current scope does not justify
the added concurrency complexity; the repository interface leaves room to add it later as a
localised change.

## Rating display

A small number of records carry an `overallRating.overall` of 0, which represents the absence
of a meaningful score rather than a genuine "zero" rating. These are rendered as "No rating"
rather than "0.0", so the UI does not imply a property was rated zero. All other values convert
from the source 1–100 scale to a one-decimal /10 value.

## API endpoint URLs

The upstream gist URLs were pinned to a specific commit of an account that has since been
renamed (`PedroTrabuloHostelworld` → `pedrotrabulo-hw`), so those exact commit-pinned URLs
return 404. The gist IDs and data are unchanged. The app therefore targets the commit-less raw
URLs for the same gists, which always serve the latest revision and do not break when a gist is
edited.

## Network performance reporting

Request durations are measured client-side and reported to a stats endpoint after each
network call. Reporting is isolated, runs off the critical path, and fails silently so it
can never degrade the experience it measures.

Both outcomes are reported, under distinct action labels — `load`/`load-failed` for the property
fetch and `load-details`/`load-details-failed` for the rates fetch — so success and failure
latencies are not conflated. The duration is measured to the point the call resolved (time-to-parsed
on success, time-to-failure on failure). A failed data call still propagates its
`NetworkError`/`ParseError` to the caller; the telemetry is purely a side effect. The label is the
only outcome information carried: there is intentionally no retry, no severity, and no error
taxonomy. The provided endpoint accepts a GET; a production pipeline would POST a batched payload
that carries a structured outcome/error type rather than encoding it in the label string.
