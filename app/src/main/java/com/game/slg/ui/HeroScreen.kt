package com.game.slg.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.game.slg.R
import com.game.slg.core.hero.Hero
import com.game.slg.core.hero.HeroRole
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroStage

/**
 * 영웅 탭 — 보유 영웅 배치(배치)와 미보유 영웅 영입(영입).
 *
 * 초상화 10장(2:3)은 `drawable-nodpi/slg_hero_01~10.jpg` 로 탑재되어 있고,
 * 카드에서는 `ContentScale.Fit` 으로 잘리지 않게 보여 준다.
 */
@Composable
fun HeroScreen(
    state: SlgGameState,
    onScout: (String) -> Unit,
    onPersuade: (String) -> Unit,
    onHire: (String) -> Unit,
    onAssign: (String, HeroRole) -> Unit,
    onUnassign: (String) -> Unit,
    tabIndex: Int = 0
) {
    SlgScreenFrame {
        when (HeroTab.entries[tabIndex.coerceIn(0, HeroTab.entries.lastIndex)]) {
            HeroTab.PLACE -> HeroPlaceSection(state, onAssign, onUnassign)
            HeroTab.RECRUIT -> HeroRecruitSection(state, onScout, onPersuade, onHire)
        }
    }
}

/** 초상화 리소스 — 10장 고정 */
fun heroPortraitRes(heroId: String): Int = when (heroId) {
    "hero_01" -> R.drawable.slg_hero_01
    "hero_02" -> R.drawable.slg_hero_02
    "hero_03" -> R.drawable.slg_hero_03
    "hero_04" -> R.drawable.slg_hero_04
    "hero_05" -> R.drawable.slg_hero_05
    "hero_06" -> R.drawable.slg_hero_06
    "hero_07" -> R.drawable.slg_hero_07
    "hero_08" -> R.drawable.slg_hero_08
    "hero_09" -> R.drawable.slg_hero_09
    else -> R.drawable.slg_hero_10
}

/** 2:3 세로 초상화 — 잘리지 않게 Fit */
@Composable
fun HeroPortrait(hero: Hero, widthDp: Int = 88, modifier: Modifier = Modifier) {
    val heightDp = widthDp * 3 / 2
    Box(
        modifier = modifier
            .width(widthDp.dp)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SlgColors.SurfaceAlt)
            .border(1.dp, Color(hero.role.accentHex).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(heroPortraitRes(hero.id)),
            contentDescription = hero.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(heightDp.dp)
        )
    }
}

// ────────────────────────────────────────────────────────────────
// 배치
// ────────────────────────────────────────────────────────────────

@Composable
private fun HeroPlaceSection(
    state: SlgGameState,
    onAssign: (String, HeroRole) -> Unit,
    onUnassign: (String) -> Unit
) {
    val roster = state.heroes
    val bonus = state.heroBonus

    SlgSectionCard(
        title = "배치 보너스",
        badge = "${roster.placedCount}명 배치",
        accent = SlgColors.Violet
    ) {
        Text(
            bonus.summary(),
            color = if (bonus.isEmpty) SlgColors.Muted else SlgColors.Text,
            fontSize = SlgType.BODY_SMALL,
            fontWeight = FontWeight.Bold,
            lineHeight = SlgLine.BODY
        )
        Text(
            "보유 ${roster.ownedCount}명 · 배치 ${roster.placedCount}명 · 대기 ${roster.unplaced.size}명",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
    }

    SlgSectionCard(title = "영지 슬롯", badge = "내정·기술·상업·작전", accent = SlgColors.Brass) {
        HeroRole.primarySlots.forEach { role ->
            HeroSlotRow(state = state, role = role, onUnassign = onUnassign)
        }
    }

    SlgSectionCard(title = "외교 슬롯", badge = "외교·정보", accent = SlgColors.Steel) {
        HeroRole.supportSlots.forEach { role ->
            HeroSlotRow(state = state, role = role, onUnassign = onUnassign)
        }
    }

    if (roster.unplaced.isNotEmpty()) {
        SlgSectionCard(
            title = "대기 영웅",
            badge = "${roster.unplaced.size}명",
            accent = SlgColors.Success
        ) {
            roster.unplaced.forEach { hero ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroPortrait(hero = hero, widthDp = 40)
                    Spacer(Modifier.width(9.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(hero.name, color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                        Text(
                            "${hero.role.label} · ${hero.effectLabel}",
                            color = SlgColors.Muted,
                            fontSize = SlgType.MICRO,
                            lineHeight = SlgLine.MICRO
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(hero.role.accentHex).copy(alpha = 0.2f))
                            .border(1.dp, Color(hero.role.accentHex).copy(alpha = 0.7f), RoundedCornerShape(9.dp))
                            .clickable { onAssign(hero.id, hero.role) }
                            .padding(horizontal = 11.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "배치",
                            color = Color(hero.role.accentHex),
                            fontSize = SlgType.LABEL,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Text(
                "배치는 역할이 같은 슬롯에만 앉습니다. 이미 앉은 영웅은 대기로 내려갑니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
        }
    }

    SlgExpand(
        summary = "슬롯별 효과 정리",
        detail = "내정 = 절기 자금 · 기술 = 연구 비용 할인 · 상업 = 절기 보급 · 작전 = 작전 전투력 · " +
            "외교 = 설득력(영웅 설득 명성 +) · 정보 = 첩보력. 효과는 배치한 동안 계속 적용됩니다.",
        accent = SlgColors.Violet
    )
}

@Composable
private fun HeroSlotRow(state: SlgGameState, role: HeroRole, onUnassign: (String) -> Unit) {
    val hero = state.heroes.heroIn(role)
    val accent = Color(role.accentHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(if (hero != null) accent.copy(alpha = 0.12f) else SlgColors.SurfaceAlt)
            .border(
                1.dp,
                if (hero != null) accent.copy(alpha = 0.5f) else SlgColors.Line.copy(alpha = 0.6f),
                RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 30.dp)
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${role.emoji} ${role.label}",
                color = accent,
                fontSize = SlgType.MICRO,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        if (hero == null) {
            Column(modifier = Modifier.weight(1f)) {
                Text("빈 슬롯", color = SlgColors.Muted, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                Text(
                    "${role.effectLabel} 대기 중 · 영입에서 채우세요",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
            }
        } else {
            HeroPortrait(hero = hero, widthDp = 38)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hero.name, color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.ExtraBold)
                Text(
                    hero.effectLabel,
                    color = accent,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(SlgColors.Surface)
                    .clickable { onUnassign(hero.id) }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text("내리기", color = SlgColors.Muted, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// 영입 — 탐색 → 설득 → 고용
// ────────────────────────────────────────────────────────────────

@Composable
private fun HeroRecruitSection(
    state: SlgGameState,
    onScout: (String) -> Unit,
    onPersuade: (String) -> Unit,
    onHire: (String) -> Unit
) {
    val roster = state.heroes
    val diplomacy = state.heroBonus.diplomacyBonus
    val recruitables = roster.recruitables

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SlgSummaryChip("💰", "${state.gold}", SlgColors.Brass, Modifier.weight(1f))
        SlgSummaryChip("⭐", "명성 ${state.prestige}", SlgColors.Violet, Modifier.weight(1f))
        SlgSummaryChip("🤝", "설득 +$diplomacy", SlgColors.Steel, Modifier.weight(1f))
    }

    SlgExpand(
        summary = "영입 순서 — 탐색 → 설득 → 고용",
        detail = "① 탐색 ${HeroRoster.SCOUT_COST}G — 신상과 접점을 확인하고 호감도 ${HeroRoster.SCOUT_AFFINITY}을 얻습니다.\n" +
            "② 설득 — 명성(＋외교 영웅 설득력)이 필요 명성 이상이면 성공합니다.\n" +
            "③ 고용 — 자금을 지급하면 보유 영웅이 되고, 빈 슬롯에 자동 배치됩니다.\n" +
            "설득에 실패해도 호감도가 오르므로 명성을 쌓아 다시 시도할 수 있습니다.",
        accent = SlgColors.Violet
    )

    if (state.heroMessage != null) {
        ResultBox(state.heroMessage, SlgColors.Violet)
    }

    if (recruitables.isEmpty()) {
        SlgSectionCard(title = "영입 완료", badge = "10명 보유", accent = SlgColors.Success) {
            Text(
                "10명의 영웅을 모두 영입했습니다. 배치 탭에서 슬롯을 바꿔 보세요.",
                color = SlgColors.Text,
                fontSize = SlgType.BODY_SMALL
            )
        }
        return
    }

    recruitables.forEach { hero ->
        RecruitCard(
            hero = hero,
            state = state,
            stage = roster.recordOf(hero.id).stage,
            affinity = roster.recordOf(hero.id).affinity,
            onScout = onScout,
            onPersuade = onPersuade,
            onHire = onHire
        )
    }
}

@Composable
private fun RecruitCard(
    hero: Hero,
    state: SlgGameState,
    stage: HeroStage,
    affinity: Int,
    onScout: (String) -> Unit,
    onPersuade: (String) -> Unit,
    onHire: (String) -> Unit
) {
    val accent = Color(hero.role.accentHex)
    val discovered = stage != HeroStage.UNDISCOVERED
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            HeroPortrait(hero = hero, widthDp = 96)
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(hero.name, color = SlgColors.Text, fontSize = SlgType.TITLE, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(6.dp))
                    SlgPill("${hero.role.emoji} ${hero.role.label}", accent)
                }
                Text(hero.title, color = SlgColors.Muted, fontSize = SlgType.MICRO)
                Text(
                    hero.effectLabel,
                    color = accent,
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.Bold,
                    lineHeight = SlgLine.MICRO
                )
                if (discovered) {
                    Text(
                        "“${hero.quote}”",
                        color = SlgColors.Text.copy(alpha = 0.85f),
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO,
                        maxLines = 3
                    )
                    Text(
                        "호감도 $affinity · 필요 명성 ${hero.requiredPrestige}",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO
                    )
                } else {
                    Text(
                        "탐색하면 신상과 접점을 알 수 있습니다.",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO
                    )
                }
            }
        }

        HeroStageBar(stage = stage)

        when (stage) {
            HeroStage.UNDISCOVERED -> HeroActionButton(
                label = "🔎 탐색 · ${HeroRoster.SCOUT_COST}G",
                enabled = state.gold >= HeroRoster.SCOUT_COST,
                accent = SlgColors.Brass,
                onClick = { onScout(hero.id) }
            )

            HeroStage.SCOUTED -> {
                val power = state.prestige + state.heroBonus.diplomacyBonus
                HeroActionButton(
                    label = if (power >= hero.requiredPrestige) {
                        "🗣 설득 (명성 $power / ${hero.requiredPrestige})"
                    } else {
                        "🗣 설득 — 명성 ${hero.requiredPrestige} 필요 (현재 $power)"
                    },
                    enabled = true,
                    accent = SlgColors.Violet,
                    onClick = { onPersuade(hero.id) }
                )
            }

            HeroStage.PERSUADED -> HeroActionButton(
                label = "✍ 고용 · ${hero.hireCost}G",
                enabled = state.gold >= hero.hireCost,
                accent = SlgColors.Success,
                onClick = { onHire(hero.id) }
            )

            HeroStage.HIRED -> HeroActionButton(
                label = "보유 중",
                enabled = false,
                accent = SlgColors.Muted,
                onClick = {}
            )
        }
    }
}

/** 탐색 → 설득 → 고용 3단계 진행 표시 */
@Composable
private fun HeroStageBar(stage: HeroStage) {
    val steps = listOf(HeroStage.SCOUTED to "탐색", HeroStage.PERSUADED to "설득", HeroStage.HIRED to "고용")
    val reached = when (stage) {
        HeroStage.UNDISCOVERED -> -1
        HeroStage.SCOUTED -> 0
        HeroStage.PERSUADED -> 1
        HeroStage.HIRED -> 2
    }
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        steps.forEachIndexed { index, (_, label) ->
            val done = index <= reached
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (done) SlgColors.Violet.copy(alpha = 0.22f) else SlgColors.SurfaceAlt
                    )
                    .border(
                        1.dp,
                        if (done) SlgColors.Violet.copy(alpha = 0.6f) else SlgColors.Line,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${if (done) "✓" else (index + 1).toString()} $label",
                    color = if (done) SlgColors.Violet else SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HeroActionButton(
    label: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(if (enabled) accent.copy(alpha = 0.92f) else SlgColors.SurfaceAlt)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (enabled) Color(0xFF10131A) else SlgColors.Muted,
            fontSize = SlgType.BODY_SMALL,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/** 보유 영웅 한 줄 — 헤더용 요약 (지도 화면 등에서 재사용) */
@Composable
fun HeroMiniRow(hero: Hero, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(hero.role.accentHex))
        )
        Spacer(Modifier.width(5.dp))
        Text(hero.name, color = SlgColors.Text, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text(hero.role.label, color = SlgColors.Muted, fontSize = SlgType.MICRO)
    }
}
