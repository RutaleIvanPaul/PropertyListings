package io.github.rutaleivanpaul.propertylistings.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.rutaleivanpaul.propertylistings.R
import io.github.rutaleivanpaul.propertylistings.domain.model.Property

/**
 * Stateful entry point for the property list screen.
 *
 * Collects [ListUiState] and one-shot [ListEffect]s from [ListViewModel] and renders the
 * appropriate state. The composables below are pure functions of that state — all decisions live in
 * the ViewModel; the UI only forwards [ListIntent]s and consumes effects (navigation, the transient
 * refresh-failure notice).
 *
 * @param onNavigateToDetail invoked when a property is tapped; wired to navigation in M4.
 */
@Composable
fun ListScreen(
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshFailedMessage = stringResource(R.string.list_refresh_failed)

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ListEffect.NavigateToDetail -> onNavigateToDetail(effect.propertyId)
                ListEffect.ShowRefreshError -> snackbarHostState.showSnackbar(refreshFailedMessage)
            }
        }
    }

    ListScaffold(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListScaffold(
    state: ListUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (ListIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = (state as? ListUiState.Content)?.properties?.firstOrNull()?.city
        ?.let { stringResource(R.string.list_title, it) }
        ?: stringResource(R.string.list_title_fallback)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(title) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (state) {
            ListUiState.Loading -> LoadingState(Modifier.padding(padding))
            is ListUiState.Content -> RefreshableContent(
                properties = state.properties,
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(ListIntent.Refresh) },
                onPropertyClick = { onIntent(ListIntent.SelectProperty(it)) },
                contentPadding = padding,
            )

            ListUiState.Empty -> RefreshableMessage(
                onRefresh = { onIntent(ListIntent.Refresh) },
                contentPadding = padding,
            ) {
                MessageBlock(
                    title = stringResource(R.string.list_empty_title),
                    body = stringResource(R.string.list_empty_body),
                )
            }

            ListUiState.Error -> RefreshableMessage(
                onRefresh = { onIntent(ListIntent.Retry) },
                contentPadding = padding,
            ) {
                MessageBlock(
                    title = stringResource(R.string.list_error_title),
                    body = stringResource(R.string.list_error_body),
                ) {
                    Button(onClick = { onIntent(ListIntent.Retry) }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
        }
    }
}

/**
 * Initial load: a shimmering skeleton that previews the card layout (thumbnail + text-line
 * placeholders), so the loading state shows the shape of the content to come rather than a bare
 * spinner. The whole block carries a single spoken "loading" description; the placeholders
 * themselves are decorative. No pull-to-refresh during the first load.
 */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.list_loading)
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = description },
    ) {
        PropertyListSkeleton()
    }
}

/** The list of property cards, wrapped in pull-to-refresh. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshableContent(
    properties: List<Property>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPropertyClick: (Int) -> Unit,
    contentPadding: PaddingValues,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.padding(contentPadding),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items = properties, key = { it.id }) { property ->
                PropertyCard(property = property, onClick = onPropertyClick)
            }
        }
    }
}

/**
 * Full-screen message (empty / error) wrapped in pull-to-refresh. A scrollable container is used so
 * the pull gesture is available even though the content does not itself scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshableMessage(
    onRefresh: () -> Unit,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = false,
        onRefresh = onRefresh,
        modifier = Modifier.padding(contentPadding),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    content()
                }
            }
        }
    }
}

/** Centred title + body, with an optional action (e.g. the error Retry button). */
@Composable
private fun MessageBlock(
    title: String,
    body: String,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(32.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.padding(4.dp))
            action()
        }
    }
}
