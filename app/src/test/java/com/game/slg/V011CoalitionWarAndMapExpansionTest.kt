package com.game.slg

import com.game.slg.core.empire.ManagedTerritory
import com.game.slg.core.empire.TerritoryStatus
import com.game.slg.core.hero.Hero
import com.game.slg.core.hero.HeroDispositionEngine
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroRosterState
import com.game.slg.core.hero.HeroStage
import com.game.slg.core.hero.PendingDisposition
import com.game.slg.core.map.MapExpansionEngine
import com.game.slg.core.map.MapViewport
import com.game.slg.core.map.TerritoryMapLayout
import com.game.slg.core.region.AdjacentActionKind
import com.game.slg.core.region.CasusBelli
import com.game.slg.core.region.CoalitionMuster
import com.game.slg.core.region.CoalitionWarEngine
import com.game.slg.core.region.ConquestRecord
import com.game.slg.core.region.RegionCatalog
import com.game.slg.core.region.WarAgenda
import com.game.slg.core.region.WarAgendaEngine
import com.game.slg.core.region.WarTargetKind
import com.game.slg.ui.SlgGameState
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.11.0 — ① 리더 연합 출진 ② 통치 비례 지도 확장 ③ 점령 영웅 자동 귀속.
 *
 * 사용자 요청(2026-11-27)을 한 번에 반영한 3가지 시스템을 순수 엔진과 뷰모델 양쪽에서 검증한다.
 */
class V011CoalitionWarAndMapExpansionTest {

    private val tangjeong = RegionCatalog.defaultRegion()

    private fun playingVm(): SlgGameViewModel {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        return vm
    }

    private fun territory(code: String, status: TerritoryStatus = TerritoryStatus.DIRECT) =
        ManagedTerritory(
            code = "$code:ai_test",
            name = "시험 영지",
            population = 8_000,
            status = status
        )

    // ── ① 리더 연합 출진 엔진 ────────────────────────────────────

    @Test
    fun `편입 리더는 자기 병력의 35퍼센트를 내고 최대 6명까지 합류한다`() {
        val leaders = (1..8).map { index -> "리더$index" to index * 1_000 }
        val musters = CoalitionWarEngine.mustersOf(leaders)

        assertEquals(CoalitionWarEngine.MAX_MUSTER_LEADERS, musters.size)
        // 병력이 많은 리더부터
        assertEquals(listOf("리더8", "리더7", "리더6", "리더5", "리더4", "리더3"), musters.map { it.leaderName })
        musters.forEach { muster ->
            val source = leaders.first { it.first == muster.leaderName }.second
            assertEquals(source * 35 / 100, muster.troops)
        }
        // 병력이 없는 리더는 합류하지 않는다
        assertTrue(CoalitionWarEngine.mustersOf(listOf("빈 리더" to 0)).isEmpty())
    }

    @Test
    fun `사상자는 출진 병력 비율대로 나뉘고 합은 전체 사상자와 같다`() {
        val musters = listOf(
            CoalitionMuster("가 리더", 300),
            CoalitionMuster("나 리더", 200),
            CoalitionMuster("다 리더", 100)
        )
        val (mine, perLeader) = CoalitionWarEngine.splitLosses(losses = 600, ownTroops = 600, musters = musters)

        assertEquals(300, mine)
        assertEquals(listOf(150, 100, 50), perLeader)
        assertEquals(600, mine + perLeader.sum())

        // 합류 리더가 없으면 내가 전부 부담한다
        val solo = CoalitionWarEngine.splitLosses(120, 800, emptyList())
        assertEquals(120, solo.first)
        assertTrue(solo.second.isEmpty())
    }

    @Test
    fun `출진 계획은 가결 명분과 병력 합산을 함께 보여 준다`() {
        val agenda = WarAgendaEngine.vote(
            WarAgendaEngine.sponsor(
                kind = WarTargetKind.ADJACENT,
                regionCode = "4420034000",
                regionName = "배방읍",
                targetName = "배방읍 리더 연합",
                seasonTurn = 2,
                supporters = 3
            ),
            opposingVotes = 1,
            ourVotes = 4
        )
        val plan = CoalitionWarEngine.plan(
            agenda = agenda,
            ownTroops = 1_000,
            leaders = listOf("김 리더" to 1_000, "박 리더" to 400),
            enemyName = "최 리더",
            enemyTroops = 900
        )

        assertEquals(1_000, plan.ownTroops)
        assertEquals(350 + 140, plan.coalitionTroops)
        assertEquals(1_490, plan.totalTroops)
        assertEquals(2, plan.leaderCount)
        assertFalse(plan.casusBelli.isEmpty)
        assertTrue(plan.headline.contains("연합 병력"))
        val lines = plan.breakdown().joinToString("\n")
        assertTrue(lines.contains("배방읍") || lines.contains("적"))
        assertTrue("명분 줄이 있어야 한다", lines.contains("명분"))
    }

    // ── ① 뷰모델 연결 ─────────────────────────────────────────

    @Test
    fun `가결된 안건으로 연합 출진을 준비하고 실행하면 명분이 소모된다`() {
        val vm = playingVm()
        vm.sponsorWarAgenda(WarTargetKind.SUPERIOR)
        val agenda = vm.uiState.value.warAgendas.single()
        vm.voteWarAgenda(agenda.id)
        assertTrue(vm.uiState.value.warAgendas.single().active)

        vm.prepareCoalitionWar(agenda.id)
        val plan = vm.uiState.value.coalitionWarPlan
        assertNotNull("출진 확인 계획이 만들어져야 한다", plan)
        assertEquals(agenda.id, plan!!.agendaId)
        assertTrue(plan.ownTroops > 0)
        assertFalse(plan.casusBelli.isEmpty)

        val troopsBefore = vm.uiState.value.troops
        vm.launchCoalitionWar()

        val state = vm.uiState.value
        assertNull("출진 뒤 확인 카드는 닫힌다", state.coalitionWarPlan)
        val after = state.warAgendas.single()
        assertTrue("가결 명분은 출진 1회로 소모된다", after.consumed)
        assertFalse(after.active)
        assertNotNull("출진 결과 보고가 남는다", state.lastReport)
        assertTrue(state.warAgendaMessage?.contains("연합 출진") == true)
        assertTrue("아군 사상자가 병력에 반영된다", state.troops <= troopsBefore)
    }

    @Test
    fun `표결 전 안건은 출진할 수 없다`() {
        val vm = playingVm()
        vm.sponsorWarAgenda(WarTargetKind.SUPERIOR)
        val agenda = vm.uiState.value.warAgendas.single()

        vm.prepareCoalitionWar(agenda.id)

        assertNull(vm.uiState.value.coalitionWarPlan)
        assertTrue(vm.uiState.value.statusMessage.contains("가결"))
        // 실행 시도도 막힌다
        vm.launchCoalitionWar()
        assertEquals(false, vm.uiState.value.warAgendas.single().consumed)
    }

    // ── ② 지도 범위 자동 확장 ──────────────────────────────────

    @Test
    fun `통치 영지 수가 늘면 작전 반경이 단계적으로 넓어진다`() {
        assertEquals(0, MapExpansionEngine.territoryBonusHop(2))
        assertEquals(1, MapExpansionEngine.territoryBonusHop(3))
        assertEquals(2, MapExpansionEngine.territoryBonusHop(6))
        assertEquals(3, MapExpansionEngine.territoryBonusHop(12))

        assertEquals(0, MapExpansionEngine.effectiveUnlockedHop(0, 1, 0))
        assertEquals(1, MapExpansionEngine.effectiveUnlockedHop(0, 3, 0))
        assertEquals(2, MapExpansionEngine.effectiveUnlockedHop(0, 6, 0))
        assertEquals(3, MapExpansionEngine.effectiveUnlockedHop(0, 12, 0))
        // 지휘 레벨이 이미 넓으면 줄지 않는다
        assertEquals(4, MapExpansionEngine.effectiveUnlockedHop(4, 1, 0))
        // 편입 영지가 더 멀면 그 거리를 따라간다
        assertEquals(3, MapExpansionEngine.effectiveUnlockedHop(1, 1, 3))
        // 상한
        assertEquals(
            MapExpansionEngine.MAX_EFFECTIVE_HOP,
            MapExpansionEngine.effectiveUnlockedHop(5, 20, 20)
        )
    }

    @Test
    fun `상태의 작전 반경은 지휘 레벨과 통치 영지를 함께 본다`() {
        val six = SlgGameState(
            regionCode = tangjeong.code,
            unlockedHop = 0,
            territories = listOf(
                territory(tangjeong.code),
                territory("4420034000"),
                territory("4420035000"),
                territory("4420036000"),
                territory("4420051000"),
                territory("4420052000")
            )
        )
        // 영지 6곳 → +2hop
        assertEquals(2, six.effectiveUnlockedHop)

        val far = SlgGameState(
            regionCode = tangjeong.code,
            unlockedHop = 0,
            territories = listOf(territory(tangjeong.code)),
            conquests = listOf(
                ConquestRecord(
                    leaderId = "ai_far",
                    leaderName = "먼 리더",
                    regionCode = "4420051000", // 온양1동 = hop 2
                    regionName = "온양1동",
                    status = TerritoryStatus.VASSAL,
                    population = 9_000
                )
            )
        )
        assertEquals("편입 영지의 거리까지 열린다", 2, far.effectiveUnlockedHop)
        assertEquals("자동 확장 배지", true, far.mapExpansionLabel != null)
    }

    @Test
    fun `미리보기 고리와 세계 경계는 해금 범위를 따라 넓어진다`() {
        assertEquals(2, TerritoryMapLayout.previewHopOf(1))
        assertEquals(6, TerritoryMapLayout.previewHopOf(5))
        assertEquals(TerritoryMapLayout.MAX_PREVIEW_HOP, TerritoryMapLayout.previewHopOf(7))
        assertTrue(
            "가장 바깥 고리도 기본 지도 경계 안에 있어야 한다",
            TerritoryMapLayout.ringRadius(6) < TerritoryMapLayout.WORLD_HALF_EXTENT
        )
        assertTrue(
            "통치 범위가 넓어지면 세계 경계가 넓어진다",
            TerritoryMapLayout.worldHalfExtentFor(6) > TerritoryMapLayout.WORLD_HALF_EXTENT
        )
        assertEquals(
            TerritoryMapLayout.WORLD_HALF_EXTENT,
            TerritoryMapLayout.worldHalfExtentFor(1)
        )
    }

    @Test
    fun `지도는 넓어진 경계까지 이동하고 새 고리가 화면에 들어오게 배율을 보정한다`() {
        val base = MapViewport()
        val widened = base.withWorldExtent(1.4f)

        assertTrue(widened.panLimit > base.panLimit)
        assertEquals(1.4f, widened.worldHalfExtent, 0.0001f)
        assertEquals("좁아지지 않는다", base, base.withWorldExtent(0.2f))

        // 바깥 고리를 보여 주려면 기본 배율에서 조금 멀어진다
        val fitted = base.fitPreview(4)
        assertTrue(fitted.scale < base.scale)
        assertTrue("너무 멀어지지는 않는다", fitted.scale >= TerritoryMapLayout.MIN_SCALE)
        // 안쪽 고리만 있으면 배율을 건드리지 않는다
        assertEquals(base, base.fitPreview(1))
    }

    @Test
    fun `영지를 편입하면 지도 노드의 해금 범위가 실제로 넓어진다`() {
        val vm = playingVm()
        repeat(10) { vm.advanceSeason() }
        val hopBefore = vm.uiState.value.effectiveUnlockedHop
        val adjacent = vm.uiState.value.mapNodes.firstOrNull { it.hopDistance == 1 && it.unlocked }
        assertNotNull(adjacent)

        vm.selectTerritory(adjacent!!.code)
        vm.openAdjacentAction(AdjacentActionKind.WAR)
        val target = vm.uiState.value.selectedAdjacentPanel?.warTargets?.firstOrNull()
        assertNotNull(target)
        repeat(60) {
            if (vm.uiState.value.pendingConquest == null) vm.challengeAdjacentLeader(target!!.id)
        }
        assertNotNull("편입 제안이 열려야 한다", vm.uiState.value.pendingConquest)
        vm.resolveConquest(TerritoryStatus.DIRECT)

        val state = vm.uiState.value
        assertEquals("영지가 늘었다", 2, state.territories.size)
        assertTrue(
            "편입 지역(hop 1)이 작전 범위에 들어온다",
            state.effectiveUnlockedHop >= maxOf(hopBefore, 1)
        )
        assertTrue("지도 노드가 새 범위로 다시 그려진다", state.mapNodes.any { it.hopDistance >= 2 })
    }

    // ── ③ 점령 영웅 자동 귀속 ──────────────────────────────────

    @Test
    fun `점령하면 소속 영웅 1명이 등용비 없이 우리 로스터로 귀속된다`() {
        val captured = HeroDispositionEngine.heroesForLord("ai_lord_x")
            .map { PendingDisposition(it, "ai_lord_x", "김 리더") }
        assertTrue(captured.isNotEmpty())
        val marked = HeroDispositionEngine.markCaptured(HeroRosterState.initial(), captured)

        val outcome = HeroDispositionEngine.autoHireOnConquest(marked, captured)

        assertEquals(HeroDispositionEngine.AUTO_HIRE_PER_CONQUEST, outcome.hired.size)
        val hired = outcome.hired.single()
        assertEquals(HeroStage.HIRED, outcome.roster.recordOf(hired.id).stage)
        assertNull("소속 리더에서 떨어진다", outcome.roster.recordOf(hired.id).allegianceLordId)
        assertTrue("나머지는 처분 대기로 남는다", outcome.pending.size == captured.size - 1)
        assertTrue(outcome.pending.none { it.heroId == hired.id })
        assertTrue(outcome.describe().contains("자동 귀속"))
        assertTrue(outcome.describe().contains("0G"))
    }

    @Test
    fun `빈 역할 슬롯이 있으면 즉시 배치된다`() {
        // 시작 영웅 3명이 3개 슬롯을 차지한 상태 — 비어 있는 슬롯이 없을 수 있다
        val roster = HeroRosterState.initial()
        val captured = HeroDispositionEngine.heroesForLord("ai_slot_check")
            .map { PendingDisposition(it, "ai_slot_check", "박 리더") }
        val marked = HeroDispositionEngine.markCaptured(roster, captured)
        val outcome = HeroDispositionEngine.autoHireOnConquest(marked, captured)
        val hired = outcome.hired.single()
        val occupiedRoles = roster.ownedHeroes.mapNotNull { roster.recordOf(it.id).assignedRole }
        val placedRole = outcome.roster.recordOf(hired.id).assignedRole

        if (hired.role !in occupiedRoles) {
            assertEquals("빈 슬롯이면 즉시 배치", hired.role, placedRole)
        } else {
            assertNull("슬롯이 차 있으면 대기", placedRole)
        }
    }

    @Test
    fun `이미 보유한 영웅은 다시 자동 귀속하지 않는다`() {
        val owned: Hero = HeroRoster.byId(HeroRosterState.STARTING_IDS.first())!!
        val outcome = HeroDispositionEngine.autoHireOnConquest(
            HeroRosterState.initial(),
            listOf(PendingDisposition(owned.id, "ai_lord_x", "김 리더"))
        )

        assertTrue(outcome.hired.isEmpty())
        assertEquals(1, outcome.pending.size)
        assertTrue(outcome.describe().isEmpty())
    }

    @Test
    fun `자동 귀속된 영웅은 저장했다 다시 열어도 우리 로스터에 남는다`() {
        val store = com.game.slg.core.save.InMemoryCampaignStore()
        val vm = SlgGameViewModel(store = store)
        vm.chooseLord(vm.uiState.value.lords.first().id)
        repeat(10) { vm.advanceSeason() }
        val adjacent = vm.uiState.value.mapNodes.firstOrNull { it.hopDistance == 1 && it.unlocked }!!
        vm.selectTerritory(adjacent.code)
        vm.openAdjacentAction(AdjacentActionKind.WAR)
        val target = vm.uiState.value.selectedAdjacentPanel?.warTargets?.firstOrNull()!!
        repeat(60) {
            if (vm.uiState.value.pendingConquest == null) vm.challengeAdjacentLeader(target.id)
        }
        val offer = vm.uiState.value.pendingConquest!!
        val ownedBefore = vm.uiState.value.heroes.ownedHeroes.map { it.id }.toSet()
        vm.resolveConquest(TerritoryStatus.DIRECT)

        val capturedIds = HeroDispositionEngine.heroesForLord(offer.leaderId)
            .filterNot { it in ownedBefore }
        val state = vm.uiState.value
        val newlyOwned = state.heroes.ownedHeroes.map { it.id }.filter { it in capturedIds }
        assertEquals("1명이 자동 귀속된다", 1, newlyOwned.size)
        assertTrue(state.statusMessage.contains("자동 귀속"))
        // 처분 대기 + 자동 귀속 = 원래 소속 영웅 수
        assertEquals(capturedIds.size, state.pendingDisposition.size + newlyOwned.size)

        val restored = SlgGameViewModel(store = store).uiState.value
        assertTrue(restored.heroes.ownedHeroes.any { it.id == newlyOwned.first() })
    }

    // ── 참조 무결성 ────────────────────────────────────────────

    @Test
    fun `출진 안건과 명분 소모는 안건 목록에 그대로 남는다`() {
        val agenda = WarAgendaEngine.sponsor(
            kind = WarTargetKind.SUPERIOR,
            regionCode = "superior:충청남도 아산시",
            regionName = "충청남도 아산시 연합",
            targetName = "충청남도 아산시 연합 의회",
            seasonTurn = 1,
            supporters = 2
        )
        val voted = WarAgendaEngine.vote(agenda, opposingVotes = 1, ourVotes = 3)
        val consumed = WarAgendaEngine.consume(listOf(voted), voted.regionCode).single()

        assertEquals(true, consumed.passed)
        assertTrue(consumed.consumed)
        assertFalse(consumed.active)
        assertEquals(CasusBelli.NONE, consumed.casusBelli)
        assertEquals("명분 소모", consumed.statusLabel)
        // 상위 단위 안건도 왕복 저장된다
        val decoded: WarAgenda? = com.game.slg.core.region.WarAgendaCodec.decode(consumed.encode())
        assertNotNull(decoded)
        assertEquals(consumed.id, decoded!!.id)
        assertEquals(consumed.kind, decoded.kind)
        assertEquals(consumed.regionCode, decoded.regionCode)
        assertEquals(true, decoded.passed)
        assertTrue(decoded.consumed)
    }
}
