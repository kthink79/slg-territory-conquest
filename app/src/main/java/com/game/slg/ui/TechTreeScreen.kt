package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 4대 계통 기술 발전 트리 (집현전/학당) — 내정 · 과학기술 · 문화 · 군사.
 * 서브 메뉴 「테크트리」 화면과 호족 의회 화면이 공유한다.
 */
@Composable
fun TechTreeSection(
    state: SlgGameState,
    onResearch: (String) -> Unit,
    showChamberHeader: Boolean = true
) {
    if (showChamberHeader) {
        CouncilChamberHeader(
            title = "매탄동 집현전 · 학당",
            subtitle = "4대 계통 기술 발전 트리 — 선행 단계를 마쳐야 다음 단계가 열립니다"
        )
    }

    val branches = listOf(
        "내정" to Color(0xFF81C784),
        "과학기술" to Color(0xFF64B5F6),
        "문화" to Color(0xFFB39DDB),
        "군사" to Color(0xFFE57373)
    )
    SlgSectionCard(
        title = "4대 테크트리",
        badge = "완료 ${state.researchedTechs.size}/${SlgWorld.TECH_ITEMS.size}",
        accent = SlgColors.Brass
    ) {
        branches.forEach { (branch, color) ->
            val items = SlgWorld.TECH_ITEMS.filter { it.branch == branch }.sortedBy { it.stage }
            val done = items.count { state.researchedTechs.contains(it.id) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                SlgPill(branch, color, filled = done == items.size)
                Spacer(Modifier.width(6.dp))
                Text("$done/${items.size}단계", color = SlgColors.Muted, fontSize = 9.sp)
            }
            SlgStatBar(ratio = done.toFloat() / items.size, color = color, height = 5)
            items.forEach { tech ->
                val researched = state.researchedTechs.contains(tech.id)
                val prereq = items.firstOrNull { it.stage == tech.stage - 1 }
                val prereqDone = prereq == null || state.researchedTechs.contains(prereq.id)
                val affordable = state.gold >= tech.cost
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (researched) color.copy(alpha = 0.12f) else SlgColors.SurfaceAlt)
                        .padding(9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${tech.stage}단계 ${tech.name}",
                                color = if (researched) color else SlgColors.Text,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(5.dp))
                            SlgPill(
                                when {
                                    researched -> "완료"
                                    !prereqDone -> "선행 필요"
                                    !affordable -> "자금 부족"
                                    else -> "연구 가능"
                                },
                                when {
                                    researched -> SlgColors.Success
                                    !prereqDone -> SlgColors.Muted
                                    !affordable -> SlgColors.Danger
                                    else -> color
                                }
                            )
                        }
                        Text(tech.effect, color = SlgColors.Muted, fontSize = 8.sp)
                        Text("${tech.cost}G · ${tech.description}", color = SlgColors.Muted, fontSize = 8.sp)
                    }
                    SlgChip(
                        if (researched) "완료" else "연구 ${tech.cost}G",
                        selected = researched,
                        accent = color,
                        onClick = { if (!researched) onResearch(tech.id) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    SlgSectionCard(title = "기술 계통 설명", accent = SlgColors.Steel) {
        Text(
            "① 내정 — 수로 개간 → 장시 → 대동세 → 호패법 (자금·민심)\n" +
                    "② 과학기술 — 석조 축조 → 야금·제련 → 도로망 측량 → 화약 공성무기 (수성·보급)\n" +
                    "③ 문화 — 향약·두레 → 서원 → 가문 명망 → 점령지 동화 (민심·외교)\n" +
                    "④ 군사 — 창병 방진 → 복합노궁 → 중갑기병 → 공성 충차 (저손실 전투)",
            color = SlgColors.Muted,
            fontSize = 9.5.sp,
            lineHeight = 15.sp
        )
    }
}

/** 의회·학당 배경 헤더 */
@Composable
fun CouncilChamberHeader(title: String, subtitle: String) {
    val image = androidx.compose.ui.res.painterResource(com.game.slg.R.drawable.slg_council_chamber)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(14.dp))
    ) {
        androidx.compose.foundation.Image(
            painter = image,
            contentDescription = "호족 의회",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0x22000000), Color(0xE60C0E13))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, color = SlgColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SlgColors.Muted, fontSize = 9.sp)
        }
    }
}
