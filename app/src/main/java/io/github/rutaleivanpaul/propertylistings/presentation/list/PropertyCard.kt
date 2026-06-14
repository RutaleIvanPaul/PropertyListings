package io.github.rutaleivanpaul.propertylistings.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.github.rutaleivanpaul.propertylistings.R
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType
import io.github.rutaleivanpaul.propertylistings.presentation.common.MoneyFormatter
import io.github.rutaleivanpaul.propertylistings.presentation.common.labelRes
import java.util.Locale

/** Square thumbnail: a 1:1 crop sits naturally with landscape photos (no stretch) and stays compact. */
private val ThumbnailSize = 92.dp

/**
 * A single scannable property card: a square leading thumbnail, then name, type, location, a rating
 * caption, and a bottom row pairing the graded rating pill with a prominent price.
 *
 * The thumbnail is vertically centred so the card stays balanced whatever the text height, and the
 * rating tier/count live on their own caption line so the pill and price share one tidy row rather
 * than fighting for width. Orange is reserved for the featured badge and the price.
 */
@Composable
fun PropertyCard(
    property: Property,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = { onClick(property.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            // The API has no cover/hero field, so the first gallery image is used as the
            // representative thumbnail (see DECISIONS.md). May be empty → neutral placeholder.
            PropertyThumbnail(
                imageUrl = property.imageUrls.firstOrNull(),
                propertyName = property.name,
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = property.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (property.isFeatured) {
                        Spacer(Modifier.width(8.dp))
                        FeaturedBadge()
                    }
                }

                Spacer(Modifier.height(3.dp))
                PropertySubtitle(property)

                if (property.hasRating) {
                    Spacer(Modifier.height(6.dp))
                    RatingCaption(property)
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    RatingPill(rating = property.ratingOutOf10)
                    PriceLabel(formattedPrice = MoneyFormatter.format(property.price))
                }
            }
        }
    }
}

/**
 * Square leading thumbnail.
 *
 * The neutral background reserves the space and shows while the image loads, on failure, or when
 * there is no URL — never a broken-image icon — so nothing reflows when the bitmap swaps in. The
 * image is decorative when absent (null description).
 */
@Composable
private fun PropertyThumbnail(
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
            .size(ThumbnailSize)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** Type + location, e.g. "Hostel · Dublin, Ireland"; OTHER drops the redundant "Property ·" prefix. */
@Composable
private fun PropertySubtitle(property: Property) {
    val text = if (property.type == PropertyType.OTHER) {
        stringResource(R.string.property_location, property.city, property.country)
    } else {
        stringResource(
            R.string.property_type_location,
            stringResource(property.type.labelRes()),
            property.city,
            property.country,
        )
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** Tier label + rating count, e.g. "Fabulous · 11,133 ratings" — context that frees the pill row. */
@Composable
private fun RatingCaption(property: Property) {
    val tierLabel = stringResource(RatingTier.forRating(property.ratingOutOf10).labelRes)
    val text = if (property.numberOfRatings > 0) {
        val count = stringResource(
            R.string.ratings_count,
            String.format(Locale.US, "%,d", property.numberOfRatings),
        )
        "$tierLabel · $count"
    } else {
        tierLabel
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/** The lowest nightly price — the loudest element on the card, in the brand accent. */
@Composable
private fun PriceLabel(formattedPrice: String) {
    val description = stringResource(R.string.price_per_night_description, formattedPrice)
    Text(
        text = stringResource(R.string.price_per_night, formattedPrice),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        modifier = Modifier.semantics { contentDescription = description },
    )
}

/** Small orange badge marking a featured property. */
@Composable
private fun FeaturedBadge() {
    val description = stringResource(R.string.featured_badge_description)
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Text(
            text = stringResource(R.string.featured_badge),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
