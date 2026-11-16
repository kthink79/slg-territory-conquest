package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.R
import com.game.slg.core.model.PromotionStage

/**
 * 시나리오 카드 — 일러스트 + 선택지 2개 + 효과 배지.
 *
 * 기존에는 사건마다 4~6줄짜리 서사 문단이 화면을 채웠다. 이제는
 * ① 일러스트(148dp) ② 한 줄 상황 ③ 효과 배지가 붙은 선택지 ④ 결과 한 줄 만 보이고,
 * 서사 원문은 「?」를 눌러야 열린다.
 */

/** 이벤트 A — 원천동 상인연합의 야간 물자 협약 */
@Composable
fun EventMerchantCard(state: SlgGameState, onChooseEventA: (Boolean) -> Unit) {
    SlgArtHeader(
        image = R.drawable.slg_event_merchant,
        title = "원천동 야간 협약",
        badges = listOf("🌙 야간" to SlgColors.Violet, "상인연합" to SlgColors.Stone)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(SlgColors.Surface)
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            "관세를 면제하면 야간 보급로로 곡물을 대겠다는 제안.",
            color = SlgColors.Text.copy(alpha = 0.9f),
            fontSize = SlgType.BODY_SMALL,
            maxLines = 2
        )
        if (state.eventAChoice == null) {
            SlgChoiceCard(
                title = "관세 면제 · 보급 협정",
                icon = "🤝",
                effects = listOf(
                    Effect("💰", "-600", SlgColors.Brass),
                    Effect("🌾", "+1,200", SlgColors.Success),
                    Effect("❤", "+8"),
                    Effect("🤝", "+12", SlgColors.Steel)
                ),
                accent = SlgColors.Violet,
                onClick = { onChooseEventA(true) }
            )
            SlgChoiceCard(
                title = "통행세 징수",
                icon = "💰",
                effects = listOf(
                    Effect("💰", "+900", SlgColors.Brass),
                    Effect("❤", "-10", SlgColors.Danger),
                    Effect("🤝", "-15", SlgColors.Danger)
                ),
                accent = SlgColors.Danger,
                onClick = { onChooseEventA(false) }
            )
        } else {
            ResultBox(state.eventAResult, SlgColors.Violet)
        }
        SlgExpand(
            summary = "사절의 말 — 원문",
            detail = "「관세를 면제해 주면 야간 보급로로 곡물과 연료를 대겠습니다. " +
                "대신 우리 화물에는 통행세를 매기지 마십시오.」",
            accent = SlgColors.Violet
        )
    }
}

/** 이벤트 B — 광교산 협곡의 정체불명 정찰대 */
@Composable
fun EventScoutCard(state: SlgGameState, onChooseEventB: (Boolean) -> Unit) {
    SlgArtHeader(
        image = R.drawable.slg_event_scout,
        title = "광교산 정찰대 30",
        badges = listOf("🏔 산악" to SlgColors.Stone, "경보" to SlgColors.Danger)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(SlgColors.Surface)
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            "군장 없이 곡괭이와 측량 장비를 든 30여 명. 광교 깃발은 없다.",
            color = SlgColors.Text.copy(alpha = 0.9f),
            fontSize = SlgType.BODY_SMALL,
            maxLines = 2
        )
        if (state.eventBChoice == null) {
            SlgChoiceCard(
                title = "산악 매복 요격",
                icon = "🏔",
                effects = listOf(
                    Effect("⚔", "-80", SlgColors.Danger),
                    Effect("💰", "+250", SlgColors.Brass),
                    Effect("⭐", "+6", SlgColors.Brass)
                ),
                accent = SlgColors.Danger,
                footnote = "광교 보복 위험",
                onClick = { onChooseEventB(true) }
            )
            SlgChoiceCard(
                title = "첩자 파견 · 배후 파악",
                icon = "🕵",
                effects = listOf(
                    Effect("💰", "-200", SlgColors.Brass),
                    Effect("❤", "+3"),
                    Effect("🎯", "군량고", SlgColors.Steel)
                ),
                accent = SlgColors.Steel,
                onClick = { onChooseEventB(false) }
            )
        } else {
            ResultBox(state.eventBResult, SlgColors.Steel)
        }
        SlgExpand(
            summary = "감시탑 보고 — 원문",
            detail = "「미확인 인원 30여 명, 군장 없이 곡괭이와 측량 장비를 들고 협곡을 오르고 있습니다. " +
                "광교 호족연합의 깃발은 없습니다.」",
            accent = SlgColors.Steel
        )
    }
}

/** 「연방 붕괴의 그림자」 유민 유입 딜레마 */
@Composable
fun RefugeeCrisisCard(state: SlgGameState, onChooseRefugee: (String) -> Unit) {
    SlgArtHeader(
        image = R.drawable.slg_event_refugee,
        title = "유민 800 · 성문 앞",
        badges = listOf("🚪 긴급" to SlgColors.Danger, "D+14" to SlgColors.Stone)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(SlgColors.Surface)
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SlgSummaryChip("🌾", "${state.food}", SlgColors.Success, Modifier.weight(1f))
            SlgSummaryChip("👥", "${state.population}", SlgColors.Text, Modifier.weight(1f))
            SlgSummaryChip("⭐", "${state.culturePrestige}", SlgColors.Brass, Modifier.weight(1f))
        }
        if (state.refugeeChoice == null) {
            SlgChoiceCard(
                title = "성문 개방 · 식량 배급",
                icon = "🚪",
                effects = listOf(
                    Effect("🌾", "-500", SlgColors.Danger),
                    Effect("👥", "+800", SlgColors.Success),
                    Effect("❤", "+10"),
                    Effect("⭐", "+8", SlgColors.Brass)
                ),
                accent = SlgColors.Success,
                enabled = state.food >= 500,
                footnote = if (state.food < 500) "식량 ${state.food}석 — 개방 불가" else null,
                onClick = { onChooseRefugee("REFUGEE_A_OPEN") }
            )
            SlgChoiceCard(
                title = "성문 통제 · 선별 수용",
                icon = "🛡",
                effects = listOf(
                    Effect("💰", "+200", SlgColors.Brass),
                    Effect("❤", "-12", SlgColors.Danger),
                    Effect("🤝", "-8", SlgColors.Danger)
                ),
                accent = SlgColors.Danger,
                onClick = { onChooseRefugee("REFUGEE_B_CONTROL") }
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SlgPill("선택 완료", SlgColors.Success, filled = true)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.isRefugeeAccepted) "성문 개방 · 유민 수용" else "성문 통제 · 선별 수용",
                    color = SlgColors.Text,
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.SemiBold
                )
            }
            ResultBox(state.refugeeResult, if (state.isRefugeeAccepted) SlgColors.Success else SlgColors.Danger)
        }
        SlgExpand(
            summary = "성문 앞 — 원문",
            detail = "연방 통신·물류망이 끊긴 세 번째 밤, 매탄동 성문 앞에 유민 800명이 몰려들었다. " +
                "아이를 안은 여성이 성문을 두드린다. 「호족님, 하룻밤만이라도… 물 한 모금이면 됩니다.」",
            accent = SlgColors.Danger
        )
    }
}

/**
 * 👑 성주 승격 마일스톤 — 로드맵 트랙 + 조건 게이지 2개 + 승격 버튼
 */
@Composable
fun PromotionRoadmapCard(
    state: SlgGameState,
    onPromote: () -> Unit
) {
    val stages = PromotionStage.entries.toList()
    val next = state.nextPromotionStage
    val currentIndex = stages.indexOfFirst { it == state.promotionStage }.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        SlgRoadmapTrack(
            stages = stages.map { it.displayName.substringBefore(" ") },
            currentIndex = currentIndex,
            modifier = Modifier.fillMaxWidth().height(74.dp)
        )
        if (next != null) {
            val alliedRatio = if (next.requiredAlliedTerritories == 0) 1f
            else (state.alliedTerritories.toFloat() / next.requiredAlliedTerritories).coerceIn(0f, 1f)
            val prestigeRatio = if (next.requiredPrestige == 0) 1f
            else (state.prestige.toFloat() / next.requiredPrestige).coerceIn(0f, 1f)
            SlgGaugeRow(
                icon = "🤝",
                value = "${state.alliedTerritories}/${next.requiredAlliedTerritories}",
                ratio = alliedRatio,
                color = SlgColors.Steel
            )
            SlgGaugeRow(
                icon = "⭐",
                value = "${state.prestige}/${next.requiredPrestige}",
                ratio = prestigeRatio,
                color = SlgColors.Brass
            )
            SlgChoiceCard(
                title = "${next.displayName} 승격 의식",
                icon = "👑",
                effects = listOf(
                    Effect("🏛", "권한 +${next.councilAuthorityBonus}%", SlgColors.Brass)
                ),
                accent = SlgColors.Brass,
                enabled = state.isPromotionEligible,
                footnote = if (state.isPromotionEligible) null else "조건 미달",
                onClick = onPromote
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(listOf(SlgColors.Brass, Color(0xFFF5D98A)))
                    )
                    .padding(10.dp)
            ) {
                Text(
                    "👑 수원시 통합군주 — 로드맵 완주 · 의회 권한 +${state.councilAuthorityBonus}%",
                    color = Color(0xFF1A1305),
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/** 승격 의식 배너 */
@Composable
fun PromotionCeremonyBanner(state: SlgGameState, onDismissCeremony: () -> Unit) {
    val ceremony = state.promotionCeremony ?: return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFE2B755), Color(0xFFF5D98A), Color(0xFFB5484A))
                )
            )
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "👑 ${state.promotionStage.displayName} 승격",
                color = Color(0xFF1A1305),
                fontSize = SlgType.TITLE,
                fontWeight = FontWeight.ExtraBold
            )
            Text(ceremony, color = Color(0xFF24190A), fontSize = SlgType.LABEL, lineHeight = 17.sp)
            SlgChoiceCard(
                title = "의회 권한 +${state.councilAuthorityBonus}% · 닫기",
                effects = emptyList(),
                accent = Color(0xFF6D4C1A),
                onClick = onDismissCeremony
            )
        }
    }
}
