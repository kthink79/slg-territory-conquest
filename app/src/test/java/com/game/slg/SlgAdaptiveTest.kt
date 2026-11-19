package com.game.slg

import com.game.slg.ui.SlgLayout
import com.game.slg.ui.SlgWindowClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 디바이스 해상도 대응 계산 — 폰·폴더블·태블릿에서 콘텐츠 폭과 열 수가 자동으로 바뀐다.
 */
class SlgAdaptiveTest {

    @Test
    fun `화면 폭으로 세 단계를 나눈다`() {
        assertEquals(SlgWindowClass.COMPACT, SlgWindowClass.of(320f))
        assertEquals(SlgWindowClass.COMPACT, SlgWindowClass.of(599f))
        assertEquals(SlgWindowClass.MEDIUM, SlgWindowClass.of(600f))
        assertEquals(SlgWindowClass.EXPANDED, SlgWindowClass.of(840f))
        assertEquals(SlgWindowClass.EXPANDED, SlgWindowClass.of(1280f))
    }

    @Test
    fun `태블릿에서도 콘텐츠 폭을 720dp로 묶어 가로로 늘어나지 않는다`() {
        assertEquals(360f, SlgLayout.contentWidth(360f))
        assertEquals(720f, SlgLayout.contentWidth(720f))
        assertEquals(720f, SlgLayout.contentWidth(1280f))
        assertEquals(720f, SlgLayout.contentWidth(2000f))
    }

    @Test
    fun `여백과 영웅 그리드 열 수가 화면 폭에 맞춰 바뀐다`() {
        assertEquals(12, SlgLayout.horizontalPadding(400f))
        assertEquals(24, SlgLayout.horizontalPadding(900f))
        assertEquals(1, SlgLayout.heroColumns(400f))
        assertEquals(2, SlgLayout.heroColumns(700f))
        assertEquals(2, SlgLayout.heroColumns(1000f))
    }

    @Test
    fun `영웅 초상화는 어떤 폭에서도 2대3 비율을 지킨다`() {
        listOf(40, 78, 96, 120).forEach { width ->
            assertEquals(width * 3 / 2, SlgLayout.portraitHeight(width))
            assertEquals(
                width.toDouble() / SlgLayout.portraitHeight(width),
                2.0 / 3.0,
                0.001
            )
        }
    }

    @Test
    fun `좁은 화면에서는 HUD를 두 줄로 나눈다`() {
        assertFalse(SlgLayout.hudSingleRow(320f))
        assertTrue(SlgLayout.hudSingleRow(400f))
    }
}
