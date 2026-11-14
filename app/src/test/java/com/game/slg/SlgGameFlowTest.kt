package com.game.slg

import com.game.slg.core.model.EspionageOpType
import com.game.slg.core.model.TreatyType
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * UI 초안(영지·이벤트·내정·전장·외교) 흐름 단위 테스트
 */
class SlgGameFlowTest {

    @Test
    fun `이벤트A 보급 협정은 식량을 늘리고 자금을 줄인다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.chooseEventA(acceptPact = true)

        val after = vm.uiState.value
        assertEquals("A_PACT", after.eventAChoice)
        assertEquals(before.food + 1200, after.food)
        assertEquals(before.gold - 600, after.gold)
        assertEquals(before.morale + 8, after.morale)
        assertNotNull(after.eventAResult)
    }

    @Test
    fun `이벤트A 통행세 징수는 자금을 늘리고 민심을 떨어뜨린다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.chooseEventA(acceptPact = false)

        val after = vm.uiState.value
        assertEquals(before.gold + 900, after.gold)
        assertEquals(before.morale - 10, after.morale)
        assertEquals(before.diplomacy - 15, after.diplomacy)
    }

    @Test
    fun `이벤트B 매복 요격은 병력을 잃고 노획과 명성을 얻는다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.chooseEventB(ambush = true)

        val after = vm.uiState.value
        assertEquals(before.troops - 80, after.troops)
        assertEquals(before.gold + 250, after.gold)
        assertEquals(before.prestige + 6, after.prestige)
    }

    @Test
    fun `이벤트B 첩자 파견은 적 군량고 정보를 확보한다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.chooseEventB(ambush = false)

        val after = vm.uiState.value
        assertTrue(after.intelGained)
        assertEquals(before.gold - 200, after.gold)
    }

    @Test
    fun `테크트리는 선행 단계 없이는 연구할 수 없다`() {
        val vm = SlgGameViewModel()

        vm.research("admin_1")
        val afterFirst = vm.uiState.value
        assertTrue(afterFirst.researchedTechs.contains("admin_1"))
        assertEquals(4200 - 400, afterFirst.gold)

        vm.research("admin_3")
        val blocked = vm.uiState.value
        assertTrue("선행 기술이 없으면 3단계는 잠긴다", !blocked.researchedTechs.contains("admin_3"))
        assertEquals(afterFirst.gold, blocked.gold)

        vm.research("admin_2")
        assertTrue(vm.uiState.value.researchedTechs.contains("admin_2"))
    }

    @Test
    fun `호족 의회 표결은 가결 여부와 반란 트리거를 산출한다`() {
        val vm = SlgGameViewModel()

        vm.voteBill("bill_levy")

        val bill = vm.uiState.value.bills.first { it.id == "bill_levy" }
        assertNotNull("표결 결과가 기록되어야 한다", bill.passed)
        assertTrue(bill.favor + bill.against > 0)
        assertNotNull(bill.message)
    }

    @Test
    fun `요새 수성 작전은 80퍼센트 사상자 감면으로 방어에 성공한다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.runOperation("OP_FORTRESS")

        val report = vm.uiState.value.lastReport
        assertNotNull(report)
        assertTrue("요새 수성은 방어 성공이어야 한다", report!!.defenderWon)
        assertTrue("아군 사상자는 병력의 25% 이하여야 한다", report.defenderLosses <= before.troops / 4)
        assertEquals(before.troops - report.defenderLosses, vm.uiState.value.troops)
    }

    @Test
    fun `산악 매복과 하천 도하 요격도 결과 리포트를 남긴다`() {
        val ambush = SlgGameViewModel()
        ambush.runOperation("OP_AMBUSH")
        assertNotNull(ambush.uiState.value.lastReport)
        assertEquals("광교산 협곡 매복", ambush.uiState.value.lastReport!!.operation)

        val river = SlgGameViewModel()
        river.runOperation("OP_RIVER")
        assertNotNull(river.uiState.value.lastReport)
        assertEquals("매탄천 도하 요격", river.uiState.value.lastReport!!.operation)
    }

    @Test
    fun `외교 조약 체결은 조약 목록과 외교 지표를 갱신한다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.proposeTreaty(TreatyType.NON_AGGRESSION_PACT)

        val after = vm.uiState.value
        assertEquals(1, after.treaties.size)
        assertEquals("불가침 조약", after.treaties.first().typeLabel)
        assertEquals(before.diplomacy + 6, after.diplomacy)
    }

    @Test
    fun `선전포고 명분은 한 번만 확보된다`() {
        val vm = SlgGameViewModel()

        vm.secureCasusBelli()
        val first = vm.uiState.value.casusBelli
        assertNotNull(first)

        vm.secureCasusBelli()
        assertEquals(first, vm.uiState.value.casusBelli)
    }

    @Test
    fun `첩자 공작은 결과 메시지를 남기고 실행 횟수를 센다`() {
        val vm = SlgGameViewModel()

        vm.conductEspionage(EspionageOpType.GATHER_INTEL)

        val after = vm.uiState.value
        assertNotNull(after.espionageMessage)
        assertEquals(1, after.espionageCount)
    }

    @Test
    fun `절기 진행은 자금과 식량을 갱신한다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.advanceSeason()

        val after = vm.uiState.value
        assertTrue("봄 조세로 자금이 늘어야 한다", after.gold > before.gold)
        assertTrue(after.statusMessage.contains("절기 순환"))
    }
}
