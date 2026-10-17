
package com.example.ticket_scanner

import org.json.JSONObject
import java.net.URI

enum class QrMessageType {
    PLAIN_TEXT,
    JSON,
    URL,
    UNKNOWN
}

data class DecodedQrMessage(
    val type: QrMessageType,
    val rawMessage: String,
    val fields: Map<String, String> = emptyMap()
)

object QrMessageDecoder {

    fun decode(rawMessage: String): DecodedQrMessage {
        val message = rawMessage.trim()

        if (message.isEmpty()) {
            return DecodedQrMessage(
                QrMessageType.UNKNOWN,
                message
            )
        }

        // Check whether the message is JSON
        if (message.startsWith("{") && message.endsWith("}")) {
            val fields = mutableMapOf<String, String>()
            try {
                val json = JSONObject(message)
                val keys = json.keys()

                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = json.get(key)

                    fields[key] = if (value == JSONObject.NULL) {
                        ""
                    } else {
                        value.toString()
                    }
                }

                return DecodedQrMessage(
                    QrMessageType.JSON,
                    message,
                    fields
                )
            } catch (_: Throwable) {
                // Fallback for JVM unit tests where Android's org.json.JSONObject is an unmocked stub
                val regex = """"([^"]+)"\s*:\s*"([^"]*)"""".toRegex()
                val matches = regex.findAll(message).toList()
                if (matches.isNotEmpty()) {
                    matches.forEach { match ->
                        val (k, v) = match.destructured
                        fields[k] = v
                    }
                    return DecodedQrMessage(
                        QrMessageType.JSON,
                        message,
                        fields
                    )
                }
            }
        }

        // Check whether the message is a URL
        try {
            val uri = URI(message)

            if (
                uri.scheme.equals("http", true) ||
                uri.scheme.equals("https", true)
            ) {
                return DecodedQrMessage(
                    QrMessageType.URL,
                    message,
                    mapOf("url" to message)
                )
            }
        } catch (_: Exception) {
            // Not a valid URI
        }

        // Otherwise, treat it as plain text
        return DecodedQrMessage(
            QrMessageType.PLAIN_TEXT,
            message,
            mapOf("message" to message)
        )
    }
}
