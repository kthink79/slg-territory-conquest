package com.game.slg

import com.game.slg.core.map.MapNode
import com.game.slg.core.map.MapViewport
import com.game.slg.core.map.TerritoryMapLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 메인 3단 화면 상단 1/3 — 제한 범위 지도 · 확대/축소 · 이동 경계 · 영지 선택 테스트.
 */
class TerritoryMapViewTest {

    private val width = 1080f
    private val height = 600f

    @Test
    fun `기본 지도는 본진 중심 배율 1배로 시작한다`() {
        val viewport = MapViewport()

        assertEquals(TerritoryMapLayout.DEFAULT_SCALE, viewport.scale, 0.0001f)
        assertEquals(0f, viewport.centerX, 0.0001f)
        assertEquals(0f, viewport.centerY, 0.0001f)
        assertEquals(TerritoryMapLayout.BASE_VIEW_RADIUS, viewport.viewHalfExtent, 0.0001f)
    }

    @Test
    fun `확대는 최대 2_5배에서 멈춘다`() {
        var viewport = MapViewport()
        repeat(12) { viewport = viewport.zoomIn() }

        assertEquals(TerritoryMapLayout.MAX_SCALE, viewport.scale, 0.0001f)
    }

    @Test
    fun `축소는 최소 0_7배에서 멈춘다`() {
        var viewport = MapViewport()
        repeat(12) { viewport = viewport.zoomOut() }

        assertEquals(TerritoryMapLayout.MIN_SCALE, viewport.scale, 0.0001f)
    }

    @Test
    fun `핀치 배율도 같은 한계로 제한된다`() {
        assertEquals(
            TerritoryMapLayout.MAX_SCALE,
            MapViewport().zoomedBy(50f).scale,
            0.0001f
        )
        assertEquals(
            TerritoryMapLayout.MIN_SCALE,
            MapViewport().zoomedBy(0.001f).scale,
            0.0001f
        )
        // 잘못된 입력은 상태를 흔들지 않는다
        assertEquals(MapViewport(), MapViewport().zoomedBy(0f))
        assertEquals(MapViewport(), MapViewport().zoomedBy(Float.NaN))
    }

    @Test
    fun `확대하면 보이는 세계 범위가 좁아진다`() {
        val wide = MapViewport()
        val zoomed = wide.zoomIn()

        assertTrue(zoomed.viewHalfExtent < wide.viewHalfExtent)
        assertTrue(zoomed.visibleBounds().width < wide.visibleBounds().width)
        // 확대해도 본진(0,0)은 화면 안에 남는다
        assertTrue(zoomed.isVisible(0f, 0f))
    }

    @Test
    fun `이동은 표시 가능한 세계 경계를 넘지 않는다`() {
        val limit = MapViewport().panLimit
        assertEquals(
            TerritoryMapLayout.WORLD_HALF_EXTENT - TerritoryMapLayout.BASE_VIEW_RADIUS,
            limit,
            0.0001f
        )

        val panned = MapViewport().pannedBy(5f, -5f)
        assertEquals(limit, panned.centerX, 0.0001f)
        assertEquals(-limit, panned.centerY, 0.0001f)

        val zoomedPan = MapViewport().zoomedBy(2.5f).pannedBy(9f, 9f)
        assertEquals(zoomedPan.panLimit, zoomedPan.centerX, 0.0001f)
        assertTrue("확대 시 이동 여지가 더 넓다", zoomedPan.panLimit > limit)
    }

    @Test
    fun `최소 배율에서는 이동이 본진에 고정된다`() {
        val viewport = MapViewport(scale = TerritoryMapLayout.MIN_SCALE).pannedBy(2f, 2f)

        assertEquals(0f, viewport.panLimit, 0.0001f)
        assertEquals(0f, viewport.centerX, 0.0001f)
        assertEquals(0f, viewport.centerY, 0.0001f)
    }

    @Test
    fun `내 영지 중심 버튼은 배율과 중심을 초기화한다`() {
        val moved = MapViewport().zoomIn().pannedBy(0.3f, 0.3f)
        val reset = moved.reset()

        assertEquals(TerritoryMapLayout.DEFAULT_SCALE, reset.scale, 0.0001f)
        assertEquals(0f, reset.centerX, 0.0001f)
        assertEquals(0f, reset.centerY, 0.0001f)
    }

    @Test
    fun `표시 범위는 본진 기준 hop 거리로 제한된다`() {
        val local = TerritoryMapLayout.localNodes()
        assertEquals(4, local.size)
        assertTrue(local.all { it.hopDistance <= TerritoryMapLayout.LOCAL_HOP_LIMIT })
        assertTrue(local.any { it.isHome })

        val outside = TerritoryMapLayout.outOfRangeNodes()
        assertEquals(4, outside.size)
        assertEquals(4, TerritoryMapLayout.outOfRangeCount())
        assertTrue(outside.none { TerritoryMapLayout.isInRange(it) })

        // hop 2 는 범위 안, hop 3 은 범위 밖
        assertTrue(TerritoryMapLayout.isInRange(local.first { it.name == "용인 수지" }))
        assertFalse(TerritoryMapLayout.isInRange(outside.first()))
    }

    @Test
    fun `확대하면 화면 밖으로 나간 영지는 그리지 않는다`() {
        val all = MapViewport().visibleNodes()
        assertEquals(4, all.size)

        val zoomed = MapViewport().zoomedBy(2.5f).visibleNodes()
        assertEquals("본진만 남는다", 1, zoomed.size)
        assertTrue(zoomed.single().isHome)

        // 이동해서 다시 잡으면 다른 영지가 보인다
        val panned = MapViewport()
            .zoomedBy(2.5f)
            .pannedBy(0.62f, -0.34f)
            .visibleNodes()
        assertTrue(panned.any { it.name == "원천동" })
    }

    @Test
    fun `본진은 화면 중앙에 그려진다`() {
        val point = MapViewport().screenAt(0f, 0f, width, height)

        assertEquals(width / 2f, point.x, 0.01f)
        assertEquals(height / 2f, point.y, 0.01f)
    }

    @Test
    fun `화면 좌표와 세계 좌표 변환이 서로 맞는다`() {
        val viewport = MapViewport().zoomedBy(1.6f).pannedBy(0.2f, -0.1f)
        val node = TerritoryMapLayout.localNodes().first { it.name == "광교동" }

        val screen = viewport.screenAt(node.worldX, node.worldY, width, height)
        val (wx, wy) = viewport.worldAt(screen.x, screen.y, width, height)

        assertEquals(node.worldX, wx, 0.001f)
        assertEquals(node.worldY, wy, 0.001f)
    }

    @Test
    fun `지도 탭으로 영지를 선택한다`() {
        val viewport = MapViewport()
        val woncheon = TerritoryMapLayout.localNodes().first { it.name == "원천동" }
        val point = viewport.screenAt(woncheon.worldX, woncheon.worldY, width, height)

        val hit = viewport.hitTest(point.x, point.y, width, height)

        assertNotNull(hit)
        assertEquals("원천동", hit?.name)
    }

    @Test
    fun `영지가 없는 곳을 탭하면 선택되지 않는다`() {
        val viewport = MapViewport()

        assertNull(viewport.hitTest(width / 2f, height - 10f, width, height))
    }

    @Test
    fun `확대해서 화면 밖으로 나간 영지는 탭으로 선택할 수 없다`() {
        val viewport = MapViewport().zoomedBy(2.5f)
        val woncheon = TerritoryMapLayout.localNodes().first { it.name == "원천동" }
        val point = viewport.screenAt(woncheon.worldX, woncheon.worldY, width, height)

        assertNull(viewport.hitTest(point.x, point.y, width, height))
    }

    @Test
    fun `가장 가까운 영지 하나만 선택된다`() {
        val viewport = MapViewport()
        val home = TerritoryMapLayout.localNodes().first { it.isHome }
        // 본진에서 살짝 벗어난 지점도 본진으로 잡힌다
        val hit: MapNode? = viewport.hitTest(
            width / 2f + 10f,
            height / 2f - 8f,
            width,
            height
        )

        assertEquals(home.code, hit?.code)
    }
}
