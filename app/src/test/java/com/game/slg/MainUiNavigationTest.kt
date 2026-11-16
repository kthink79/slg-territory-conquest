package com.game.slg

import com.game.slg.ui.AlertSeverity
import com.game.slg.ui.CouncilTab
import com.game.slg.ui.DiplomacyTab
import com.game.slg.ui.EventTab
import com.game.slg.ui.HomeLayout
import com.game.slg.ui.MainInfoPanel
import com.game.slg.ui.MainNavigationState
import com.game.slg.ui.MainRoute
import com.game.slg.ui.PromotionTab
import com.game.slg.ui.ScreenTabs
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.StatusAlertBuilder
import com.game.slg.ui.SubMenu
import com.game.slg.ui.TechTab
import com.game.slg.ui.TerritoryTab
import com.game.slg.ui.WarTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 메인 화면 · 서브 메뉴 이동 · 탭 구성 · 경보 칩 테스트.
 */
class MainUiNavigationTest {

    @Test
    fun `앱을 열면 메인 화면에서 시작한다`() {
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
    fun `메인 버튼으로 홈에 돌아온다`() {
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
        listOf(
            SubMenu.TERRITORY, SubMenu.TECH, SubMenu.COUNCIL, SubMenu.WAR,
            SubMenu.DIPLOMACY, SubMenu.EVENTS, SubMenu.PROMOTION
        ).forEach { assertTrue("$it 누락", SubMenu.detailMenus.contains(it)) }
    }

    @Test
    fun `메뉴 타일 라벨은 3글자 이내이고 이모지 아이콘을 가진다`() {
        SubMenu.entries.forEach { menu ->
            assertTrue("${menu.label} 이 너무 길다", menu.label.length <= 3)
            assertTrue("${menu.label} 이모지 없음", menu.emoji.isNotBlank())
            assertTrue("${menu.title} 제목 없음", menu.title.isNotBlank())
        }
    }

    @Test
    fun `모든 상세 화면은 2~4개의 짧은 탭을 가진다`() {
        SubMenu.detailMenus.forEach { menu ->
            val tabs = ScreenTabs.labelsOf(menu)
            assertTrue("$menu 탭이 없다", tabs.isNotEmpty())
            assertTrue("$menu 탭이 4개를 넘는다", tabs.size <= ScreenTabs.MAX_TABS)
            assertTrue("$menu 탭 라벨이 길다", tabs.all { it.length <= 3 })
            assertEquals("$menu 탭 중복", tabs.size, tabs.distinct().size)
        }
        assertEquals(emptyList<String>(), ScreenTabs.labelsOf(SubMenu.SEASON))
    }

    @Test
    fun `테크트리 탭은 4대 계통과 1대1로 맞물린다`() {
        assertEquals(4, TechTab.entries.size)
        assertEquals(listOf("내정", "과학기술", "문화", "군사"), TechTab.entries.map { it.branch })
        TechTab.entries.forEach { tab ->
            assertEquals(
                "4단계 계통",
                4,
                com.game.slg.ui.SlgWorld.TECH_ITEMS.count { it.branch == tab.branch }
            )
        }
    }

    @Test
    fun `사건 탭은 세 사건과 대응한다`() {
        assertEquals(listOf("유민", "상인", "정찰대"), EventTab.entries.map { it.label })
        assertEquals(3, TerritoryTab.entries.size)
        assertEquals(2, CouncilTab.entries.size)
        assertEquals(3, WarTab.entries.size)
        assertEquals(3, DiplomacyTab.entries.size)
        assertEquals(3, PromotionTab.entries.size)
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
        assertTrue(alerts.all { it.target.opensDetail })
        assertEquals(AlertSeverity.CRITICAL, alerts.first { it.id == "alert_refugee" }.severity)
    }

    @Test
    fun `경보 칩 문구는 짧고 아이콘을 가진다`() {
        val alerts = StatusAlertBuilder.build(SlgGameViewModel().uiState.value)
        alerts.forEach { alert ->
            assertTrue("${alert.short} 칩 문구가 길다", alert.short.length <= 8)
            assertTrue("${alert.short} 아이콘 없음", alert.icon.isNotBlank())
            assertTrue("${alert.title} 제목이 길다", alert.title.length <= 16)
        }
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

    @Test
    fun `메인 화면에서 지도가 가장 큰 영역을 차지한다`() {
        // 360×800dp 기준: 상단 40 + 상태 88 + 메뉴 158 을 빼면 지도가 남논다
        assertEquals(514, HomeLayout.mapHeight(800))
        assertTrue(HomeLayout.mapHeight(800) > 800 / 2)
        assertTrue(HomeLayout.mapHeight(640) >= 320)
        assertEquals(0, HomeLayout.mapHeight(120))
    }

    @Test
    fun `글자 크기는 최소 10sp 이상으로 읽기 쉽게 유지된다`() {
        assertTrue(com.game.slg.ui.SlgType.MICRO.value >= 10f)
        assertTrue(com.game.slg.ui.SlgType.LABEL.value >= 11f)
        assertTrue(com.game.slg.ui.SlgType.BODY.value >= 13f)
    }

    @Test
    fun `상단 1줄은 자원 3종만 보여 주고 상세 정보는 정보 패널로 간다`() {
        val state = SlgGameViewModel().uiState.value
        val top = MainInfoPanel.topSummary(state)
        val detail = MainInfoPanel.detailRows(state)

        assertEquals(4, top.size)
        assertTrue(top.all { it.value.length <= 7 })
        // 상세 행은 2열 그리드로 접히므로 홈보다 훨씬 많은 정보를 담을 수 있다
        assertTrue(detail.size >= 7)
        assertTrue(detail.all { it.label.length <= 3 })
        assertEquals(MainInfoPanel.BUTTON_LABEL, "📊 정보")
        assertEquals(StatusAlertBuilder.build(state).size, MainInfoPanel.alertCount(state))
        assertTrue(MainInfoPanel.hasUrgentAlert(state))
    }
}
