package io.github.rutaleivanpaul.propertylistings.presentation.detail

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
import io.github.rutaleivanpaul.propertylistings.domain.model.RatingCategory
import io.github.rutaleivanpaul.propertylistings.domain.model.RatingScore
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
 * Robolectric-backed Compose render tests for the stateless [DetailScaffold]. Run on the JVM with
 * the rest of the unit suite (a fake Coil engine makes image loading hermetic), they assert that
 * each [DetailUiState] renders its key elements and that the currency toggle and retry forward the
 * right intents.
 */
@OptIn(ExperimentalCoilApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [33])
class DetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUpFakeImageLoader() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val engine = FakeImageLoaderEngine.Builder().default(ColorDrawable(Color.GRAY)).build()
        Coil.setImageLoader(ImageLoader.Builder(context).components { add(engine) }.build())
    }

    @After
    fun tearDownImageLoader() = Coil.reset()

    private val property = Property(
        id = 1,
        name = "Abbey Court Hostel",
        isFeatured = true,
        ratingOutOf10 = 8.7,
        numberOfRatings = 11133,
        price = Money(14.18, Currency.EUR),
        overview = "Dublin's liveliest hostel.",
        type = PropertyType.HOSTEL,
        city = "Dublin",
        country = "Ireland",
        imageUrls = listOf("https://example.test/hero.jpg"),
        district = "Temple Bar",
        address = "29 Bachelors Walk, Dublin 1",
        ratingBreakdown = listOf(RatingScore(RatingCategory.LOCATION, 9.5)),
    )

    private fun content(
        selected: Currency = Currency.EUR,
        available: List<Currency> = listOf(Currency.EUR, Currency.USD, Currency.GBP),
        price: Money = Money(14.18, Currency.EUR),
    ) = DetailUiState.Content(
        property = property,
        selectedCurrency = selected,
        displayedPrice = price,
        availableCurrencies = available,
    )

    @Test
    fun content_rendersKeyFields_andHeroDescription() {
        composeRule.setContent {
            PropertyListingsTheme {
                DetailScaffold(state = content(), onIntent = {}, onBack = {})
            }
        }

        // The app-bar title is on-screen; the scrollable body (tall hero pushes it below the short
        // test viewport) is asserted by existence — the point is that each node is emitted.
        composeRule.onNodeWithText("Abbey Court Hostel", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("€14.18", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Hostel · Temple Bar", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("29 Bachelors Walk, Dublin 1", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Dublin's liveliest hostel.", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription("Location rated 9.5 out of 10", useUnmergedTree = true)
            .assertExists()
        // Hero is described for accessibility when an image URL is present.
        composeRule.onNodeWithContentDescription("Photo of Abbey Court Hostel", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun currencyToggle_forwardsSelectCurrencyIntent() {
        var intent: DetailIntent? = null
        composeRule.setContent {
            PropertyListingsTheme {
                DetailScaffold(state = content(), onIntent = { intent = it }, onBack = {})
            }
        }

        composeRule.onNodeWithText("USD").performClick()

        assertEquals(DetailIntent.SelectCurrency(Currency.USD), intent)
    }

    @Test
    fun eurOnly_hidesTheCurrencyToggle() {
        composeRule.setContent {
            PropertyListingsTheme {
                DetailScaffold(state = content(available = listOf(Currency.EUR)), onIntent = {}, onBack = {})
            }
        }

        // Price still shows, but no toggle to a currency we can't convert to.
        composeRule.onNodeWithText("€14.18", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("USD").assertDoesNotExist()
    }

    @Test
    fun error_showsRetry_andForwardsRetryIntent() {
        var intent: DetailIntent? = null
        composeRule.setContent {
            PropertyListingsTheme {
                DetailScaffold(state = DetailUiState.Error, onIntent = { intent = it }, onBack = {})
            }
        }

        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(DetailIntent.Retry, intent)
    }
}
