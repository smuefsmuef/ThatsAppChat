package fhnw.emoba.thatsapp.data

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.UUID


class MqttConnector(
    mqttBroker: String,
    private val qos: MqttQos = MqttQos.AT_LEAST_ONCE
) {

    private val client = Mqtt5Client.builder()
        .serverHost(mqttBroker)
        .identifier(UUID.randomUUID().toString())
        .buildAsync()

    fun connectAndSubscribe(
        topic: String,
        userTopic: String,
        onNewMessage: (JSONObject) -> Unit,
        onNewUser: (JSONObject) -> Unit,
        onError: (Exception, String) -> Unit = { e, _ -> e.printStackTrace() },
        onConnectionFailed: () -> Unit = {},
        onConnection: () -> Unit = {}
    ) {
        client.connectWith()
            .cleanStart(true)
            .keepAlive(30)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    onConnectionFailed()
                } else { //erst wenn die Connection aufgebaut ist, kann subscribed werden
                    onConnection()
                    subscribe(topic, onNewMessage, onError)
                    subscribe("$userTopic", onNewUser, onError)
                }
            }
    }

    fun subscribe(
        topic: String,
        onNewMessage: (JSONObject) -> Unit,
        onError: (Exception, String) -> Unit = { e, _ -> e.printStackTrace() }
    ) {
        client.subscribeWith()
            .topicFilter(topic)
            .qos(qos)
            .noLocal(true)
            .callback {
                try {
                    System.out.println("Received message on topic - banana ${it.payloadAsString()}")
                    onNewMessage(it.payloadAsJSONObject()) // code wird aufgerufen, wenn wir was empfangen (dass wir subscribed haben)
                } catch (e: Exception) {
                    onError(e, it.payloadAsString())
                }
            }
            .send()
    }

    fun publish(
        topic: String,
        message: Message,
        onPublished: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        client.publishWith()
            .topic(topic)
            .payload(message.asPayload())
            .qos(qos)
            .retain(true)
            .messageExpiryInterval(5000)
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    onError()
                    System.out.println("eroooor message on topic - ${message.asJsonString()}")
                } else {
                    onPublished()
                    System.out.println("Published message on topic - $topic")
                }
            }
    }

    fun disconnect() {
        client.disconnectWith()
            .sessionExpiryInterval(0)
            .send()
    }
}

// praktische Extension Functions
private fun String.asPayload(): ByteArray =
    toByteArray(StandardCharsets.UTF_8) // macht ein Mqtt- byte array draus

private fun Mqtt5Publish.payloadAsJSONObject(): JSONObject = JSONObject(payloadAsString())
private fun Mqtt5Publish.payloadAsString(): String =
    String(payloadAsBytes, StandardCharsets.UTF_8) // macht einen String

private fun Message.asPayload(): ByteArray = asJsonString().asPayload()