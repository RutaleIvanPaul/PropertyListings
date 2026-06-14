package io.github.rutaleivanpaul.propertylistings.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.rutaleivanpaul.propertylistings.presentation.detail.DetailScreen
import io.github.rutaleivanpaul.propertylistings.presentation.list.ListScreen

/**
 * The app's navigation graph: a list screen and a property detail screen.
 *
 * A deliberately flat, two-destination graph (the brief's scope). The list emits a property id when
 * a card is tapped; this host turns it into a typed [DetailDestination] route, and the detail
 * ViewModel reads the id back from its `SavedStateHandle`. Back is the default pop behaviour.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ListDestination.ROUTE,
        modifier = modifier,
    ) {
        composable(ListDestination.ROUTE) {
            ListScreen(
                onNavigateToDetail = { propertyId ->
                    navController.navigate(DetailDestination.route(propertyId))
                },
            )
        }

        composable(
            route = DetailDestination.ROUTE,
            arguments = listOf(
                navArgument(DetailDestination.ARG_PROPERTY_ID) { type = NavType.IntType },
            ),
        ) {
            DetailScreen(onBack = navController::popBackStack)
        }
    }
}
