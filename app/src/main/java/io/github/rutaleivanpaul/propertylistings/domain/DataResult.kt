package io.github.rutaleivanpaul.propertylistings.domain

/**
 * The outcome of a repository fetch.
 *
 * Models the two failure modes that abort a whole request distinctly, so the UI can react
 * differently to each:
 * - [NetworkError] — the request never produced a usable response (no connectivity, timeout, 5xx).
 * - [ParseError] — a response arrived but its body could not be deserialized at all.
 *
 * The other two failure modes from the brief are handled lower down and never surface here: a
 * single item missing fields (degraded per-item) or carrying corrupt values (clamped/defaulted)
 * is absorbed by the mapper, so a partially-bad payload still yields a [Success] with the valid
 * items.
 */
sealed interface DataResult<out T> {

    /** The fetch succeeded; [data] is the mapped, validated payload. */
    data class Success<out T>(val data: T) : DataResult<T>

    /** The request failed to produce a usable response (connectivity, timeout, 5xx). */
    data object NetworkError : DataResult<Nothing>

    /** A response arrived but its body could not be parsed. */
    data object ParseError : DataResult<Nothing>
}
