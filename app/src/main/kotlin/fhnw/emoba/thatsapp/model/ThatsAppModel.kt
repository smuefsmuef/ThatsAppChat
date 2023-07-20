package fhnw.emoba.thatsapp.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.AndroidUriHandler
import fhnw.emoba.R
import fhnw.emoba.thatsapp.data.CameraAppConnector
import fhnw.emoba.thatsapp.data.Coordinates
import fhnw.emoba.thatsapp.data.User
import fhnw.emoba.thatsapp.data.Flap
import fhnw.emoba.thatsapp.data.GPSConnector
import fhnw.emoba.thatsapp.data.MqttConnector
import fhnw.emoba.thatsapp.data.Screen
import fhnw.emoba.thatsapp.data.downloadBitmapFromURL
import fhnw.emoba.thatsapp.data.uploadPhotoToFileIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

class ThatsAppModel(
    private val context: ComponentActivity,
    private val locator: GPSConnector,
    private val cameraAppConnector: CameraAppConnector
) {
    // mutables 'mix'
    var currentScreen by mutableStateOf(Screen.OVERVIEW)

    // corutine
    private val modelScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // geo location
    val waypoint = mutableStateOf<Coordinates>(Coordinates(0.0, 0.0))

    //camera Image & file upload
    var photo by mutableStateOf<Bitmap?>(null)
    private var fileURLToSend = ""
    var uploadInProgress by mutableStateOf(false)

    // mutables 'messages/flaps
    val allFlaps = mutableStateListOf<Flap>()
    var unreadMessages = mutableStateListOf<Flap>()
    var notificationMessage by mutableStateOf("")
    var message by mutableStateOf("Hello")

    //  'user/chatbuddies'
    var petra = User(UUID.randomUUID().toString(), "Petra", "petra", "true", waypoint.value)
    var myUserName by mutableStateOf(petra.name)
    var myUser by mutableStateOf(petra)
    var myAvatar = loadImage(
        context.resources.getIdentifier(myUser.imageName, "drawable", context.packageName)
    )
    var profilePic by mutableStateOf(myAvatar)
    val allChatBuddies = mutableStateListOf<User>()
    var currentChatBuddy: User? by mutableStateOf(null)
    var currentChatBuddyId by mutableStateOf("0")
    var currentChatBuddyName by mutableStateOf("")

    // topics
    val mqttBroker = "broker.hivemq.com"
    val baseTopic = "fhnw/emoba/thatsapp/pekafr"
    val usersTopic = "$baseTopic/users"
    val myMessageTopic = "$baseTopic/messages/${petra.id}"
    var currentChatBuddyTopic by mutableStateOf("$baseTopic/messages/${currentChatBuddy?.id}")

    private val mqttConnector by lazy { MqttConnector(mqttBroker) }
    private val soundPlayerNewMessage by lazy { MediaPlayer.create(context, R.raw.what_302) }
    private val soundPlayerNewUser by lazy { MediaPlayer.create(context, R.raw.friend) }

    fun connectAndSubscribe() {
        // connect and subscribe to my own topic
        mqttConnector.connectAndSubscribe(
            topic = myMessageTopic,
            userTopic = usersTopic,
            onNewMessage = {
                var newMessage = Flap(it)

                // notify user when a new message arrives (except in certain cases)
                if (currentScreen == Screen.OVERVIEW || currentChatBuddyId != Flap(it).senderId) {
                    unreadMessages.add(Flap(it))
                }
                playSound("message")

                // distinguish incoming message by type:
                if (newMessage.imageUrl != "") {
                    downloadBitmapFromURL(
                        url = newMessage.imageUrl,
                        onSuccess = {
                            newMessage.bitmap = it
                            System.out.println("downloaded bitmap!!!!!!!!!")
                            allFlaps.add(newMessage)
                        },
                        onError = {}
                    )
                } else {
                    allFlaps.add(newMessage)
                }
            },
            onNewUser = { it ->
                val buddy = User(it)
                // if id is already there, remove the old one (f.e. in case of a name change)
                allChatBuddies.forEach {
                    if (it.id == buddy.id) {
                        allChatBuddies.remove(it)
                    }
                }
                // add new users
                allChatBuddies.add(buddy)
                // make sure to exclude myself from the list
                allChatBuddies.forEach {
                    if (it.id == petra.id) {
                        allChatBuddies.remove(it)
                    }
                }
                playSound("user")
                println("New Number user:" + allChatBuddies.size)
            },
            onError = { _, p ->
                notificationMessage = p
            },
            onConnection = {
                publishThatImOnline()
            }
        )

    }

    fun publishThatImOnline() {
        mqttConnector.publish(
            topic = "$usersTopic",
            message = petra,
            onPublished = {
                System.out.println(" hey, i'm online: $usersTopic published, $petra")
            }
        )
    }

    fun publish() {
        val myMessage = Flap(
            senderId = myUser.id,
            senderName = myUserName,
            receiverId = currentChatBuddyId,
            content = message,
            imageUrl = fileURLToSend,
            coordinates = waypoint.value,
            timestamp = System.currentTimeMillis(),
            bitmap = photo
        )

        mqttConnector.publish(
            topic = currentChatBuddyTopic,
            message = myMessage,
            onPublished = { flapsPublished(myMessage) }
        )
    }


    private fun flapsPublished(myMessage: Flap) {
        // add my own messages to flap list
        allFlaps.add(myMessage)
        // and clear the message afterwards
        message = ""
        photo = null
        fileURLToSend = ""
        waypoint.value = Coordinates(0.0, 0.0)
    }

    private fun playSound(type: String) {
        if (type == "message") {
            soundPlayerNewMessage.seekTo(0)
            soundPlayerNewMessage.start()
        } else {
            soundPlayerNewUser.seekTo(0)
            soundPlayerNewUser.start()
        }
    }

    fun setChatBuddy(user: User) {
        // everything that happens when I click on a chatbuddy
        unreadMessages.removeIf { it.senderId == user.id }
        currentChatBuddy = user
        currentChatBuddyId = user.id
        currentChatBuddyName = user.name
        currentChatBuddyTopic = "$baseTopic/messages/${currentChatBuddy?.id}"
        currentScreen = Screen.CHAT
    }

    fun updateCurrentUser() {
        myUser.name = myUserName
        publishThatImOnline()
    }

    // location
    fun rememberCurrentPosition() {
        modelScope.launch {
            locator.getLocation(
                onNewLocation = {
                    waypoint.value = it
                    message = "Mein neuer Standort:\r\n${it.dms()}"
                },
                onFailure = {
                    notificationMessage = "Standort konnte nicht ermittelt werden."
                },
                onPermissionDenied = {
                    notificationMessage = "Keine Berechtigung."
                },
            )
        }
    }

    fun showOnMap(position: Coordinates) =
        AndroidUriHandler(context).openUri(position.asOpenStreetMapsURL())

    // images/camera
    private fun loadImage(@DrawableRes id: Int) =
        BitmapFactory.decodeResource(context.resources, id).asImageBitmap()

    private fun uploadPhoto() {
        modelScope.launch {
            if (photo != null) {
                uploadPhotoToFileIO(
                    bitmap = photo!!,
                    onSuccess = {
                        fileURLToSend = it
                        message = "${fileURLToSend}"
                    },
                    onError = { int, _ ->
                        notificationMessage = "$int: Could not upload image from URL."
                    }
                )
                uploadInProgress = false
            }
        }
    }

    fun takePhoto() {
        cameraAppConnector.getBitmap(
            onSuccess = {
                photo = it
                uploadPhoto()
            },
            onCanceled = { notificationMessage = "leider nein" }
        )
    }

    fun updateProfilePic(name: String) {
        myUser.imageName = name

        profilePic = loadImage(
            context.resources.getIdentifier(myUser.imageName, "drawable", context.packageName)
        )
    }

    // simple solution to allow to set profile pics for every user
    fun getImageResource(imageName: String): Int {
        return when (imageName) {
            "margo" -> R.drawable.margo
            "petra" -> R.drawable.petra
            "kevin" -> R.drawable.kevin
            "karin" -> R.drawable.karin
            "freya" -> R.drawable.freya
            "eduardo" -> R.drawable.eduardo
            else -> R.drawable.petra
        }
    }
}

