package io.github.rutaleivanpaul.propertylistings.presentation.list

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import io.github.rutaleivanpaul.propertylistings.domain.model.Currency
import io.github.rutaleivanpaul.propertylistings.domain.model.Money
import io.github.rutaleivanpaul.propertylistings.domain.model.Property
import io.github.rutaleivanpaul.propertylistings.domain.model.PropertyType
import io.github.rutaleivanpaul.propertylistings.presentation.theme.PropertyListingsTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Robolectric-backed Compose render tests for [PropertyCard]. These run on the JVM with the rest of
 * the unit suite (no device), so they're deterministic.
 *
 * Image *loading* is not under test — a fake Coil engine returns a solid drawable for any request,
 * so loading is hermetic and instant. The tests assert the card renders its key fields and is
 * clickable both with and without an image URL, and that the image is described for accessibility
 * only when an image is present.
 */
@OptIn(ExperimentalCoilApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [33])
class PropertyCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUpFakeImageLoader() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val engine = FakeImageLoaderEngine.Builder()
            .default(ColorDrawable(Color.GRAY))
            .build()
        Coil.setImageLoader(ImageLoader.Builder(context).components { add(engine) }.build())
    }

    @After
    fun tearDownImageLoader() = Coil.reset()

    private fun property(imageUrls: List<String>) = Property(
        id = 7,
        name = "Abbey Court Hostel",
        isFeatured = true,
        ratingOutOf10 = 8.5,
        numberOfRatings = 11133,
        price = Money(14.18, Currency.EUR),
        overview = "",
        type = PropertyType.HOSTEL,
        city = "Dublin",
        country = "Ireland",
        imageUrls = imageUrls,
    )

    @Test
    fun rendersKeyFields_withImageUrl() {
        composeRule.setContent {
            PropertyListingsTheme {
                PropertyCard(property = property(listOf("https://example.test/a.jpg")), onClick = {})
            }
        }

        composeRule.onNodeWithText("Abbey Court Hostel", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("€14.18 / night", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Hostel · Dublin, Ireland", useUnmergedTree = true).assertIsDisplayed()
        // The image carries an accessibility description when present.
        composeRule.onNodeWithContentDescription("Photo of Abbey Court Hostel", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun rendersKeyFields_withoutImageUrl() {
        composeRule.setContent {
            PropertyListingsTheme {
                PropertyCard(property = property(emptyList()), onClick = {})
            }
        }

        // Card still renders fully with no image.
        composeRule.onNodeWithText("Abbey Court Hostel", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("€14.18 / night", useUnmergedTree = true).assertIsDisplayed()
        // With no image the thumbnail is decorative — no photo description.
        composeRule.onNodeWithContentDescription("Photo of Abbey Court Hostel", useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun click_invokesCallbackWithPropertyId() {
        var clickedId = -1
        composeRule.setContent {
            PropertyListingsTheme {
                PropertyCard(property = property(emptyList()), onClick = { clickedId = it })
            }
        }

        composeRule.onNodeWithText("Abbey Court Hostel").performClick()

        assertEquals(7, clickedId)
    }
}
