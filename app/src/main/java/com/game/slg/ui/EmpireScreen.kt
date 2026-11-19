package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.game.slg.core.empire.AbsorptionMethod
import com.game.slg.core.empire.EmpireEngine
import com.game.slg.core.empire.ManpowerEngine
import com.game.slg.core.empire.MercenaryEngine
import com.game.slg.core.empire.TerritoryStatus

/**
 * 영토 탭 — 흡수 / 통치 / 병력.
 *
 * · 흡수: 같은 지역의 다른 리더 세력을 6가지 방법으로 편입한다.
 * · 통치: 편입 영지를 골라 전환하고, 영웅을 태수로 일임해 자동 운영한다.
 * · 병력: 인구 5% 정규 상한과 별도로 고비용 용병을 운용한다.
 *
 * 모든 리더·영웅은 유저/AI 구분 없이 같은 표기와 같은 규칙으로 보인다(블라인드 원칙).
 */
@Composable
fun EmpireScreen(
    state: SlgGameState,
    onSelectMethod: (AbsorptionMethod, String) -> Unit,
    onCreateCommonEnemy: (String) -> Unit,
    onInstigate: (String) -> Unit,
    onSwitchTerritory: (String) -> Unit,
    onAssignViceroy: (String, String) -> Unit,
    onUnassignViceroy: (String) -> Unit,
    onDevelopTerritory: (String) -> Unit,
    onRecruitRegular: () -> Unit,
    onHireMercenary: () -> Unit,
    onDismissMercenary: (String) -> Unit,
    onGatherPrivateTroops: () -> Unit,
    onReportMerit: () -> Unit,
    onDeclareIndependence: () -> Unit,
    tabIndex: Int = 0
) {
    SlgScreenFrame {
        when (EmpireTab.entries[tabIndex.coerceIn(0, EmpireTab.entries.lastIndex)]) {
            EmpireTab.ABSORB -> AbsorptionPanel(state, onSelectMethod, onCreateCommonEnemy, onInstigate)
            EmpireTab.GOVERN -> GovernancePanel(
                state = state,
                onSwitchTerritory = onSwitchTerritory,
                onAssignViceroy = onAssignViceroy,
                onUnassignViceroy = onUnassignViceroy,
                onDevelopTerritory = onDevelopTerritory,
                onGatherPrivateTroops = onGatherPrivateTroops,
                onReportMerit = onReportMerit,
                onDeclareIndependence = onDeclareIndependence
            )

            EmpireTab.MANPOWER -> ManpowerPanel(state, onRecruitRegular, onHireMercenary, onDismissMercenary)
        }
    }
}

// ────────────────────────────────────────────────────────────────
// 흡수
// ────────────────────────────────────────────────────────────────

@Composable
private fun AbsorptionPanel(
    state: SlgGameState,
    onSelectMethod: (AbsorptionMethod, String) -> Unit,
    onCreateCommonEnemy: (String) -> Unit,
    onInstigate: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SlgSummaryChip("🏙", "영지 ${state.territories.size}", SlgColors.Brass, Modifier.weight(1f))
        SlgSummaryChip("🤝", "편입 ${state.incorporatedCount}", SlgColors.Success, Modifier.weight(1f))
        SlgSummaryChip("⚔", "${state.totalTroops}", SlgColors.Steel, Modifier.weight(1f))
    }

    SlgExpand(
        summary = "흡수 6가지 길",
        detail = AbsorptionMethod.entries.joinToString("\n") {
            "${it.icon} ${it.label} — ${it.condition}"
        },
        accent = SlgColors.Brass
    )

    if (state.openFactions.isEmpty()) {
        SlgSectionCard(title = "편입 완료", badge = "${state.incorporatedCount}곳", accent = SlgColors.Success) {
            Text(
                "이 지역의 모든 세력을 우리 연방으로 묶었습니다. 속국이 많을수록 총력 보너스가 커집니다 " +
                    "(현재 +${state.coalitionBonusPercent}%).",
                color = SlgColors.Text,
                fontSize = SlgType.BODY_SMALL
            )
        }
    }

    val ctx = state.empireContext()
    state.openFactions.take(8).forEach { faction ->
        SlgSectionCard(
            title = "${faction.name} 리더",
            badge = faction.relation.label,
            accent = when (faction.relation) {
                com.game.slg.core.empire.FactionRelation.HOSTILE -> SlgColors.Danger
                com.game.slg.core.empire.FactionRelation.WARY -> SlgColors.Brass
                else -> SlgColors.Steel
            }
        ) {
            Text(
                "${faction.territoryName} · ⚔ ${"%,d".format(faction.troops)} · 민심 ${faction.morale} · " +
                    "우호 ${faction.affinity} · 명성 ${faction.prestige}",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            if (!faction.commonEnemy) {
                SlgChoiceCard(
                    title = "공동 적 조성",
                    icon = "🎯",
                    effects = listOf(
                        Effect("💰", "250G", SlgColors.Brass),
                        Effect("🤝", "우호 +8", SlgColors.Success)
                    ),
                    accent = SlgColors.Steel,
                    onClick = { onCreateCommonEnemy(faction.id) }
                )
            }
            if (faction.morale > 40) {
                SlgChoiceCard(
                    title = "첩자 민심 공작",
                    icon = "🕵",
                    effects = listOf(
                        Effect("💰", "300G", SlgColors.Brass),
                        Effect("❤", "민심 -14", SlgColors.Danger),
                        Effect("🕵", "첩보 ${state.espionagePower}", SlgColors.Violet)
                    ),
                    accent = SlgColors.Violet,
                    enabled = state.gold >= 300,
                    onClick = { onInstigate(faction.id) }
                )
            }
            val available = AbsorptionMethod.entries
                .map { it to com.game.slg.core.empire.AbsorptionEngine.availability(it, faction, ctx) }
            available.filter { it.second == null }.forEach { (method, _) ->
                SlgChoiceCard(
                    title = "${method.label} · ${method.short}",
                    icon = method.icon,
                    effects = listOf(Effect("✓", "가능", SlgColors.Success)),
                    accent = SlgColors.Brass,
                    onClick = { onSelectMethod(method, faction.id) }
                )
            }
            val blocked = available.mapNotNull { (method, reason) -> reason?.let { method to it } }
            if (blocked.isNotEmpty()) {
                SlgExpand(
                    summary = "불가 ${blocked.size}가지 — 사유",
                    detail = blocked.joinToString("\n") { (method, reason) -> "${method.icon} ${method.label} — $reason" },
                    accent = SlgColors.Muted
                )
            }
        }
    }

    if (state.lastAbsorption != null) {
        ResultBox(state.lastAbsorption.message, if (state.lastAbsorption.success) SlgColors.Success else SlgColors.Danger)
    }
    SlgLogBox(state.log, maxLines = 3)
}

// ────────────────────────────────────────────────────────────────
// 통치 — 영지 전환 · 태수 일임
// ────────────────────────────────────────────────────────────────

@Composable
private fun GovernancePanel(
    state: SlgGameState,
    onSwitchTerritory: (String) -> Unit,
    onAssignViceroy: (String, String) -> Unit,
    onUnassignViceroy: (String) -> Unit,
    onDevelopTerritory: (String) -> Unit,
    onGatherPrivateTroops: () -> Unit,
    onReportMerit: () -> Unit,
    onDeclareIndependence: () -> Unit
) {
    // 영웅으로 시작한 유저의 경력 패널
    state.heroCareer?.let { career ->
        SlgSectionCard(
            title = "영웅 경력",
            badge = if (career.independent) "독립" else "휘하",
            accent = SlgColors.Violet
        ) {
            Text(career.summary, color = SlgColors.Text, fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
            Text(
                "${career.employerTerritory} · 상대가 누구인지와 무관하게 능력치·명성으로만 평가받습니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            SlgChoiceCard(
                title = "사병 모집",
                icon = "🥷",
                effects = listOf(Effect("⚔", "+60명", SlgColors.Steel), Effect("💰", "420G", SlgColors.Brass)),
                accent = SlgColors.Steel,
                onClick = onGatherPrivateTroops
            )
            SlgChoiceCard(
                title = "공적 보고",
                icon = "📜",
                effects = listOf(Effect("⭐", "명성 +3", SlgColors.Brass), Effect("❤", "충성 +2", SlgColors.Success)),
                accent = SlgColors.Brass,
                onClick = onReportMerit
            )
            SlgChoiceCard(
                title = if (career.merit >= 40 && state.prestige >= 55) "독립 선언" else "독립 (공적 40 · 명성 55)",
                icon = "🏳",
                effects = listOf(
                    Effect("⚔", "사병 ${"%,d".format(career.privateTroops)}명", SlgColors.Steel),
                    Effect("⭐", "${state.prestige}/55", SlgColors.Brass)
                ),
                accent = SlgColors.Danger,
                enabled = career.merit >= 40 && state.prestige >= 55,
                onClick = onDeclareIndependence
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SlgSummaryChip("👥", "%,d".format(state.totalTerritoryPopulation), SlgColors.Text, Modifier.weight(1f))
        SlgSummaryChip("🏙", "${state.territories.size}곳", SlgColors.Brass, Modifier.weight(1f))
        SlgSummaryChip("💪", "${state.empirePower}", SlgColors.Steel, Modifier.weight(1f))
    }

    SlgSectionCard(
        title = "편입 영지",
        badge = "전환 · 통치",
        accent = SlgColors.Brass
    ) {
        Text(
            "영지를 골라 직접 통치하거나 태수를 앉힙니다. 편입이 늘수록 총력 보너스 +${state.coalitionBonusPercent}%.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        state.territories.forEach { territory ->
            val selected = territory.code == state.selectedManaged?.code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (selected) SlgColors.Brass.copy(alpha = 0.14f) else SlgColors.SurfaceAlt)
                    .border(
                        1.dp,
                        if (selected) SlgColors.Brass.copy(alpha = 0.7f) else SlgColors.Line.copy(alpha = 0.6f),
                        RoundedCornerShape(11.dp)
                    )
                    .clickable { onSwitchTerritory(territory.code) }
                    .padding(horizontal = 9.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(territory.status.icon, fontSize = SlgType.BODY)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        territory.name,
                        color = SlgColors.Text,
                        fontSize = SlgType.BODY_SMALL,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        "${territory.summaryLine()} · 태수 ${territory.viceroyName ?: "없음"}",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO,
                        maxLines = 1
                    )
                }
                if (selected) SlgPill("선택", SlgColors.Brass, filled = true)
            }
        }
    }

    state.selectedManaged?.let { territory ->
        SlgSectionCard(
            title = "${territory.name} 통치",
            badge = territory.status.label,
            accent = SlgColors.Steel
        ) {
            Text(territory.summaryLine(), color = SlgColors.Text, fontSize = SlgType.BODY_SMALL)
            Text(
                "절기 세수 ${"%,d".format(territory.taxIncome())}G · 식량 ${"%,d".format(territory.foodIncome())}석 · " +
                    "징병 ${"%,d".format(territory.leviesPerSeason())}명 · 정규 상한 ${"%,d".format(territory.troopCap)}명",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            Text(
                "편입 경위 · ${territory.absorbedBy}",
                color = SlgColors.Brass,
                fontSize = SlgType.MICRO
            )
            SlgChoiceCard(
                title = "직접 개발",
                icon = "🏗",
                effects = listOf(Effect("💰", "350G", SlgColors.Brass), Effect("📈", "개발 +5", SlgColors.Success)),
                accent = SlgColors.Brass,
                onClick = { onDevelopTerritory(territory.code) }
            )
            if (territory.viceroyHeroId != null) {
                SlgChoiceCard(
                    title = "태수 해임 — 직접 통치",
                    icon = "✋",
                    effects = listOf(Effect("👤", territory.viceroyName ?: "", SlgColors.Violet)),
                    accent = SlgColors.Muted,
                    onClick = { onUnassignViceroy(territory.code) }
                )
            }
        }
    }

    SlgSectionCard(
        title = "영웅 일임",
        badge = "태수 배치",
        accent = SlgColors.Violet
    ) {
        Text(
            "영웅을 태수로 앉히면 매 절기 능력치대로 개발·민심·세수·징병·방어가 자동으로 돌아갑니다. " +
                "(내정=세수, 상업=식량, 작전=징병·방어, 외교=민심, 기술=개발)",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        if (state.heroes.ownedHeroes.isEmpty()) {
            Text("보유 영웅이 없습니다 — 영웅 탭에서 탐색·영입하세요.", color = SlgColors.Muted, fontSize = SlgType.MICRO)
        }
        state.heroes.ownedHeroes.take(24).forEach { hero ->
            val assignedTerritory = state.territories.firstOrNull { it.viceroyHeroId == hero.id }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SlgColors.SurfaceAlt)
                    .clickable { state.selectedManaged?.let { onAssignViceroy(hero.id, it.code) } }
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroPortrait(hero = hero, widthDp = 34)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(hero.name, color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                    Text(
                        "${hero.role.label} · ${hero.statLine}",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO,
                        maxLines = 2,
                        lineHeight = SlgLine.MICRO
                    )
                }
                Text(
                    assignedTerritory?.name ?: "일임 →",
                    color = if (assignedTerritory != null) SlgColors.Success else SlgColors.Violet,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }

    state.empireMessage?.let { ResultBox(it, SlgColors.Brass) }
    SlgLogBox(state.log, maxLines = 3)
}

// ────────────────────────────────────────────────────────────────
// 병력 — 인구 5% 상한 · 용병
// ────────────────────────────────────────────────────────────────

@Composable
private fun ManpowerPanel(
    state: SlgGameState,
    onRecruitRegular: () -> Unit,
    onHireMercenary: () -> Unit,
    onDismissMercenary: (String) -> Unit
) {
    val cap = state.troopCap
    val room = (cap - state.troops).coerceAtLeast(0)
    var mercSize by remember { mutableStateOf(300) }

    SlgSectionCard(title = "정규 병력 상한", badge = "인구 5%", accent = SlgColors.Steel) {
        Text(
            ManpowerEngine.label(state.totalTerritoryPopulation, state.troops, state.mercenaryTroops),
            color = SlgColors.Text,
            fontSize = SlgType.BODY_SMALL,
            fontWeight = FontWeight.Bold,
            lineHeight = SlgLine.BODY
        )
        SlgStatBar(
            ratio = if (cap == 0) 0f else state.troops.toFloat() / cap,
            color = if (room == 0) SlgColors.Danger else SlgColors.Steel,
            height = 6
        )
        Text(
            "남은 정규 정원 ${"%,d".format(room)}명 · 편입 영지 인구 ${"%,d".format(state.totalTerritoryPopulation)}명. " +
                "영지를 편입하면 상한 자체가 올라갑니다.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        SlgChoiceCard(
            title = "정규병 300명 모집",
            icon = "🪖",
            effects = listOf(
                Effect("💰", "500G", SlgColors.Brass),
                Effect("🌾", "400석", SlgColors.Success),
                Effect("⚔", if (room == 0) "상한 도달" else "+${minOf(300, room)}명", SlgColors.Steel)
            ),
            accent = SlgColors.Steel,
            enabled = room > 0 && state.gold >= 500 && state.food >= 400,
            onClick = onRecruitRegular
        )
    }

    SlgSectionCard(title = "고비용 용병", badge = "${state.mercenaries.size}부대", accent = SlgColors.Brass) {
        Text(
            "용병은 정규 상한과 별도로 운용됩니다. 고용비 ${"%,d".format(MercenaryEngine.HIRE_PER_100)}G/100명 · " +
                "유지비 ${"%,d".format(MercenaryEngine.UPKEEP_PER_100)}G/100명(절기). 정규 모집의 3~4배 비용이고, " +
                "유지비를 못 주면 35%가 이탈합니다.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        listOf(300, 600, 900).forEach { size ->
            SlgChoiceCard(
                title = "용병 ${size}명 계약 조건",
                icon = "⚔",
                effects = listOf(
                    Effect("💰", "고용 ${"%,d".format(MercenaryEngine.hireCostOf(size))}G", SlgColors.Brass),
                    Effect("📆", "유지 ${"%,d".format(MercenaryEngine.upkeepOf(size))}G/절기", SlgColors.Muted)
                ),
                accent = if (mercSize == size) SlgColors.Brass else SlgColors.Line,
                onClick = { mercSize = size }
            )
        }
        SlgChoiceCard(
            title = "용병 ${mercSize}명 고용",
            icon = "🗡",
            effects = listOf(
                Effect("💰", "${"%,d".format(MercenaryEngine.hireCostOf(mercSize))}G", SlgColors.Brass),
                Effect("⚔", "+${mercSize}명", SlgColors.Steel)
            ),
            accent = SlgColors.Brass,
            enabled = state.gold >= MercenaryEngine.hireCostOf(mercSize),
            onClick = onHireMercenary
        )
        state.mercenaries.forEach { band ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SlgColors.SurfaceAlt)
                    .padding(horizontal = 9.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗡", fontSize = SlgType.BODY_SMALL)
                Spacer(Modifier.width(7.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(band.name, color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                    Text(band.summary, color = SlgColors.Muted, fontSize = SlgType.MICRO, maxLines = 1)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(SlgColors.Surface)
                        .clickable { onDismissMercenary(band.id) }
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                    Text("해지", color = SlgColors.Muted, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    SlgSectionCard(title = "총력", badge = "${state.empirePower}", accent = SlgColors.Success) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SlgSummaryChip("⚔", "정규 ${state.troops}", SlgColors.Steel, Modifier.weight(1f))
            SlgSummaryChip("🗡", "용병 ${state.mercenaryTroops}", SlgColors.Brass, Modifier.weight(1f))
            SlgSummaryChip("🤝", "+${state.coalitionBonusPercent}%", SlgColors.Success, Modifier.weight(1f))
        }
        Text(
            "속국·자치령이 많을수록 연방 총력 보너스가 커집니다. 이 보너스는 모든 작전의 전투력에 더해집니다.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        Text(
            "편입 영지 절기 세수 예상 ${"%,d".format(EmpireEngine.projectedTax(state.territories, state.factions))}G",
            color = SlgColors.Brass,
            fontSize = SlgType.MICRO
        )
    }

    state.empireMessage?.let { ResultBox(it, SlgColors.Brass) }
    SlgLogBox(state.log, maxLines = 3)
}
