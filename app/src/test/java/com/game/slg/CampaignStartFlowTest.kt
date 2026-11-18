package com.game.slg

import com.game.slg.core.economy.ResourceBalance
import com.game.slg.core.region.AiLordGenerator
import com.game.slg.core.save.InMemoryCampaignStore
import com.game.slg.core.region.RegionCatalog
import com.game.slg.ui.GameStage
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 시작 흐름 — 인트로(4장면) → 지역 선택 → AI 호족 인계 → 플레이.
 */
class CampaignStartFlowTest {

    @Test
    fun `앱을 처음 열면 인트로에서 시작하고 기본 지역은 탕정면이다`() {
        val vm = SlgGameViewModel()
        val state = vm.uiState.value

        assertEquals(GameStage.INTRO, state.stage)
        assertFalse(state.introSeen)
        assertEquals("탕정면", state.regionName)
        assertEquals("충청남도 아산시 탕정면", state.regionFullName)
        assertEquals(48604, state.regionPopulation)
        assertEquals(48, state.lordCapacity)
        assertEquals(48, state.lords.size)
        assertNull(state.playerLordId)
        assertEquals(0, state.registeredLords)
        assertEquals(0, state.unlockedHop)
    }

    @Test
    fun `인트로를 건너뛰면 지역 선택으로 간다`() {
        val vm = SlgGameViewModel()

        vm.finishIntro()

        assertEquals(GameStage.REGION_SELECT, vm.uiState.value.stage)
        assertTrue(vm.uiState.value.introSeen)
    }

    @Test
    fun `지역을 고르면 인구에 맞는 호족 정원만큼 AI 호족이 만들어진다`() {
        val vm = SlgGameViewModel()
        val buldang = RegionCatalog.area("4413151000")!! // 천안 불당1동 31,539명

        vm.chooseStartRegion(buldang.code)

        val state = vm.uiState.value
        assertEquals(GameStage.LORD_SELECT, state.stage)
        assertEquals("불당1동", state.regionName)
        assertEquals(31539, state.regionPopulation)
        assertEquals(31, state.lordCapacity)
        assertEquals(31, state.lords.size)
        assertTrue(state.lords.all { it.stronghold.startsWith("불당1동") })
        assertEquals("불당1동", state.mapNodes.first { it.isHome }.name)
    }

    @Test
    fun `AI 호족 한 명을 인계받으면 그 세력으로 플레이가 시작된다`() {
        val vm = SlgGameViewModel()
        vm.chooseStartRegion("4420033000")
        val profile = AiLordGenerator.generate(RegionCatalog.defaultRegion()).first()
        val target = vm.uiState.value.lords.first { it.id == profile.id }

        vm.chooseLord(target.id)

        val state = vm.uiState.value
        assertEquals(GameStage.PLAY, state.stage)
        assertEquals(target.id, state.playerLordId)
        assertEquals(1, state.registeredLords)
        assertEquals(47, state.aiLordCount)
        assertEquals(target.troops + ResourceBalance.FOUNDING_TROOPS, state.troops)
        assertEquals(target.gold + ResourceBalance.FOUNDING_GOLD, state.gold)
        assertEquals(target.grain + ResourceBalance.FOUNDING_FOOD, state.food)
        assertEquals(target.prestige, state.prestige)
        assertTrue(state.lords.first { it.id == target.id }.isPlayer)
        assertEquals("이광수", state.lords.first { it.id == target.id }.name)
        assertTrue(state.statusMessage.contains("인계"))
    }

    @Test
    fun `인계 후에도 나머지 AI 호족은 그대로 운영된다`() {
        val vm = SlgGameViewModel()
        vm.chooseStartRegion("4420033000")
        val first = vm.uiState.value.lords.first()

        vm.chooseLord(first.id)

        val others = vm.uiState.value.lords.filter { !it.isPlayer }
        assertEquals(47, others.size)
        assertTrue(others.none { it.name == "이광수" })
        assertTrue(others.all { it.troops > 0 })
    }

    @Test
    fun `인트로 다시보기는 플레이 상태를 유지한 채 4장면을 다시 보여 준다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        assertEquals(GameStage.PLAY, vm.uiState.value.stage)

        vm.replayIntro()
        assertEquals(GameStage.INTRO, vm.uiState.value.stage)
        assertTrue(vm.uiState.value.introReplay)
        assertNotNull(vm.uiState.value.playerLordId)

        vm.finishIntro()
        assertEquals("다시보기 후에는 플레이로 복귀한다", GameStage.PLAY, vm.uiState.value.stage)
        assertFalse(vm.uiState.value.introReplay)
    }

    @Test
    fun `호족 선택에서 지역 선택으로 돌아갈 수 있다`() {
        val vm = SlgGameViewModel()
        vm.finishIntro()
        vm.chooseStartRegion("4111156000") // 수원 광교1동

        vm.backToRegionSelect()

        assertEquals(GameStage.REGION_SELECT, vm.uiState.value.stage)
    }

    @Test
    fun `잠긴 거점은 지도에서 선택할 수 없다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val locked = vm.uiState.value.mapNodes.first { it.isLocked }
        val homeCode = vm.uiState.value.regionCode

        vm.selectTerritory(locked.code)

        assertEquals(homeCode, vm.uiState.value.selectedTerritoryCode)
        assertTrue(vm.uiState.value.statusMessage.contains("미개방"))
    }

    @Test
    fun `지도 해금은 지휘 레벨을 따라 넓어진다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        assertEquals(1, vm.uiState.value.unlockedNodeCount)
        assertEquals(3, vm.uiState.value.lockedNodeCount)

        // 방어전 2회 → Lv2 (70+70 XP, 60/100 필요)
        vm.runOperation("OP_BASE")
        vm.runOperation("OP_BASE")

        val state = vm.uiState.value
        assertEquals(2, state.command.level)
        assertEquals(1, state.unlockedHop)
        assertEquals(4, state.unlockedNodeCount)
        assertTrue(state.mapNodes.filter { it.hopDistance == 1 }.all { it.unlocked })
    }

    @Test
    fun `지역을 다시 고르면 호족 명부와 성장이 초기화된다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        vm.runOperation("OP_BASE")
        assertTrue(vm.uiState.value.command.level >= 1)

        vm.backToRegionSelect()
        vm.chooseStartRegion("1168051000") // 강남 역삼1동

        val state = vm.uiState.value
        assertEquals(34, state.lordCapacity)
        assertEquals(1, state.command.level)
        assertEquals(0, state.unlockedHop)
        assertNull(state.playerLordId)
    }

    @Test
    fun `인트로를 본 뒤에는 다음 실행에서 인트로를 건너뛴다`() {
        val store = InMemoryCampaignStore()
        val first = SlgGameViewModel(store = store)
        assertEquals(GameStage.INTRO, first.uiState.value.stage)
        first.finishIntro()

        val second = SlgGameViewModel(store = store)

        assertEquals(GameStage.REGION_SELECT, second.uiState.value.stage)
        assertTrue(second.uiState.value.introSeen)
    }

    @Test
    fun `호족을 인계하면 다음 실행에서 그 진행으로 이어진다`() {
        val store = InMemoryCampaignStore()
        val first = SlgGameViewModel(store = store)
        first.finishIntro()
        first.chooseStartRegion("4420033000")
        val lord = first.uiState.value.lords.first()
        first.chooseLord(lord.id)
        first.runOperation("OP_BASE")
        val saved = first.uiState.value

        val second = SlgGameViewModel(store = store)
        val restored = second.uiState.value

        assertEquals(GameStage.PLAY, restored.stage)
        assertEquals("탕정면", restored.regionName)
        assertEquals(lord.id, restored.playerLordId)
        assertEquals(saved.command.level, restored.command.level)
        assertEquals(saved.gold, restored.gold)
        assertEquals(saved.troops, restored.troops)
        assertEquals(saved.prestige, restored.prestige)
        assertEquals(saved.unlockedHop, restored.unlockedHop)
        assertTrue(restored.isCampaignStarted)
    }

    @Test
    fun `새 게임은 저장을 지우고 지역 선택으로 돌아간다`() {
        val store = InMemoryCampaignStore()
        val vm = SlgGameViewModel(store = store)
        vm.chooseLord(vm.uiState.value.lords.first().id)
        vm.restartCampaign()

        val state = vm.uiState.value
        assertEquals(GameStage.REGION_SELECT, state.stage)
        assertNull(state.playerLordId)
        assertEquals(0, state.unlockedHop)
        assertNull(store.load())
    }
}
