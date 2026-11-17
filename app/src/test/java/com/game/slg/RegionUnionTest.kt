package com.game.slg

import com.game.slg.core.region.AttackScope
import com.game.slg.core.region.AttackSide
import com.game.slg.core.region.AttackVerdict
import com.game.slg.core.region.RegionUnion
import com.game.slg.core.region.RegionUnionEngine
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 지역 연합 · 스폰 보호 — 7턴 동안 같은 지역 교전 금지 + 외부 침공 차단.
 */
class RegionUnionTest {

    @Test
    fun `지역을 인계받으면 7턴 스폰 보호가 열린다`() {
        val vm = SlgGameViewModel()

        vm.chooseLord(vm.uiState.value.lords.first().id)

        val union = vm.uiState.value.union
        assertEquals(RegionUnion.DEFAULT_TURNS, union.remainingTurns)
        assertEquals(7, union.remainingTurns)
        assertTrue(union.active)
        assertTrue(vm.uiState.value.spawnProtected)
    }

    @Test
    fun `보호 상태 문구에 남은 턴이 그대로 보인다`() {
        val union = RegionUnionEngine.open("4420033000", "탕정면")

        assertEquals("🛡️ 지역 연합 · 스폰 보호 7턴 남음", union.statusLabel)
        assertEquals("🛡️ 보호 7턴", union.chipLabel)
    }

    @Test
    fun `연합 기간에는 지역 내 교전이 막힌다`() {
        val union = RegionUnionEngine.open("4420033000", "탕정면")

        assertEquals(
            AttackVerdict.BLOCKED_UNION,
            RegionUnionEngine.decide(union, AttackScope.SAME_REGION, AttackSide.EXPEDITION)
        )
        assertEquals(
            AttackVerdict.BLOCKED_UNION,
            RegionUnionEngine.decide(union, AttackScope.SAME_REGION, AttackSide.INVASION)
        )
    }

    @Test
    fun `연합 기간에는 외부 침공도 보호막이 막는다`() {
        val union = RegionUnionEngine.open("4420033000", "탕정면")

        assertEquals(
            AttackVerdict.BLOCKED_SHIELD,
            RegionUnionEngine.decide(union, AttackScope.ADJACENT, AttackSide.INVASION)
        )
        assertEquals(
            AttackVerdict.BLOCKED_SHIELD,
            RegionUnionEngine.decide(union, AttackScope.EXTERNAL, AttackSide.INVASION)
        )
    }

    @Test
    fun `대외 원정은 보호 기간에도 나갈 수 있다`() {
        val union = RegionUnionEngine.open("4420033000", "탕정면")

        assertEquals(
            AttackVerdict.ALLOWED,
            RegionUnionEngine.decide(union, AttackScope.ADJACENT, AttackSide.EXPEDITION)
        )
    }

    @Test
    fun `보호가 끝나면 모든 공격이 허용된다`() {
        val union = RegionUnionEngine.open("4420033000", "탕정면", turns = 0)

        assertFalse(union.active)
        assertEquals("⚔ 지역 연합 종료 — 교전 가능", union.statusLabel)
        AttackScope.entries.forEach { scope ->
            AttackSide.entries.forEach { side ->
                assertEquals(
                    "$scope / $side",
                    AttackVerdict.ALLOWED,
                    RegionUnionEngine.decide(union, scope, side)
                )
            }
        }
    }

    @Test
    fun `절기가 지날 때마다 1턴씩 줄고 0이 되면 종료 안내가 나온다`() {
        var union = RegionUnionEngine.open("4420033000", "탕정면")

        repeat(6) {
            val (next, message) = RegionUnionEngine.onTurnPassed(union)
            union = next
            assertNotNull(message)
            assertTrue(union.active)
            assertTrue(message!!.contains("남음"))
        }
        assertEquals(1, union.remainingTurns)

        val (expired, last) = RegionUnionEngine.onTurnPassed(union)

        assertEquals(0, expired.remainingTurns)
        assertFalse(expired.active)
        assertTrue(last!!.contains("종료"))
    }

    @Test
    fun `이미 끝난 연합은 더 줄지 않는다`() {
        val union = RegionUnionEngine.open("4420033000", "탕정면", turns = 0)

        val (next, message) = RegionUnionEngine.onTurnPassed(union)

        assertEquals(0, next.remainingTurns)
        assertNull(message)
    }

    @Test
    fun `절기를 진행하면 남은 보호 턴이 줄어든다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        assertEquals(7, vm.uiState.value.union.remainingTurns)

        vm.advanceSeason()

        assertEquals(6, vm.uiState.value.union.remainingTurns)
        assertTrue(vm.uiState.value.log.any { it.contains("지역 연합") })
    }

    @Test
    fun `일곱 번의 절기 뒤에는 보호가 풀린다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)

        repeat(7) { vm.advanceSeason() }

        assertFalse(vm.uiState.value.union.active)
        assertFalse(vm.uiState.value.spawnProtected)
    }

    @Test
    fun `보호 기간 연합 대상은 같은 지역 AI 리더 전부다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)

        val state = vm.uiState.value
        assertEquals(state.lordCapacity - 1, state.unionPartnerCount)
        assertEquals(state.aiLordCount, state.unionPartnerCount)
        // 지도 노드(인접 읍·면·동)는 외부 구역이라 표시를 바꾸지 않는다
        assertTrue(state.mapNodes.none { it.relationLabel == "지역 연합" })
    }

    @Test
    fun `보호 기간 기지 방어는 사상자 없이 침공을 막는다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val before = vm.uiState.value

        vm.runOperation("OP_BASE")

        val after = vm.uiState.value
        val report = after.lastReport
        assertNotNull(report)
        assertTrue(report!!.defenderWon)
        assertEquals(0, report.defenderLosses)
        assertEquals(before.troops, after.troops)
        assertTrue(after.statusMessage.contains("스폰 보호막"))
        assertTrue("보호 중 방어도 지휘 경험을 준다", after.command.xp > 0 || after.command.level > 1)
    }

    @Test
    fun `보호 기간에는 지역 리더 교전이 막힌다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val target = vm.uiState.value.lords.first { !it.isPlayer }
        val before = vm.uiState.value

        vm.challengeRegionLord(target.id)

        val after = vm.uiState.value
        assertTrue(after.statusMessage.contains("지역 연합 기간"))
        assertEquals(before.troops, after.troops)
        assertEquals(before.lords.first { it.id == target.id }.troops, after.lords.first { it.id == target.id }.troops)
    }

    @Test
    fun `보호가 끝나면 지역 리더 교전이 실제로 일어난다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        repeat(7) { vm.advanceSeason() }
        val target = vm.uiState.value.lords.first { !it.isPlayer }
        val before = vm.uiState.value

        vm.challengeRegionLord(target.id)

        val after = vm.uiState.value
        assertTrue(after.statusMessage.contains("교전"))
        assertEquals("지역 리더 압박", after.lastReport?.operation)
        assertEquals(
            before.lords.first { it.id == target.id }.troops - after.lastReport!!.attackerLosses,
            after.lords.first { it.id == target.id }.troops
        )
    }

    @Test
    fun `보호 상태는 저장되고 복원된다`() {
        val store = com.game.slg.core.save.InMemoryCampaignStore()
        val first = SlgGameViewModel(store = store)
        first.chooseLord(first.uiState.value.lords.first().id)
        first.advanceSeason()
        first.advanceSeason()
        assertEquals(5, first.uiState.value.union.remainingTurns)

        val second = SlgGameViewModel(store = store)

        assertEquals(5, second.uiState.value.union.remainingTurns)
        assertTrue(second.uiState.value.spawnProtected)
    }

    @Test
    fun `새 게임을 시작하면 보호가 다시 7턴으로 열린다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        repeat(7) { vm.advanceSeason() }
        assertFalse(vm.uiState.value.union.active)

        vm.restartCampaign()
        assertEquals(0, vm.uiState.value.union.remainingTurns)

        vm.chooseLord(vm.uiState.value.lords.first().id)
        assertEquals(7, vm.uiState.value.union.remainingTurns)
    }
}
