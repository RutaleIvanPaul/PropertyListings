package io.github.rutaleivanpaul.propertylistings.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.rutaleivanpaul.propertylistings.domain.DataResult
import io.github.rutaleivanpaul.propertylistings.domain.repository.PropertyRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI ViewModel for the property list screen.
 *
 * Exposes an immutable [ListUiState] via [state] and one-shot [ListEffect]s via [effects]; the UI is
 * a pure function of that state and only communicates back through [onIntent]. All business
 * decisions (what state a result produces, when to keep last-good content) live here, not in the
 * composables.
 *
 * The repository owns the network/cache; this ViewModel maps a [DataResult] into UI state and
 * applies one presentation rule the repository can't: on a *refresh* failure, the existing content
 * is kept on screen (the last-good list) with a transient error notice, rather than dropping to a
 * full-screen error.
 */
@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: PropertyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val state: StateFlow<ListUiState> = _state.asStateFlow()

    private val _effects = Channel<ListEffect>(Channel.BUFFERED)
    val effects: Flow<ListEffect> = _effects.receiveAsFlow()

    init {
        load(forceRefresh = false, isRefresh = false)
    }

    /** Single entry point for the UI to drive the screen. */
    fun onIntent(intent: ListIntent) {
        when (intent) {
            ListIntent.Load -> load(forceRefresh = false, isRefresh = false)
            ListIntent.Refresh -> load(forceRefresh = true, isRefresh = true)
            ListIntent.Retry -> load(forceRefresh = true, isRefresh = false)
            is ListIntent.SelectProperty -> emitEffect(ListEffect.NavigateToDetail(intent.id))
        }
    }

    private fun load(forceRefresh: Boolean, isRefresh: Boolean) {
        viewModelScope.launch {
            val previous = _state.value
            // A refresh keeps existing content visible (with a spinner); a first load/retry shows
            // the dedicated loading state.
            _state.value = if (isRefresh && previous is ListUiState.Content) {
                previous.copy(isRefreshing = true)
            } else {
                ListUiState.Loading
            }

            when (val result = repository.getProperties(forceRefresh)) {
                is DataResult.Success ->
                    _state.value = if (result.data.isEmpty()) {
                        ListUiState.Empty
                    } else {
                        ListUiState.Content(result.data)
                    }

                DataResult.NetworkError, DataResult.ParseError ->
                    if (isRefresh && previous is ListUiState.Content) {
                        // Transient refresh failure: keep the last-good list, just notify.
                        _state.value = previous.copy(isRefreshing = false)
                        emitEffect(ListEffect.ShowRefreshError)
                    } else {
                        _state.value = ListUiState.Error
                    }
            }
        }
    }

    private fun emitEffect(effect: ListEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
