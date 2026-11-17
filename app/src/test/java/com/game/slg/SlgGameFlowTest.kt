package com.game.slg

import com.game.slg.core.model.EspionageOpType
import com.game.slg.core.model.TreatyType
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 인트로 이후 실제 플레이 흐름(영지·사건·내정·전장·외교) 단위 테스트.
 * 시작 지역은 기본값인 충남 아산시 탕정면이고, 첫 AI 호족을 인계받아 플레이한다.
 */
class SlgGameFlowTest {

    private fun playingVm(): SlgGameViewModel {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        return vm
    }

    @Test
    fun `이벤트A 보급 협정은 보급을 늘리고 자금을 줄인다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.chooseEventA(acceptPact = true)

        val after = vm.uiState.value
        assertEquals("A_PACT", after.eventAChoice)
        assertEquals(before.food + 1200, after.food)
        assertEquals((before.gold - 600).coerceAtLeast(0), after.gold)
        assertEquals(before.morale + 8, after.morale)
        assertNotNull(after.eventAResult)
    }

    @Test
    fun `이벤트A 통행료 징수는 자금을 늘리고 민심을 떨어뜨린다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.chooseEventA(acceptPact = false)

        val after = vm.uiState.value
        assertEquals(before.gold + 900, after.gold)
        assertEquals(before.morale - 10, after.morale)
        assertEquals(before.diplomacy - 15, after.diplomacy)
    }

    @Test
    fun `이벤트B 무인기 요격은 소수 병력을 잃고 노획과 명성을 얻는다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.chooseEventB(ambush = true)

        val after = vm.uiState.value
        assertEquals(before.troops - 8, after.troops)
        assertEquals(before.gold + 250, after.gold)
        assertEquals(before.prestige + 6, after.prestige)
    }

    @Test
    fun `이벤트B 첩보 파견은 적 보급 거점 정보를 확보한다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.chooseEventB(ambush = false)

        val after = vm.uiState.value
        assertTrue(after.intelGained)
        assertEquals(before.gold - 200, after.gold)
    }

    @Test
    fun `테크트리는 선행 단계 없이는 연구할 수 없다`() {
        val vm = playingVm()
        // AI 호족 인계 직후에는 자금이 적으므로 절기 정산으로 재정을 확보한다
        vm.advanceSeason()
        val funded = vm.uiState.value

        vm.research("admin_1")
        val afterFirst = vm.uiState.value
        assertTrue(afterFirst.researchedTechs.contains("admin_1"))
        // 기술 영웅(윤도현 · -12%)이 배치되어 400G → 352G 로 할인된다
        assertEquals(funded.gold - 352, afterFirst.gold)

        vm.research("admin_3")
        val blocked = vm.uiState.value
        assertTrue("선행 기술이 없으면 3단계는 잠긴다", !blocked.researchedTechs.contains("admin_3"))
        assertEquals(afterFirst.gold, blocked.gold)

        vm.research("admin_2")
        assertTrue(vm.uiState.value.researchedTechs.contains("admin_2"))
    }

    @Test
    fun `호족 의회 표결은 가결 여부와 불복 트리거를 산출한다`() {
        val vm = playingVm()

        vm.voteBill("bill_levy")

        val bill = vm.uiState.value.bills.first { it.id == "bill_levy" }
        assertNotNull("표결 결과가 기록되어야 한다", bill.passed)
        assertTrue(bill.favor + bill.against > 0)
        assertNotNull(bill.message)
    }

    @Test
    fun `통합 방호기지 방어는 방호 시설 우위로 저손실 방어에 성공한다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.runOperation("OP_BASE")

        val after = vm.uiState.value
        val report = after.lastReport
        assertNotNull(report)
        assertTrue("방호 기지 방어는 성공이어야 한다", report!!.defenderWon)
        assertEquals("통합 방호기지 방어", report.operation)
        assertTrue("아군 사상자는 병력의 25% 이하여야 한다", report.defenderLosses <= before.troops / 4)
        assertEquals(before.troops - report.defenderLosses, after.troops)
        assertTrue("방어 성공은 지휘 경험을 준다", after.command.xp > 0 || after.command.level > 1)
    }

    @Test
    fun `Lv1에서는 인접 구역 작전이 잠기고 방어전 경험으로 Lv2가 열린다`() {
        val vm = playingVm()
        assertEquals(1, vm.uiState.value.command.level)
        assertEquals(0, vm.uiState.value.unlockedHop)

        // Lv1 — 인접 구역 작전 불가
        vm.runOperation("OP_DRONE")
        assertEquals(null, vm.uiState.value.lastReport)
        assertTrue(vm.uiState.value.statusMessage.contains("작전 불가"))

        // 관할 구역 방어로 경험 → Lv2, 인접 구역 해금
        vm.runOperation("OP_BASE")
        assertEquals(2, vm.uiState.value.command.level)
        assertEquals(1, vm.uiState.value.unlockedHop)
        assertTrue(vm.uiState.value.mapNodes.any { it.hopDistance == 1 && it.unlocked })

        // Lv2 — 인접 구역 무인기 작전 가능
        vm.runOperation("OP_DRONE")
        val report = vm.uiState.value.lastReport
        assertNotNull(report)
        assertEquals("무인기 정찰·타격", report!!.operation)
    }

    @Test
    fun `교량 봉쇄 작전도 결과 리포트를 남긴다`() {
        val vm = playingVm()
        vm.runOperation("OP_BASE") // Lv2 확보
        vm.runOperation("OP_BRIDGE")

        val report = vm.uiState.value.lastReport
        assertNotNull(report)
        assertEquals("교량·도하 봉쇄", report!!.operation)
        assertTrue(report.composition.contains("기동보병"))
    }

    @Test
    fun `외교 조약 체결은 조약 목록과 외교 지표를 갱신한다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.proposeTreaty(TreatyType.NON_AGGRESSION_PACT)

        val after = vm.uiState.value
        assertEquals(1, after.treaties.size)
        assertEquals("불가침 조약", after.treaties.first().typeLabel)
        assertEquals(before.diplomacy + 6, after.diplomacy)
    }

    @Test
    fun `무력 대응 명분은 한 번만 확보된다`() {
        val vm = playingVm()

        vm.secureCasusBelli()
        val first = vm.uiState.value.casusBelli
        assertNotNull(first)

        vm.secureCasusBelli()
        assertEquals(first, vm.uiState.value.casusBelli)
    }

    @Test
    fun `첩보 공작은 결과 메시지를 남기고 실행 횟수를 센다`() {
        val vm = playingVm()

        vm.conductEspionage(EspionageOpType.GATHER_INTEL)

        val after = vm.uiState.value
        assertNotNull(after.espionageMessage)
        assertEquals(1, after.espionageCount)
    }

    @Test
    fun `절기 진행은 자금을 갱신하고 지휘 경험을 준다`() {
        val vm = playingVm()
        val before = vm.uiState.value

        vm.advanceSeason()

        val after = vm.uiState.value
        assertTrue("봄 지방세로 자금이 늘어야 한다", after.gold > before.gold)
        assertTrue(after.statusMessage.contains("절기 순환"))
        assertTrue(after.command.xp > 0 || after.command.level > 1)
    }

    @Test
    fun `시설 건설은 자금을 쓰고 방어·내정 효과를 켠다`() {
        val vm = playingVm()
        vm.advanceSeason()
        val before = vm.uiState.value

        val result = vm.buildFacility(com.game.slg.core.model.FacilityType.RADAR_NETWORK)

        val after = vm.uiState.value
        assertTrue(result.canBuild)
        assertTrue(after.facilities.contains(com.game.slg.core.model.FacilityType.RADAR_NETWORK))
        assertEquals(before.gold - 1100, after.gold)
        assertTrue(after.espionagePower >= before.espionagePower + 5)

        // 같은 시설은 두 번 지을 수 없다
        val again = vm.buildFacility(com.game.slg.core.model.FacilityType.RADAR_NETWORK)
        assertFalse(again.canBuild)
    }

    @Test
    fun `절기 진행은 계절을 순환시킨다`() {
        val vm = playingVm()

        vm.advanceSeason()
        assertTrue(vm.uiState.value.seasonName.startsWith("여름"))

        vm.advanceSeason()
        assertTrue(vm.uiState.value.seasonName.startsWith("가을"))
    }
}
