package com.game.slg.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 디바이스 해상도 대응 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-19): *"폰·태블릿·폴더블 등 디바이스 해상도에 맞게 자동 조정하며,
 * 태블릿 등 넓은 화면에서도 세로 비율이 깨지거나 가로로 과도하게 늘어나지 않도록
 * 최대 콘텐츠 너비 제한·중앙 정렬, 영웅 초상화 2:3 비율 고정, 지도/HUD/그리드 열 수 자동 보정"*
 *
 * 화면 폭만 보고 세 단계로 나눈다. 안드로이드 의존성 없이 계산부만 JVM에서 검증한다.
 */
enum class SlgWindowClass(val label: String) {
    COMPACT("폰"),
    MEDIUM("폴더블·소형 태블릿"),
    EXPANDED("태블릿·가로");

    companion object {
        /** 폰 상한 */
        const val COMPACT_MAX = 600f

        /** 태블릿 시작 */
        const val EXPANDED_MIN = 840f

        fun of(widthDp: Float): SlgWindowClass = when {
            widthDp < COMPACT_MAX -> COMPACT
            widthDp < EXPANDED_MIN -> MEDIUM
            else -> EXPANDED
        }
    }
}

object SlgLayout {

    /** 태블릿에서도 이 폭을 넘기지 않고 가운데 정렬한다 */
    const val MAX_CONTENT_WIDTH = 720

    fun windowClassOf(widthDp: Float): SlgWindowClass = SlgWindowClass.of(widthDp)

    /** 콘텐츠가 실제로 쓸 폭 — 넓은 화면에서는 최대 폭으로 묶는다 */
    fun contentWidth(widthDp: Float): Float = widthDp.coerceAtMost(MAX_CONTENT_WIDTH.toFloat())

    /** 좌우 여백 — 폰 12dp · 넓은 화면 24dp */
    fun horizontalPadding(widthDp: Float): Int =
        if (widthDp >= SlgWindowClass.EXPANDED_MIN) 24 else 12

    /** 영웅 카드 그리드 열 수 — 폰 1열, 태블릿 2열 */
    fun heroColumns(widthDp: Float): Int = when (SlgWindowClass.of(widthDp)) {
        SlgWindowClass.COMPACT -> 1
        SlgWindowClass.MEDIUM -> 2
        SlgWindowClass.EXPANDED -> 2
    }

    /** 지도 HUD 칩을 한 줄에 놓을 수 있는지 */
    fun hudSingleRow(widthDp: Float): Boolean = widthDp >= 360f

    /** 2:3 초상화 높이 — 폭이 커져도 비율이 깨지지 않는다 */
    fun portraitHeight(widthDp: Int): Int = widthDp * 3 / 2

    /** 초상화 최대 폭 — 태블릿에서 지나치게 커지지 않게 한다 */
    fun portraitWidth(base: Int, widthDp: Float): Int =
        if (SlgWindowClass.of(widthDp) == SlgWindowClass.EXPANDED) (base * 1.15f).toInt() else base
}

/**
 * 모든 상세 화면의 공통 틀.
 * 넓은 화면에서 내용을 [SlgLayout.MAX_CONTENT_WIDTH]로 묶어 가운데 놓는다.
 */
@Composable
fun SlgAdaptiveFrame(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val horizontal = SlgLayout.horizontalPadding(maxWidth.value)
        Column(
            modifier = Modifier
                .widthIn(max = SlgLayout.MAX_CONTENT_WIDTH.dp)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = horizontal.dp, vertical = verticalPadding),
            content = content
        )
    }
}

/** 화면 폭을 컴포저블 안에서 읽어 쓰는 도우미 */
@Composable
fun SlgWithWidth(content: @Composable (Float) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        content(maxWidth.value)
    }
}

/** 넓은 화면 안내 — 태블릿에서 세로 비율을 유지한다는 것을 한 줄로 알린다 */
@Composable
fun SlgWidthBadge(widthDp: Float) {
    val window = SlgLayout.windowClassOf(widthDp)
    if (window == SlgWindowClass.EXPANDED) {
        Text(
            "태블릿 · 콘텐츠 ${SlgLayout.MAX_CONTENT_WIDTH}dp 중앙 정렬 · 2:3 초상화 유지",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
    }
}
