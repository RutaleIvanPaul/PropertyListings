package io.github.rutaleivanpaul.propertylistings

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 *
 * Annotated with [HiltAndroidApp] to generate the application-level Hilt component that is the
 * root of the dependency graph for the whole app.
 */
@HiltAndroidApp
class PropertyListingsApp : Application()
