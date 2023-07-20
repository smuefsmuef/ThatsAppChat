package fhnw.emoba.thatsapp.ui


import androidx.compose.animation.Crossfade
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import fhnw.emoba.thatsapp.model.ThatsAppModel
import fhnw.emoba.modules.module04.beers_solution.ui.theme.MaterialAppTheme
import fhnw.emoba.thatsapp.model.ChatScreen
import fhnw.emoba.thatsapp.model.OverviewScreen
import fhnw.emoba.thatsapp.model.ProfileScreen
import fhnw.emoba.thatsapp.data.Screen

@Composable
fun AppUI(model: ThatsAppModel) {
    with(model) {
        val snackbarHostState = remember { SnackbarHostState() }
        MaterialAppTheme {
            Crossfade(targetState = currentScreen) { screen ->
                when (screen) {
                    Screen.OVERVIEW -> {
                        OverviewScreen(model)
                    }

                    Screen.CHAT -> {
                        ChatScreen(model)
                    }

                    Screen.PROFILE -> {
                        ProfileScreen(model)
                    }
                }
            }
        }
        Notification(model, snackbarHostState)
    }

}

@Composable
private fun Notification(model: ThatsAppModel, snackbarHostState: SnackbarHostState) {
    with(model) {
        if (notificationMessage.isNotBlank()) {
            LaunchedEffect(snackbarHostState) {
                snackbarHostState.showSnackbar(
                    message = notificationMessage,
                    actionLabel = "OK"
                )
                notificationMessage = ""
            }
        }
    }
}
