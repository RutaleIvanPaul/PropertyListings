package io.github.rutaleivanpaul.propertylistings.presentation.list

/**
 * Immutable UI state for the property list screen (MVI).
 *
 * The four cases are mutually exclusive by construction, so the UI can render exactly one of
 * loading, content, empty or error at any time. Concrete payloads (the mapped property list and
 * structured error information) are attached once the domain models and mappers land in the
 * data-layer and list-screen milestones; this skeleton fixes only the shape of the state.
 */
sealed interface ListUiState {

    /** Initial / in-progress load with nothing yet to show. */
    data object Loading : ListUiState

    /** Properties loaded successfully and available to render. */
    data object Content : ListUiState

    /** Load succeeded but returned no properties. */
    data object Empty : ListUiState

    /** Load failed; the screen offers a retry. */
    data object Error : ListUiState
}
