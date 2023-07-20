package fhnw.emoba.thatsapp.model

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import fhnw.emoba.thatsapp.data.Coordinates
import fhnw.emoba.thatsapp.data.Flap
import fhnw.emoba.thatsapp.data.Screen
import fhnw.emoba.thatsapp.ui.theme.tealA400
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(model: ThatsAppModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                content = { Drawer(model) }
            )
        },
        content = {
            Scaffold(
                topBar = { HomeTopBar(model) },
                content = {
                    ChatBody(model, it)
                }
            )
        }
    )
    BackHandler(enabled = true) {
        scope.launch {
            drawerState.close()
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(model: ThatsAppModel) {
    with(model) {
        TopAppBar(
            title = {
                Text(currentChatBuddyName)
            },
            navigationIcon = {
                IconButton(onClick = { currentScreen = Screen.OVERVIEW; currentChatBuddy = null }) {
                    Icon(Icons.Filled.ArrowBack, "nextScreen")
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        )
    }
}


@Composable
fun ChatBody(model: ThatsAppModel, paddingValues: PaddingValues) {
    with(model) {
        ConstraintLayout(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
        {
            val (allFlapsPanel, message) = createRefs()
            AllFlapsPanel(
                allFlaps.filter { it.senderId == currentChatBuddyId || it.receiverId == currentChatBuddyId },
                Modifier.constrainAs(allFlapsPanel) {
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                    top.linkTo(parent.top, 15.dp)
                    start.linkTo(parent.start, 10.dp)
                    end.linkTo(parent.end, 10.dp)
                    bottom.linkTo(message.top, 15.dp)
                }, model
            )

            if (currentChatBuddyId != myUser.id) {
                NewMessage(model, Modifier.constrainAs(message) {
                    width = Dimension.fillToConstraints
                    start.linkTo(parent.start, 10.dp)
                    end.linkTo(parent.end, 10.dp)
                    bottom.linkTo(parent.bottom, 15.dp)
                })
            }
        }
    }
}

@Composable
private fun AllFlapsPanel(flaps: List<Flap>, modifier: Modifier, model: ThatsAppModel) {
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.small)
    ) {
        if (flaps.isEmpty()) {
            Text(
                text = "Start a new Chat",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            AllFlaps(flaps, model)
        }
    }
}

@Composable
private fun AllFlaps(flaps: List<Flap>, model: ThatsAppModel) {
    val scrollState = rememberLazyListState()
    LazyColumn(state = scrollState) {
        items(flaps) { SingleFlap(it, model) }
    }
    LaunchedEffect(flaps.size) {
        scrollState.animateScrollToItem(flaps.size)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleFlap(flap: Flap, model: ThatsAppModel) {
    with(flap) {
        ListItem(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            headlineText = {
                ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                    val (message, picture) = createRefs()

                    if (imageUrl != "") {
                        Photo(bitmap = bitmap!!,
                            model = model,
                            modifier = Modifier.constrainAs(picture) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            })
                    } else {
                        MessageBox(content = content,
                            modifier = Modifier.constrainAs(message) {
                                top.linkTo(parent.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                width = Dimension.fillToConstraints
                            })

                    }
                }
            },
            supportingText = {
                Text(
                    DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
                        .withZone(ZoneId.systemDefault()).format(Instant.ofEpochMilli(timestamp))
                )
            },

            trailingContent = {
                Column(
                    modifier = Modifier
                        .width(70.dp)
                        .padding(2.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    with(flap) {
                        if (flap.senderId != model.myUser.id) {
                            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                                Text(senderName)
                            }
                        }
                        if (flap.coordinates?.latitude != 0.0 || flap.coordinates?.longitude != 0.0) {
                            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {

                                IconButton(onClick = {
                                    flap.coordinates?.let {
                                        model.showOnMap(
                                            Coordinates(
                                                it.latitude,
                                                it.longitude,
                                                it.altitude
                                            )
                                        )
                                    }
                                }, modifier = Modifier.padding(1.dp))
                                {
                                    Icon(Icons.Outlined.Place, "showLocation")
                                }

                            }

                        }

                    }
                }
            }
        )
        Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
    }
}

@Composable
private fun PublishButton(model: ThatsAppModel, modifier: Modifier) {
    val focusManager = LocalFocusManager.current
    IconButton(
        onClick = { model.publish(); focusManager.clearFocus() },
        modifier = modifier.padding(1.dp)
    )
    {
        Icon(Icons.Filled.Send, "publishMessage", tint = tealA400)
    }
}

@Composable
private fun AddLocationButton(model: ThatsAppModel, modifier: Modifier) {
    IconButton(onClick = {
        model.rememberCurrentPosition()
    }, modifier = modifier.padding(1.dp))
    {
        Icon(Icons.Outlined.AddLocationAlt, "addLocation")
    }
}

@Composable
private fun AddPhotoButton(model: ThatsAppModel, modifier: Modifier) {
    IconButton(onClick = {
        model.takePhoto()
    }, modifier = modifier.padding(1.dp))
    {
        Icon(Icons.Outlined.CameraAlt, "addPhoto")
    }
}


@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun NewMessage(model: ThatsAppModel, modifier: Modifier) {
    with(model) {
        val keyboard = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = modifier
                .fillMaxWidth()
                .padding(1.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
            label = { Text("Message") },
            placeholder = { Text("type something..") },
            leadingIcon = {
                Row()
                {
                    AddLocationButton(model, Modifier.size(35.dp))
                    AddPhotoButton(model, modifier.size(35.dp))
                }
            },
            trailingIcon = {
                PublishButton(model, Modifier.size(35.dp))
            }
        )
    }
}

@Composable
private fun Photo(bitmap: Bitmap, model: ThatsAppModel, modifier: Modifier) {
    with(model) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "picture",
            modifier = modifier
                .clip(shape = MaterialTheme.shapes.medium)
                .padding(1.dp)
        )
    }
}


@Composable
private fun MessageBox(modifier: Modifier, content: String) {
    Text(
        text = content,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier
    )
}
