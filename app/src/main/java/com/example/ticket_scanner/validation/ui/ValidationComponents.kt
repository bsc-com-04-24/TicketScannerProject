package com.example.ticket_scanner.validation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colors for the validation screens (kept here so they can't clash with theme/Color.kt). */
internal object ValidationColors {
    val Green = Color(0xFF1B9E4B)
    val Red = Color(0xFFE02B2B)
    val Orange = Color(0xFFE59A0B)
    val Purple = Color(0xFF4C3FB1)
    val PageBackground = Color(0xFFF3F4F8)
    val LabelGray = Color(0xFF6B6B7B)
    val ValueDark = Color(0xFF232332)
}

/** The bordered card + scrolling content + full-width button shared by all four screens. */
@Composable
internal fun ValidationCard(
    color: Color,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ValidationColors.PageBackground)
            .systemBarsPadding()
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(cardShape)
                .background(Color.White)
                .border(3.dp, color, cardShape)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                content()
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onButtonClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)
            ) {
                Text(buttonText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Big colored circle with an icon, then the title. */
@Composable
internal fun StatusHeader(icon: ImageVector, title: String, color: Color) {
    Box(
        modifier = Modifier.size(76.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(42.dp))
    }
    Spacer(Modifier.height(14.dp))
    Text(
        text = title,
        color = color,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
internal fun StatusMessage(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
}

/** Label/value rows. Rows whose value is null are skipped. */
@Composable
internal fun DetailRows(rows: List<Pair<String, String?>>) {
    rows.forEach { (label, value) ->
        if (value != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(label, color = ValidationColors.LabelGray, fontSize = 13.sp, modifier = Modifier.weight(0.4f))
                Text(
                    value,
                    color = ValidationColors.ValueDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}

@Composable
internal fun WarningBanner(text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(Icons.Filled.Info, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = color, fontSize = 12.sp)
    }
}