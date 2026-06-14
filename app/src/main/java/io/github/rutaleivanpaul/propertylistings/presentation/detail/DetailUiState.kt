package io.github.rutaleivanpaul.propertylistings.presentation.detail

/**
 * Immutable UI state for the property detail screen (MVI).
 *
 * Mirrors the list screen's four mutually-exclusive cases for consistency. Concrete payloads (the
 * selected property and the currently-selected currency price) are attached in the detail-screen
 * milestone; this skeleton fixes only the shape of the state.
 */
sealed interface DetailUiState {

    /** Loading the selected property's details. */
    data object Loading : DetailUiState

    /** Details loaded successfully and available to render. */
    data object Content : DetailUiState

    /** The requested property could not be found. */
    data object Empty : DetailUiState

    /** Load failed; the screen offers a retry. */
    data object Error : DetailUiState
}
