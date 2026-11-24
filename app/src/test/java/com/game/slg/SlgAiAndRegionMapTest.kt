package com.game.slg

import com.game.slg.core.region.LeaderStatus
import com.game.slg.core.save.InMemoryCampaignStore
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * AI 리더 활동 · 지역 세부맵 — 게임 흐름 통합 테스트.
 *
 * 사용자 요청(2026-11-24)
 *  · "AI 리더들이 성장·모병·전투로 세력을 넓히게"
 *  · "선택 지역을 클릭하거나 확대하면 리더 배치·현황 세부맵"
 */
class SlgAiAndRegionMapTest {

    private fun startedViewModel(store: InMemoryCampaignStore = InMemoryCampaignStore()): SlgGameViewModel {
        val vm = SlgGameViewModel(store = store)
        vm.chooseLord(vm.uiState.value.lords.first().id)
        return vm
    }

    @Test
    fun `절기를 넘기면 AI 리더들이 성장하고 동향이 남는다`() {
        val vm = startedViewModel()
        val before = vm.uiState.value.lords.filterNot { it.isPlayer }.associate { it.id to it.troops }

        vm.advanceSeason()

        val after = vm.uiState.value
        assertTrue("AI 동향 로그가 비었다", after.aiNews.isNotEmpty())
        assertTrue(after.aiNews.first().contains("[AI 동향]"))
        assertTrue("세부맵용 절기 카운터", after.seasonTurn == 1)
        val grownCount = after.lords.filterNot { it.isPlayer }.count { lord ->
            val previous = before[lord.id] ?: 0
            lord.troops > previous || lord.activity.isNotBlank()
        }
        assertTrue("성장한 AI 리더가 없다", grownCount > after.lords.size / 2)
        assertTrue("로그에 AI 동향이 남아야 한다", after.log.any { it.contains("[AI 동향]") })
    }

    @Test
    fun `AI 병력은 세력 목록에도 반영된다`() {
        val vm = startedViewModel()
        vm.advanceSeason()
        val state = vm.uiState.value
        state.factions.filterNot { it.incorporated }.forEach { faction ->
            val lord = state.lords.firstOrNull { it.id == faction.id.removePrefix("faction_") }
            if (lord != null) {
                assertEquals("${faction.name} 병력 불일치", lord.troops, faction.troops)
            }
        }
    }

    @Test
    fun `지역 세부맵은 리더 정원만큼의 배치를 만든다`() {
        val vm = startedViewModel()
        vm.advanceSeason()
        val state = vm.uiState.value

        vm.openRegionDetail(state.regionCode)

        val board = vm.uiState.value.regionDetailBoard
        assertNotNull(board)
        assertTrue(board!!.live)
        assertEquals(state.lordCapacity, board.totalCount)
        assertEquals(LeaderStatus.PLAYER, board.leaders.first().status)
        assertTrue("플레이어는 0번 자리", board.seatOf(state.playerLordId!!)!!.column == 0)
        assertTrue("2차원 스크롤", board.columns >= 2 && board.rows >= 2)
        assertEquals(state.lordCapacity, board.seats.size)
    }

    @Test
    fun `선택 지역을 한 번 더 누르면 세부맵이 열리고 닫힌다`() {
        val vm = startedViewModel()
        val code = vm.uiState.value.selectedTerritoryCode

        vm.selectTerritory(code)
        assertEquals(code, vm.uiState.value.regionDetailCode)

        vm.closeRegionDetail()
        assertNull(vm.uiState.value.regionDetailCode)
    }

    @Test
    fun `다른 지역도 세부맵으로 볼 수 있고 추정 명부로 표시된다`() {
        val vm = startedViewModel()
        val neighbor = vm.uiState.value.mapNodes.firstOrNull { it.hopDistance == 1 && it.unlocked }

        if (neighbor != null) {
            vm.openRegionDetail(neighbor.code)
            val board = vm.uiState.value.regionDetailBoard
            assertNotNull(board)
            assertFalse(board!!.live)
            assertTrue(board.totalCount > 0)
        }
    }

    @Test
    fun `AI 성장과 병합은 저장되고 복원된다`() {
        val store = InMemoryCampaignStore()
        val first = startedViewModel(store)
        repeat(3) { first.advanceSeason() }
        val before = first.uiState.value
        val savedLord = before.lords.first { !it.isPlayer }

        val second = SlgGameViewModel(store = store)

        assertEquals(before.seasonTurn, second.uiState.value.seasonTurn)
        val restored = second.uiState.value.lords.first { it.id == savedLord.id }
        assertEquals(savedLord.troops, restored.troops)
        assertEquals(savedLord.gold, restored.gold)
    }

    @Test
    fun `연합이 끝난 뒤에도 세부맵 관계 요약이 정상이다`() {
        val vm = startedViewModel()
        repeat(9) { vm.advanceSeason() }
        val state = vm.uiState.value
        vm.openRegionDetail(state.regionCode)
        val board = vm.uiState.value.regionDetailBoard!!
        assertTrue(board.relationSummary().contains("독립"))
        assertTrue(board.leaders.all { it.troops >= 0 && it.gold >= 0 && it.food >= 0 })
    }
}
