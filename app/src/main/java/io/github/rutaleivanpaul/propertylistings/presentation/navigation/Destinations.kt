package io.github.rutaleivanpaul.propertylistings.presentation.navigation

/**
 * The app's navigation destinations and their routes.
 *
 * A deliberately small, flat graph: a list screen and a detail screen that takes the property id as
 * its only argument. No nested graphs — the brief's scope is two screens, and a flat graph keeps the
 * routes obvious. Route strings live here (not scattered in the [androidx.navigation.NavHost]) so the
 * argument key is defined once and shared by the route, the navigation call, and the ViewModel that
 * reads it from its `SavedStateHandle`.
 */
object ListDestination {
    const val ROUTE = "list"
}

object DetailDestination {
    /** Nav argument key; also the `SavedStateHandle` key the detail ViewModel reads. */
    const val ARG_PROPERTY_ID = "propertyId"

    /** Route pattern with the id placeholder, e.g. `detail/{propertyId}`. */
    const val ROUTE = "detail/{$ARG_PROPERTY_ID}"

    /** Builds a concrete route to the detail screen for [propertyId]. */
    fun route(propertyId: Int): String = "detail/$propertyId"
}
