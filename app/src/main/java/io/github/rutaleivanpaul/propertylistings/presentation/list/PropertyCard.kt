package io.github.rutaleivanpaul.propertylistings.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.rutaleivanpaul.propertylistings.R
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType
import io.github.rutaleivanpaul.propertylistings.presentation.common.MoneyFormatter
import io.github.rutaleivanpaul.propertylistings.presentation.common.labelRes
import java.util.Locale

/**
 * A single scannable property card: name and featured badge up top, location beneath, then the
 * rating pill (with its quality label and rating count) paired against a prominent price.
 *
 * Orange is used only where it earns attention — the featured badge and the price — keeping the
 * accent sparing. The whole card is one touch target; Compose merges the children's semantics so a
 * screen reader announces the property in one pass.
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = property.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (property.isFeatured) {
                    Spacer(Modifier.width(8.dp))
                    FeaturedBadge()
                }
            }

            Spacer(Modifier.height(4.dp))
            // Subtitle leads with the property type (Hostel / Guesthouse / Hotel) so the mixed
            // dataset reads honestly; OTHER is omitted to avoid a redundant "Property ·" prefix.
            val subtitle = if (property.type == PropertyType.OTHER) {
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
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingPill(rating = property.ratingOutOf10)
                    if (property.hasRating) {
                        Spacer(Modifier.width(8.dp))
                        RatingMeta(property)
                    }
                }
                PriceLabel(formattedPrice = MoneyFormatter.format(property.price))
            }
        }
    }
}

/** The tier label plus, when known, the number of ratings — secondary context beside the pill. */
@Composable
private fun RatingMeta(property: Property) {
    val tierLabel = stringResource(RatingTier.forRating(property.ratingOutOf10).labelRes)
    Column {
        Text(
            text = tierLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
        if (property.numberOfRatings > 0) {
            Text(
                text = stringResource(
                    R.string.ratings_count,
                    String.format(Locale.US, "%,d", property.numberOfRatings),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** The lowest nightly price — the loudest element on the card, in the brand accent. */
@Composable
private fun PriceLabel(formattedPrice: String) {
    val description = stringResource(R.string.price_per_night_description, formattedPrice)
    Text(
        text = stringResource(R.string.price_per_night, formattedPrice),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
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
