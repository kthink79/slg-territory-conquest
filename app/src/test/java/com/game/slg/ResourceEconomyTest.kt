package com.game.slg

import com.game.slg.core.economy.BoostKind
import com.game.slg.core.economy.ResourceBalance
import com.game.slg.core.economy.ResourceBoostAction
import com.game.slg.core.economy.ResourceBoosts
import com.game.slg.core.economy.ResourceKind
import com.game.slg.core.model.FacilityType
import com.game.slg.ui.DomainTab
import com.game.slg.ui.MainTab
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 재원 밸런스(초반 부족 완화) + 재원 탭 서브 메뉴(늘리는 방법) 단위 테스트.
 * 사용자 요청(2026-11-18):
 *  · "SLG 게임 초기 시작시 재원이 너무 부족해"
 *  · "화면에서 식량, 골드, 인구수, 병력을 선택 했을때 증가 시킬수 있는 방법들이 나오는 서브 메뉴가 나오도록"
 */
class ResourceEconomyTest {

    private fun playingVm(): SlgGameViewModel {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        return vm
    }

    // ── 초반 재원 ─────────────────────────────────────────────

    @Test
    fun `호족 인계 직후 재원은 첫 연구와 시설을 지을 만큼 넉넉하다`() {
        val state = playingVm().uiState.value

        assertTrue("자금 ${state.gold}G", state.gold >= 8_000)
        assertTrue("식량 ${state.food}석", state.food >= 16_000)
        assertTrue("병력 ${state.troops}명", state.troops >= 1_200)
        // 첫 연구 400G · 첫 시설 700G · 영웅 탐색 200G 를 한 번에 해도 남는다
        assertTrue(state.gold >= 400 + 700 + 200)
    }

    @Test
    fun `인계 전 미리보기 상태도 기본 재원을 갖고 있다`() {
        val state = SlgGameViewModel().uiState.value

        assertEquals(ResourceBalance.START_GOLD, state.gold)
        assertEquals(ResourceBalance.START_FOOD, state.food)
        assertEquals(ResourceBalance.START_TROOPS, state.troops)
    }

    @Test
    fun `절기 기본 수급은 인구에 비례하고 물류 허브를 지으면 늘어난다`() {
        val plain = ResourceBalance.seasonFood(48_604)
        val withHub = ResourceBalance.seasonFood(48_604, setOf(FacilityType.LOGISTICS_HUB))
        val small = ResourceBalance.seasonFood(10_000)

        assertTrue(plain > 2_500)
        assertTrue(withHub > plain)
        assertTrue(plain > small)
        assertTrue(ResourceBalance.seasonGold(48_604) > ResourceBalance.seasonGold(10_000))
        assertTrue(ResourceBalance.seasonGold(48_604) >= 2_000)
    }

    @Test
    fun `절기를 넘기면 기본 수급이 실제 자금과 식량에 더해진다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.advanceSeason()

        val after = vm.uiState.value
        assertTrue(after.gold >= before.gold + ResourceBalance.seasonGold(before.population))
        assertTrue(after.food >= before.food - 1_000)
        assertTrue(after.statusMessage.contains("수급"))
    }

    @Test
    fun `겨울 유지비를 내도 초반 보급이 바닥나지 않는다`() {
        val vm = playingVm()

        repeat(4) { vm.advanceSeason() } // 봄 → 여름 → 가을 → 겨울

        val state = vm.uiState.value
        assertTrue("겨울 뒤 보급 ${state.food}", state.food > 5_000)
        assertTrue("겨울 뒤 병력 ${state.troops}", state.troops > 1_000)
    }

    // ── 재원 탭 서브 메뉴 ─────────────────────────────────────

    @Test
    fun `재원 4종 모두 증가 방법을 4개 이상 가지고 있다`() {
        ResourceKind.entries.forEach { kind ->
            val actions = ResourceBoosts.of(kind)
            assertTrue("${kind.label} 방법 ${actions.size}개", actions.size >= 4)
            assertTrue(
                "${kind.label} 즉시 실행 항목 없음",
                actions.any { it.kind == BoostKind.INSTANT }
            )
        }
    }

    @Test
    fun `증가 방법 id 는 중복이 없고 화면 이동 항목은 목표 화면을 갖는다`() {
        val ids = ResourceBoosts.ALL.map { it.id }

        assertEquals(ids.size, ids.distinct().size)
        ResourceBoosts.ALL.forEach { action ->
            if (action.kind != BoostKind.INSTANT) {
                assertTrue("${action.id} 이동 목표 없음", action.target != null)
            }
            assertTrue("${action.id} 제목 없음", action.title.isNotBlank())
            assertTrue("${action.id} 효과 없음", action.effect.isNotBlank())
        }
    }

    @Test
    fun `식량 매입은 자금을 쓰고 식량을 늘린다`() {
        val outcome = ResourceBoosts.execute(
            actionId = "food_buy",
            gold = 5_000,
            food = 1_000,
            population = 48_604,
            troops = 1_200,
            morale = 60,
            usedThisSeason = 0
        )

        assertTrue(outcome.ok)
        assertEquals(-ResourceBoosts.MARKET_BUY_GOLD, outcome.goldDelta)
        assertEquals(ResourceBoosts.MARKET_BUY_FOOD, outcome.foodDelta)
    }

    @Test
    fun `자금이 모자라면 식량을 살 수 없다`() {
        val outcome = ResourceBoosts.execute("food_buy", 100, 1_000, 48_604, 1_200, 60, 0)

        assertFalse(outcome.ok)
        assertTrue(outcome.message.contains("자금"))
        assertEquals(0, outcome.foodDelta)
    }

    @Test
    fun `비축 식량 판매는 자금을 늘리고 식량을 줄인다`() {
        val outcome = ResourceBoosts.execute("gold_sell_food", 1_000, 5_000, 48_604, 1_200, 60, 0)

        assertTrue(outcome.ok)
        assertEquals(ResourceBoosts.MARKET_SELL_GOLD, outcome.goldDelta)
        assertEquals(-ResourceBoosts.MARKET_SELL_FOOD, outcome.foodDelta)
    }

    @Test
    fun `긴급 세수는 자금을 늘리고 민심을 깎으며 절기당 2회까지다`() {
        val action = ResourceBoosts.byId("gold_levy")!!
        assertEquals(2, action.requirement.perSeasonLimit)
        assertNull(ResourceBoosts.availability(action, 1_000, 1_000, 1))

        val outcome = ResourceBoosts.execute("gold_levy", 1_000, 1_000, 48_604, 1_200, 60, 1)
        assertTrue(outcome.ok)
        assertEquals(ResourceBoosts.LEVY_GOLD, outcome.goldDelta)
        assertEquals(ResourceBoosts.LEVY_MORALE, outcome.moraleDelta)

        val blocked = ResourceBoosts.execute("gold_levy", 1_000, 1_000, 48_604, 1_200, 60, 2)
        assertFalse(blocked.ok)
        assertTrue(blocked.message.contains("모두 썼습니다"))
    }

    @Test
    fun `병력 모집은 자금과 식량을 쓰고 병력을 늘린다`() {
        val outcome = ResourceBoosts.execute("troops_recruit", 3_000, 3_000, 48_604, 1_200, 60, 0)

        assertTrue(outcome.ok)
        assertEquals(-ResourceBoosts.RECRUIT_GOLD, outcome.goldDelta)
        assertEquals(-ResourceBoosts.RECRUIT_FOOD, outcome.foodDelta)
        assertEquals(ResourceBoosts.RECRUIT_TROOPS, outcome.troopsDelta)
    }

    @Test
    fun `정착 지원은 인구를 늘리고 민심을 올린다`() {
        val outcome = ResourceBoosts.execute("pop_settle", 3_000, 3_000, 48_604, 1_200, 60, 0)

        assertTrue(outcome.ok)
        assertEquals(ResourceBoosts.SETTLE_POPULATION, outcome.populationDelta)
        assertEquals(ResourceBoosts.SETTLE_MORALE, outcome.moraleDelta)
        assertEquals(-ResourceBoosts.SETTLE_GOLD, outcome.goldDelta)
    }

    @Test
    fun `화면 이동 항목은 즉시 실행되지 않는다`() {
        val outcome = ResourceBoosts.execute("gold_tech", 9_999, 9_999, 48_604, 1_200, 60, 0)

        assertFalse(outcome.ok)
        assertTrue(outcome.message.contains("화면"))
    }

    // ── 게임 상태에 붙은 효과 ─────────────────────────────────

    @Test
    fun `식량 매입을 실행하면 상태의 자금과 식량이 함께 움직인다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.boostResource("food_buy")

        val after = vm.uiState.value
        assertEquals(before.gold - ResourceBoosts.MARKET_BUY_GOLD, after.gold)
        assertEquals(before.food + ResourceBoosts.MARKET_BUY_FOOD, after.food)
        assertTrue(after.resourceMessage!!.contains("식량"))
        assertEquals(1, after.boostUses["food_buy"])
    }

    @Test
    fun `병력 모집은 지도 거점 병력 표시까지 갱신한다`() {
        val vm = playingVm()
        val before = vm.uiState.value
        val homeBefore = before.mapNodes.first { it.isHome }.troops

        vm.boostResource("troops_recruit")

        val after = vm.uiState.value
        assertEquals(before.troops + ResourceBoosts.RECRUIT_TROOPS, after.troops)
        assertEquals(homeBefore + ResourceBoosts.RECRUIT_TROOPS, after.mapNodes.first { it.isHome }.troops)
    }

    @Test
    fun `절기당 사용 한도를 넘기면 실행되지 않는다`() {
        val vm = playingVm()

        repeat(3) { vm.boostResource("troops_recruit") }
        val capped = vm.uiState.value

        vm.boostResource("troops_recruit")

        val after = vm.uiState.value
        assertEquals(capped.troops, after.troops)
        assertTrue(after.resourceMessage!!.contains("모두 썼습니다"))
    }

    @Test
    fun `절기를 넘기면 사용 횟수가 초기화된다`() {
        val vm = playingVm()
        repeat(2) { vm.boostResource("gold_levy") }
        assertEquals(2, vm.uiState.value.boostUses["gold_levy"])

        vm.advanceSeason()

        assertTrue(vm.uiState.value.boostUses.isEmpty())
    }

    @Test
    fun `이동 항목을 누르면 해당 화면이 열린다`() {
        val vm = playingVm()

        vm.boostResource("gold_tech")

        assertEquals(MainTab.DOMAIN, vm.navigation.value.tab)
        assertEquals(DomainTab.TECH.ordinal, vm.navigation.value.sub)
    }

    @Test
    fun `영웅 배치 이동 항목은 영웅 탭 배치 화면으로 간다`() {
        val vm = playingVm()

        vm.boostResource("troops_hero")

        assertEquals(MainTab.HERO, vm.navigation.value.tab)
    }

    @Test
    fun `절기 항목은 절기를 실제로 넘긴다`() {
        val vm = playingVm()
        val beforeSeason = vm.uiState.value.seasonName

        vm.boostResource("food_season")

        assertTrue(vm.uiState.value.seasonName != beforeSeason)
    }

    @Test
    fun `재원 숫자는 HUD 와 시트에서 같은 값을 가리킨다`() {
        val state = playingVm().uiState.value

        assertEquals(state.gold, ResourceBoosts.valueOf(ResourceKind.GOLD, state.gold, state.food, state.population, state.troops))
        assertEquals(state.food, ResourceBoosts.valueOf(ResourceKind.FOOD, state.gold, state.food, state.population, state.troops))
        assertEquals(
            state.population,
            ResourceBoosts.valueOf(ResourceKind.POPULATION, state.gold, state.food, state.population, state.troops)
        )
        assertEquals(state.troops, ResourceBoosts.valueOf(ResourceKind.TROOPS, state.gold, state.food, state.population, state.troops))
    }

    @Test
    fun `재원별 방법은 모두 20개이고 재원 종류에 정확히 묶여 있다`() {
        val actions: List<ResourceBoostAction> = ResourceBoosts.ALL
        assertTrue(actions.all { it.resource in ResourceKind.entries })
        assertEquals(20, actions.size)
        assertEquals(5, ResourceBoosts.of(ResourceKind.GOLD).size)
        assertEquals(5, ResourceBoosts.of(ResourceKind.FOOD).size)
        assertEquals(5, ResourceBoosts.of(ResourceKind.POPULATION).size)
        assertEquals(5, ResourceBoosts.of(ResourceKind.TROOPS).size)
    }
}
