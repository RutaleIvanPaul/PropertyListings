package io.github.rutaleivanpaul.propertylistings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.AndroidEntryPoint
import io.github.rutaleivanpaul.propertylistings.presentation.theme.PropertyListingsTheme

/**
 * Single activity host for the app's Compose UI.
 *
 * Annotated with [AndroidEntryPoint] so Hilt can inject the screen-level dependencies that are
 * added in later milestones. For now it shows a placeholder shell; the list and detail screens
 * and their navigation are wired up in the screen milestones.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PropertyListingsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SkeletonPlaceholder(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/** Temporary content shown by the empty skeleton until the real screens are built. */
@Composable
private fun SkeletonPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(id = R.string.skeleton_placeholder))
    }
}
