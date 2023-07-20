package fhnw.emoba.thatsapp.data

import org.json.JSONObject


data class User(
    var id: String,
    var name: String,
    var imageName: String,
    var online: String = "true",
    var location: Coordinates
) : Message {

    constructor(json: JSONObject) : this(
        id = json.getString("id"),
        name = json.getString("name"),
        imageName = json.getString("imageName"),
        online = json.getString("online"),
        location = Coordinates(
            latitude = 0.0,
            longitude = 0.0
        ),
    )

    override fun asJsonString(): String {
        return """
            {"id": "$id",
             "name": "$name",
             "imageName": "$imageName",
             "online": "$online"
            }
            """.trimIndent()
    }
}