package com.example.ticket_scanner

import org.junit.Assert.assertEquals
import org.junit.Test

class QrMessageDecoderTest {

    @Test
    fun decodesJsonMessage() {
        val result = QrMessageDecoder.decode(
            """{"ticketId":"T001","event":"Music Night"}"""
        )

        assertEquals(QrMessageType.JSON, result.type)
        assertEquals("T001", result.fields["ticketId"])
        assertEquals("Music Night", result.fields["event"])
    }

    @Test
    fun decodesPlainText() {
        val result = QrMessageDecoder.decode(
            "Welcome to Music Night"
        )

        assertEquals(QrMessageType.PLAIN_TEXT, result.type)
        assertEquals(
            "Welcome to Music Night",
            result.rawMessage
        )
    }

    @Test
    fun decodesUrl() {
        val result = QrMessageDecoder.decode(
            "https://example.com/ticket"
        )

        assertEquals(QrMessageType.URL, result.type)
    }

    @Test
    fun decodesEmptyMessage() {
        val result = QrMessageDecoder.decode("")

        assertEquals(QrMessageType.UNKNOWN, result.type)
    }
}
