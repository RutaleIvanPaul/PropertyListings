package io.github.rutaleivanpaul.propertylistings.presentation.list

/**
 * One-shot side effects emitted by [ListViewModel] that the UI consumes exactly once.
 *
 * Kept separate from [ListUiState] because these are events, not state: replaying them on
 * recomposition or configuration change (as state would) must not re-trigger navigation or a
 * duplicate message.
 */
sealed interface ListEffect {

    /** Navigate to the detail screen for the given property. Wired to navigation in M4. */
    data class NavigateToDetail(val propertyId: Int) : ListEffect

    /** A pull-to-refresh failed while existing content remained on screen; show a transient notice. */
    data object ShowRefreshError : ListEffect
}
