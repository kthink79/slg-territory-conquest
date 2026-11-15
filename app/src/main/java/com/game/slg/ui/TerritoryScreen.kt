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
 * 서브 메뉴 「영지·내정」 상세 화면 — 본진 일러스트 · 자치구 통치 지표 · 인접 행정구역 · 절기 순환.
 * (승격 로드맵·시나리오는 각자의 서브 메뉴에서 열린다)
 */
@Composable
fun TerritoryScreen(
    state: SlgGameState,
    onAdvanceSeason: () -> Unit,
    onDismissCeremony: () -> Unit
) {
    SlgScreenFrame {
        // ── 👑 승격 의식 배너 ──
        PromotionCeremonyBanner(state = state, onDismissCeremony = onDismissCeremony)

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
