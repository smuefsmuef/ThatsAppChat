package fhnw.emoba.thatsapp.model

import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import fhnw.emoba.thatsapp.data.User
import fhnw.emoba.thatsapp.data.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(model: ThatsAppModel) {
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
                topBar = { HomeTopBar(model, drawerState) },
                content = {
                    OverviewBody(model, it)
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
private fun HomeTopBar(model: ThatsAppModel, drawerState: DrawerState) {
    with(model) {
        TopAppBar(
            title = { Text(Screen.OVERVIEW.title) },
            navigationIcon = { DrawerIcon(drawerState = drawerState) },
            colors = TopAppBarDefaults.mediumTopAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        )
    }
}

@Composable
fun OverviewBody(model: ThatsAppModel, paddingValues: PaddingValues) {
    with(model) {
        ConstraintLayout(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        )
        {
            val (allChatBuddiesPanel) = createRefs()
            AllChatBuddiesPanel(allChatBuddies, model, Modifier.constrainAs(allChatBuddiesPanel) {
                top.linkTo(parent.top, 7.dp)
            })
        }
    }
}

@Composable
private fun AllChatBuddiesPanel(users: List<User>, model: ThatsAppModel, modifier: Modifier) {
    Box(
        modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (users.isEmpty()) {
            Text(
                text = "keine Chatbuddies",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            AllUsers(users, model)
        }
    }
}

@Composable
private fun AllUsers(users: List<User>, model: ThatsAppModel) {
    val scrollState = rememberLazyListState()
    LazyColumn(state = scrollState) {
        items(users) { SingleUser(it, model) }
    }

    LaunchedEffect(users.size) {
        scrollState.animateScrollToItem(users.size)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleUser(user: User, model: ThatsAppModel) {
    with(user) {
        ListItem(
            headlineText = { Text(name) },
            trailingContent = {
                Column(
                    modifier = Modifier
                        .width(30.dp)
                        .padding(2.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    with(user) {
                        Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Column {
                                Row {
                                    model.unreadMessages.find { it.senderId == user.id }?.let {
                                        Icon(
                                            imageVector = Icons.Outlined.ChatBubbleOutline,
                                            contentDescription = "newMessage",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(0.2.dp),
                                            text = model.unreadMessages.count { it.senderId == user.id }
                                                .toString()
                                        )
                                    }
                                }
                            }
                        }

                    }
                }
            },
            leadingContent = { ProfilePic(imageName, model) },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(onClick = { model.setChatBuddy(user) })
                .clip(CircleShape)
                .padding(top = 7.dp, bottom = 7.dp)
        )
    }
}

@Composable
private fun ProfilePic(name: String, model: ThatsAppModel) {
    with(name) {
        Row {
            Image(
                painterResource(model.getImageResource(name)),
                contentDescription = "profilePicBuddy",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
        }
    }
}
