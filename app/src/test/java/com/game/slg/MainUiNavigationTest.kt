package com.game.slg

import com.game.slg.ui.AlertSeverity
import com.game.slg.ui.MainNavigationState
import com.game.slg.ui.MainRoute
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.StatusAlertBuilder
import com.game.slg.ui.SubMenu
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 메인 3단 화면 — 서브 메뉴 이동 상태 · 지도 영지 선택 · 현재 상태 경보 테스트.
 */
class MainUiNavigationTest {

    @Test
    fun `앱을 열면 메인 3단 화면에서 시작한다`() {
        val navigation = MainNavigationState()

        assertTrue(navigation.isHome)
        assertNull(navigation.openedMenu)
        assertEquals(MainRoute.Home, navigation.route)
    }

    @Test
    fun `서브 메뉴를 누르면 해당 상세 화면이 열린다`() {
        SubMenu.detailMenus.forEach { menu ->
            val opened = MainNavigationState().open(menu)

            assertFalse("$menu 는 상세 화면이 있어야 한다", opened.isHome)
            assertEquals(menu, opened.openedMenu)
        }
    }

    @Test
    fun `메인 3단 화면 버튼으로 홈에 돌아온다`() {
        val opened = MainNavigationState().open(SubMenu.WAR)
        val back = opened.back()

        assertTrue(back.isHome)
        assertNull(back.openedMenu)
    }

    @Test
    fun `절기 진행은 실행형이라 화면을 열지 않는다`() {
        assertTrue(SubMenu.SEASON.isAction)
        assertFalse(SubMenu.SEASON.opensDetail)
        assertTrue(MainNavigationState().open(SubMenu.SEASON).isHome)
    }

    @Test
    fun `서브 메뉴는 7개 상세 화면과 1개 실행 항목으로 구성된다`() {
        assertEquals(8, SubMenu.entries.size)
        assertEquals(7, SubMenu.detailMenus.size)
        assertEquals(SubMenu.entries.size, SubMenu.entries.map { it.id }.distinct().size)
        assertTrue(SubMenu.entries.all { it.label.isNotBlank() && it.caption.isNotBlank() })
        // 기존 4탭 기능이 모두 서브 메뉴로 남아 있다
        listOf(
            SubMenu.TERRITORY, SubMenu.TECH, SubMenu.COUNCIL, SubMenu.WAR,
            SubMenu.DIPLOMACY, SubMenu.EVENTS, SubMenu.PROMOTION
        ).forEach { assertTrue("$it 누락", SubMenu.detailMenus.contains(it)) }
    }

    @Test
    fun `지도에서 영지를 선택하면 상태에 반영된다`() {
        val vm = SlgGameViewModel()

        vm.selectTerritory("4111156000")

        assertEquals("4111156000", vm.uiState.value.selectedTerritoryCode)
        assertEquals("광교동", vm.uiState.value.selectedTerritory?.name)
        assertEquals("광교동", vm.uiState.value.selectedMapNode?.name)
    }

    @Test
    fun `지도에 없는 영지는 선택되지 않는다`() {
        val vm = SlgGameViewModel()
        vm.selectTerritory("4111156000")

        vm.selectTerritory("9999999999")

        assertEquals("4111156000", vm.uiState.value.selectedTerritoryCode)
    }

    @Test
    fun `첫 화면의 선택 영지는 본진이다`() {
        val vm = SlgGameViewModel()

        assertEquals("4111154000", vm.uiState.value.selectedTerritoryCode)
        assertTrue(vm.uiState.value.selectedMapNode?.isHome == true)
    }

    @Test
    fun `뷰모델의 서브 메뉴 이동이 화면 경로에 반영된다`() {
        val vm = SlgGameViewModel()
        assertTrue(vm.navigation.value.isHome)

        vm.openSubMenu(SubMenu.COUNCIL)
        assertEquals(SubMenu.COUNCIL, vm.navigation.value.openedMenu)

        vm.backToMain()
        assertTrue(vm.navigation.value.isHome)
    }

    @Test
    fun `첫 상태에는 미결 사건 경보가 모두 뜬다`() {
        val vm = SlgGameViewModel()
        val alerts = StatusAlertBuilder.build(vm.uiState.value)
        val ids = alerts.map { it.id }

        assertTrue(ids.contains("alert_refugee"))
        assertTrue(ids.contains("alert_scout"))
        assertTrue(ids.contains("alert_merchant"))
        assertTrue(ids.contains("alert_bills"))
        assertTrue(ids.contains("alert_night_meeting"))
        // 경보는 모두 상세 화면이 있는 서브 메뉴로 연결된다
        assertTrue(alerts.all { it.target.opensDetail })
        assertEquals(AlertSeverity.CRITICAL, alerts.first { it.id == "alert_refugee" }.severity)
    }

    @Test
    fun `유민 사건을 처리하면 경보가 사라진다`() {
        val vm = SlgGameViewModel()
        vm.chooseRefugeeCrisis("REFUGEE_A_OPEN")

        val ids = StatusAlertBuilder.build(vm.uiState.value).map { it.id }

        assertFalse(ids.contains("alert_refugee"))
    }

    @Test
    fun `의회 표결을 마치면 표결 대기 경보가 줄어든다`() {
        val vm = SlgGameViewModel()
        assertEquals(2, vm.uiState.value.bills.count { it.passed == null })

        vm.voteBill("bill_levy")

        val pending = vm.uiState.value.bills.count { it.passed == null }
        val billAlert = StatusAlertBuilder.build(vm.uiState.value).first { it.id == "alert_bills" }
        assertEquals(1, pending)
        assertTrue(billAlert.title.contains("1건"))
    }

    @Test
    fun `군량이 바닥나면 긴급 경보가 추가된다`() {
        val state = SlgGameViewModel().uiState.value.copy(food = 320)
        val foodAlert = StatusAlertBuilder.build(state).first { it.id == "alert_food" }

        assertEquals(AlertSeverity.CRITICAL, foodAlert.severity)
        assertEquals(SubMenu.TERRITORY, foodAlert.target)
    }

    @Test
    fun `우호 영지와 명성이 조건을 채우면 승격 경보가 뜬다`() {
        val base = SlgGameViewModel().uiState.value
        val ready = base.copy(alliedTerritories = 3, prestige = 88)

        assertTrue(ready.isPromotionEligible)
        val promotionAlert = StatusAlertBuilder.build(ready).first { it.id == "alert_promotion" }
        assertEquals(SubMenu.PROMOTION, promotionAlert.target)
        assertEquals(AlertSeverity.INFO, promotionAlert.severity)
    }

    @Test
    fun `원천동 협정과 두 조약으로 우호 영지 3곳을 확보한다`() {
        val vm = SlgGameViewModel()

        vm.chooseEventA(acceptPact = true)
        vm.proposeTreaty(com.game.slg.core.model.TreatyType.VASSAL_TRIBUTARY)
        vm.proposeTreaty(com.game.slg.core.model.TreatyType.DEFENSIVE_ALLIANCE)

        assertEquals(3, vm.uiState.value.alliedTerritories)
    }
}
