package com.game.slg.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.R

/**
 * 탭 1 — 🗺️ 영지·지도 (홈)
 */
@Composable
fun TerritoryScreen(
    state: SlgGameState,
    onAdvanceSeason: () -> Unit,
    onChooseEventA: (Boolean) -> Unit,
    onChooseEventB: (Boolean) -> Unit
) {
    SlgScreenFrame {
        // ── 자원 바 ──
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            SlgResourceChip("👥", "인구", "${state.population}", SlgColors.Text, Modifier.weight(1f))
            SlgResourceChip("💰", "자금", "${state.gold}", SlgColors.Brass, Modifier.weight(1f))
            SlgResourceChip("🌾", "식량", "${state.food}", SlgColors.Success, Modifier.weight(1f))
            SlgResourceChip("⚔", "병력", "${state.troops}", SlgColors.Steel, Modifier.weight(1f))
        }

        // ── 본진 도시 일러스트 헤더 ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.slg_city_maetan),
                contentDescription = "매탄동 본진",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x33000000), Color(0xE60C0E13))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SlgPill("본진", SlgColors.Brass, filled = true)
                    Spacer(Modifier.width(6.dp))
                    SlgPill(state.seasonName, SlgColors.Steel)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "경기도 수원시 영통구 매탄동",
                    color = SlgColors.Text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "정보통신 허브 · 산성 요새 · 호족 5석",
                    color = SlgColors.Muted,
                    fontSize = 10.sp
                )
            }
            // 지휘관 포트레이트
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("지휘관", color = SlgColors.Muted, fontSize = 8.sp)
                    Text("이광수 호족", color = SlgColors.Brass, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("성주", color = SlgColors.Steel, fontSize = 8.sp)
                }
                Spacer(Modifier.width(6.dp))
                Image(
                    painter = painterResource(R.drawable.slg_commander_hero),
                    contentDescription = "지휘관",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .border(1.dp, SlgColors.Brass, CircleShape)
                )
            }
        }

        // ── 민심·명성·외교 ──
        SlgSectionCard(title = "자치구 통치 지표", accent = SlgColors.Steel) {
            MetricRow("민심", state.morale, SlgColors.Success)
            MetricRow("명성", state.prestige, SlgColors.Brass)
            MetricRow("외교", state.diplomacy, SlgColors.Steel)
        }

        // ── 인접 행정구역 ──
        SlgSectionCard(title = "인접 행정구역", badge = "읍·면·동 단위", accent = SlgColors.Brass) {
            SlgWorld.TERRITORIES.forEach { territory ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                territory.name,
                                color = SlgColors.Text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(6.dp))
                            SlgPill(territory.relation.label, relationColor(territory.relation.label))
                        }
                        Text(
                            "${territory.region} · 인구 ${territory.population} · 병력 ${territory.troops}",
                            color = SlgColors.Muted,
                            fontSize = 9.sp
                        )
                        Text(territory.note, color = SlgColors.Muted, fontSize = 8.sp)
                    }
                    Text(
                        "${territory.terrain.name}",
                        color = SlgColors.Steel,
                        fontSize = 8.sp
                    )
                }
            }
        }

        // ── 샘플 이벤트 A ──
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

        // ── 샘플 이벤트 B ──
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

        // ── 절기 순환 ──
        SlgSectionCard(title = "절기 순환", badge = state.seasonName, accent = SlgColors.Success) {
            Text(
                "봄 조세 → 여름 치수·풍수해 → 가을 대추수 → 겨울 혹한기 유지비 순으로 4계절이 돕니다. " +
                        "여름에는 치수 사업을 반드시 마쳐야 홍수 피해를 막을 수 있습니다.",
                color = SlgColors.Muted,
                fontSize = 9.sp,
                lineHeight = 14.sp
            )
            SlgActionTile(
                label = "다음 절기 진행 (치수 완비)",
                hint = "조세·추수 수입, 유지비 정산",
                accent = SlgColors.Success,
                onClick = onAdvanceSeason
            )
            SlgLogBox(state.log, maxLines = 5)
        }
    }
}

@Composable
fun MetricRow(label: String, value: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = SlgColors.Muted, fontSize = 10.sp, modifier = Modifier.width(32.dp))
        Box(modifier = Modifier.weight(1f)) {
            SlgStatBar(ratio = value / 100f, color = color, height = 7)
        }
        Spacer(Modifier.width(6.dp))
        Text("$value", color = color, fontSize = 10.sp)
    }
}

@Composable
fun ResultBox(text: String?, accent: Color) {
    if (text == null) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(10.dp)
    ) {
        Text(text, color = SlgColors.Text.copy(alpha = 0.9f), fontSize = 10.sp, lineHeight = 15.sp)
    }
}
