package com.game.slg.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.R

/**
 * 인트로 장면 1개 — 현대 한국 배경 4장면.
 * TCG 《듀얼 게이트》 도입부와 같은 방식(터치로 다음, SKIP)으로 진행한다.
 */
data class IntroScene(
    @DrawableRes val image: Int,
    val kicker: String,
    val title: String,
    val narration: String,
    val tag: String
)

object SlgIntro {
    val SCENES: List<IntroScene> = listOf(
        IntroScene(
            image = R.drawable.slg_intro_01,
            kicker = "2026년 겨울 · D-0",
            title = "연방의 전원이 꺼졌다",
            narration = "예산도, 통신도 끊긴 밤. 신호등이 멈춘 도로 위로 사람들이 걸어 나왔다.\n" +
                "중앙은 더 이상 아무것도 보내지 않았다.",
            tag = "연방 붕괴"
        ),
        IntroScene(
            image = R.drawable.slg_intro_02,
            kicker = "D+3",
            title = "깃발이 먼저 올라갔다",
            narration = "시청과 구청이 스스로 통치를 선언했다. 남은 것은 인구와 조직,\n" +
                "그리고 서로를 견제하는 수십 명의 호족이었다.",
            tag = "자치구 독립"
        ),
        IntroScene(
            image = R.drawable.slg_intro_03,
            kicker = "D+9",
            title = "평화는 장비로 지켜진다",
            narration = "K2 전차와 장갑차, 정찰 드론과 방공 레이더가 거리로 나왔다.\n" +
                "이제 협상은 병력과 보급으로 결정된다.",
            tag = "현대 편제"
        ),
        IntroScene(
            image = R.drawable.slg_intro_04,
            kicker = "D+14",
            title = "당신의 구역이 시작된다",
            narration = "지휘통제실의 지도 위에 수십 개의 호족 거점이 떠오른다.\n" +
                "하나를 인계받아, 인구가 허락하는 만큼의 세력을 키워라.",
            tag = "호족 인계"
        )
    )

    const val GUIDE = "화면을 누르면 다음 장면 · 오른쪽 위 SKIP으로 건너뛰기"
}

/**
 * 인트로 화면 — 일러스트 전체 배경 + 하단 내레이션 + 진행 점.
 */
@Composable
fun SlgIntroScreen(
    sceneIndex: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scenes = SlgIntro.SCENES
    val index = sceneIndex.coerceIn(0, scenes.lastIndex)
    val scene = scenes[index]
    val isLast = index == scenes.lastIndex

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlgColors.Background)
            .clickable { if (isLast) onSkip() else onNext() }
    ) {
        Image(
            painter = painterResource(scene.image),
            contentDescription = scene.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x33000000),
                        0.45f to Color(0x66000000),
                        1f to Color(0xF20C0E13)
                    )
                )
        )

        // 상단 — 장면 표식 + SKIP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlgPill("D+${index * 4} · ${scene.tag}", SlgColors.Brass)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xCC0C0E13))
                    .border(1.dp, SlgColors.Brass.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .clickable { onSkip() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text("SKIP ▸▸", color = SlgColors.Brass, fontSize = SlgType.LABEL, fontWeight = FontWeight.ExtraBold)
            }
        }

        // 하단 — 내레이션
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                scene.kicker,
                color = SlgColors.Brass,
                fontSize = SlgType.LABEL,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                scene.title,
                color = SlgColors.Text,
                fontSize = 27.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                scene.narration,
                color = SlgColors.Text.copy(alpha = 0.88f),
                fontSize = SlgType.BODY,
                lineHeight = 21.sp
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                scenes.forEachIndexed { i, _ ->
                    Box(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .size(width = if (i == index) 20.dp else 7.dp, height = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (i == index) SlgColors.Brass else SlgColors.Line)
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SlgColors.Brass)
                        .clickable { if (isLast) onSkip() else onNext() }
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        if (isLast) "지역 선택 ▸" else "다음 ▸",
                        color = Color(0xFF10131A),
                        fontSize = SlgType.BODY_SMALL,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                SlgIntro.GUIDE,
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                textAlign = TextAlign.Start
            )
        }
    }
}
