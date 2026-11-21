package com.game.slg

import com.game.slg.core.map.MapNode
import com.game.slg.core.map.MapViewport
import com.game.slg.core.map.TerritoryMapLayout
import com.game.slg.core.region.RegionCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 메인 화면 지도 — 제한 범위 · 단계별 안개 · 확대/축소 · 이동 경계 · 거점 선택 테스트.
 * 기준 지역은 사용자가 예로 든 충남 아산시 탕정면이다.
 */
class TerritoryMapViewTest {

    private val width = 1080f
    private val height = 600f
    private val tangjeong = RegionCatalog.defaultRegion()

    private fun nodesAt(level: Int): List<MapNode> =
        TerritoryMapLayout.build(
            start = tangjeong,
            unlockedHop = com.game.slg.core.region.ExpansionTier.unlockedHop(level),
            playerName = "이광수",
            homeTroops = 1200
        )

    @Test
    fun `기본 지도는 본진 중심 배율 1_15배로 시작한다`() {
        val viewport = MapViewport()

        assertEquals(TerritoryMapLayout.DEFAULT_SCALE, viewport.scale, 0.0001f)
        assertEquals(1.15f, viewport.scale, 0.0001f)
        assertEquals(0f, viewport.centerX, 0.0001f)
        assertEquals(0f, viewport.centerY, 0.0001f)
        assertEquals(
            TerritoryMapLayout.BASE_VIEW_RADIUS / TerritoryMapLayout.DEFAULT_SCALE,
            viewport.viewHalfExtent,
            0.0001f
        )
    }

    @Test
    fun `기본 배율에서는 가까운 영지 세 고리가 한 화면에 들어온다`() {
        val viewport = MapViewport()

        listOf(1, 2, 3).forEach { hop ->
            val radius = TerritoryMapLayout.ringRadius(hop)
            assertTrue("hop $hop 영지가 기본 화면 안에 있어야 한다", radius < viewport.viewHalfExtent)
        }
    }

    @Test
    fun `확대는 최대 4배에서 멈춘다`() {
        var viewport = MapViewport()
        repeat(12) { viewport = viewport.zoomIn() }

        assertEquals(4f, TerritoryMapLayout.MAX_SCALE, 0.0001f)
        assertEquals(TerritoryMapLayout.MAX_SCALE, viewport.scale, 0.0001f)
    }

    @Test
    fun `축소는 최소 0_4배에서 멈춘다`() {
        var viewport = MapViewport()
        repeat(12) { viewport = viewport.zoomOut() }

        assertEquals(0.4f, TerritoryMapLayout.MIN_SCALE, 0.0001f)
        assertEquals(TerritoryMapLayout.MIN_SCALE, viewport.scale, 0.0001f)
    }

    @Test
    fun `플러스 마이너스 버튼은 네 번 안에 최대 배율까지 간다`() {
        var viewport = MapViewport()
        var steps = 1
        while (viewport.scale < TerritoryMapLayout.MAX_SCALE && steps <= 6) {
            viewport = viewport.zoomIn()
            steps++
        }
        assertTrue("확대 단계가 6번을 넘지 않는다", steps <= 6)
        assertEquals(TerritoryMapLayout.MAX_SCALE, viewport.scale, 0.0001f)

        var out = MapViewport()
        var outSteps = 1
        while (out.scale > TerritoryMapLayout.MIN_SCALE && outSteps <= 6) {
            out = out.zoomOut()
            outSteps++
        }
        assertTrue("축소 단계가 6번을 넘지 않는다", outSteps <= 6)
        assertEquals(TerritoryMapLayout.MIN_SCALE, out.scale, 0.0001f)
    }

    @Test
    fun `영지 간 간격은 이전 배치보다 25~35퍼센트 좁아졌다`() {
        // 이전 값: hop1 0.52 · hop2 0.80 · hop3 1.04
        listOf(1 to 0.52f, 2 to 0.80f, 3 to 1.04f, 4 to 1.26f).forEach { (hop, before) ->
            val after = TerritoryMapLayout.ringRadius(hop)
            assertTrue("hop $hop 간격이 25% 이상 줄어야 한다", after <= before * 0.75f)
            assertTrue("hop $hop 간격이 35% 넘게 줄면 안 된다", after >= before * 0.65f)
        }
        assertTrue(
            "가장 바깥 고리도 지도 경계 안에 들어와야 한다",
            TerritoryMapLayout.ringRadius(6) < TerritoryMapLayout.WORLD_HALF_EXTENT
        )
    }

    @Test
    fun `영지 노드와 라벨은 기본 배율에서 충분히 크고 축소해도 점처럼 작아지지 않는다`() {
        val default = TerritoryMapLayout.nodeRadiusDp(TerritoryMapLayout.DEFAULT_SCALE)
        assertEquals(13f, default, 0.001f)
        assertTrue("지름 26dp 이상", default * 2f >= 26f)

        val zoomed = TerritoryMapLayout.nodeRadiusDp(TerritoryMapLayout.MAX_SCALE)
        assertTrue("확대하면 노드도 커진다", zoomed > default)

        val zoomedOut = TerritoryMapLayout.nodeRadiusDp(TerritoryMapLayout.MIN_SCALE)
        assertTrue("최소 배율에서도 9dp 아래로 내려가지 않는다", zoomedOut >= 9f)
    }

    @Test
    fun `핀치 배율도 같은 한계로 제한된다`() {
        assertEquals(TerritoryMapLayout.MAX_SCALE, MapViewport().zoomedBy(50f).scale, 0.0001f)
        assertEquals(TerritoryMapLayout.MIN_SCALE, MapViewport().zoomedBy(0.001f).scale, 0.0001f)
        assertEquals(MapViewport(), MapViewport().zoomedBy(0f))
        assertEquals(MapViewport(), MapViewport().zoomedBy(Float.NaN))
    }

    @Test
    fun `확대하면 보이는 세계 범위가 좁아진다`() {
        val wide = MapViewport()
        val zoomed = wide.zoomIn()

        assertTrue(zoomed.viewHalfExtent < wide.viewHalfExtent)
        assertTrue(zoomed.visibleBounds().width < wide.visibleBounds().width)
        assertTrue(zoomed.isVisible(0f, 0f))
    }

    @Test
    fun `이동은 표시 가능한 세계 경계를 넘지 않는다`() {
        val limit = MapViewport().panLimit
        assertEquals(
            TerritoryMapLayout.WORLD_HALF_EXTENT -
                TerritoryMapLayout.BASE_VIEW_RADIUS / TerritoryMapLayout.DEFAULT_SCALE,
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
    fun `최소 배율에서는 이동 여지가 거의 없다`() {
        val viewport = MapViewport(scale = TerritoryMapLayout.MIN_SCALE).pannedBy(2f, 2f)

        assertTrue("최소 배율 이동 여지는 0.25 이하", viewport.panLimit <= 0.25f)
        assertEquals(viewport.panLimit, viewport.centerX, 0.0001f)
        assertEquals(viewport.panLimit, viewport.centerY, 0.0001f)
    }

    @Test
    fun `내 구역 중심 버튼은 배율과 중심을 초기화한다`() {
        val moved = MapViewport().zoomIn().pannedBy(0.3f, 0.3f)
        val reset = moved.reset()

        assertEquals(TerritoryMapLayout.DEFAULT_SCALE, reset.scale, 0.0001f)
        assertEquals(0f, reset.centerX, 0.0001f)
        assertEquals(0f, reset.centerY, 0.0001f)
    }

    @Test
    fun `Lv1 지도는 내 읍면동과 다음 단계 잠긴 거점만 보여 준다`() {
        val nodes = nodesAt(1)

        val home = nodes.single { it.isHome }
        assertEquals("탕정면", home.name)
        assertTrue(home.unlocked)

        // hop 1 (배방읍·음봉면·염치읍)은 미리 보이지만 잠겨 있다
        val adjacent = nodes.filter { it.hopDistance == 1 }
        assertEquals(3, adjacent.size)
        assertTrue(adjacent.none { it.unlocked })
        assertEquals(3, TerritoryMapLayout.lockedCount(nodes))
        assertEquals(1, TerritoryMapLayout.unlockedCount(nodes))

        // hop 2 는 아직 그리지 않는다
        assertTrue(nodes.none { it.hopDistance >= 2 })
    }

    @Test
    fun `Lv2 지도는 인접 읍면동을 해금하고 Lv3은 연접 구역을 연다`() {
        val level2 = nodesAt(2)
        val adjacent = level2.filter { it.hopDistance == 1 }
        assertEquals(3, adjacent.size)
        assertTrue("인접 구역이 해금되어야 한다", adjacent.all { it.unlocked })
        assertEquals(setOf("배방읍", "음봉면", "염치읍"), adjacent.map { it.name }.toSet())

        // hop 2 는 다음 단계 미리보기로 잠겨 있다
        val nextRing = level2.filter { it.hopDistance == 2 }
        assertTrue(nextRing.isNotEmpty())
        assertTrue(nextRing.none { it.unlocked })

        val level3 = nodesAt(3)
        assertTrue(level3.filter { it.hopDistance == 2 }.all { it.unlocked })
    }

    @Test
    fun `지휘 레벨이 오를수록 지도에 그리는 범위가 넓어진다`() {
        val level1 = nodesAt(1).size
        val level2 = nodesAt(2).size
        val level4 = nodesAt(4).size

        assertTrue("Lv2가 Lv1보다 넓다", level2 > level1)
        assertTrue("Lv4가 Lv2보다 넓다", level4 > level2)
        assertTrue(
            "해금 hop 이 커지면 범위 밖 거점 수가 줄어든다",
            TerritoryMapLayout.outOfRangeCount(tangjeong, 4) <=
                TerritoryMapLayout.outOfRangeCount(tangjeong, 1)
        )
    }

    @Test
    fun `최대로 확대하면 보이는 거점이 줄어들고 본진은 남는다`() {
        val nodes = nodesAt(2)
        val all = MapViewport().visibleNodes(nodes)
        assertTrue(all.size > 1)

        val zoomed = MapViewport().zoomedBy(TerritoryMapLayout.MAX_SCALE).visibleNodes(nodes)
        assertTrue("확대하면 보이는 거점이 줄어든다", zoomed.size < all.size)
        assertEquals("본진은 화면에 남는다", 1, zoomed.count { it.isHome })
    }

    @Test
    fun `본진은 화면 중앙에 그려진다`() {
        val point = MapViewport().screenAt(0f, 0f, width, height)

        assertEquals(width / 2f, point.x, 0.01f)
        assertEquals(height / 2f, point.y, 0.01f)
    }

    @Test
    fun `화면 좌표와 세계 좌표 변환이 서로 맞는다`() {
        val nodes = nodesAt(2)
        val viewport = MapViewport().zoomedBy(1.6f).pannedBy(0.2f, -0.1f)
        val node = nodes.first { it.name == "배방읍" }

        val screen = viewport.screenAt(node.worldX, node.worldY, width, height)
        val (wx, wy) = viewport.worldAt(screen.x, screen.y, width, height)

        assertEquals(node.worldX, wx, 0.001f)
        assertEquals(node.worldY, wy, 0.001f)
    }

    @Test
    fun `지도 탭으로 해금된 거점을 선택한다`() {
        val nodes = nodesAt(2)
        val viewport = MapViewport()
        val baebang = nodes.first { it.name == "배방읍" }
        val point = viewport.screenAt(baebang.worldX, baebang.worldY, width, height)

        val hit = viewport.hitTest(point.x, point.y, width, height, nodes)

        assertNotNull(hit)
        assertEquals("배방읍", hit?.name)
    }

    @Test
    fun `잠긴 거점은 탭으로 선택되지 않는다`() {
        val nodes = nodesAt(1)
        val viewport = MapViewport()
        val locked = nodes.first { it.isLocked }
        val point = viewport.screenAt(locked.worldX, locked.worldY, width, height)

        assertNull(viewport.hitTest(point.x, point.y, width, height, nodes))
    }

    @Test
    fun `거점이 없는 곳을 탭하면 선택되지 않는다`() {
        val viewport = MapViewport()

        assertNull(viewport.hitTest(width / 2f, height - 10f, width, height, nodesAt(2)))
    }

    @Test
    fun `가장 가까운 거점 하나만 선택된다`() {
        val nodes = nodesAt(2)
        val viewport = MapViewport()
        val home = nodes.first { it.isHome }
        val hit: MapNode? = viewport.hitTest(
            width / 2f + 10f,
            height / 2f - 8f,
            width,
            height,
            nodes
        )

        assertEquals(home.code, hit?.code)
    }

    @Test
    fun `지도 거점은 인구와 호족 정원을 함께 가진다`() {
        val nodes = nodesAt(2)
        nodes.forEach { node ->
            assertTrue("${node.name} 인구", node.population > 0)
            assertTrue("${node.name} 호족 정원", node.lordCapacity >= 1)
            assertEquals(node.hopDistance == 0, node.isHome)
        }
        val home = nodes.first { it.isHome }
        assertEquals(48, home.lordCapacity)
        assertFalse(home.isLocked)
    }
}
