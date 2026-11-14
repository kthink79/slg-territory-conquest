package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 「단일 연방의 황혼」 팔레트 — 심야 청회색 + 황동 금색
 */
object SlgColors {
    val Background = Color(0xFF0C0E13)
    val Surface = Color(0xFF161A22)
    val SurfaceAlt = Color(0xFF1F2531)
    val Line = Color(0xFF2E3542)
    val Brass = Color(0xFFE2B755)
    val Steel = Color(0xFF7FA4D6)
    val Text = Color(0xFFEDEAE3)
    val Muted = Color(0xFF98A1B0)
    val Danger = Color(0xFFE57373)
    val Success = Color(0xFF81C784)
    val Crimson = Color(0xFFB5484A)
}

fun relationColor(label: String): Color = when (label) {
    Relation.ALLIED.label -> SlgColors.Steel
    Relation.NEUTRAL_MERCHANT.label -> Color(0xFFB39DDB)
    Relation.WARY.label -> SlgColors.Danger
    else -> Color(0xFF90A4AE)
}

@Composable
fun SlgScreenFrame(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlgColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
fun SlgSectionCard(
    title: String,
    badge: String? = null,
    accent: Color = SlgColors.Brass,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlgColors.Surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent)
                )
                Spacer(Modifier.width(8.dp))
                Text(title, color = SlgColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    SlgPill(badge, accent)
                }
            }
            content()
        }
    }
}

@Composable
fun SlgPill(text: String, color: Color = SlgColors.Brass, filled: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color.copy(alpha = 0.9f) else color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text,
            color = if (filled) Color(0xFF10131A) else color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SlgStatBar(ratio: Float, color: Color, modifier: Modifier = Modifier, height: Int = 8) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF262D3A))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(ratio.coerceIn(0.001f, 1f))
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun SlgResourceChip(icon: String, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SlgColors.SurfaceAlt)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text("$icon $label", color = SlgColors.Muted, fontSize = 8.sp)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SlgActionTile(
    label: String,
    hint: String? = null,
    accent: Color = SlgColors.Brass,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(if (enabled) accent.copy(alpha = 0.13f) else SlgColors.SurfaceAlt)
            .border(1.dp, if (enabled) accent.copy(alpha = 0.5f) else SlgColors.Line, RoundedCornerShape(11.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            color = if (enabled) SlgColors.Text else SlgColors.Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (hint != null) {
            Spacer(Modifier.height(2.dp))
            Text(hint, color = if (enabled) accent else SlgColors.Muted, fontSize = 9.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
fun SlgChip(label: String, selected: Boolean, accent: Color = SlgColors.Brass, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.9f) else SlgColors.SurfaceAlt)
            .border(1.dp, if (selected) accent else SlgColors.Line, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 11.dp, vertical = 5.dp)
    ) {
        Text(
            label,
            color = if (selected) Color(0xFF10131A) else SlgColors.Muted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SlgLogBox(lines: List<String>, maxLines: Int = 6) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF10131A))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        lines.takeLast(maxLines).forEach { line ->
            Text("· $line", color = SlgColors.Muted, fontSize = 10.sp, lineHeight = 15.sp)
        }
        if (lines.isEmpty()) {
            Text("· 기록 없음", color = SlgColors.Muted, fontSize = 10.sp)
        }
    }
}
