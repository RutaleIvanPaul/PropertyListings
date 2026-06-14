package io.github.rutaleivanpaul.propertylistings.presentation.list

/**
 * User intents for the property list screen (MVI).
 *
 * Intents are the only way the UI asks the ViewModel to do something; the ViewModel reduces them
 * into [ListUiState]. Handling is wired in the list-screen milestone.
 */
sealed interface ListIntent {

    /** Load the property list (first display). */
    data object Load : ListIntent

    /** Pull-to-refresh the property list. */
    data object Refresh : ListIntent

    /** Retry after an error. */
    data object Retry : ListIntent

    /** Open the detail screen for the property with the given id. */
    data class SelectProperty(val id: Int) : ListIntent
}
