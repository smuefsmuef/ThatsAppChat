package fhnw.emoba.thatsapp.data

import android.graphics.Bitmap
import org.json.JSONObject


data class Flap(
    val senderId: String,
    val senderName: String,
    val receiverId: String,
    val content: String,
    var imageUrl: String,
    var coordinates: Coordinates?,
    var timestamp: Long,
    var bitmap: Bitmap?
) : Message {


    constructor(json: JSONObject) : this(
        senderId = json.getString("senderId"),
        senderName = json.getString("senderName"),
        receiverId = json.getString("receiverId"),
        content = json.getString("content"),
        imageUrl = json.getString("imageUrl"),
        coordinates = Coordinates(
            latitude = json.getDouble("lat"),
            longitude = json.getDouble("lon")
        ),
        timestamp = json.getLong("timestamp"),
        bitmap = null
    )

    override fun asJsonString(): String {
        return """
            {"senderId": "$senderId",
            "senderName": "$senderName",
            "receiverId": "$receiverId",
            "content": "$content",
            "imageUrl": "$imageUrl",
             "lat": ${coordinates?.latitude},
              "lon": ${coordinates?.longitude},
            "timestamp": $timestamp
            }
            """.trimIndent()
    }

}
