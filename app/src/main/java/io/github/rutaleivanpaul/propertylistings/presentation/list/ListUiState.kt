package io.github.rutaleivanpaul.propertylistings.presentation.list

import io.github.rutaleivanpaul.propertylistings.domain.model.Property

/**
 * Immutable UI state for the property list screen (MVI).
 *
 * The four cases are mutually exclusive by construction, so the UI can render exactly one of
 * loading, content, empty or error at any time. The ViewModel that reduces intents into this state
 * is introduced in the list-screen milestone.
 */
sealed interface ListUiState {

    /** Initial / in-progress load with nothing yet to show. */
    data object Loading : ListUiState

    /**
     * Properties loaded successfully and available to render.
     *
     * @property properties the mapped, non-empty list to display.
     * @property isRefreshing whether a pull-to-refresh is in flight over the existing content (the
     *   list stays visible rather than reverting to [Loading]).
     */
    data class Content(
        val properties: List<Property>,
        val isRefreshing: Boolean = false,
    ) : ListUiState

    /** Load succeeded but returned no properties — a distinct, non-error empty state. */
    data object Empty : ListUiState

    /** Load failed; the screen offers a retry. */
    data object Error : ListUiState
}
