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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.core.model.PromotionStage

/**
 * 시나리오 카드 모음 — 「원천동 야간 협약」·「광교산 정찰대」·「유민 800명 딜레마」·「성주 승격 로드맵」.
 * 영지·내정 상세 화면과 이벤트/성주 로드맵 서브 메뉴가 같은 카드를 공유한다.
 */

/** 이벤트 A — 원천동 상인연합의 야간 물자 협약 제안 */
@Composable
fun EventMerchantCard(state: SlgGameState, onChooseEventA: (Boolean) -> Unit) {
    SlgSectionCard(
        title = "원천동 상인연합의 야간 물자 협약 제안",
        badge = "연방 해체 D+14",
        accent = Color(0xFFB39DDB)
    ) {
        Text(
            "밤 11시, 원천동 상인연합의 사절이 매탄동 창고 앞에 도착했다. " +
                    "\"관세를 면제해 주면 야간 보급로로 곡물과 연료를 대겠습니다. " +
                    "대신 우리 화물에는 통행세를 매기지 마십시오.\"",
            color = SlgColors.Text.copy(alpha = 0.85f),
            fontSize = 11.sp,
            lineHeight = 17.sp
        )
        if (state.eventAChoice == null) {
            SlgActionTile(
                label = "관세 면제 · 상호 보급 협정 체결",
                hint = "자금 -600 · 식량 +1,200 · 민심 +8 · 외교 +12",
                accent = Color(0xFFB39DDB),
                onClick = { onChooseEventA(true) }
            )
            SlgActionTile(
                label = "통행세 징수 및 자체 비축",
                hint = "자금 +900 · 민심 -10 · 외교 -15",
                accent = SlgColors.Danger,
                onClick = { onChooseEventA(false) }
            )
        } else {
            ResultBox(state.eventAResult, Color(0xFFB39DDB))
        }
    }
}

/** 이벤트 B — 광교산 협곡에 정체불명 정찰대 출현 */
@Composable
fun EventScoutCard(state: SlgGameState, onChooseEventB: (Boolean) -> Unit) {
    SlgSectionCard(
        title = "광교산 협곡에 정체불명 정찰대 출현",
        badge = "산악 경보",
        accent = SlgColors.Danger
    ) {
        Text(
            "새벽 4시, 광교산 협곡 감시탑에서 무전이 들어온다. " +
                    "\"미확인 인원 30여 명, 군장 없이 곡괭이와 측량 장비를 들고 협곡을 오르고 있습니다. " +
                    "광교 호족연합의 깃발은 없습니다.\"",
            color = SlgColors.Text.copy(alpha = 0.85f),
            fontSize = 11.sp,
            lineHeight = 17.sp
        )
        if (state.eventBChoice == null) {
            SlgActionTile(
                label = "산악 매복 요격",
                hint = "병력 -80 · 자금 +250 (노획) · 명성 +6 · 광교 보복 위험",
                accent = SlgColors.Danger,
                onClick = { onChooseEventB(true) }
            )
            SlgActionTile(
                label = "첩자 파견으로 배후 파악",
                hint = "자금 -200 · 민심 +3 · 적 군량고 위치 확보",
                accent = SlgColors.Steel,
                onClick = { onChooseEventB(false) }
            )
        } else {
            ResultBox(state.eventBResult, SlgColors.Steel)
        }
    }
}

/** 「연방 붕괴의 그림자」 유민 유입 딜레마 */
@Composable
fun RefugeeCrisisCard(state: SlgGameState, onChooseRefugee: (String) -> Unit) {
    SlgSectionCard(
        title = "연방 붕괴의 그림자 — 광교·용인 유민 유입",
        badge = "긴급 민생 · 유민 800명",
        accent = SlgColors.Danger
    ) {
        Text(
            "연방 통신·물류망이 끊긴 세 번째 밤, 매탄동 성문 앞에 유민 800명이 몰려들었다. " +
                    "광교산 협곡에서 내려온 이들, 용인 수지에서 밀려난 이들이 담요를 둘렀다. " +
                    "아이를 안은 여성이 성문을 두드린다.\n" +
                    "「호족님, 하룻밤만이라도… 물 한 모금이면 됩니다.」",
            color = SlgColors.Text.copy(alpha = 0.86f),
            fontSize = 11.sp,
            lineHeight = 17.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("누적 인구", color = SlgColors.Muted, fontSize = 8.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("${state.population}", color = SlgColors.Text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("문화 명성", color = SlgColors.Muted, fontSize = 8.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("${state.culturePrestige}", color = SlgColors.Brass, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("식량", color = SlgColors.Muted, fontSize = 8.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("${state.food}석", color = SlgColors.Success, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("자금", color = SlgColors.Muted, fontSize = 8.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("${state.gold}G", color = SlgColors.Brass, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (state.refugeeChoice == null) {
            SlgActionTile(
                label = "A. 임시 거주지 개방 및 식량 배급",
                hint = "식량 -500 · 인구 +800 · 민심 +10 · 문화 명성 +8",
                accent = SlgColors.Success,
                enabled = state.food >= 500,
                onClick = { onChooseRefugee("REFUGEE_A_OPEN") }
            )
            SlgActionTile(
                label = "B. 성문 통제 및 선별 수용",
                hint = "자금 +200 (관세) · 민심 -12 · 외교 평판 -8",
                accent = SlgColors.Danger,
                onClick = { onChooseRefugee("REFUGEE_B_CONTROL") }
            )
            if (state.food < 500) {
                Text(
                    "식량이 500석 미만이라 개방 배급을 선택할 수 없습니다. (보유 ${state.food}석)",
                    color = SlgColors.Danger,
                    fontSize = 9.sp
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SlgPill("선택 완료", SlgColors.Success)
                Spacer(Modifier.width(6.dp))
                Text(
                    if (state.isRefugeeAccepted) "성문 개방 · 유민 수용" else "성문 통제 · 선별 수용",
                    color = SlgColors.Text,
                    fontSize = 10.sp
                )
            }
            ResultBox(state.refugeeResult, if (state.isRefugeeAccepted) SlgColors.Success else SlgColors.Danger)
            if (state.isRefugeeAccepted) {
                Text(
                    "유민들이 매탄동 골목에 자리를 잡고 장시를 열었다 — 문화 명성 ${state.culturePrestige} · 인구 ${state.population}명.",
                    color = SlgColors.Success,
                    fontSize = 9.sp
                )
            }
        }
    }
}

/**
 * 👑 성주 승격 마일스톤 로드맵
 * 읍·면·동 호족 → 과반 연합 성주 → 수원시 통합군주
 */
@Composable
fun PromotionRoadmapCard(
    state: SlgGameState,
    onPromote: () -> Unit
) {
    val stages = PromotionStage.entries.toList()
    val next = state.nextPromotionStage

    SlgSectionCard(
        title = "성주 승격 마일스톤",
        badge = state.promotionStage.displayName,
        accent = SlgColors.Brass
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            stages.forEachIndexed { index, stage ->
                val achieved = stage.order <= state.promotionStage.order
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (achieved) "●" else "○",
                        color = if (achieved) SlgColors.Brass else SlgColors.Line,
                        fontSize = 11.sp
                    )
                    Text(
                        stage.displayName,
                        color = if (achieved) SlgColors.Brass else SlgColors.Muted,
                        fontSize = 9.sp,
                        fontWeight = if (stage == state.promotionStage) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }
                if (index < stages.lastIndex) {
                    SlgStatBar(
                        ratio = if (stages[index + 1].order <= state.promotionStage.order) 1f else 0.001f,
                        color = SlgColors.Brass,
                        modifier = Modifier.width(10.dp),
                        height = 3
                    )
                }
            }
        }

        if (next != null) {
            val alliedRatio = if (next.requiredAlliedTerritories == 0) 1f
            else (state.alliedTerritories.toFloat() / next.requiredAlliedTerritories).coerceIn(0f, 1f)
            val prestigeRatio = if (next.requiredPrestige == 0) 1f
            else (state.prestige.toFloat() / next.requiredPrestige).coerceIn(0f, 1f)
            Text(
                "'${next.displayName}' 승격 조건",
                color = SlgColors.Text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("우호·동맹 영지", color = SlgColors.Muted, fontSize = 9.sp, modifier = Modifier.width(72.dp))
                Box(modifier = Modifier.weight(1f)) {
                    SlgStatBar(ratio = alliedRatio, color = SlgColors.Steel, height = 6)
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    "${state.alliedTerritories}/${next.requiredAlliedTerritories}",
                    color = SlgColors.Steel,
                    fontSize = 9.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("명성", color = SlgColors.Muted, fontSize = 9.sp, modifier = Modifier.width(72.dp))
                Box(modifier = Modifier.weight(1f)) {
                    SlgStatBar(ratio = prestigeRatio, color = SlgColors.Brass, height = 6)
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    "${state.prestige}/${next.requiredPrestige}",
                    color = SlgColors.Brass,
                    fontSize = 9.sp
                )
            }
            Text(
                "우호·동맹은 외교·첩보의 상호방위·속국 조약과 원천동 물자 협정으로 확보합니다. " +
                    "(현재 ${state.alliedTerritories}곳 — 원천동 협정 · 광교동 속국 · 용인 수지 동맹)",
                color = SlgColors.Muted,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
            SlgActionTile(
                label = "👑 ${next.displayName} 승격 의식 개최",
                hint = if (state.isPromotionEligible)
                    "조건 충족 — 의회 권한 +${next.councilAuthorityBonus}% 확장"
                else
                    "조건 미달 (영지 ${state.alliedTerritories}/${next.requiredAlliedTerritories} · 명성 ${state.prestige}/${next.requiredPrestige})",
                accent = SlgColors.Brass,
                enabled = state.isPromotionEligible,
                onClick = onPromote
            )
        } else {
            SlgPill("👑 수원시 통합군주 — 로드맵 완주", SlgColors.Brass, filled = true)
            Text(
                "영통·매탄·광교·수지가 하나의 행정 질서로 묶였습니다. 의회 권한 보너스 +${state.councilAuthorityBonus}%가 유지됩니다.",
                color = SlgColors.Brass,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
        }

        if (state.promotionMessage != null && state.promotionCeremony == null) {
            Text(state.promotionMessage, color = SlgColors.Muted, fontSize = 9.sp, lineHeight = 14.sp)
        }
    }
}

/** 승격 의식 배너 (지도·상태 패널에서 승격을 알린다) */
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
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                "👑 ${state.promotionStage.displayName} 승격",
                color = Color(0xFF1A1305),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                ceremony,
                color = Color(0xFF24190A),
                fontSize = 10.sp,
                lineHeight = 15.sp
            )
            SlgActionTile(
                label = "의회 권한 확장 보너스 +${state.councilAuthorityBonus}% · 배너 닫기",
                accent = Color(0xFF6D4C1A),
                onClick = onDismissCeremony
            )
        }
    }
}
