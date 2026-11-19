package com.game.slg

import com.game.slg.core.empire.AbsorptionEngine
import com.game.slg.core.empire.AbsorptionMethod
import com.game.slg.core.empire.EmpireContext
import com.game.slg.core.empire.EmpireEngine
import com.game.slg.core.empire.Faction
import com.game.slg.core.empire.FactionRelation
import com.game.slg.core.empire.ManpowerEngine
import com.game.slg.core.empire.ManagedTerritory
import com.game.slg.core.empire.MercenaryEngine
import com.game.slg.core.empire.TerritoryStatus
import com.game.slg.core.empire.ViceroyEngine
import com.game.slg.core.hero.HeroStats
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.StartMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 영토 흡수 · 일임 통치 · 인구 5% 병력 상한 · 고비용 용병 — 2026-11-19 신규 시스템.
 */
class EmpireSystemTest {

    private fun playingVm(): SlgGameViewModel {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        return vm
    }

    private fun faction(
        troops: Int = 500,
        morale: Int = 60,
        affinity: Int = 40,
        commonEnemy: Boolean = false,
        aidCount: Int = 0
    ) = Faction(
        id = "f1",
        name = "박도현",
        territoryCode = "t1",
        territoryName = "탕정면 5구역",
        population = 8_000,
        troops = troops,
        morale = morale,
        affinity = affinity,
        prestige = 55,
        commonEnemy = commonEnemy,
        aidCount = aidCount
    )

    private fun ctx(
        troops: Int = 2_000,
        gold: Int = 9_000,
        food: Int = 9_000,
        espionage: Int = 60,
        diplomacyHero: Int = 0
    ) = EmpireContext(
        troops = troops,
        prestige = 70,
        gold = gold,
        food = food,
        morale = 60,
        diplomacy = 55,
        espionagePower = espionage,
        diplomacyHeroBonus = diplomacyHero
    )

    // ── 인구 5% 병력 상한 ────────────────────────────────────

    @Test
    fun `정규 병력 상한은 전체 인구의 5퍼센트다`() {
        assertEquals(2_430, ManpowerEngine.capOf(48_604))
        assertEquals(0, ManpowerEngine.capOf(0))
        assertEquals(5, ManpowerEngine.capOf(100))
        assertEquals(1_500, ManpowerEngine.roomFor(48_604, 930))
    }

    @Test
    fun `영지를 편입하면 인구가 늘어 상한도 함께 올라간다`() {
        val home = ManagedTerritory("a", "탕정면", 48_604, TerritoryStatus.DIRECT)
        val vassal = ManagedTerritory("b", "배방읍 1구역", 10_000, TerritoryStatus.VASSAL)

        assertEquals(2_430, ManpowerEngine.empireCap(listOf(home)))
        assertEquals(2_930, ManpowerEngine.empireCap(listOf(home, vassal)))
    }

    @Test
    fun `속국이 많을수록 총력 지수와 전투 보너스가 커진다`() {
        val home = ManagedTerritory("a", "탕정면", 48_604, TerritoryStatus.DIRECT, development = 40)
        val v1 = ManagedTerritory("b", "1구역", 8_000, TerritoryStatus.VASSAL, development = 40)
        val v2 = ManagedTerritory("c", "2구역", 9_000, TerritoryStatus.AUTONOMOUS, development = 40)

        assertTrue(
            ManpowerEngine.totalPower(listOf(home, v1, v2), 1_500, 0) >
                ManpowerEngine.totalPower(listOf(home), 1_500, 0)
        )
        assertEquals(0, EmpireEngine.coalitionBonusPercent(listOf(home)))
        assertTrue(EmpireEngine.coalitionBonusPercent(listOf(home, v1, v2)) > 0)
    }

    @Test
    fun `VM에서 정규 모집은 상한을 넘지 못한다`() {
        val vm = playingVm()
        val cap = vm.uiState.value.troopCap
        assertTrue(cap > 0)

        repeat(20) { vm.recruitRegular(300) }

        val state = vm.uiState.value
        assertTrue("정규 병력 ${state.troops} ≤ 상한 $cap", state.troops <= cap)
    }

    // ── 고비용 용병 ──────────────────────────────────────────

    @Test
    fun `용병은 정규 상한과 별도로 고용되고 유지비가 매우 비싸다`() {
        val vm = playingVm()
        val before = vm.uiState.value
        assertTrue(before.gold > MercenaryEngine.hireCostOf(300))

        vm.hireMercenary(300)

        val after = vm.uiState.value
        assertEquals(1, after.mercenaries.size)
        assertEquals(300, after.mercenaryTroops)
        assertEquals(300, after.totalTroops - after.troops)
        assertTrue("용병 유지비", after.mercenaries.first().upkeepPerSeason >= 900)
        assertTrue("고용비", after.gold < before.gold)
    }

    @Test
    fun `유지비를 못 주면 용병이 이탈한다`() {
        val (band, _) = MercenaryEngine.hire(emptyList(), 600, 99_999, 0)
        assertNotNull(band)

        val kept = MercenaryEngine.maintain(listOf(band!!), 900)
        assertTrue("이탈 ${kept.deserted}", kept.deserted > 0)
        assertEquals(600 - kept.deserted, kept.bands.first().troops)

        val paid = MercenaryEngine.maintain(listOf(band), 99_999)
        assertEquals(0, paid.deserted)
        assertEquals(1, paid.bands.size)
    }

    // ── 흡수 6가지 길 ─────────────────────────────────────────

    @Test
    fun `전투 흡수는 병력 105퍼센트가 필요하고 직할로 편입된다`() {
        val target = faction(troops = 500)
        val weak = AbsorptionEngine.availability(AbsorptionMethod.CONQUEST, target, ctx(troops = 400))
        assertNotNull(weak)

        val outcome = AbsorptionEngine.resolve(AbsorptionMethod.CONQUEST, target, ctx(troops = 2_000))
        assertTrue(outcome.success)
        assertTrue(outcome.absorbed)
        assertEquals(TerritoryStatus.DIRECT, outcome.territory?.status)
        assertEquals(0, outcome.faction.troops)
        assertTrue(outcome.troopLoss > 0)
    }

    @Test
    fun `첩자 민심 전환은 첩보력 50과 민심 40 이하가 필요하다`() {
        assertNotNull(
            AbsorptionEngine.availability(
                AbsorptionMethod.SUBVERSION, faction(morale = 60), ctx(espionage = 60)
            )
        )
        assertNotNull(
            AbsorptionEngine.availability(
                AbsorptionMethod.SUBVERSION, faction(morale = 30), ctx(espionage = 20)
            )
        )
        assertNull(
            AbsorptionEngine.availability(
                AbsorptionMethod.SUBVERSION, faction(morale = 30), ctx(espionage = 60)
            )
        )
        val outcome = AbsorptionEngine.resolve(
            AbsorptionMethod.SUBVERSION, faction(morale = 30), ctx(espionage = 60)
        )
        assertTrue(outcome.success)
        assertEquals(TerritoryStatus.VASSAL, outcome.territory?.status)
        assertTrue(outcome.territory!!.tributePerSeason > 0)
    }

    @Test
    fun `무력 압박은 병력 160퍼센트에서 조공 속국을 만든다`() {
        assertNotNull(
            AbsorptionEngine.availability(
                AbsorptionMethod.TRIBUTE_PRESSURE, faction(troops = 500), ctx(troops = 700)
            )
        )
        val outcome = AbsorptionEngine.resolve(
            AbsorptionMethod.TRIBUTE_PRESSURE, faction(troops = 500), ctx(troops = 2_000)
        )
        assertTrue(outcome.success)
        assertEquals(FactionRelation.VASSAL, outcome.faction.relation)
        assertEquals(TerritoryStatus.VASSAL, outcome.territory?.status)
    }

    @Test
    fun `연방 결성은 우호도 60과 공동 적이 필요하다`() {
        assertNotNull(
            AbsorptionEngine.availability(
                AbsorptionMethod.FEDERATION, faction(affinity = 80), ctx()
            )
        )
        val outcome = AbsorptionEngine.resolve(
            AbsorptionMethod.FEDERATION, faction(affinity = 80, commonEnemy = true), ctx()
        )
        assertTrue(outcome.success)
        assertEquals(FactionRelation.ALLIED, outcome.faction.relation)
        assertTrue(outcome.prestigeDelta > 0)
    }

    @Test
    fun `경제 종속 편입은 원조 두 번으로 완성된다`() {
        val first = AbsorptionEngine.resolve(
            AbsorptionMethod.ECONOMIC_DEPENDENCE, faction(affinity = 40), ctx()
        )
        assertTrue(first.success)
        assertFalse(first.absorbed)
        assertEquals(1, first.faction.aidCount)

        val second = AbsorptionEngine.resolve(AbsorptionMethod.ECONOMIC_DEPENDENCE, first.faction, ctx())
        assertTrue(second.success)
        assertTrue(second.absorbed)
        assertEquals(TerritoryStatus.VASSAL, second.territory?.status)
    }

    @Test
    fun `친선 자치령은 외교 영웅이 있어야 합류한다`() {
        assertNotNull(
            AbsorptionEngine.availability(
                AbsorptionMethod.GOODWILL_AUTONOMY, faction(affinity = 50), ctx(diplomacyHero = 0)
            ).let { reason -> if (reason == null) null else reason }
        )
        val outcome = AbsorptionEngine.resolve(
            AbsorptionMethod.GOODWILL_AUTONOMY, faction(affinity = 50), ctx(diplomacyHero = 12)
        )
        assertTrue(outcome.success)
        assertEquals(TerritoryStatus.AUTONOMOUS, outcome.territory?.status)
    }

    // ── 일임 통치 ───────────────────────────────────────────

    @Test
    fun `영웅 능력치에 따라 태수 운영 결과가 달라진다`() {
        val land = ManagedTerritory("a", "탐정면", 48_604, TerritoryStatus.DIRECT, development = 30)
        val strong = ViceroyEngine.govern(land, admin = 90, tech = 90, commerce = 90, ops = 90, diplomacy = 90)
        val weak = ViceroyEngine.govern(land, admin = 30, tech = 20, commerce = 25, ops = 30, diplomacy = 30)

        assertTrue(strong.developmentGain > weak.developmentGain)
        assertTrue(strong.troopGain >= weak.troopGain)
        assertTrue(strong.goldGain > weak.goldGain)
        assertTrue(strong.moraleGain > weak.moraleGain)
    }

    @Test
    fun `절기 정산은 태수 영지의 개발과 세수를 올리고 방치 영지는 민심을 떨어뜨린다`() {
        val governed = ManagedTerritory(
            "a", "탐정면", 40_000, TerritoryStatus.DIRECT, morale = 60, development = 30,
            viceroyHeroId = "hero_001", viceroyName = "한서연"
        )
        val neglected = ManagedTerritory("b", "2구역", 20_000, TerritoryStatus.DIRECT, morale = 60)

        val result = EmpireEngine.processSeason(
            territories = listOf(governed, neglected),
            factions = listOf(faction().copy(incorporated = true, tributePerSeason = 100)),
            regularTroops = 1_000,
            seasonName = "봄",
            heroStats = mapOf("hero_001" to HeroStats(admin = 82, tech = 48, commerce = 55, ops = 21, diplomacy = 74))
        )

        val updatedGoverned = result.territories.first { it.code == "a" }
        val updatedNeglected = result.territories.first { it.code == "b" }
        assertTrue(updatedGoverned.development > 30)
        assertTrue(updatedNeglected.morale < 60)
        assertTrue(result.goldGain > 100)
        assertTrue(result.lines.any { it.contains("[일임]") })
        assertTrue(result.lines.any { it.contains("[조공]") })
    }

    @Test
    fun `VM에서 영지를 전환하고 영웅을 태수로 일임할 수 있다`() {
        val vm = playingVm()
        val factionTarget = vm.uiState.value.openFactions.first()
        vm.attemptAbsorption(AbsorptionMethod.CONQUEST, factionTarget.id)
        val absorbed = vm.uiState.value.territories.last()
        assertTrue(vm.uiState.value.territories.size >= 2)

        vm.switchManagedTerritory(absorbed.code)
        assertEquals(absorbed.code, vm.uiState.value.selectedManagedCode)

        val hero = vm.uiState.value.heroes.ownedHeroes.first()
        vm.assignViceroy(hero.id, absorbed.code)
        val assigned = vm.uiState.value.territories.first { it.code == absorbed.code }
        assertEquals(hero.id, assigned.viceroyHeroId)
        assertEquals(hero.name, assigned.viceroyName)

        vm.unassignViceroy(absorbed.code)
        assertNull(vm.uiState.value.territories.first { it.code == absorbed.code }.viceroyHeroId)
    }

    @Test
    fun `영웅으로 시작하면 리더에게 등용을 청할 수 있고 결과는 같은 기준으로 나온다`() {
        val vm = SlgGameViewModel()
        vm.chooseStartMode(StartMode.HERO)
        vm.pickHeroForStart("hero_239") // 신도경 · 외교 SSR
        val lord = vm.uiState.value.lords.first()

        vm.requestEmployment(lord.id)

        val request = vm.uiState.value.pendingEmployment
        assertNotNull(request)
        assertTrue(request!!.reasonLine.contains("문턱"))
        if (request.accepted) {
            assertEquals(StartMode.HERO, vm.uiState.value.startMode)
            assertNotNull(vm.uiState.value.heroCareer)
            assertEquals("hero_239", vm.uiState.value.heroCareer!!.heroId)
        }
    }
}

/**
 * 편입 영지·용병·무기 기술이 저장·복원되는지 — 앱을 다시 열어도 이어서 운영된다.
 */
class EmpireSaveRestoreTest {

    @Test
    fun `편입 영지와 용병 무기 기술이 저장되고 복원된다`() {
        val store = com.game.slg.core.save.InMemoryCampaignStore()
        val first = SlgGameViewModel(store = store)
        first.chooseLord(first.uiState.value.lords.first().id)
        val faction = first.uiState.value.openFactions.first()
        first.attemptAbsorption(AbsorptionMethod.CONQUEST, faction.id)
        first.hireMercenary(300)
        first.researchWeaponTech(com.game.slg.core.engine.WeaponTech.DRONE)
        val saved = first.uiState.value

        val restored = SlgGameViewModel(store = store).uiState.value

        assertEquals(saved.territories.size, restored.territories.size)
        assertTrue(restored.territories.any { it.absorbedBy == AbsorptionMethod.CONQUEST.label })
        assertEquals(300, restored.mercenaryTroops)
        assertEquals(1, restored.weaponTech.levelOf(com.game.slg.core.engine.WeaponTech.DRONE))
    }
}

class EmpireRestoreConsistencyTest {

    @Test
    fun `복원하면 편입한 세력은 다시 열린 세력으로 돌아오지 않는다`() {
        val store = com.game.slg.core.save.InMemoryCampaignStore()
        val first = SlgGameViewModel(store = store)
        first.chooseLord(first.uiState.value.lords.first().id)
        val faction = first.uiState.value.openFactions.first()
        first.attemptAbsorption(AbsorptionMethod.CONQUEST, faction.id)
        val absorbedCount = first.uiState.value.incorporatedCount
        assertTrue(absorbedCount >= 1)

        val restored = SlgGameViewModel(store = store).uiState.value

        assertEquals(absorbedCount, restored.incorporatedCount)
        assertTrue(restored.openFactions.none { it.territoryCode == faction.territoryCode })
    }
}
