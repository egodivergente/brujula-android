package es.brujula.jitai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import es.brujula.jitai.core.designsystem.BrujulaTheme
import es.brujula.jitai.ui.BrujulaViewModel
import es.brujula.jitai.ui.navigation.BrujulaAppShell

class MainActivity : ComponentActivity() {

    private val viewModel: BrujulaViewModel by viewModels {
        BrujulaViewModel.Factory((application as BrujulaApp).repository)
    }

    private var pendingOpenDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingOpenDestination = intent?.getStringExtra("open_destination")

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            BrujulaTheme(
                themePreference = state.settings.themePreference,
                largeText = state.settings.largeTextEnabled,
                hapticsEnabled = state.settings.hapticsEnabled
            ) {
                BrujulaAppShell(
                    viewModel = viewModel,
                    initialDestination = pendingOpenDestination,
                    onDestinationConsumed = { pendingOpenDestination = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingOpenDestination = intent.getStringExtra("open_destination")
    }
}
