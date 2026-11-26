package com.game.slg

import com.game.slg.core.empire.GovernanceEngine
import com.game.slg.core.empire.GovernancePolicy
import com.game.slg.core.empire.LevyQuota
import com.game.slg.core.empire.ManagedTerritory
import com.game.slg.core.empire.TaxLevel
import com.game.slg.core.empire.TerritoryFacility
import com.game.slg.core.empire.TerritoryStatus
import com.game.slg.core.region.AdjacentActionKind
import com.game.slg.core.region.AdminMenu
import com.game.slg.core.region.AdminTier
import com.game.slg.core.region.AdminTierEngine
import com.game.slg.core.region.CasusBelli
import com.game.slg.core.region.ConquestEngine
import com.game.slg.core.region.ConquestRecord
import com.game.slg.core.region.WarAgendaEngine
import com.game.slg.core.region.WarTargetKind
import com.game.slg.ui.DomainTab
import com.game.slg.ui.MainTab
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.10.0 — 인접 지역 무력 병합 · 의회 전쟁 안건 · 병합 후 세부 통치 · 행정 단위 4단계.
 * 사용자 요청(2026-11-26)의 네 축을 단위 테스트로 검증한다.
 */
class VassalConquestGovernanceTest {

    private fun territory(
        code: String = "t1",
        status: TerritoryStatus = TerritoryStatus.DIRECT,
        policy: GovernancePolicy = GovernancePolicy()
    ): ManagedTerritory = ManagedTerritory(
        code = code,
        name = "시험 영지",
        population = 30_000,
        status = status,
        morale = 60,
        development = 40,
        troops = 300,
        tributePerSeason = if (status == TerritoryStatus.DIRECT) 0 else 60,
        absorbedBy = "시험",
        policy = policy
    )

    // ── ① 무력 병합 ─────────────────────────────────────────────

    @Test
    fun `적 병력을 전멸시키면 편입 제안이 열린다`() {
        val offer = ConquestEngine.offerAfterBattle(
            leaderId = "ai_a",
            leaderName = "김 리더",
            stronghold = "온양2동",
            regionCode = "asan_onyang2",
            regionName = "온양2동",
            troopsBefore = 900,
            losses = 950,
            ourTroops = 1_200,
            won = true,
            population = 12_000,
            gold = 800,
            food = 1_500
        )
        assertNotNull(offer)
        assertEquals(0, offer!!.remainingTroops)
        assertEquals("전멸", offer.reasonLabel)
    }

    @Test
    fun `전멸도 항복 조건도 아니면 편입 제안이 없다`() {
        val offer = ConquestEngine.offerAfterBattle(
            leaderId = "ai_a",
            leaderName = "김 리더",
            stronghold = "온양2동",
            regionCode = "asan_onyang2",
            regionName = "온양2동",
            troopsBefore = 900,
            losses = 300,
            ourTroops = 1_200,
            won = true,
            population = 12_000,
            gold = 800,
            food = 1_500
        )
        assertNull(offer)
    }

    @Test
    fun `두 배 이상 압도해 잔여 15퍼센트 아래로 몰면 항복으로 편입된다`() {
        val offer = ConquestEngine.offerAfterBattle(
            leaderId = "ai_a",
            leaderName = "김 리더",
            stronghold = "온양2동",
            regionCode = "asan_onyang2",
            regionName = "온양2동",
            troopsBefore = 1_000,
            losses = 900,
            ourTroops = 2_400,
            won = true,
            population = 12_000,
            gold = 800,
            food = 1_500
        )
        assertNotNull(offer)
        assertTrue(offer!!.surrender)
        assertEquals("항복", offer.reasonLabel)
    }

    @Test
    fun `직할 속국 자치령 세 형태로 편입된다`() {
        val offer = ConquestEngine.offerForEmptyGarrison(
            leaderId = "ai_a",
            leaderName = "김 리더",
            stronghold = "온양2동",
            regionCode = "asan_onyang2",
            regionName = "온양2동",
            population = 12_000,
            gold = 900,
            food = 1_500,
            ourTroops = 1_200
        )
        val direct = ConquestEngine.resolve(offer, TerritoryStatus.DIRECT)
        val vassal = ConquestEngine.resolve(offer, TerritoryStatus.VASSAL)
        val autonomous = ConquestEngine.resolve(offer, TerritoryStatus.AUTONOMOUS)

        assertEquals(TerritoryStatus.DIRECT, direct.territory!!.status)
        assertEquals(TerritoryStatus.VASSAL, vassal.territory!!.status)
        assertEquals(TerritoryStatus.AUTONOMOUS, autonomous.territory!!.status)
        // 속국·자치령은 절기 조공이 붙는다
        assertTrue(vassal.territory!!.tributePerSeason > 0)
        assertTrue(autonomous.territory!!.tributePerSeason > 0)
        assertEquals(0, direct.territory!!.tributePerSeason)
        // 형태마다 명성 보상이 다르다 — 직할이 가장 크다
        assertTrue(direct.prestigeDelta > vassal.prestigeDelta)
        assertTrue(vassal.prestigeDelta > autonomous.prestigeDelta)
    }

    @Test
    fun `의회 명분이 있으면 병합 전리품이 늘어난다`() {
        val offer = ConquestEngine.offerForEmptyGarrison(
            leaderId = "ai_a",
            leaderName = "김 리더",
            stronghold = "온양2동",
            regionCode = "asan_onyang2",
            regionName = "온양2동",
            population = 12_000,
            gold = 1_000,
            food = 1_500,
            ourTroops = 1_200
        )
        val plain = ConquestEngine.resolve(offer, TerritoryStatus.DIRECT)
        val withCause = ConquestEngine.resolve(
            offer,
            TerritoryStatus.DIRECT,
            CasusBelli(combatBonusPercent = 10, casualtyReductionPercent = 12, lootBonusPercent = 40)
        )
        assertTrue(withCause.goldDelta > plain.goldDelta)
    }

    @Test
    fun `편입 기록은 저장 문자열로 왕복한다`() {
        val record = ConquestRecord(
            leaderId = "ai_a",
            leaderName = "김 리더",
            regionCode = "asan_onyang2",
            regionName = "온양2동",
            status = TerritoryStatus.VASSAL,
            population = 12_000
        )
        val restored = ConquestRecord.decode(record.encode())
        assertEquals(record, restored)
    }

    @Test
    fun `전멸시킨 인접 리더는 영지로 편입되고 소속 영웅이 처분 대기로 온다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        // 지휘 Lv 을 올려 인접 지역을 열고
        repeat(10) { vm.advanceSeason() }
        val adjacent = vm.uiState.value.mapNodes
            .firstOrNull { it.hopDistance == 1 && it.unlocked }
        assertNotNull("인접 지역이 열리지 않았다", adjacent)
        vm.selectTerritory(adjacent!!.code)
        vm.openAdjacentAction(AdjacentActionKind.WAR)
        val target = vm.uiState.value.selectedAdjacentPanel?.warTargets?.firstOrNull()
        assertNotNull("교전 대상이 없다", target)

        // 병력이 0 이 될 때까지 반복 교전
        repeat(60) {
            if (vm.uiState.value.pendingConquest == null) {
                vm.challengeAdjacentLeader(target!!.id)
            }
        }
        val offer = vm.uiState.value.pendingConquest
        assertNotNull("전멸 뒤 편입 제안이 열리지 않았다", offer)
        val before = vm.uiState.value.territories.size
        vm.resolveConquest(TerritoryStatus.VASSAL)

        val state = vm.uiState.value
        assertNull(state.pendingConquest)
        assertEquals(before + 1, state.territories.size)
        assertTrue(state.conquests.any { it.leaderId == offer!!.leaderId })
        assertTrue("속국 영지가 조공을 낸다", state.territories.last().tributePerSeason > 0)
    }

    @Test
    fun `세율과 시설은 저장하고 다시 열어도 남는다`() {
        val store = com.game.slg.core.save.InMemoryCampaignStore()
        val vm = SlgGameViewModel(store = store)
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val code = vm.uiState.value.territories.first().code
        vm.setTerritoryTax(code, TaxLevel.HIGH)
        vm.investTerritoryFacility(code, TerritoryFacility.WALL.id)

        val reloaded = SlgGameViewModel(store = store)
        val restored = reloaded.uiState.value.territories.first { it.code == code }
        assertEquals(TaxLevel.HIGH, restored.policy.taxLevel)
        assertTrue(restored.policy.facilities.contains(TerritoryFacility.WALL))
    }

    // ── ② 병합 후 세부 통치 ─────────────────────────────────────

    @Test
    fun `세율을 올리면 세수가 늘고 민심이 흘러내린다`() {
        val normal = territory()
        val high = territory(policy = GovernancePolicy(taxLevel = TaxLevel.HIGH))
        assertTrue(high.taxIncome() > normal.taxIncome())
        val season = GovernanceEngine.season(high)
        assertTrue(season.moraleDelta < 0)
        assertTrue(GovernanceEngine.season(normal).moraleDelta >= 0)
    }

    @Test
    fun `치안이 낮고 세율 징병이 높으면 반란 위험이 커진다`() {
        val safe = territory(policy = GovernancePolicy(security = 90))
        val risky = territory(
            policy = GovernancePolicy(
                taxLevel = TaxLevel.HIGH,
                levyQuota = LevyQuota.HIGH,
                security = 10
            )
        )
        assertTrue(risky.policy.rebellionRisk > safe.policy.rebellionRisk)
        assertTrue(risky.policy.rebellionRisk >= 60)
        assertTrue(GovernanceEngine.season(risky).rebellion)
        assertFalse(GovernanceEngine.season(safe).rebellion)
    }

    @Test
    fun `순찰은 자금을 써서 치안을 올리고 반란 위험을 낮춘다`() {
        val policy = GovernancePolicy(security = 30)
        val poor = GovernanceEngine.patrol(policy, gold = 50)
        assertFalse(poor.success)
        val done = GovernanceEngine.patrol(policy, gold = 1_000)
        assertTrue(done.success)
        assertEquals(-GovernanceEngine.PATROL_COST, done.goldDelta)
        assertTrue(done.policy.security > policy.security)
        assertTrue(done.policy.rebellionRisk < policy.rebellionRisk)
    }

    @Test
    fun `지역 시설은 한 번만 지을 수 있고 효과가 세수와 징병에 붙는다`() {
        val policy = GovernancePolicy()
        val office = GovernanceEngine.invest(policy, TerritoryFacility.OFFICE.id, gold = 5_000)
        assertTrue(office.success)
        assertTrue(office.policy.facilities.contains(TerritoryFacility.OFFICE))
        val again = GovernanceEngine.invest(office.policy, TerritoryFacility.OFFICE.id, gold = 5_000)
        assertFalse(again.success)

        val base = territory()
        val built = territory(policy = office.policy)
        assertTrue(built.taxIncome() > base.taxIncome())

        val barracks = GovernanceEngine.invest(policy, TerritoryFacility.BARRACKS.id, gold = 5_000)
        val withBarracks = territory(policy = barracks.policy)
        assertTrue(withBarracks.leviesPerSeason() > base.leviesPerSeason())
    }

    @Test
    fun `속국 조공률과 지원군 차출을 조절한다`() {
        val policy = GovernancePolicy(tributeRate = 30)
        val lower = GovernanceEngine.setTributeRate(policy, 50)
        val rich = territory(status = TerritoryStatus.VASSAL, policy = lower.policy)
        val plain = territory(status = TerritoryStatus.VASSAL, policy = policy)
        assertTrue(rich.taxIncome() > plain.taxIncome())

        val support = GovernanceEngine.callSupport(policy, garrison = 400)
        assertTrue(support.success)
        assertTrue(support.armyTroopDelta > 0)
        assertEquals(-support.armyTroopDelta, support.territoryTroopDelta)
        assertEquals(support.armyTroopDelta, support.policy.supportTroops)
        // 주둔 병력이 적으면 차출할 수 없다
        assertFalse(GovernanceEngine.callSupport(policy, garrison = 10).success)
    }

    @Test
    fun `뷰모델에서도 세율과 시설 투자가 영지에 반영된다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val code = vm.uiState.value.territories.first().code
        val before = vm.uiState.value.territories.first()

        vm.setTerritoryTax(code, TaxLevel.HIGH)
        val taxed = vm.uiState.value.territories.first()
        assertEquals(TaxLevel.HIGH, taxed.policy.taxLevel)
        assertTrue(taxed.taxIncome() > before.taxIncome())

        vm.investTerritoryFacility(code, TerritoryFacility.GUILD.id)
        val built = vm.uiState.value.territories.first()
        assertTrue(built.policy.facilities.contains(TerritoryFacility.GUILD))
    }

    // ── ③ 의회 전쟁 안건 ────────────────────────────────────────

    @Test
    fun `의회에서 상위 단위 전쟁 안건을 상정하고 표결해 명분을 얻는다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        vm.sponsorWarAgenda(WarTargetKind.SUPERIOR)
        val agenda = vm.uiState.value.warAgendas.singleOrNull()
        assertNotNull(agenda)
        assertTrue(agenda!!.pending)

        vm.voteWarAgenda(agenda.id)
        val voted = vm.uiState.value.warAgendas.single()
        assertEquals(true, voted.passed)
        assertTrue(voted.active)
        val cause = voted.casusBelli
        assertTrue(cause.combatBonusPercent > 0)
        assertTrue(cause.casualtyReductionPercent > 0)
        assertTrue(cause.lootBonusPercent > 0)
    }

    @Test
    fun `가결된 명분은 그 지역 원정 한 번에 쓰고 소모된다`() {
        val agenda = WarAgendaEngine.vote(
            WarAgendaEngine.sponsor(
                kind = WarTargetKind.ADJACENT,
                regionCode = "asan_onyang2",
                regionName = "온양2동",
                targetName = "온양2동 리더 연합",
                seasonTurn = 3,
                supporters = 3
            ),
            opposingVotes = 1,
            ourVotes = 4
        )
        assertEquals(true, agenda.passed)
        val list = listOf(agenda)
        assertTrue(WarAgendaEngine.casusBelliFor(list, "asan_onyang2").combatBonusPercent > 0)
        val consumed = WarAgendaEngine.consume(list, "asan_onyang2")
        assertTrue(ConsumedCheck.isEmpty(WarAgendaEngine.casusBelliFor(consumed, "asan_onyang2")))
    }

    private object ConsumedCheck {
        fun isEmpty(c: CasusBelli): Boolean = c.isEmpty
    }

    @Test
    fun `같은 대상의 전쟁 안건은 겹쳐 상정되지 않는다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        vm.sponsorWarAgenda(WarTargetKind.SUPERIOR)
        vm.sponsorWarAgenda(WarTargetKind.SUPERIOR)
        assertEquals(1, vm.uiState.value.warAgendas.size)
    }

    // ── ④ 행정 단위 4단계 ───────────────────────────────────────

    @Test
    fun `행정 단위가 올라갈수록 관리 메뉴가 늘어난다`() {
        val counts = AdminTier.entries.map { AdminTierEngine.menusOf(it).size }
        assertEquals(counts.sorted(), counts)
        assertTrue(AdminTierEngine.menusOf(AdminTier.DONG).isNotEmpty())
        assertTrue(AdminTierEngine.menusOf(AdminTier.STATE).size > AdminTierEngine.menusOf(AdminTier.PROVINCE).size)
        // 단계별 새 메뉴가 실제로 있다
        AdminTier.entries.filterNot { it.isFinal }.forEach { tier ->
            assertTrue("${tier.label} 새 메뉴 없음", AdminTierEngine.newlyOpenedAt(tier.next!!).isNotEmpty())
        }
    }

    @Test
    fun `상위 행정 단위 메뉴는 하위 단위에서 잠겨 있다`() {
        val ctx = com.game.slg.core.region.AdminContext(gold = 99_000, troopCap = 5_000, troops = 1_000)
        val locked = AdminTierEngine.act(AdminMenu.MOBILIZATION, AdminTier.DONG, ctx)
        assertFalse(locked.success)
        val opened = AdminTierEngine.act(AdminMenu.MOBILIZATION, AdminTier.STATE, ctx)
        assertTrue(opened.success)
        assertTrue(opened.troopDelta > 0)
    }

    @Test
    fun `행정 승격은 조건을 채워야 하고 승격하면 새 메뉴가 열린다`() {
        val poor = AdminTierEngine.promote(
            current = AdminTier.DONG,
            territories = 1,
            allied = 0,
            prestige = 10,
            authority = 0,
            gold = 100
        )
        assertFalse(poor.success)

        val rich = AdminTierEngine.promote(
            current = AdminTier.DONG,
            territories = 3,
            allied = 4,
            prestige = 70,
            authority = 12,
            gold = 5_000
        )
        assertTrue(rich.success)
        assertEquals(AdminTier.CITY, rich.tier)
        assertTrue(rich.newlyOpened.isNotEmpty())
        assertTrue(rich.newlyOpened.all { it.tier == AdminTier.CITY })
    }

    @Test
    fun `뷰모델 행정 화면은 기본 읍면동 메뉴를 열어 준다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val state = vm.uiState.value
        assertEquals(AdminTier.DONG, state.adminTier)
        assertEquals(AdminTierEngine.menusOf(AdminTier.DONG).size, state.adminMenus.size)
        assertTrue(state.nextAdminMenus.isNotEmpty())
        // 조건이 모자라면 승격되지 않는다
        vm.promoteAdminTier()
        assertEquals(AdminTier.DONG, vm.uiState.value.adminTier)
        // 행정 탭이 영지 화면에 있다
        vm.openTab(MainTab.DOMAIN, DomainTab.ADMIN.ordinal)
        assertEquals(DomainTab.ADMIN.ordinal, vm.navigation.value.sub)
    }

    @Test
    fun `통합 세제는 전 영지 세율을 한 단계 올린다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        // 자금을 채우고 시군구 단계로 직접 올린 뒤 집행한다
        val state = vm.uiState.value
        val result = AdminTierEngine.act(
            AdminMenu.UNIFIED_TAX,
            AdminTier.CITY,
            state.adminContext()
        )
        assertTrue(result.success)
        assertEquals(1, result.taxStep)
        assertTrue(result.goldDelta < 0)
    }
}
