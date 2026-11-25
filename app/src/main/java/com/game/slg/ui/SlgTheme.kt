package com.game.slg.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val Violet = Color(0xFFB39DDB)
    val Water = Color(0xFF4E7C9E)
    val Stone = Color(0xFF90A4AE)
}

/**
 * 모바일 가독 기준 글자 크기 — 2026-11-18 사용자 요청으로 한 단계 더 키웠다.
 *
 * "글씨가 너무 작아 메뉴를 다 넣을 공간이 없다면 탭을 추가해서라도 더 키워줘"
 * → 가장 작은 글자도 13sp, 본문 16sp, 제목 19sp, 히어로 24sp.
 * 화면에 한 번에 보이는 정보량은 글자 크기가 아니라 **탭 분리**로 처리다.
 */
object SlgType {
    /** 배지·기록·보조 수치 */
    val MICRO = 13.sp

    /** 칩·라벨·버튼 보조 */
    val LABEL = 14.sp

    /** 카드 제목·탭 라벨 */
    val BODY_SMALL = 15.sp

    /** 본문 */
    val BODY = 16.sp

    /** 화면 제목 */
    val TITLE = 19.sp

    /** 히어로 숫자·대제목 */
    val HERO = 24.sp
}

/** 줄 간격 — 작은 글씨도 읽히도록 본문의 1.35배 */
object SlgLine {
    val MICRO = 18.sp
    val BODY = 22.sp
}

fun relationColor(label: String): Color = when (label) {
    Relation.ALLIED.label -> SlgColors.Steel
    Relation.NEUTRAL_MERCHANT.label -> SlgColors.Violet
    Relation.WARY.label -> SlgColors.Danger
    Relation.CIVIC.label -> SlgColors.Success
    Relation.LOCKED.label -> SlgColors.Line
    Relation.MOUNTAIN_GATE.label -> SlgColors.Stone
    else -> SlgColors.Stone
}

/**
 * 스크롤 가능한 상세 화면 본문.
 *
 * 폰·폴더블·태블릿 어디서든 가로로 늘어나지 않도록 최대 [SlgLayout.MAX_CONTENT_WIDTH]로
 * 묶고 가운데 정렬한다. 여백은 화면 폭에 따라 12~24dp로 바뀐다.
 */
@Composable
fun SlgScreenFrame(content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SlgColors.Background),
        contentAlignment = Alignment.TopCenter
    ) {
        val horizontal = SlgLayout.horizontalPadding(maxWidth.value)
        Column(
            modifier = Modifier
                .widthIn(max = SlgLayout.MAX_CONTENT_WIDTH.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontal.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

/**
 * 가로 꽉 찬 탭 바 — 한 줄에 4개까지, 넘치면 다음 줄로 넘긴다.
 * 글자를 키운 만큼 탭을 잘게 나누고 두 줄까지 허용한다.
 */
@Composable
fun SlgTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    accent: Color = SlgColors.Brass,
    badges: List<Int> = emptyList(),
    onSelect: (Int) -> Unit
) {
    if (tabs.isEmpty()) return
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF11151C))
            .border(1.dp, SlgColors.Line, RoundedCornerShape(14.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.chunked(4).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEachIndexed { columnIndex, label ->
                    val index = rowIndex * 4 + columnIndex
                    val selected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (selected) {
                                    Brush.horizontalGradient(
                                        listOf(accent.copy(alpha = 0.95f), accent.copy(alpha = 0.60f))
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF1A1F29), Color(0xFF1A1F29)))
                                }
                            )
                            .clickable { onSelect(index) }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) Color(0xFF10131A) else SlgColors.Muted,
                            fontSize = SlgType.BODY_SMALL,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        // 상단 서브 메뉴 배지 — 하단 탭 숫자를 메뉴별로 나눠 보여 준다
                        val badge = badges.getOrElse(index) { 0 }
                        if (badge > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 3.dp, end = 4.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Color(0xFF10131A) else SlgColors.Danger),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (badge > 9) "9+" else "$badge",
                                    color = if (selected) accent else Color(0xFF10131A),
                                    fontSize = SlgType.MICRO,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * 하단 메인 탭 바 — 지도 · 영지 · 영웅 · 작전 · 외교.
 * 큰 이모지 + 큰 글씨로 엄지 하나로 오가게 한다.
 */
@Composable
fun SlgBottomNav(
    selected: MainTab,
    modifier: Modifier = Modifier,
    badgeOf: (MainTab) -> Int = { 0 },
    onSelect: (MainTab) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0E13))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MainTab.bottomOrder.forEach { tab ->
            val active = tab == selected
            val accent = Color(tab.accentHex)
            val badge = badgeOf(tab)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) accent.copy(alpha = 0.18f) else SlgColors.Surface)
                    .border(
                        1.dp,
                        if (active) accent.copy(alpha = 0.75f) else SlgColors.Line.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(tab) }
                    .padding(vertical = 7.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(tab.emoji, fontSize = 21.sp)
                    Text(
                        tab.label,
                        color = if (active) accent else SlgColors.Muted,
                        fontSize = if (MainTab.bottomOrder.size >= 6) SlgType.MICRO else SlgType.BODY_SMALL,
                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                if (badge > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(SlgColors.Danger),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$badge",
                            color = Color(0xFF10131A),
                            fontSize = SlgType.MICRO,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlgSectionCard(
    title: String,
    badge: String? = null,
    accent: Color = SlgColors.Brass,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .border(1.dp, SlgColors.Line.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 15.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
            Spacer(Modifier.width(7.dp))
            Text(title, color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
            if (badge != null) {
                Spacer(Modifier.width(7.dp))
                SlgPill(badge, accent)
            }
        }
        content()
    }
}

@Composable
fun SlgPill(text: String, color: Color = SlgColors.Brass, filled: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color.copy(alpha = 0.9f) else color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text,
            color = if (filled) Color(0xFF10131A) else color,
            fontSize = SlgType.MICRO,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun SlgStatBar(ratio: Float, color: Color, modifier: Modifier = Modifier, height: Int = 6) {
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

/** 상단 1줄 요약 칩 — 아이콘 + 숫자만 */
@Composable
fun SlgSummaryChip(
    icon: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xCC161A22))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = SlgType.LABEL)
        Spacer(Modifier.width(3.dp))
        Text(value, color = color, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** 자원 게이지 한 줄 — 라벨 대신 아이콘만 */
@Composable
fun SlgGaugeRow(
    icon: String,
    value: String,
    ratio: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = SlgType.LABEL)
        Spacer(Modifier.width(4.dp))
        Box(modifier = Modifier.weight(1f)) {
            SlgStatBar(ratio = ratio, color = color, height = 5)
        }
        Spacer(Modifier.width(4.dp))
        Text(value, color = color, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** 선택지 효과 배지 — "+800" 같은 짧은 수치만 남긴다 */
data class Effect(val icon: String, val text: String, val tone: Color = SlgColors.Muted)

@Composable
fun SlgEffectRow(effects: List<Effect>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        effects.forEach { effect ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(effect.tone.copy(alpha = 0.13f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(effect.icon, fontSize = SlgType.MICRO)
                Spacer(Modifier.width(2.dp))
                Text(
                    effect.text,
                    color = effect.tone,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 선택지 카드 — 제목 1줄 + 효과 배지 + (필요할 때만) 경고 1줄.
 * 기존「라벨 + 긴 설명문」타일을 대체한다.
 */
@Composable
fun SlgChoiceCard(
    title: String,
    effects: List<Effect>,
    accent: Color = SlgColors.Brass,
    icon: String? = null,
    enabled: Boolean = true,
    footnote: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(if (enabled) accent.copy(alpha = 0.10f) else SlgColors.SurfaceAlt)
            .border(
                1.dp,
                if (enabled) accent.copy(alpha = 0.45f) else SlgColors.Line,
                RoundedCornerShape(11.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Text(icon, fontSize = SlgType.BODY_SMALL)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                title,
                color = if (enabled) SlgColors.Text else SlgColors.Muted,
                fontSize = SlgType.BODY_SMALL,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(
                if (enabled) "›" else "—",
                color = if (enabled) accent else SlgColors.Muted,
                fontSize = SlgType.BODY,
                fontWeight = FontWeight.Bold
            )
        }
        if (effects.isNotEmpty()) SlgEffectRow(effects)
        if (footnote != null) {
            Text(footnote, color = SlgColors.Danger, fontSize = SlgType.MICRO, maxLines = 2)
        }
    }
}

/**
 * 긴 규칙 설명의 대체 — 기본은 한 줄 요약, 「?」를 누르면 원문이 열린다.
 * 텍스트 벽을 화면에서 걷어내면서 규칙 정보는 잃지 않기 위한 장치.
 */
@Composable
fun SlgExpand(summary: String, detail: String, accent: Color = SlgColors.Muted) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFF11151C))
            .clickable { expanded = !expanded }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f))
                    .border(1.dp, accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = accent, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
            Text(
                summary,
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                modifier = Modifier.weight(1f),
                maxLines = if (expanded) 6 else 1
            )
            Text(if (expanded) "▲" else "▼", color = accent, fontSize = SlgType.MICRO)
        }
        if (expanded) {
            Text(detail, color = SlgColors.Muted, fontSize = SlgType.MICRO, lineHeight = SlgLine.MICRO)
        }
    }
}

/** 서브 메뉴 그리드 타일 — 큰 이모지 + 짧은 라벨 + 미결 배지 */
@Composable
fun SlgMenuTile(
    emoji: String,
    label: String,
    badge: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.16f), SlgColors.Surface)
                )
            )
            .border(1.dp, accent.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 21.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                color = SlgColors.Text,
                fontSize = SlgType.LABEL,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        if (badge > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(15.dp)
                    .clip(CircleShape)
                    .background(SlgColors.Danger),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$badge",
                    color = Color(0xFF10131A),
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/** 화면 상단 아트 헤더 — 일러스트 위에 제목·배지만 */
@Composable
fun SlgArtHeader(
    @DrawableRes image: Int,
    title: String,
    badges: List<Pair<String, Color>> = emptyList(),
    height: Int = 148
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x11000000), Color(0xE60C0E13))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                badges.forEach { (label, color) -> SlgPill(label, color) }
            }
            Text(title, color = SlgColors.Text, fontSize = SlgType.TITLE, fontWeight = FontWeight.ExtraBold)
        }
    }
}

/** 기록 — 기본 3줄만 보이고 나머지는 접는다 */
@Composable
fun SlgLogBox(lines: List<String>, maxLines: Int = 3) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF11151C))
            .clickable { expanded = !expanded }
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val visible = if (expanded) lines.takeLast(12) else lines.takeLast(maxLines)
        visible.forEach { line ->
            Text("· $line", color = SlgColors.Muted, fontSize = SlgType.MICRO, lineHeight = SlgLine.MICRO)
        }
        if (lines.isEmpty()) {
            Text("· 기록 없음", color = SlgColors.Muted, fontSize = SlgType.MICRO)
        } else if (lines.size > maxLines) {
            Text(
                if (expanded) "▲ 접기" else "▼ 기록 ${lines.size}건",
                color = SlgColors.Brass,
                fontSize = SlgType.MICRO,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** 결과 1줄 알림 */
@Composable
fun ResultBox(text: String?, accent: Color) {
    if (text == null) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            .padding(9.dp)
    ) {
        Text(text, color = SlgColors.Text.copy(alpha = 0.92f), fontSize = SlgType.MICRO, lineHeight = SlgLine.MICRO)
    }
}

/** 「← 메인」 복귀 아이콘 버튼 */
@Composable
fun SlgBackButton(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SlgColors.Brass.copy(alpha = 0.14f))
            .border(1.dp, SlgColors.Brass.copy(alpha = 0.55f), RoundedCornerShape(50))
            .clickable { onBack() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            "‹ 메인",
            color = SlgColors.Brass,
            fontSize = SlgType.LABEL,
            fontWeight = FontWeight.Bold
        )
    }
}
