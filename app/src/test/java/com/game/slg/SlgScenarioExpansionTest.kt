package com.game.slg

import com.game.slg.core.engine.HiddenTalentNightEngine
import com.game.slg.core.engine.IHiddenTalentNightEngine
import com.game.slg.core.engine.IPromotionEngine
import com.game.slg.core.engine.IRefugeeCrisisEngine
import com.game.slg.core.engine.PromotionEngine
import com.game.slg.core.engine.RefugeeCrisisEngine
import com.game.slg.core.model.NightMeetingChoice
import com.game.slg.core.model.PromotionStage
import com.game.slg.core.model.RefugeeResponse
import com.game.slg.core.model.TreatyType
import com.game.slg.ui.SlgGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 추가 시나리오 3종 & 관련 UI 단위 테스트
 * 1) 「연방 붕괴의 그림자: 광교·용인 유민 유입 딜레마」
 * 2) 「호족 의회의 이면: 히든 인재 야간 밀회 (포섭·숙청·이중첩자)」
 * 3) 「성주(Castellan) 승격 마일스톤 로드맵」
 */
class SlgScenarioExpansionTest {

    // =========================================================================
    // 1. 유민 유입 딜레마
    // =========================================================================

    @Test
    fun `성문 개방은 식량을 내주고 인구와 민심 문화 명성을 얻는다`() {
        val engine: IRefugeeCrisisEngine = RefugeeCrisisEngine()
        assertEquals(800, engine.refugeeCount)
        assertEquals(2, engine.choices().size)

        val open = engine.choices().first { it.response == RefugeeResponse.OPEN_GATE }
        val result = engine.resolve(open.id, food = 8600, gold = 4200)

        assertTrue(result.isSuccess)
        assertEquals(-500, result.foodChange)
        assertEquals(800, result.populationChange)
        assertEquals(10, result.moraleChange)
        assertEquals(8, result.culturePrestigeChange)
        assertEquals(0, result.goldChange)
        assertTrue(result.message.contains("통제선 개방"))
    }

    @Test
    fun `성문 개방은 식량이 500석 미만이면 거부된다`() {
        val engine: IRefugeeCrisisEngine = RefugeeCrisisEngine()
        val open = engine.choices().first { it.response == RefugeeResponse.OPEN_GATE }

        val result = engine.resolve(open.id, food = 320, gold = 4200)

        assertFalse(result.isSuccess)
        assertEquals(0, result.foodChange)
        assertEquals(0, result.populationChange)
        assertTrue(result.failureReason!!.contains("식량"))
    }

    @Test
    fun `성문 통제는 관세를 얻지만 민심과 외교 평판을 잃는다`() {
        val engine: IRefugeeCrisisEngine = RefugeeCrisisEngine()
        val control = engine.choices().first { it.response == RefugeeResponse.CONTROL_GATE }

        val result = engine.resolve(control.id, food = 8600, gold = 4200)

        assertTrue(result.isSuccess)
        assertEquals(200, result.goldChange)
        assertEquals(-12, result.moraleChange)
        assertEquals(-8, result.diplomacyChange)
        assertEquals(0, result.populationChange)
    }

    @Test
    fun `영지 화면 유민 이벤트는 선택지에 따라 자원을 갱신하고 두 번 처리되지 않는다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        val result = vm.chooseRefugeeCrisis("REFUGEE_A_OPEN")

        assertTrue(result!!.isSuccess)
        val after = vm.uiState.value
        assertEquals(before.food - 500, after.food)
        assertEquals(before.population + 800, after.population)
        assertEquals(before.morale + 10, after.morale)
        assertEquals(before.culturePrestige + 8, after.culturePrestige)
        assertEquals("REFUGEE_A_OPEN", after.refugeeChoice)
        assertTrue(after.isRefugeeAccepted)
        assertNotNull(after.refugeeResult)

        // 두 번째 처리는 무시된다
        val again = vm.chooseRefugeeCrisis("REFUGEE_B_CONTROL")
        assertNull(again)
        assertEquals("REFUGEE_A_OPEN", vm.uiState.value.refugeeChoice)
        assertEquals(after.gold, vm.uiState.value.gold)
    }

    // =========================================================================
    // 2. 히든 인재 야간 밀회
    // =========================================================================

    @Test
    fun `야간 밀회는 의심도 50 이상만 대상이 된다`() {
        val engine: IHiddenTalentNightEngine = HiddenTalentNightEngine()

        assertEquals(50, engine.suspicionThreshold)
        assertTrue(engine.isEligible(62))
        assertFalse(engine.isEligible(48))
        assertEquals(3, engine.choices().size)

        val rejected = engine.resolve(NightMeetingChoice.RECRUIT, "서이레", suspicion = 48, gold = 4200)
        assertFalse(rejected.isSuccess)
        assertTrue(rejected.failureReason!!.contains("미달"))
    }

    @Test
    fun `포섭은 의심도를 낮추고 명성과 첩보력을 올린다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.openNightMeeting("한도윤")
        assertEquals("한도윤", vm.uiState.value.nightMeetingTalentName)

        val result = vm.resolveNightMeeting(NightMeetingChoice.RECRUIT)

        assertTrue(result!!.isSuccess)
        val after = vm.uiState.value
        val talent = after.hiddenTalents.first { it.name == "한도윤" }
        assertEquals(32, talent.suspicion)                       // 62 - 30
        assertEquals(before.prestige + 5, after.prestige)
        assertEquals(before.espionagePower + 10, after.espionagePower)
        assertEquals("RECRUIT", after.nightMeetingChoice)
        assertNotNull(after.nightMeetingResult)
        assertEquals(before.gold, after.gold)
    }

    @Test
    fun `숙청은 자금을 몰수하고 인재를 제거하며 민심을 떨어뜨린다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.openNightMeeting("한도윤")
        val result = vm.resolveNightMeeting(NightMeetingChoice.PURGE)

        assertTrue(result!!.isSuccess)
        assertTrue(result.talentRemoved)
        val after = vm.uiState.value
        assertEquals(before.gold + 400, after.gold)
        assertEquals(before.morale - 8, after.morale)
        assertTrue("숙청한 인재는 명부에서 사라진다", after.hiddenTalents.none { it.name == "한도윤" })
        assertNull(after.doubleAgentBuff)
    }

    @Test
    fun `이중첩자 역파견은 자금을 쓰고 광교동 교란 버프를 얻는다`() {
        val vm = SlgGameViewModel()
        val before = vm.uiState.value

        vm.openNightMeeting("한도윤")
        val result = vm.resolveNightMeeting(NightMeetingChoice.DOUBLE_AGENT)

        assertTrue(result!!.isSuccess)
        val after = vm.uiState.value
        assertEquals(before.gold - 150, after.gold)
        assertNotNull(after.doubleAgentBuff)
        assertTrue(after.doubleAgentBuff!!.contains("상대 거점"))
        assertEquals("이중첩자는 명부에 남아 감시 대상이 된다", 52, after.hiddenTalents.first { it.name == "한도윤" }.suspicion)
        assertEquals(before.enemyFood - 200, after.enemyFood)

        // 교란 효과 발동 시 적 군량을 한 번 더 무너뜨린다
        vm.exploitDoubleAgent()
        assertEquals(after.enemyFood - 300, vm.uiState.value.enemyFood)
    }

    @Test
    fun `이중첩자 역파견은 자금이 150 미만이면 실패한다`() {
        val engine: IHiddenTalentNightEngine = HiddenTalentNightEngine()

        val result = engine.resolve(NightMeetingChoice.DOUBLE_AGENT, "한도윤", suspicion = 62, gold = 100)

        assertFalse(result.isSuccess)
        assertEquals(0, result.goldChange)
        assertNull(result.doubleAgentBuff)
        assertTrue(result.failureReason!!.contains("부족"))
    }

    @Test
    fun `야간 밀회 대상은 의심도 50 이상 인재만 후보로 노출된다`() {
        val vm = SlgGameViewModel()
        val candidates = vm.uiState.value.nightMeetingCandidates

        assertEquals(1, candidates.size)
        assertEquals("한도윤", candidates.first().name)

        // 의심도가 기준 미달인 인재에게는 서신을 보낼 수 없다
        vm.openNightMeeting("서이레")
        assertNull(vm.uiState.value.nightMeetingTalentName)
        assertTrue(vm.uiState.value.statusMessage.contains("미달"))
    }

    // =========================================================================
    // 3. 성주 승격 마일스톤 로드맵
    // =========================================================================

    @Test
    fun `승격 로드맵은 세 단계이며 조건 미달이면 승격되지 않는다`() {
        val engine: IPromotionEngine = PromotionEngine()

        assertEquals(3, PromotionStage.values().size)
        assertEquals(PromotionStage.UNITED_CASTELLAN, PromotionStage.DISTRICT_LORD.nextStage)
        assertTrue(PromotionStage.CITY_SOVEREIGN.isFinal)

        val evaluation = engine.evaluate(PromotionStage.DISTRICT_LORD, alliedTerritories = 1, prestige = 74)
        assertFalse(evaluation.isEligible)
        assertEquals(2, evaluation.requiredAlliedTerritories)
        assertEquals(80, evaluation.requiredPrestige)
        assertTrue(evaluation.requirementSummary.contains("우호·동맹"))

        val blocked = engine.promote(PromotionStage.DISTRICT_LORD, alliedTerritories = 1, prestige = 74)
        assertFalse(blocked.isSuccess)
        assertEquals(PromotionStage.DISTRICT_LORD, blocked.stage)
        assertTrue(blocked.failureReason!!.contains("조건"))
    }

    @Test
    fun `과반 연합 성주 승격은 의식 배너와 의회 권한 보너스를 준다`() {
        val engine: IPromotionEngine = PromotionEngine()

        val result = engine.promote(PromotionStage.DISTRICT_LORD, alliedTerritories = 2, prestige = 80)

        assertTrue(result.isSuccess)
        assertTrue(result.promoted)
        assertEquals(PromotionStage.UNITED_CASTELLAN, result.stage)
        assertEquals(10, result.councilAuthorityBonus)
        assertNotNull(result.ceremonyTitle)
        assertTrue(result.ceremonyTitle!!.contains("연합 대표 승격 의식"))
        assertNotNull(result.ceremonyBody)
    }

    @Test
    fun `최종 단계인 광역 통합군주는 세 영지 연합과 명성 95를 요구한다`() {
        val engine: IPromotionEngine = PromotionEngine()

        val short = engine.promote(PromotionStage.UNITED_CASTELLAN, alliedTerritories = 2, prestige = 90)
        assertFalse(short.isSuccess)

        val result = engine.promote(PromotionStage.UNITED_CASTELLAN, alliedTerritories = 3, prestige = 95)
        assertTrue(result.isSuccess)
        assertEquals(PromotionStage.CITY_SOVEREIGN, result.stage)
        assertEquals(25, result.councilAuthorityBonus)
        assertTrue(result.ceremonyTitle!!.contains("통합군주"))

        // 최종 단계에서는 더 승격할 수 없다
        val finished = engine.promote(PromotionStage.CITY_SOVEREIGN, alliedTerritories = 3, prestige = 100)
        assertFalse(finished.isSuccess)
        assertTrue(finished.message.contains("마지막 단계"))
    }

    @Test
    fun `영지 탭 로드맵은 조약과 협정으로 우호 영지를 세고 승격 의식을 연다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        assertEquals(PromotionStage.DISTRICT_LORD, vm.uiState.value.promotionStage)

        // 인접 물류 협정 + 상호방위 조약 → 우호 2곳
        vm.chooseEventA(acceptPact = true)
        vm.proposeTreaty(TreatyType.DEFENSIVE_ALLIANCE)
        assertEquals(2, vm.uiState.value.alliedTerritories)

        // 승격 조건 판정·의식은 명성 조건과 함께 엔진이 처리한다
        val engine: IPromotionEngine = PromotionEngine()
        val evaluation = engine.evaluate(PromotionStage.DISTRICT_LORD, 2, 80)
        assertTrue("우호 2곳·명성 80이면 조건 충족", evaluation.isEligible)

        val result = engine.promote(PromotionStage.DISTRICT_LORD, alliedTerritories = 2, prestige = 80)
        assertTrue(result.isSuccess)
        assertEquals(PromotionStage.UNITED_CASTELLAN, result.stage)
        assertEquals(10, result.councilAuthorityBonus)
        assertNotNull(result.ceremonyBody)

        // 조건이 모자라면 의회 승격은 열리지 않는다
        val poor = SlgGameViewModel()
        poor.chooseLord(poor.uiState.value.lords.first().id)
        poor.chooseEventA(acceptPact = true)
        poor.proposeTreaty(TreatyType.DEFENSIVE_ALLIANCE)
        assertFalse("명성이 부족하면 승격 불가", poor.uiState.value.isPromotionEligible)
        val failed = poor.promote()
        assertFalse(failed.isSuccess)
        assertTrue(failed.message.contains("조건"))
    }

    @Test
    fun `승격 조건이 부족하면 승격되지 않고 안내 메시지만 남는다`() {
        val vm = SlgGameViewModel()

        val result = vm.promote()

        assertFalse(result.isSuccess)
        val after = vm.uiState.value
        assertEquals(PromotionStage.DISTRICT_LORD, after.promotionStage)
        assertEquals(0, after.councilAuthorityBonus)
        assertNotNull(after.promotionMessage)
        assertNull(after.promotionCeremony)
    }
}
