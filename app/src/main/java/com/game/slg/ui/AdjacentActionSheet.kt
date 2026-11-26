package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.game.slg.core.map.TerritoryMapLayout
import com.game.slg.core.model.TreatyType
import com.game.slg.core.region.AdjacentActionKind
import com.game.slg.core.region.CasusBelli
import com.game.slg.core.region.AdjacentDiplomacyEngine
import com.game.slg.core.region.AdjacentRegionEngine
import com.game.slg.core.region.AdjacentStrategyOp
import com.game.slg.core.region.AdjacentLeaderTarget

/**
 * 지도 인접 지역 하단 패널에서 「전투 · 외교 · 전략」을 눌렀을 때 열리는 시트.
 *
 * 사용자 요청(2026-11-25 07:45)
 *  · 인접 지역을 고르면 전투·외교·전략 메뉴를 바로 띄운다.
 *  · 통합 리더(단일 연방 지배자)가 아직 없으면 지역 전체가 아니라 개별 리더 단위로 전투한다.
 *  · 의회에 안건을 올려 지역 리더 연합으로 공동 전투·외교·전략을 진행한다.
 *
 * 모바일 기준 — 큰 글씨, 한 줄에 버튼 2~3개, 세로 스크롤로 하단 인셋을 피한다.
 */
@Composable
fun AdjacentActionSheetOverlay(
    state: SlgGameState,
    onClose: () -> Unit,
    onChallenge: (String) -> Unit,
    onDiplomacy: (TreatyType) -> Unit,
    onStrategy: (String) -> Unit,
    onSponsor: (AdjacentActionKind) -> Unit,
    onConquestMode: (com.game.slg.core.empire.TerritoryStatus) -> Unit = {}
) {
    val sheet = state.adjacentSheet ?: return
    val node = TerritoryMapLayout.node(state.mapNodes, sheet.code)
    val panel = AdjacentRegionEngine.panel(
        code = sheet.code,
        name = sheet.name,
        relationLabel = node?.relationLabel ?: "경계 세력",
        board = sourceBoard(state, sheet.code),
        unionActive = state.union.active
    )
    val accent = when (sheet.kind) {
        AdjacentActionKind.WAR -> SlgColors.Danger
        AdjacentActionKind.DIPLOMACY -> SlgColors.Success
        AdjacentActionKind.STRATEGY -> SlgColors.Violet
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF20A0C11))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(18.dp))
                .background(SlgColors.Surface)
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .clickable { }
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            // ── 머리말 ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${sheet.kind.icon} ${sheet.name} ${sheet.kind.label}",
                    color = SlgColors.Text,
                    fontSize = SlgType.TITLE,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SlgColors.SurfaceAlt)
                        .clickable { onClose() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text("✕", color = SlgColors.Text, fontSize = SlgType.BODY, fontWeight = FontWeight.Bold)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                SlgPill(panel.relationLabel, relationColor(panel.relationLabel))
                SlgPill(panel.headcountLabel, if (panel.unified) SlgColors.Danger else SlgColors.Steel)
                SlgPill("⚔ ${panel.totalTroops}", SlgColors.Muted)
            }
            Text(
                panel.headline,
                color = SlgColors.Text.copy(alpha = 0.90f),
                fontSize = SlgType.BODY_SMALL,
                fontWeight = FontWeight.Bold,
                lineHeight = SlgLine.MICRO
            )

            // ── 전멸·항복 편입 선택 — 교전 직후 이 자리에서 고른다 ──
            val conquest = state.pendingConquest
            if (conquest != null) {
                SlgSectionCard(
                    title = conquest.headline,
                    badge = "${conquest.reasonLabel} · ${conquest.populationLabel}",
                    accent = SlgColors.Brass
                ) {
                    SlgIconMenuGrid(
                        entries = com.game.slg.core.empire.TerritoryStatus.entries.map { mode ->
                            SlgIconEntry(
                                id = mode.name,
                                icon = mode.icon,
                                label = conquest.optionLabel(mode),
                                tone = when (mode) {
                                    com.game.slg.core.empire.TerritoryStatus.DIRECT -> SlgColors.Brass
                                    com.game.slg.core.empire.TerritoryStatus.VASSAL -> SlgColors.Success
                                    else -> SlgColors.Steel
                                }
                            )
                        },
                        columns = 3,
                        onSelect = { entry ->
                            com.game.slg.core.empire.TerritoryStatus.entries
                                .firstOrNull { it.name == entry.id }
                                ?.let(onConquestMode)
                        }
                    )
                }
            }

            when (sheet.kind) {
                AdjacentActionKind.WAR -> WarSection(
                    panel = panel,
                    unionActive = state.union.active,
                    coalitionBonus = state.coalition.battleBonusPercent,
                    casusBelli = state.casusBelliFor(sheet.code),
                    onChallenge = onChallenge
                )

                AdjacentActionKind.DIPLOMACY -> DiplomacySection(
                    panel = panel,
                    gold = state.gold,
                    diplomacy = state.diplomacy,
                    onPick = onDiplomacy
                )

                AdjacentActionKind.STRATEGY -> StrategySection(
                    panel = panel,
                    gold = state.gold,
                    discount = state.coalition.strategyDiscountPercent,
                    onPick = onStrategy
                )
            }

            // ── 결과 ──
            val result = sheet.result
            val message = state.adjacentMessage
            if (result != null) {
                ResultBox(result.oneLine(), if (result.success) SlgColors.Success else SlgColors.Danger)
            } else if (message != null) {
                ResultBox(message, SlgColors.Steel)
            }

            // ── 의회에 연합 안건 상정 ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .background(SlgColors.Brass.copy(alpha = 0.10f))
                    .border(1.dp, SlgColors.Brass.copy(alpha = 0.40f), RoundedCornerShape(13.dp))
                    .padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    "🏛 의회에 공동 작전 안건 상정",
                    color = SlgColors.Brass,
                    fontSize = SlgType.BODY_SMALL,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "나 혼자가 아니라 지역 리더 연합으로 나섭니다 — 상정 뒤 영지 › 의회 › 연합에서 표결합니다. " +
                        "내 의결권 ${state.councilVotingPower}표가 더해집니다.",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    AdjacentActionKind.entries.forEach { kind ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlgColors.Brass.copy(alpha = 0.20f))
                                .border(1.dp, SlgColors.Brass.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                .clickable { onSponsor(kind) }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${kind.icon} ${kind.label}",
                                color = SlgColors.Brass,
                                fontSize = SlgType.LABEL,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 세부맵과 같은 명부를 쓴다 — 화면 밖 계산을 한 곳으로 모은다 */
private fun sourceBoard(state: SlgGameState, code: String) = state.regionBoardFor(code)

@Composable
private fun WarSection(
    panel: com.game.slg.core.region.AdjacentRegionPanel,
    unionActive: Boolean,
    coalitionBonus: Int,
    casusBelli: com.game.slg.core.region.CasusBelli,
    onChallenge: (String) -> Unit
) {
    SlgSectionCard(
        title = if (panel.unified) "지역 대표와 전투" else "개별 리더 전투",
        badge = panel.headcountLabel,
        accent = SlgColors.Danger
    ) {
        // 규칙 설명은 한 줄만 — 전투 판단에 필요한 것은 수치·배지로 보인다
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (casusBelli != CasusBelli.NONE) {
                SlgPill("의회 명분 ${casusBelli.label}", SlgColors.Success)
            }
            if (coalitionBonus > 0) {
                SlgPill("연합 +$coalitionBonus%", SlgColors.Brass)
            }
            if (panel.absorbedLeaders.isNotEmpty()) {
                SlgPill("편입 ${panel.absorbedLeaders.size}명", SlgColors.Success)
            }
        }
        if (unionActive) {
            SlgPill("🛡 지역 연합 중 · 인접 원정 가능", SlgColors.Success)
        }
        val targets = panel.warTargets
        if (targets.isEmpty()) {
            Text(
                if (panel.absorbedLeaders.isNotEmpty()) "이 지역 리더들이 모두 우리 연합에 들어왔습니다 — 영토 › 통치에서 운영하세요."
                else "개별 교전 대상이 없습니다 — 이 지역은 이미 우두머리에게 묶여 있습니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.LABEL
            )
        }
        targets.take(14).forEach { target ->
            TargetRow(target = target, isRuler = panel.unified, onChallenge = onChallenge)
        }
        if (targets.size > 14) {
            Text(
                "외 ${targets.size - 14}명은 「확대」 세부맵에서",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO
            )
        }
    }
}

@Composable
private fun TargetRow(
    target: AdjacentLeaderTarget,
    isRuler: Boolean,
    onChallenge: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(SlgColors.SurfaceAlt)
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(if (isRuler) SlgColors.Danger else SlgColors.Steel)
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    target.name,
                    color = SlgColors.Text,
                    fontSize = SlgType.BODY_SMALL,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (isRuler) {
                    Spacer(Modifier.width(5.dp))
                    SlgPill("통합 지배자", SlgColors.Danger)
                }
            }
            Text(
                "${target.stronghold} · ⚔ ${target.troops} · ⭐ ${target.prestige}",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(SlgColors.Danger)
                .clickable { onChallenge(target.id) }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                "⚔ 교전",
                color = Color(0xFF10131A),
                fontSize = SlgType.LABEL,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun DiplomacySection(
    panel: com.game.slg.core.region.AdjacentRegionPanel,
    gold: Int,
    diplomacy: Int,
    onPick: (TreatyType) -> Unit
) {
    SlgSectionCard(
        title = "외교 협상",
        badge = "상대 ${panel.diplomacyPartner}",
        accent = SlgColors.Success
    ) {
        Text(
            if (panel.unified) {
                "통합 리더와 한 번에 협상합니다 — 성공률이 높습니다."
            } else {
                "통합 지배자가 없어 리더 연합 전체와 협상합니다 — 리더마다 이해가 달라 성공률이 낮습니다."
            },
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        Text(
            "외교 ${diplomacy} · 자금 ${gold}G",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
        TreatyType.entries.forEach { type ->
            val cost = AdjacentDiplomacyEngine.costOf(type)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(SlgColors.SurfaceAlt)
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        AdjacentDiplomacyEngine.typeLabel(type),
                        color = SlgColors.Text,
                        fontSize = SlgType.BODY_SMALL,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${AdjacentDiplomacyEngine.benefitOf(type)} · ${cost}G",
                        color = if (gold >= cost) SlgColors.Muted else SlgColors.Danger,
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (gold >= cost) SlgColors.Success else SlgColors.Surface)
                        .border(
                            1.dp,
                            if (gold >= cost) SlgColors.Success else SlgColors.Line,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable(enabled = gold >= cost) { onPick(type) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "제안",
                        color = if (gold >= cost) Color(0xFF10131A) else SlgColors.Muted,
                        fontSize = SlgType.LABEL,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategySection(
    panel: com.game.slg.core.region.AdjacentRegionPanel,
    gold: Int,
    discount: Int,
    onPick: (String) -> Unit
) {
    SlgSectionCard(
        title = "전략 공작",
        badge = panel.name,
        accent = SlgColors.Violet
    ) {
        Text(
            "첩보력과 영웅 정보 보너스가 성공률을 끌어올립니다.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
        if (discount > 0) {
            SlgPill("연합 공동 전략 -$discount%", SlgColors.Brass)
        }
        AdjacentStrategyOp.entries.forEach { op ->
            val cost = (op.cost * (100 - discount) / 100).coerceAtLeast(1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(SlgColors.SurfaceAlt)
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "${op.icon} ${op.label}",
                        color = SlgColors.Text,
                        fontSize = SlgType.BODY_SMALL,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${op.detail} · ${cost}G",
                        color = if (gold >= cost) SlgColors.Muted else SlgColors.Danger,
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (gold >= cost) SlgColors.Violet else SlgColors.Surface)
                        .border(
                            1.dp,
                            if (gold >= cost) SlgColors.Violet else SlgColors.Line,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable(enabled = gold >= cost) { onPick(op.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "실행",
                        color = if (gold >= cost) Color(0xFF10131A) else SlgColors.Muted,
                        fontSize = SlgType.LABEL,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
