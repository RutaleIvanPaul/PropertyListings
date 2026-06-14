package io.github.rutaleivanpaul.propertylistings.data.stats

import android.util.Log
import io.github.rutaleivanpaul.propertylistings.data.remote.api.StatsApi
import io.github.rutaleivanpaul.propertylistings.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The telemetry events the app reports, paired with the label sent to the stats endpoint.
 *
 * Success and failure of the same request use distinct labels (`load` vs `load-failed`) so the two
 * latency populations are never conflated. This is the only outcome information carried — there is
 * deliberately no error taxonomy or severity here; a production POST body would carry a structured
 * outcome/error type instead of encoding it in the label string.
 */
enum class StatsAction(val label: String) {
    /** A successful properties fetch (initial load and pull-to-refresh). */
    LOAD("load"),

    /** An *anticipated* failed properties fetch — a network or parse failure. */
    LOAD_FAILED("load-failed"),

    /**
     * An *unanticipated* failed properties fetch — an exception we did not expect at the boundary
     * (i.e. not network or parse). Distinct so these surface as their own signal rather than hiding
     * among normal network failures. Still just the expected-vs-unexpected distinction — no error
     * taxonomy, severity, or retry implied.
     */
    LOAD_FAILED_UNEXPECTED("load-failed-unexpected"),

    /** A successful exchange-rates fetch made for the detail screen's currency toggle. */
    LOAD_DETAILS("load-details"),

    /** An *anticipated* failed exchange-rates fetch — a network or parse failure. */
    LOAD_DETAILS_FAILED("load-details-failed"),

    /**
     * An *unanticipated* failed exchange-rates fetch — see [LOAD_FAILED_UNEXPECTED]; the same
     * expected-vs-unexpected distinction for the rates flow.
     */
    LOAD_DETAILS_FAILED_UNEXPECTED("load-details-failed-unexpected"),
}

/**
 * Reports client-perceived request durations to the stats endpoint.
 *
 * Single responsibility: it only reports. Design constraints from the brief, all honoured here:
 * - **Never blocks the UI:** [report] returns immediately, launching the call on a long-lived
 *   [ApplicationScope] so it also survives the originating screen.
 * - **Fire-and-forget / silent failure:** the response is ignored and any error is swallowed with a
 *   debug log only — telemetry must never degrade the experience it measures.
 * - **Never times itself:** callers time the real request; this class only transmits the result.
 */
@Singleton
class StatsReporter @Inject constructor(
    private val statsApi: StatsApi,
    @ApplicationScope private val scope: CoroutineScope,
) {

    /**
     * Sends a single measurement. Returns immediately; the network call happens off the caller's
     * thread and its outcome is intentionally not surfaced.
     *
     * @param action what was measured.
     * @param durationMillis time from request start to fully-parsed response (client-perceived).
     */
    fun report(action: StatsAction, durationMillis: Long) {
        scope.launch {
            runCatching { statsApi.report(action.label, durationMillis) }
                .onFailure { Log.d(TAG, "Stats report failed for ${action.label}; ignoring.", it) }
        }
    }

    private companion object {
        const val TAG = "StatsReporter"
    }
}
