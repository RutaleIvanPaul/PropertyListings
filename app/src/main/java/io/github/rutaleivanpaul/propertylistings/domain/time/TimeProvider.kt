package io.github.rutaleivanpaul.propertylistings.domain.time

/**
 * Abstraction over the current time.
 *
 * Injected wherever the code reasons about elapsed time (rates cache freshness, request timing) so
 * those behaviours can be unit-tested deterministically by substituting a fake clock — no real
 * delays, no flakiness — instead of calling [System.currentTimeMillis] directly.
 */
fun interface TimeProvider {
    /** The current wall-clock time in milliseconds since the Unix epoch. */
    fun nowMillis(): Long
}
