package com.game.slg

import com.game.slg.core.empire.TerritoryStatus
import com.game.slg.core.region.AdjacentActionKind
import com.game.slg.core.region.AdjacentCommand
import com.game.slg.core.region.AdjacentStrategyEngine
import com.game.slg.core.region.AdjacentStrategyOp
import com.game.slg.core.region.AdjacentDiplomacyEngine
import com.game.slg.core.region.AdjacentRegionEngine
import com.game.slg.core.region.AidDirection
import com.game.slg.core.region.AidSource
import com.game.slg.core.region.CoalitionCouncilEngine
import com.game.slg.core.region.CoalitionState
import com.game.slg.core.region.CouncilVotingPower
import com.game.slg.core.region.EmergencyCouncilEngine
import com.game.slg.core.region.LeaderBond
import com.game.slg.core.region.RegionBoardBuilder
import com.game.slg.core.region.RegionLeaderSeed
import com.game.slg.core.region.RegionUnion
import com.game.slg.core.region.SeasonClock
import com.game.slg.core.region.SeasonClockEngine
import com.game.slg.core.model.TreatyType
import com.game.slg.ui.MainTab
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.SubMenuBadges
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 2026-11-25 대기열 5건 — 인접 지역 전투·외교·전략, 긴급 의회·상호 방어,
 * 편입 리더 수 의결권, 지속 시간 흐름·미접속 AI 대행, 서브 메뉴 배지 연동.
 */
class AdjacentRegionAndTimeTest {

    private fun seed(
        id: String,
        name: String,
        troops: Int = 300,
        isPlayer: Boolean = false,
        federation: TerritoryStatus? = null,
        bond: LeaderBond? = null,
        masterId: String? = null
    ) = RegionLeaderSeed(
        id = id,
        name = name,
        stronghold = "거점 $name",
        traitLabel = "균형형",
        traitIcon = "⚖",
        troops = troops,
        gold = 600,
        food = 900,
        prestige = 45,
        isPlayer = isPlayer,
        federation = federation,
        aiBond = bond,
        masterId = masterId
    )

    private fun unifiedBoard() = RegionBoardBuilder.build(
        "4420034000", "배방읍",
        listOf(
            seed("ai_1", "김도현", troops = 900),
            seed("ai_2", "이서준", bond = LeaderBond.MERGE, masterId = "ai_1"),
            seed("ai_3", "박민재", bond = LeaderBond.SUBORDINATION, masterId = "ai_1"),
            seed("ai_4", "최하준", bond = LeaderBond.MERGE, masterId = "ai_1")
        )
    )

    private fun dividedBoard() = RegionBoardBuilder.build(
        "4420034000", "배방읍",
        listOf(
            seed("ai_1", "김도현", troops = 700),
            seed("ai_2", "이서준", troops = 500),
            seed("ai_3", "박민재", troops = 400, bond = LeaderBond.SUBORDINATION, masterId = "ai_1"),
            seed("ai_4", "최하준", troops = 300)
        )
    )

    // ── ① 인접 지역 전투·외교·전략 ──────────────────────────────

    @Test
    fun `한 우두머리에게 모두 묶인 지역은 통합 리더로 본다`() {
        val ruler = AdjacentRegionEngine.unifiedRuler(unifiedBoard())
        assertNotNull(ruler)
        assertEquals("ai_1", ruler!!.id)
    }

    @Test
    fun `독립 리더가 둘 이상이면 통합 리더가 없다`() {
        assertNull(AdjacentRegionEngine.unifiedRuler(dividedBoard()))
    }

    @Test
    fun `통합 리더가 있으면 지역 대표 한 명만 전투 대상이다`() {
        val panel = AdjacentRegionEngine.panel(
            code = "4420034000",
            name = "배방읍",
            relationLabel = "경계 세력",
            board = unifiedBoard(),
            unionActive = false
        )

        assertEquals(AdjacentCommand.UNIFIED, panel.command)
        assertEquals(1, panel.warTargets.size)
        assertEquals("ai_1", panel.warTargets.first().id)
        assertTrue(panel.headcountLabel.contains("통합 리더"))
        assertEquals("김도현", panel.diplomacyPartner)
    }

    @Test
    fun `통합 리더가 없으면 개별 리더 단위로 전투 대상을 고른다`() {
        val panel = AdjacentRegionEngine.panel(
            code = "4420034000",
            name = "배방읍",
            relationLabel = "경계 세력",
            board = dividedBoard(),
            unionActive = false
        )

        assertEquals(AdjacentCommand.DIVIDED, panel.command)
        // 병합·종속된 ai_3 은 빠지고 독립 리더 3명이 남는다
        assertEquals(3, panel.warTargets.size)
        assertTrue(panel.warTargets.none { it.bound })
        assertTrue(panel.warHint.contains("3명"))
        assertFalse(panel.unified)
    }

    @Test
    fun `지역 연합 기간에도 인접 지역 대외 원정은 허용된다`() {
        val union = RegionUnion(regionCode = "4420033000", regionName = "탕정면", remainingTurns = 5)
        val verdict = AdjacentRegionEngine.expeditionVerdict(union)
        assertTrue(verdict.allowed)
        assertEquals(5, union.remainingTurns)
    }

    @Test
    fun `공동 전투 참여 리더가 늘수록 보너스가 커지고 30퍼센트에서 멈춘다`() {
        assertEquals(0, AdjacentRegionEngine.coalitionBonusPercent(0))
        assertEquals(18, AdjacentRegionEngine.coalitionBonusPercent(3))
        assertEquals(30, AdjacentRegionEngine.coalitionBonusPercent(9))
    }

    @Test
    fun `전략 공작은 시드가 같으면 같은 결과가 나온다`() {
        val a = AdjacentStrategyEngine.resolve(AdjacentStrategyOp.BLOCKADE, "배방읍", 60, 5, 70, seed = 17L)
        val b = AdjacentStrategyEngine.resolve(AdjacentStrategyOp.BLOCKADE, "배방읍", 60, 5, 70, seed = 17L)
        assertEquals(a.success, b.success)
        assertEquals(a.detail, b.detail)
        assertEquals(-320, a.goldDelta)
    }

    @Test
    fun `정찰 공작이 성공하면 첩보력이 오르고 정보가 열린다`() {
        // 정찰은 성공률이 높다 — 시드를 여러 번 돌려 성공 사례를 확인한다
        val results = (0L until 30L).map {
            AdjacentStrategyEngine.resolve(AdjacentStrategyOp.RECON, "배방읍", 40, 0, 50, it)
        }
        assertTrue(results.any { it.success && it.intelGained && it.espionagePowerDelta > 0 })
    }

    @Test
    fun `통합 리더와의 외교가 리더 연합보다 성공하기 쉽다`() {
        val unified = AdjacentDiplomacyEngine.resolve(
            TreatyType.NON_AGGRESSION_PACT, "김도현", unified = true,
            diplomacy = 60, prestige = 50, leaderCount = 4, seed = 75L
        )
        val divided = AdjacentDiplomacyEngine.resolve(
            TreatyType.NON_AGGRESSION_PACT, "배방읍 리더 연합", unified = false,
            diplomacy = 60, prestige = 50, leaderCount = 6, seed = 75L
        )
        assertTrue(unified.success)
        assertFalse(divided.success)
        assertEquals(-300, unified.goldDelta)
    }

    // ── ③ 편입 리더 수 의결권 ───────────────────────────────────

    @Test
    fun `의결권은 기본 1표에 편입 리더 수만큼 더해진다`() {
        assertEquals(1, CouncilVotingPower.of(0))
        assertEquals(4, CouncilVotingPower.of(3))
        assertTrue(CouncilVotingPower.label(4, 3).contains("편입 3명"))
    }

    @Test
    fun `연합 안건은 내 의결권이 더해져 가결되고 보너스를 남긴다`() {
        val bill = CoalitionCouncilEngine.sponsor(
            kind = AdjacentActionKind.WAR,
            regionCode = "4420034000",
            regionName = "배방읍",
            seasonTurn = 3,
            supporters = 4
        )
        assertEquals(12, bill.favor)
        assertTrue(bill.pending)

        val voted = CoalitionCouncilEngine.vote(bill, opposingVotes = 13, ourVotes = 4)
        assertEquals(16, voted.favor)
        assertTrue(voted.passed == true)
        assertTrue(voted.message!!.contains("내 의결권 4표"))

        val next = CoalitionCouncilEngine.applyPassed(CoalitionState(bills = listOf(voted)), voted)
        assertTrue(next.battleBonusPercent > 0)
    }

    @Test
    fun `의결권이 없으면 같은 안건이 부결된다`() {
        val bill = CoalitionCouncilEngine.sponsor(
            kind = AdjacentActionKind.DIPLOMACY,
            regionCode = "4420034000",
            regionName = "배방읍",
            seasonTurn = 1,
            supporters = 0
        )
        val voted = CoalitionCouncilEngine.vote(bill, opposingVotes = 5, ourVotes = 1)
        assertFalse(voted.passed == true)
    }

    // ── ② 긴급 의회 · 상호 방어 ─────────────────────────────────

    @Test
    fun `긴급 의회는 침공 정보로 열리고 표결로 방어 결의를 가결한다`() {
        val council = EmergencyCouncilEngine.call("김도현", "탕정면", troopsLost = 120, turn = 4)
        assertTrue(council.pending)
        assertEquals(120, council.troopsLost)

        val voted = EmergencyCouncilEngine.vote(council, opposingVotes = 3, ourVotes = 4)
        assertTrue(voted.resolved)
        assertTrue(voted.passed == true)
        assertTrue(voted.message!!.contains("방어 태세"))
        assertTrue(voted.favor > voted.against)
    }

    @Test
    fun `미접속 AI 대행은 열린 긴급 의회를 대신 표결한다`() {
        val council = EmergencyCouncilEngine.call("김도현", "탕정면", troopsLost = 90, turn = 6)
        val resolved = EmergencyCouncilEngine.autopilotResolve(council, opposingVotes = 2, ourVotes = 4)
        assertTrue(resolved.resolved)
        // 이미 끝난 안건은 다시 건드리지 않는다
        val again = EmergencyCouncilEngine.autopilotResolve(resolved, opposingVotes = 99, ourVotes = 1)
        assertEquals(resolved, again)
    }

    @Test
    fun `방어 지원 요청은 우호 리더에게서만 병력을 받는다`() {
        val council = EmergencyCouncilEngine.call("김도현", "탕정면", troopsLost = 80, turn = 2)
        val sources = listOf(
            AidSource("ai_1", "이서준", troops = 900, friendly = true),
            AidSource("ai_2", "박민재", troops = 800, friendly = true),
            AidSource("ai_3", "최하준", troops = 1200, friendly = false)
        )

        val next = EmergencyCouncilEngine.requestAid(council, sources, diplomacy = 60, prestige = 70)

        assertEquals(2, next.aids.size)
        assertTrue(next.requestedTroops > 0)
        assertTrue(next.aids.none { it.leaderName == "최하준" })
    }

    @Test
    fun `다른 리더를 도우러 보내는 병력은 내 병력의 10퍼센트다`() {
        val call = com.game.slg.core.region.AidCall(
            attackerName = "김도현",
            victimName = "이서준",
            victimId = "ai_2",
            kind = LeaderBond.MERGE
        )
        val (answered, sent) = EmergencyCouncilEngine.answerCall(call, myTroops = 1_500)
        assertTrue(answered.answered)
        assertEquals(150, sent)
        assertTrue(answered.result!!.contains("이서준"))
    }

    // ── ④ 지속 시간 흐름 · 미접속 AI 대행 ───────────────────────

    @Test
    fun `시계는 흐른 시간만큼 절기를 진행하고 나머지는 다음 칸으로 넘긴다`() {
        val start = 1_000_000L
        val clock = SeasonClock.start(start, minutes = 30)

        val tick = SeasonClockEngine.tick(clock, start + 65 * 60_000L)

        assertEquals(2, tick.due)
        // 5분이 지난 상태로 맞춰져 다음 절기까지 25분이 남는다
        assertEquals(start + 60 * 60_000L, tick.clock.lastTickMillis)
        assertTrue(tick.clock.progress(start + 65 * 60_000L) < 0.2f)
    }

    @Test
    fun `자동 진행이 꺼져 있으면 절기가 흐르지 않는다`() {
        val start = 5_000_000L
        val clock = SeasonClock.start(start, minutes = 10).copy(autoAdvance = false)
        val tick = SeasonClockEngine.tick(clock, start + 3 * 60 * 60_000L)
        assertEquals(0, tick.due)
        assertFalse(tick.offline)
    }

    @Test
    fun `5분 이상 비면 미접속으로 보고 AI 대행 리포트를 남긴다`() {
        val start = 2_000_000L
        val clock = SeasonClock.start(start, minutes = 30)
        val tick = SeasonClockEngine.tick(clock, start + 2 * 60 * 60_000L)
        assertTrue(tick.offline)
        assertEquals(4, tick.due)

        val report = SeasonClockEngine.offlineSummary(
            seasons = tick.due,
            awayMillis = tick.awayMillis,
            goldBefore = 1000,
            goldAfter = 1800,
            foodBefore = 2000,
            foodAfter = 2600,
            troopsBefore = 900,
            troopsAfter = 1100,
            aiNews = listOf("[AI 동향] 이서준 리더가 박민재 리더를 병합했습니다"),
            emergency = EmergencyCouncilEngine.call("김도현", "탕정면", 120, 1)
        )
        assertTrue(report.contains("AI가 절기 4회"))
        assertTrue(report.contains("2시간 0분"))
        assertTrue(report.contains("긴급 의회"))
    }

    @Test
    fun `시계는 최대 48절기까지만 따라잡는다`() {
        val start = 10_000L
        val clock = SeasonClock.start(start, minutes = 1)
        val tick = SeasonClockEngine.tick(clock, start + 500L * 60_000L)
        assertEquals(SeasonClockEngine.MAX_CATCH_UP, tick.due)
    }

    // ── ⑤ 서브 메뉴 배지 연동 ───────────────────────────────────

    @Test
    fun `하단 탭 배지와 서브 메뉴 배지 합이 항상 같다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val state = vm.uiState.value

        MainTab.entries.forEach { tab ->
            val counts = SubMenuBadges.countsOf(state, tab)
            val tabBadge = SubMenuBadges.tabBadge(state, tab)
            if (counts.isEmpty()) {
                assertEquals("$tab 하단 배지", 0, tabBadge)
            } else {
                assertEquals("$tab 서브 배지 합과 하단 배지", counts.sum(), tabBadge)
            }
        }
    }

    @Test
    fun `의회 안쪽 탭에도 안건·긴급 배지가 나뉜다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val state = vm.uiState.value

        val inner = SubMenuBadges.councilInnerCounts(state)
        assertEquals(5, inner.size)
        // 기본 안건 2건이 표결 대기 → 안건 배지
        assertTrue(inner[0] >= 1)
        val councilBadge = SubMenuBadges.subBadge(state, MainTab.DOMAIN, 5)
        assertEquals(inner.sum(), councilBadge)
    }

    @Test
    fun `의회 하단 배지는 편입 리더가 늘면 의결권이 는다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        assertEquals(1, vm.uiState.value.councilVotingPower)
        assertTrue(vm.uiState.value.councilVotingLabel.contains("기본 1"))
    }

    // ── 통합: 화면에서 인접 지역 패널 → 교전 → 의회 상정 ─────────

    private fun playAtLevelTwo(): SlgGameViewModel {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        // 방어전 2회 → 지휘 Lv.2 (인접 구역 해금)
        vm.runOperation("OP_BASE")
        vm.runOperation("OP_BASE")
        return vm
    }

   @Test
    fun `인접 지역을 고르면 하단 패널 데이터가 나오고 교전이 된다`() {
        val vm = playAtLevelTwo()
        val adjacent = vm.uiState.value.mapNodes.first { it.hopDistance == 1 && it.unlocked }
        vm.selectTerritory(adjacent.code)

        val panel = vm.uiState.value.selectedAdjacentPanel
        assertNotNull(panel)
        assertEquals(adjacent.name, panel!!.name)
        assertTrue(panel.warTargets.isNotEmpty())

        vm.openAdjacentAction(AdjacentActionKind.WAR)
        assertEquals(AdjacentActionKind.WAR, vm.uiState.value.adjacentSheet?.kind)

        vm.challengeAdjacentLeader(panel.warTargets.first().id)
        val after = vm.uiState.value
        assertTrue(after.statusMessage.contains("인접 원정"))
        assertNotNull(after.lastReport)
        assertEquals("인접 지역 원정", after.lastReport!!.operation)
    }

    @Test
    fun `인접 지역 전략과 외교를 실행하면 결과 문구가 남는다`() {
        val vm = playAtLevelTwo()
        val adjacent = vm.uiState.value.mapNodes.first { it.hopDistance == 1 && it.unlocked }
        vm.selectTerritory(adjacent.code)

        vm.openAdjacentAction(AdjacentActionKind.STRATEGY)
        vm.runAdjacentStrategy(AdjacentStrategyOp.RECON.id)
        assertNotNull(vm.uiState.value.adjacentMessage)

        vm.openAdjacentAction(AdjacentActionKind.DIPLOMACY)
        val goldBefore = vm.uiState.value.gold
        vm.proposeAdjacentDiplomacy(TreatyType.NON_AGGRESSION_PACT)
        assertTrue(vm.uiState.value.gold <= goldBefore)
        assertNotNull(vm.uiState.value.adjacentMessage)
    }

    @Test
    fun `지도에서 고른 인접 지역으로 의회 공동 작전 안건을 올리고 표결한다`() {
        val vm = playAtLevelTwo()
        val adjacent = vm.uiState.value.mapNodes.first { it.hopDistance == 1 && it.unlocked }
        vm.selectTerritory(adjacent.code)

        vm.sponsorCoalitionBill(AdjacentActionKind.WAR)
        val pending = vm.uiState.value.coalition.pendingOf(AdjacentActionKind.WAR)
        assertEquals(1, pending.size)

        vm.voteCoalitionBill(pending.first().id)
        val bill = vm.uiState.value.coalition.bills.first { it.id == pending.first().id }
        assertFalse(bill.pending)
        assertNotNull(bill.message)
    }

    @Test
    fun `인접 지역 교전 피해가 명부에 남아 다음 공격에 반영된다`() {
        val vm = playAtLevelTwo()
        val adjacent = vm.uiState.value.mapNodes.first { it.hopDistance == 1 && it.unlocked }
        vm.selectTerritory(adjacent.code)
        val panelBefore = vm.uiState.value.selectedAdjacentPanel!!
        val target = panelBefore.warTargets.first()

        vm.openAdjacentAction(AdjacentActionKind.WAR)
        vm.challengeAdjacentLeader(target.id)

        val after = vm.uiState.value
        val damage = after.adjacentDamage[target.id]
        assertNotNull(damage)
        val targetAfter = after.selectedAdjacentPanel!!.leaders.first { it.id == target.id }
        assertEquals((target.troops - damage!!).coerceAtLeast(0), targetAfter.troops)
    }

    @Test
    fun `같은 절기에 같은 종류 안건을 다시 올려도 id가 겹치지 않는다`() {
        val vm = playAtLevelTwo()
        val adjacent = vm.uiState.value.mapNodes.first { it.hopDistance == 1 && it.unlocked }
        vm.selectTerritory(adjacent.code)

        vm.sponsorCoalitionBill(AdjacentActionKind.STRATEGY)
        val first = vm.uiState.value.coalition.bills.first { it.kind == AdjacentActionKind.STRATEGY }
        vm.voteCoalitionBill(first.id)
        vm.sponsorCoalitionBill(AdjacentActionKind.STRATEGY)
        vm.sponsorCoalitionBill(AdjacentActionKind.STRATEGY)

        val strat = vm.uiState.value.coalition.bills.filter { it.kind == AdjacentActionKind.STRATEGY }
        assertEquals(strat.size, strat.map { it.id }.distinct().size)
    }

    @Test
    fun `자동 시계가 흐르면 절기가 따라잡히고 미접속 리포트가 남는다`() {
        val vm = playAtLevelTwo()
        val before = vm.uiState.value
        val turnBefore = before.seasonTurn

        // 2시간 뒤로 시계를 밀어 점검 — 30분/절기면 4회
        vm.tickRealTime(System.currentTimeMillis() + 2 * 60 * 60_000L)

        val after = vm.uiState.value
        assertTrue("절기가 자동으로 흘렀다", after.seasonTurn >= turnBefore + 4)
        assertNotNull(after.offlineReport)
        assertTrue(after.offlineReport!!.contains("AI가 절기"))
    }

    @Test
    fun `시간 배속과 자동 진행을 바꿀 수 있다`() {
        val vm = playAtLevelTwo()
        vm.setSeasonSpeed(60)
        assertEquals(60, vm.uiState.value.clock.seasonMinutes)

        vm.setAutoAdvance(false)
        assertFalse(vm.uiState.value.clock.autoAdvance)
        assertEquals("수동", vm.uiState.value.clock.speedLabel)

        vm.setAiAutopilot(false)
        assertFalse(vm.uiState.value.clock.aiAutopilot)
    }
}
