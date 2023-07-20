package fhnw.emoba.thatsapp.model

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import fhnw.emoba.thatsapp.data.Screen
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import fhnw.emoba.modules.module04.beers_solution.ui.theme.MaterialAppTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(model: ThatsAppModel) {
    with(model) {
        MaterialAppTheme {
            val drawerState = rememberDrawerState(DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        content = { Drawer(model) }
                    )
                }
            ) {
                Scaffold(
                    topBar = { ProfileTopBar(drawerState) },
                    floatingActionButton = { OverviewFAB(model) },
                    floatingActionButtonPosition = FabPosition.End
                ) {
                    ProfileBody(
                        model
                    ) { name ->
                        myUserName = name
                    }
                }
            }

            BackHandler(enabled = true) {
                model.currentScreen = Screen.OVERVIEW
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(drawerState: DrawerState) {
    TopAppBar(title = { Text(Screen.PROFILE.title) },
        navigationIcon = { DrawerIcon(drawerState) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ProfileBody(
    model: ThatsAppModel,
    onNameChanged: (String) -> Unit
) {
    with(model) {
        val keyboard = LocalSoftwareKeyboardController.current
        Column(
            modifier = Modifier
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            Image(
                bitmap = profilePic,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
            )
            ImageSelector(model)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hi ${myUserName} !",
                modifier = Modifier.padding(10.dp),
                style = MaterialTheme.typography.headlineMedium,
            )
            val contextForToast = LocalContext.current.applicationContext
            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current
            OutlinedTextField(
                value = myUserName,
                onValueChange = { onNameChanged(it) },
                singleLine = true,
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                trailingIcon = {
                    IconButton(onClick = {
                        updateCurrentUser(); focusManager.clearFocus()
                        Toast.makeText(contextForToast, "Saved!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save")
                    }
                }
            )
        }
    }
}

// simple solution with only 3 hardcoded options
@Composable
fun ImageSelector(model: ThatsAppModel) {
    with(model) {
        Row(
            modifier = Modifier
                .width(102.dp)
                .padding(4.dp)
        ) {
            SmallPreviewImage(model, "petra")
            SmallPreviewImage(model, "margo")
            SmallPreviewImage(model, "kevin")
        }
    }
}

@Composable
fun SmallPreviewImage(model: ThatsAppModel, imageName: String) {
    with(model) {
        Image(
            painterResource(getImageResource(imageName)),
            contentDescription = "Pic",
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable {
                    updateProfilePic(imageName)
                }
        )
    }
}

@Composable
fun OverviewFAB(model: ThatsAppModel) {
    with(model) {
        FloatingActionButton(onClick = { currentScreen = Screen.OVERVIEW }
        ) {
            BackToScreenIcon(model, Screen.OVERVIEW)
        }
    }
}