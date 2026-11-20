package com.game.slg

import com.game.slg.core.hero.HeroBonus
import com.game.slg.core.hero.HeroEngine
import com.game.slg.core.hero.HeroRecord
import com.game.slg.core.hero.HeroRole
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroRosterState
import com.game.slg.core.hero.HeroStage
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 현대 영웅(인재) 시스템 — 카탈로그 250명 · 기본 3명 · 탐색/설득/고용 · 슬롯 배치 보너스.
 */
class HeroSystemTest {

    // ── 카탈로그 ──────────────────────────────────────────────

    @Test
    fun `영웅 250명이 초상화 250장과 1대1로 맞물린다`() {
        assertEquals(250, HeroRoster.ALL.size)
        assertEquals(
            (1..250).map { "slg_hero_" + it.toString().padStart(3, '0') },
            HeroRoster.PORTRAIT_NAMES
        )
        assertEquals(250, HeroRoster.ALL.map { it.id }.distinct().size)
        assertEquals(250, HeroRoster.ALL.map { it.name }.distinct().size)
    }

    @Test
    fun `영웅 명단과 역할이 정해져 있다`() {
        assertEquals(
            listOf(
                "한서연", "윤도현", "차명숙", "강태식", "송기환",
                "배도윤", "임하경", "백지현", "구영철", "유하린"
            ),
            HeroRoster.ALL.take(10).map { it.name }
        )
        assertEquals(HeroRole.ADMIN, HeroRoster.byName("한서연")!!.role)
        assertEquals(HeroRole.TECH, HeroRoster.byName("윤도현")!!.role)
        assertEquals(HeroRole.COMMERCE, HeroRoster.byName("차명숙")!!.role)
        assertEquals(HeroRole.OPS, HeroRoster.byName("강태식")!!.role)
        assertEquals(HeroRole.DIPLOMACY, HeroRoster.byName("임하경")!!.role)
        assertEquals(HeroRole.INTEL, HeroRoster.byName("유하린")!!.role)
    }

    @Test
    fun `역할 슬롯은 영지 4개와 외교 2개로 나뉜다`() {
        assertEquals(
            listOf(HeroRole.ADMIN, HeroRole.TECH, HeroRole.COMMERCE, HeroRole.OPS),
            HeroRole.primarySlots
        )
        assertEquals(listOf(HeroRole.DIPLOMACY, HeroRole.INTEL), HeroRole.supportSlots)
        assertEquals(6, HeroRole.entries.size)
    }

    @Test
    fun `모든 영웅은 고유 효과와 명성 조건을 가진다`() {
        HeroRoster.ALL.forEach { hero ->
            assertFalse("${hero.name} 효과 없음", hero.bonus.isEmpty)
            assertTrue("${hero.name} 필요 명성", hero.requiredPrestige in 1..100)
            assertTrue("${hero.name} 대사 없음", hero.quote.isNotBlank())
            assertTrue("${hero.name} 직업 없음", hero.title.isNotBlank())
        }
    }

    // ── 시작 상태 ──────────────────────────────────────────────

    @Test
    fun `게임을 시작하면 기본 영웅 3명을 보유하고 슬롯에 앉아 있다`() {
        val roster = HeroRosterState.initial()

        assertEquals(3, roster.ownedCount)
        assertEquals(
            listOf("한서연", "윤도현", "차명숙"),
            roster.ownedHeroes.map { it.name }
        )
        assertEquals(3, roster.placedCount)
        assertEquals(0, roster.unplaced.size)
        assertNotNull(roster.heroIn(HeroRole.ADMIN))
        assertNotNull(roster.heroIn(HeroRole.TECH))
        assertNotNull(roster.heroIn(HeroRole.COMMERCE))
        assertNull(roster.heroIn(HeroRole.OPS))
    }

    @Test
    fun `나머지 247명은 미탐색 상태로 영입 대상이다`() {
        val roster = HeroRosterState.initial()

        assertEquals(247, roster.recruitables.size)
        roster.recruitables.forEach { hero ->
            assertEquals(HeroStage.UNDISCOVERED, roster.recordOf(hero.id).stage)
            assertEquals(0, roster.recordOf(hero.id).affinity)
        }
    }

    @Test
    fun `기본 3명 배치 보너스가 합산된다`() {
        val bonus = HeroRosterState.initial().activeBonus()

        assertEquals(8, bonus.goldPercent)
        assertEquals(12, bonus.researchDiscountPercent)
        assertEquals(10, bonus.foodPercent)
        assertEquals(0, bonus.combatPercent)
        assertEquals(
            "절기 자금 +8% · 절기 보급 +10% · 연구 비용 -12%",
            bonus.summary()
        )
    }

    // ── 탐색 → 설득 → 고용 ────────────────────────────────────

    @Test
    fun `탐색은 자금 200G를 쓰고 호감도를 올린다`() {
        val roster = HeroRosterState.initial()

        val result = HeroEngine.scout(roster, "hero_004", gold = 1000)

        assertTrue(result.success)
        assertEquals(-HeroRoster.SCOUT_COST, result.goldDelta)
        assertEquals(HeroStage.SCOUTED, result.roster.recordOf("hero_004").stage)
        assertEquals(HeroRoster.SCOUT_AFFINITY, result.roster.recordOf("hero_004").affinity)
        assertTrue(result.message.contains("강태식"))
    }

    @Test
    fun `자금이 모자라면 탐색할 수 없다`() {
        val result = HeroEngine.scout(HeroRosterState.initial(), "hero_004", gold = 100)

        assertFalse(result.success)
        assertEquals(0, result.goldDelta)
        assertEquals(HeroStage.UNDISCOVERED, result.roster.recordOf("hero_004").stage)
        assertTrue(result.message.contains("자금"))
    }

    @Test
    fun `같은 영웅을 두 번 탐색할 수 없다`() {
        val first = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 1000)
        val second = HeroEngine.scout(first.roster, "hero_004", 1000)

        assertFalse(second.success)
        assertTrue(second.message.contains("이미 탐색"))
    }

    @Test
    fun `보유 영웅은 탐색 대상이 아니다`() {
        val result = HeroEngine.scout(HeroRosterState.initial(), "hero_001", 1000)

        assertFalse(result.success)
        assertTrue(result.message.contains("이미 우리 리더"))
    }

    @Test
    fun `탐색 없이 설득할 수 없다`() {
        val result = HeroEngine.persuade(HeroRosterState.initial(), "hero_004", prestige = 90)

        assertFalse(result.success)
        assertTrue(result.message.contains("탐색"))
    }

    @Test
    fun `명성이 모자라면 설득에 실패하고 호감도만 오른다`() {
        val scouted = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 1000).roster
        val before = scouted.recordOf("hero_004").affinity

        val result = HeroEngine.persuade(scouted, "hero_004", prestige = 10)

        assertFalse(result.success)
        assertEquals(HeroStage.SCOUTED, result.roster.recordOf("hero_004").stage)
        assertEquals(before + HeroRoster.PERSUADE_FAIL_AFFINITY, result.roster.recordOf("hero_004").affinity)
        assertTrue(result.message.contains("48"))
    }

    @Test
    fun `명성이 충분하면 설득에 성공한다`() {
        val scouted = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 1000).roster

        val result = HeroEngine.persuade(scouted, "hero_004", prestige = 55)

        assertTrue(result.success)
        assertEquals(HeroStage.PERSUADED, result.roster.recordOf("hero_004").stage)
        assertEquals(HeroRoster.PERSUADED_AFFINITY, result.roster.recordOf("hero_004").affinity)
    }

    @Test
    fun `외교 영웅의 설득력이 필요 명성을 낮춘다`() {
        val scouted = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 1000).roster

        val without = HeroEngine.persuade(scouted, "hero_004", prestige = 45, diplomacyBonus = 0)
        val with = HeroEngine.persuade(scouted, "hero_004", prestige = 45, diplomacyBonus = 12)

        assertFalse(without.success)
        assertTrue(with.success)
    }

    @Test
    fun `설득 전에는 고용할 수 없다`() {
        val scouted = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 1000).roster

        val result = HeroEngine.hire(scouted, "hero_004", gold = 5000)

        assertFalse(result.success)
        assertTrue(result.message.contains("설득"))
    }

    @Test
    fun `고용은 자금을 쓰고 빈 슬롯에 자동 배치된다`() {
        val roster = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 1000).roster
        val persuaded = HeroEngine.persuade(roster, "hero_004", prestige = 60).roster

        val result = HeroEngine.hire(persuaded, "hero_004", gold = 2000)

        assertTrue(result.success)
        assertEquals(-HeroRoster.byId("hero_004")!!.hireCost, result.goldDelta)
        assertEquals(HeroStage.HIRED, result.roster.recordOf("hero_004").stage)
        assertEquals(HeroRole.OPS, result.roster.recordOf("hero_004").assignedRole)
        assertEquals(4, result.roster.ownedCount)
        assertEquals(12, result.roster.activeBonus().combatPercent)
    }

    @Test
    fun `자금이 모자라면 고용할 수 없다`() {
        val roster = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 1000).roster
        val persuaded = HeroEngine.persuade(roster, "hero_004", prestige = 60).roster

        val result = HeroEngine.hire(persuaded, "hero_004", gold = 100)

        assertFalse(result.success)
        assertEquals(3, result.roster.ownedCount)
    }

    @Test
    fun `탐색에서 고용까지 한 번에 이어진다`() {
        var roster = HeroRosterState.initial()
        var gold = 3000

        val scouted = HeroEngine.scout(roster, "hero_010", gold)
        roster = scouted.roster
        gold += scouted.goldDelta
        assertTrue(scouted.success)

        val persuaded = HeroEngine.persuade(roster, "hero_010", prestige = 90)
        roster = persuaded.roster
        assertTrue(persuaded.success)

        val hired = HeroEngine.hire(roster, "hero_010", gold)
        roster = hired.roster
        gold += hired.goldDelta

        assertTrue(hired.success)
        assertEquals(4, roster.ownedCount)
        assertEquals(15, roster.activeBonus().intelBonus)
        assertEquals(3000 - 200 - HeroRoster.byId("hero_010")!!.hireCost, gold)
    }

    // ── 슬롯 배치 ──────────────────────────────────────────────

    @Test
    fun `역할이 다르면 배치할 수 없다`() {
        val result = HeroEngine.assign(HeroRosterState.initial(), "hero_001", HeroRole.OPS)

        assertFalse(result.success)
        assertTrue(result.message.contains("내정"))
    }

    @Test
    fun `보유하지 않은 영웅은 배치할 수 없다`() {
        val result = HeroEngine.assign(HeroRosterState.initial(), "hero_004", HeroRole.OPS)

        assertFalse(result.success)
    }

    @Test
    fun `슬롯에서 내리면 보너스가 빠진다`() {
        val initial = HeroRosterState.initial()
        val result = HeroEngine.unassign(initial, "hero_002")

        assertTrue(result.success)
        assertEquals(2, result.roster.placedCount)
        assertEquals(0, result.roster.activeBonus().researchDiscountPercent)
        assertEquals(8, result.roster.activeBonus().goldPercent)
    }

    @Test
    fun `같은 역할의 영웅을 앉히면 기존 영웅은 대기로 내려간다`() {
        val hired = HeroEngine.scout(HeroRosterState.initial(), "hero_008", 3000).roster
            .let { HeroEngine.persuade(it, "hero_008", prestige = 90).roster }
            .let { HeroEngine.hire(it, "hero_008", gold = 3000).roster }

        // 윤도현이 기술 슬롯에 있으므로 백지현은 대기 상태로 고용된다
        assertNull(hired.recordOf("hero_008").assignedRole)

        val assigned = HeroEngine.assign(hired, "hero_008", HeroRole.TECH)

        assertTrue(assigned.success)
        assertEquals("hero_008", assigned.roster.heroIn(HeroRole.TECH)?.id)
        assertNull(assigned.roster.recordOf("hero_002").assignedRole)
        assertEquals(10, assigned.roster.activeBonus().researchDiscountPercent)
    }

    @Test
    fun `빈 슬롯 자동 채우기는 대기 영웅을 앉힌다`() {
        val hired = HeroEngine.scout(HeroRosterState.initial(), "hero_004", 3000).roster
            .let { HeroEngine.persuade(it, "hero_004", prestige = 90).roster }
            .let { HeroEngine.hire(it, "hero_004", gold = 3000).roster }

        val removed = HeroEngine.unassign(hired, "hero_004").roster
        assertNull(removed.recordOf("hero_004").assignedRole)

        val filled = HeroEngine.fillEmptySlots(removed)

        assertEquals("hero_004", filled.heroIn(HeroRole.OPS)?.id)
        assertEquals(4, filled.placedCount)
    }

    @Test
    fun `같은 역할에는 한 명만 앉는다`() {
        val roster = HeroRosterState.initial()
        val bonus = roster.activeBonus()

        HeroRole.entries.forEach { role ->
            val occupants = roster.ownedHeroes.count { roster.recordOf(it.id).assignedRole == role }
            assertTrue("$role 중복 배치", occupants <= 1)
        }
        assertEquals(HeroBonus(goldPercent = 8, foodPercent = 10, researchDiscountPercent = 12), bonus)
    }

    // ── 게임에 붙은 효과 ──────────────────────────────────────

    @Test
    fun `영웅 탭 조작이 게임 상태와 자금에 반영된다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        vm.chooseEventA(acceptPact = false) // 자금 +900
        val before = vm.uiState.value

        vm.scoutHero("hero_010")

        val after = vm.uiState.value
        assertEquals(before.gold - HeroRoster.SCOUT_COST, after.gold)
        assertEquals(HeroStage.SCOUTED, after.heroes.recordOf("hero_010").stage)
        assertEquals(after.statusMessage, after.heroMessage)
    }

    @Test
    fun `기술 영웅이 연구 비용을 할인한다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        vm.chooseEventA(acceptPact = false) // 자금 +900
        val before = vm.uiState.value
        assertEquals(12, before.heroBonus.researchDiscountPercent)

        vm.research("admin_1")

        val after = vm.uiState.value
        assertTrue(after.researchedTechs.contains("admin_1"))
        assertEquals(before.gold - 352, after.gold) // 400G · 12% 할인
    }

    @Test
    fun `영웅을 내리면 연구 할인이 사라진다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        vm.chooseEventA(acceptPact = false)
        vm.unassignHero("hero_002")
        val before = vm.uiState.value
        assertEquals(0, before.heroBonus.researchDiscountPercent)

        vm.research("admin_1")

        assertEquals(before.gold - 400, vm.uiState.value.gold)
    }

    @Test
    fun `영입한 영웅은 저장되고 복원된다`() {
        val store = com.game.slg.core.save.InMemoryCampaignStore()
        val first = SlgGameViewModel(store = store)
        first.chooseLord(first.uiState.value.lords.first().id)
        first.chooseEventA(acceptPact = false) // 자금 +900
        first.chooseEventB(ambush = true) // 자금 +250
        repeat(15) { first.runOperation("OP_BASE") } // 스폰 보호 방어로 명성 +1씩
        val hero = HeroRoster.byId("hero_005")!!
        assertTrue(first.uiState.value.prestige >= hero.requiredPrestige)

        first.scoutHero("hero_005")
        first.persuadeHero("hero_005")
        first.hireHero("hero_005")
        assertEquals(4, first.uiState.value.heroes.ownedCount)

        val restored = SlgGameViewModel(store = store)
        val state = restored.uiState.value

        assertEquals(4, state.heroes.ownedCount)
        assertEquals(HeroStage.HIRED, state.heroes.recordOf("hero_005").stage)
        assertEquals(4, state.heroes.records.size)
        assertEquals(state.heroes.ownedCount, HeroRosterState.fromSaves(
            owned = state.heroes.ownedHeroes.map { it.id }.toSet(),
            placed = state.heroes.ownedHeroes
                .mapNotNull { h -> state.heroes.recordOf(h.id).assignedRole?.let { h.id to it.name } }
                .toMap()
        ).ownedCount)
    }

    @Test
    fun `저장 기록에 없는 영웅 id 는 버린다`() {
        val roster = HeroRosterState.fromSaves(
            owned = setOf("hero_001", "hero_999"),
            placed = mapOf("hero_001" to "ADMIN", "hero_999" to "OPS")
        )

        assertEquals(1, roster.ownedCount)
        assertEquals("한서연", roster.ownedHeroes.first().name)
        assertEquals(HeroRole.ADMIN, roster.recordOf("hero_001").assignedRole)
    }

    @Test
    fun `예전 저장값의 2자리 영웅 id 도 3자리로 맞춰 복원한다`() {
        val roster = HeroRosterState.fromSaves(
            owned = setOf("hero_02", "hero_10"),
            placed = mapOf("hero_02" to "TECH")
        )

        assertEquals(2, roster.ownedCount)
        assertEquals("윤도현", roster.ownedHeroes.first().name)
        assertEquals(HeroRole.TECH, roster.recordOf("hero_002").assignedRole)
    }

    @Test
    fun `영웅 기록은 단계와 호감도를 보존한다`() {
        val record = HeroRecord(id = "hero_004", stage = HeroStage.PERSUADED, affinity = 78)
        val roster = HeroRosterState(mapOf("hero_004" to record))

        assertFalse(roster.recordOf("hero_004").owned)
        assertEquals(HeroStage.PERSUADED, roster.recordOf("hero_004").stage)
        assertEquals(78, roster.recordOf("hero_004").affinity)
        assertEquals(0, roster.ownedCount)
    }
}
