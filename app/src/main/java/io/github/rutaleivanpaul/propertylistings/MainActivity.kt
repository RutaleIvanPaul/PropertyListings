package io.github.rutaleivanpaul.propertylistings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.rutaleivanpaul.propertylistings.presentation.navigation.AppNavHost
import io.github.rutaleivanpaul.propertylistings.presentation.theme.PropertyListingsTheme

/**
 * Single activity host for the app's Compose UI.
 *
 * Annotated with [AndroidEntryPoint] so Hilt can supply the screens' ViewModels. It hosts the
 * navigation graph ([AppNavHost]) that drives the list → detail flow.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PropertyListingsTheme {
                AppNavHost()
            }
        }
    }
}
