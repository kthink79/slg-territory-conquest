package com.game.slg.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.game.slg.core.region.LeaderBond
import com.game.slg.core.region.LeaderStatus
import com.game.slg.core.region.RegionBoard
import com.game.slg.core.region.RegionLeaderInfo
import kotlinx.coroutines.launch

/**
 * 지역 세부맵 — 선택한 지역 안의 리더 배치·현황.
 *
 * 사용자 요청(2026-11-24): *"내가 선택된 지역을 클릭하거나 확대를 선택하면 지역내 각 리더의 배치와
 * 현황을 볼 수 있는 세부맵을 띄워 주도록… 3개의 리더를 병합시키거나 종속시켰다면 그런 것도 표현이
 * 되어야 하고 각 리더의 재화나 병력 상태도 간단히 표현… 좌우 상하 스크롤을 넣어줘서 이동하면서"*
 *
 * 구성
 *  · 카드 격자 — 내 세력(★)부터 우리 연방 편입(직할·속국·자치령), AI 병합·종속, 독립 순으로 앉힌다.
 *  · 연결선 — 우두머리 카드에서 예속 카드로 선을 그어 병합(실선)·종속(점선)을 보여 준다.
 *  · 카드 한 장에 이름·지위·병력·자금·식량·활동 메모를 담는다.
 *  · 리더가 48명이면 4열 × 13행 — 좌우·상하로 끌어서 본다(2차원 스크롤).
 */
@Composable
fun RegionDetailMapOverlay(
    board: RegionBoard,
    unionActive: Boolean,
    onClose: () -> Unit,
    onChallenge: (String) -> Unit = {}
) {
    var selectedId by remember(board.regionCode, board.leaders.size) { mutableStateOf<String?>(null) }
    val scrollX = rememberScrollState()
    val scrollY = rememberScrollState()
    val scope = rememberCoroutineScope()
    val selected = selectedId?.let { board.leaderOf(it) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF20A0C11))
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            // ── 머리말 ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "🗺 ${board.regionName} 세부맵",
                        color = SlgColors.Text,
                        fontSize = SlgType.TITLE,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        SlgPill("리더 ${board.totalCount}명", SlgColors.Brass)
                        SlgPill(if (board.live) "실시간 현황" else "정찰 전 추정", if (board.live) SlgColors.Success else SlgColors.Stone)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SlgColors.SurfaceAlt)
                        .border(1.dp, SlgColors.Line, RoundedCornerShape(50))
                        .clickable { onClose() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text("✕", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(7.dp))

            // ── 관계 요약 ──
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    board.relationSummary(),
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    maxLines = 2,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SlgColors.Brass.copy(alpha = 0.16f))
                        .border(1.dp, SlgColors.Brass.copy(alpha = 0.55f), RoundedCornerShape(50))
                        .clickable {
                            scope.launch { scrollX.scrollTo(0) }
                            scope.launch { scrollY.scrollTo(0) }
                        }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text("⌂ 내 세력", color = SlgColors.Brass, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── 범례 ──
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                board.leaders.map { it.status }.distinct().take(5).forEach { status ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(status.accentHex))
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(status.label, color = SlgColors.Muted, fontSize = SlgType.MICRO)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── 2차원 스크롤 격자 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlgColors.Surface)
                    .border(1.dp, SlgColors.Line.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .horizontalScroll(scrollX)
            ) {
                Box(
                    modifier = Modifier
                        .verticalScroll(scrollY)
                        .width((CardPad * 2 + board.columns * CardWidth + (board.columns - 1) * CardGapX).dp)
                        .height((CardPad * 2 + board.rows * CardHeight + (board.rows - 1) * CardGapY).dp)
                ) {
                    // 연결선 — 카드 뒤에 깔린다
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cardW = CardWidth.dp.toPx()
                        val cardH = CardHeight.dp.toPx()
                        val gapX = CardGapX.dp.toPx()
                        val gapY = CardGapY.dp.toPx()
                        val pad = CardPad.dp.toPx()
                        fun center(seat: Int): Offset {
                            val column = seat % board.columns
                            val row = seat / board.columns
                            return Offset(
                                x = pad + column * (cardW + gapX) + cardW / 2f,
                                y = pad + row * (cardH + gapY) + cardH / 2f
                            )
                        }
                        val seatOfId = board.seats.withIndex().associate { (index, seat) -> seat.id to index }
                        board.bonds.forEach { bond ->
                            val from = seatOfId[bond.masterId] ?: return@forEach
                            val to = seatOfId[bond.servantId] ?: return@forEach
                            val start = center(from)
                            val end = center(to)
                            val accent = Color(
                                if (bond.kind == LeaderBond.MERGE) LeaderStatus.MERGED.accentHex
                                else LeaderStatus.SUBORDINATED.accentHex
                            )
                            drawLine(
                                color = accent.copy(alpha = 0.75f),
                                start = start,
                                end = end,
                                strokeWidth = 2.2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = if (bond.kind == LeaderBond.SUBORDINATION) {
                                    PathEffect.dashPathEffect(floatArrayOf(9f, 7f), 0f)
                                } else null
                            )
                            drawCircle(accent.copy(alpha = 0.85f), radius = 3.5.dp.toPx(), center = end)
                        }
                    }

                    // 카드
                    board.leaders.forEach { leader ->
                        val seat = board.seatOf(leader.id) ?: return@forEach
                        LeaderCard(
                            leader = leader,
                            selected = leader.id == selectedId,
                            modifier = Modifier
                                .offset(
                                    x = (CardPad + seat.column * (CardWidth + CardGapX)).dp,
                                    y = (CardPad + seat.row * (CardHeight + CardGapY)).dp
                                )
                                .clickable { selectedId = if (selectedId == leader.id) null else leader.id }
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── 아래: 선택한 리더 상세 ──
            if (selected != null) {
                LeaderDetailBar(
                    leader = selected,
                    unionActive = unionActive,
                    onChallenge = { onChallenge(selected.id) }
                )
            } else {
                Text(
                    "좌우·상하로 끌어 이동 · 카드를 탭하면 재화·병력 상세가 열립니다. " +
                        "선(line)은 병합(실선)·종속(점선) 관계입니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
            }
        }
    }
}

/** 카드 크기 — 폰에서 2장이 보이고 좌우로 넘긴다 */
private const val CardWidth = 150
private const val CardHeight = 134
private const val CardGapX = 12
private const val CardGapY = 30
private const val CardPad = 12

@Composable
private fun LeaderCard(
    leader: RegionLeaderInfo,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = Color(leader.status.accentHex)
    Column(
        modifier = modifier
            .width(CardWidth.dp)
            .height(CardHeight.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) accent.copy(alpha = 0.16f) else SlgColors.SurfaceAlt)
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) accent else accent.copy(alpha = 0.45f),
                RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(leader.status.icon, fontSize = SlgType.LABEL)
            Spacer(Modifier.width(4.dp))
            Text(
                leader.name,
                color = SlgColors.Text,
                fontSize = SlgType.BODY_SMALL,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            SlgPill(leader.status.label, accent, filled = selected)
            Spacer(Modifier.weight(1f))
            Text("⚔ ${compactNumber(leader.troops)}", color = SlgColors.Steel, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("💰 ${compactNumber(leader.gold)}", color = SlgColors.Brass, fontSize = SlgType.MICRO, maxLines = 1)
            Text("🌾 ${compactNumber(leader.food)}", color = SlgColors.Success, fontSize = SlgType.MICRO, maxLines = 1)
        }
        SlgStatBar(
            ratio = (leader.troops.toFloat() / 2_000f).coerceIn(0.03f, 1f),
            color = accent,
            height = 4
        )
        Text(
            leader.note.ifBlank { leader.stronghold },
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LeaderDetailBar(
    leader: RegionLeaderInfo,
    unionActive: Boolean,
    onChallenge: () -> Unit
) {
    val accent = Color(leader.status.accentHex)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SlgColors.SurfaceAlt)
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(leader.status.icon, fontSize = SlgType.BODY_SMALL)
            Spacer(Modifier.width(5.dp))
            Text(
                "${leader.name} 리더",
                color = SlgColors.Text,
                fontSize = SlgType.BODY_SMALL,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            SlgPill(leader.status.label, accent)
        }
        Text(
            "${leader.traitIcon} ${leader.traitLabel} · ${leader.stronghold}",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "⚔ 병력 ${commaNumber(leader.troops)} · 💰 자금 ${commaNumber(leader.gold)} · " +
                "🌾 식량 ${commaNumber(leader.food)} · ⭐ 명성 ${leader.prestige}",
            color = SlgColors.Text.copy(alpha = 0.9f),
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        if (leader.masterName != null) {
            Text(
                "${leader.masterName} 리더 아래 · ${leader.status.label}",
                color = accent,
                fontSize = SlgType.MICRO,
                fontWeight = FontWeight.Bold
            )
        }
        if (leader.note.isNotBlank()) {
            Text(leader.note, color = SlgColors.Muted, fontSize = SlgType.MICRO, lineHeight = SlgLine.MICRO, maxLines = 2)
        }
        if (!leader.status.isOurs && !unionActive) {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(SlgColors.Danger.copy(alpha = 0.9f))
                    .clickable { onChallenge() }
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Text("⚔ 교전", color = Color(0xFF10131A), fontSize = SlgType.MICRO, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

/** 1,234 → "1,234" / 12,345 → "1.2만" */
private fun compactNumber(value: Int): String {
    if (value < 10_000) return commaNumber(value)
    val man = value / 10_000.0
    val text = "%.1f".format(man)
    return (if (text.endsWith(".0")) text.dropLast(2) else text) + "만"
}

private fun commaNumber(value: Int): String = "%,d".format(value)
