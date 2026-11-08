package com.example.ticket_scanner.eventcreation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Draws a real, scannable QR code for [content] (black squares on white).
 * Uses ZXing's encoder, so no network is needed.
 */
@Composable
fun QrCodeImage(content: String, modifier: Modifier = Modifier) {
    val matrix: BitMatrix? = remember(content) {
        try {
            QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                0,
                0,
                mapOf(
                    EncodeHintType.MARGIN to 1,
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                    EncodeHintType.CHARACTER_SET to "UTF-8"
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    Canvas(modifier = modifier.background(Color.White)) {
        val bits = matrix ?: return@Canvas
        val cell = size.width / bits.width
        for (y in 0 until bits.height) {
            for (x in 0 until bits.width) {
                if (bits.get(x, y)) {
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(x * cell, y * cell),
                        // slightly oversized so no hairline gaps show between squares
                        size = Size(cell + 0.5f, cell + 0.5f)
                    )
                }
            }
        }
    }
}