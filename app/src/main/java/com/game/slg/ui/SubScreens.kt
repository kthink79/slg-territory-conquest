package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 서브 메뉴 상세 화면 공통 틀 — 상단에 「← 메인 3단 화면」 복귀 버튼을 항상 크게 둔다.
 */
@Composable
fun SubScreenHost(
    menu: SubMenu,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlgColors.Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlgColors.Surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SlgColors.Brass.copy(alpha = 0.16f))
                    .border(1.dp, SlgColors.Brass.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .clickable { onBack() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "메인 3단 화면으로",
                    tint = SlgColors.Brass,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    "메인 3단 화면",
                    color = SlgColors.Brass,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.weight(1f))
            Text(menu.label, color = SlgColors.Muted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }
}

/** 서브 메뉴 「테크트리」 — 4대 계통 연구 */
@Composable
fun TechTreeScreen(
    state: SlgGameState,
    onResearch: (String) -> Unit
) {
    SlgScreenFrame {
        TechTreeSection(state = state, onResearch = onResearch)
    }
}

/** 서브 메뉴 「이벤트」 — 시나리오 선택 */
@Composable
fun EventScenarioScreen(
    state: SlgGameState,
    onChooseEventA: (Boolean) -> Unit,
    onChooseEventB: (Boolean) -> Unit,
    onChooseRefugee: (String) -> Unit
) {
    SlgScreenFrame {
        SlgSectionCard(title = "진행 중 시나리오", badge = "D+14", accent = SlgColors.Brass) {
            Text(
                "연방 해체 14일차 — 매탄동 본진에 세 갈래 사건이 걸려 있습니다. " +
                    "선택은 되돌릴 수 없으니 자원과 명성을 확인하고 결정하세요.",
                color = SlgColors.Muted,
                fontSize = 9.5.sp,
                lineHeight = 15.sp
            )
        }
        RefugeeCrisisCard(state = state, onChooseRefugee = onChooseRefugee)
        EventMerchantCard(state = state, onChooseEventA = onChooseEventA)
        EventScoutCard(state = state, onChooseEventB = onChooseEventB)
        SlgSectionCard(title = "사건 기록", accent = SlgColors.Muted) {
            SlgLogBox(state.log, maxLines = 6)
        }
    }
}

/** 서브 메뉴 「성주 로드맵」 — 호족 → 성주 → 통합군주 */
@Composable
fun PromotionScreen(
    state: SlgGameState,
    onPromote: () -> Unit,
    onDismissCeremony: () -> Unit
) {
    SlgScreenFrame {
        PromotionCeremonyBanner(state = state, onDismissCeremony = onDismissCeremony)
        PromotionRoadmapCard(state = state, onPromote = onPromote)
        SlgSectionCard(title = "승격 경로", accent = SlgColors.Steel) {
            Text(
                "① 읍·면·동 호족 — 매탄동 본진에서 시작 (현재 단계)\n" +
                    "② 과반 연합 성주 — 우호·동맹 영지 2곳 + 명성 80\n" +
                    "③ 수원시 통합군주 — 우호·동맹 영지 3곳 + 명성 95\n\n" +
                    "우호·동맹은 ① 원천동 야간 물자 협정 ② 광교동 속국 조공 조약 " +
                    "③ 용인 수지 상호방위 조약으로 확보합니다. 조약은 외교·첩보 서브 메뉴에서 체결하세요.",
                color = SlgColors.Muted,
                fontSize = 9.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}
