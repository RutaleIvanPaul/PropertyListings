package io.github.rutaleivanpaul.propertylistings.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.rutaleivanpaul.propertylistings.R
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.RatingScore
import io.github.rutaleivanpaul.propertylistings.presentation.common.MoneyFormatter
import io.github.rutaleivanpaul.propertylistings.presentation.common.labelRes
import io.github.rutaleivanpaul.propertylistings.presentation.list.RatingPill
import java.util.Locale

/** Single hero image. A 3:2 frame is a natural fit for landscape property photos and reserves the
 * vertical space up-front so no content reflows when the bitmap loads. */
private val HeroAspectRatio = 3f / 2f

/**
 * Stateful entry point for the property detail screen.
 *
 * Collects [DetailUiState] from [DetailViewModel] and renders it; the composables are a pure
 * function of that state and only forward [DetailIntent]s. The property is read from the cache
 * populated by the list — no refetch — so the only network call here is for exchange rates.
 *
 * @param onBack invoked when the user navigates back (top-bar arrow / system back).
 */
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DetailScaffold(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        modifier = modifier,
    )
}

/** Stateless detail UI — a pure function of [state]. Exposed (internal) for render tests. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailScaffold(
    state: DetailUiState,
    onIntent: (DetailIntent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = (state as? DetailUiState.Content)?.property?.name.orEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back_description),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when (state) {
            DetailUiState.Loading -> LoadingState(Modifier.padding(padding))
            is DetailUiState.Content -> DetailContent(
                content = state,
                onIntent = onIntent,
                modifier = Modifier.padding(padding),
            )

            DetailUiState.Error -> ErrorState(
                onRetry = { onIntent(DetailIntent.Retry) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

/** Centred spinner with a spoken label while the property and rates load. */
@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.detail_loading)
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

/** Load failed or the property was not found; offer a retry. */
@Composable
private fun ErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = stringResource(R.string.detail_error_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.detail_error_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Button(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        }
    }
}

/** The loaded property: hero image, then meta, price toggle, overview and rating breakdown. */
@Composable
private fun DetailContent(
    content: DetailUiState.Content,
    onIntent: (DetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val property = content.property
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HeroImage(imageUrl = property.imageUrls.firstOrNull(), propertyName = property.name)

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            PropertyHeader(property)
            PriceSection(content, onIntent)
            if (property.overview.isNotBlank()) {
                Section(title = stringResource(R.string.detail_overview_title)) {
                    Text(
                        text = property.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (property.ratingBreakdown.isNotEmpty()) {
                Section(title = stringResource(R.string.detail_breakdown_title)) {
                    RatingBreakdown(property.ratingBreakdown)
                }
            }
        }
    }
}

/**
 * Hero image with reserved space.
 *
 * The neutral background fills the frame while loading, on failure, or when there is no URL — never
 * a broken-image icon — so the screen is fully usable with no image and nothing reflows when the
 * bitmap swaps in. Decorative (null description) when absent.
 */
@Composable
private fun HeroImage(
    imageUrl: String?,
    propertyName: String,
    modifier: Modifier = Modifier,
) {
    val description = imageUrl?.let { stringResource(R.string.property_image_description, propertyName) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = description,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(HeroAspectRatio)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** Type · district, the rating pill, and the city/country + street address. */
@Composable
private fun PropertyHeader(property: Property) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RatingPill(rating = property.ratingOutOf10)
            Text(
                text = typeAndDistrict(property),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column {
            Text(
                text = stringResource(R.string.property_location, property.city, property.country),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (property.address.isNotBlank()) {
                Text(
                    text = property.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Lowest price with the currency toggle. The toggle is shown only when more than one currency is
 * available; otherwise the price degrades to EUR-only with no redundant single-option control. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceSection(
    content: DetailUiState.Content,
    onIntent: (DetailIntent) -> Unit,
) {
    val priceDescription = stringResource(
        R.string.price_per_night_description,
        MoneyFormatter.format(content.displayedPrice),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.detail_price_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        if (content.availableCurrencies.size > 1) {
            val selectorDescription = stringResource(R.string.detail_currency_selector_description)
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.semantics { contentDescription = selectorDescription },
            ) {
                content.availableCurrencies.forEachIndexed { index, currency ->
                    SegmentedButton(
                        selected = currency == content.selectedCurrency,
                        onClick = { onIntent(DetailIntent.SelectCurrency(currency)) },
                        shape = SegmentedButtonDefaults.itemShape(index, content.availableCurrencies.size),
                    ) {
                        Text(currency.isoCode)
                    }
                }
            }
        }
        Text(
            text = MoneyFormatter.format(content.displayedPrice),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { contentDescription = priceDescription },
        )
    }
}

/** Compact per-aspect sub-scores: one labelled `/10` row each — scannable, not a dashboard. */
@Composable
private fun RatingBreakdown(scores: List<RatingScore>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        scores.forEach { score ->
            val label = stringResource(score.category.labelRes())
            val value = String.format(Locale.US, "%.1f", score.scoreOutOf10)
            val description = stringResource(R.string.detail_breakdown_item_description, label, value)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { contentDescription = description },
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/** A titled content block used for the overview and rating-breakdown sections. */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        content()
    }
}

/** "Hostel · Temple Bar" when a district is known, otherwise just the type label. */
@Composable
private fun typeAndDistrict(property: Property): String {
    val type = stringResource(property.type.labelRes())
    return if (property.district.isNotBlank()) {
        stringResource(R.string.detail_type_district, type, property.district)
    } else {
        type
    }
}
