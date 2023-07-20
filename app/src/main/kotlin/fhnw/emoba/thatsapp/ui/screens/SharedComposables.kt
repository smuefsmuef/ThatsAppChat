package fhnw.emoba.thatsapp.model

import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face4
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.data.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerIcon(drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    IconButton(onClick = { scope.launch { drawerState.open() } }) {
        Icon(Icons.Outlined.Menu, "Menu")
    }

}

@Composable
fun BackToScreenIcon(model: ThatsAppModel, screen: Screen) {
    with(model) {
        IconButton(onClick = { currentScreen = screen }) {
            Icon(Icons.Filled.ArrowBack, "Back")
        }
    }
}

@Composable
fun Drawer(model: ThatsAppModel) {
    Column {
        DrawerRow(model, Screen.OVERVIEW, Icons.Outlined.Diversity1)
        DrawerRow(model, Screen.PROFILE, Icons.Filled.Face4)
    }
}

@Composable
fun DrawerRow(model: ThatsAppModel, screen: Screen, icon: ImageVector) {
    with(model) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            IconButton(onClick = { currentScreen = screen }) {
                Icon(icon, "Menu")
            }
            Text(
                text = screen.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth()
                    .clickable(onClick = { currentScreen = screen })
            )
        }
        Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
    }
}
