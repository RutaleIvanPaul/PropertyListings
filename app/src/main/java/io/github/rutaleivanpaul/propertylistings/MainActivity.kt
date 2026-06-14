package io.github.rutaleivanpaul.propertylistings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import io.github.rutaleivanpaul.propertylistings.presentation.list.ListScreen
import io.github.rutaleivanpaul.propertylistings.presentation.theme.PropertyListingsTheme

/**
 * Single activity host for the app's Compose UI.
 *
 * Annotated with [AndroidEntryPoint] so Hilt can supply the screens' ViewModels. For M3 it hosts
 * the list screen directly; the navigation graph to the detail screen is wired in M4 — until then,
 * a property tap is received here as a no-op.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PropertyListingsTheme {
                AppContent()
            }
        }
    }
}

/** Root composable. Navigation to detail arrives in M4; for now the tap callback is a placeholder. */
@Composable
private fun AppContent() {
    ListScreen(onNavigateToDetail = { /* Navigation wired in M4. */ })
}
