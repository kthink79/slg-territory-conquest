package com.game.slg

import com.game.slg.ui.AlertSeverity
import com.game.slg.ui.CouncilTab
import com.game.slg.ui.DiplomacyTab
import com.game.slg.ui.DomainTab
import com.game.slg.ui.EventTab
import com.game.slg.ui.HeroTab
import com.game.slg.ui.HomeLayout
import com.game.slg.ui.MainInfoPanel
import com.game.slg.ui.MainNavigationState
import com.game.slg.ui.MainTab
import com.game.slg.ui.PromotionTab
import com.game.slg.ui.ScreenTabs
import com.game.slg.ui.SlgGameViewModel
import com.game.slg.ui.StatusAlertBuilder
import com.game.slg.ui.TechTab
import com.game.slg.ui.TerritoryTab
import com.game.slg.ui.WarTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 하단 탭 · 상단 탭 이동 · 경보 칩 · 글자 크기 테스트.
 *
 * 2026-11-18 개편: 메뉴 8칸 그리드를 없애고 하단 탭 5개 + 상단 탭 2~7개로 나눴다.
 */
class MainUiNavigationTest {

    @Test
    fun `앱을 열면 지도 탭에서 시작한다`() {
        val navigation = MainNavigationState()

        assertTrue(navigation.isHome)
        assertEquals(MainTab.MAP, navigation.tab)
        assertNull(navigation.openedTab)
    }

    @Test
    fun `하단 탭 5개가 모두 화면을 연다`() {
        MainTab.bottomOrder.filter { it != MainTab.MAP }.forEach { tab ->
            val opened = MainNavigationState().open(tab)

            assertFalse("$tab 는 지도가 아니어야 한다", opened.isHome)
            assertEquals(tab, opened.openedTab)
            assertEquals(0, opened.sub)
        }
        assertEquals(5, MainTab.bottomOrder.size)
    }

    @Test
    fun `지도로 돌아오기`() {
        val opened = MainNavigationState().open(MainTab.WAR, WarTab.REPORT.ordinal)
        assertEquals(WarTab.REPORT.ordinal, opened.sub)

        val back = opened.back()
        assertTrue(back.isHome)
        assertNull(back.openedTab)
    }

    @Test
    fun `하단 탭 라벨은 2글자이고 이모지를 가진다`() {
        MainTab.entries.forEach { tab ->
            assertTrue("${tab.label} 이 너무 길다", tab.label.length <= 2)
            assertTrue("${tab.label} 이모지 없음", tab.emoji.isNotBlank())
            assertTrue("${tab.title} 제목 없음", tab.title.isNotBlank())
            assertEquals(tab.id, tab.id.lowercase())
        }
    }

    @Test
    fun `상단 탭은 화면마다 2~8개이고 라벨은 4글자 이하다`() {
        MainTab.entries.filter { it != MainTab.MAP }.forEach { tab ->
            val tabs = ScreenTabs.labelsOf(tab)
            assertTrue("$tab 탭이 없다", tabs.isNotEmpty())
            assertTrue("$tab 탭이 8개를 넘는다", tabs.size <= 8)
            assertTrue("$tab 탭 라벨이 길다", tabs.all { it.length <= 4 })
            assertEquals("$tab 탭 중복", tabs.size, tabs.distinct().size)
        }
        assertEquals(emptyList<String>(), ScreenTabs.labelsOf(MainTab.MAP))
    }

    @Test
    fun `영지 탭은 8개 상단 탭으로 나뉜다`() {
        assertEquals(
            listOf("내정", "지표", "시설", "절기", "기술", "의회", "승격", "사건"),
            DomainTab.entries.map { it.label }
        )
        assertEquals(8, ScreenTabs.labelsOf(MainTab.DOMAIN).size)
    }

    @Test
    fun `기존 세부 탭 구성이 유지된다`() {
        assertEquals(listOf("유민", "상인", "정찰대"), EventTab.entries.map { it.label })
        assertEquals(4, TerritoryTab.entries.size)
        assertEquals(2, CouncilTab.entries.size)
        assertEquals(4, WarTab.entries.size)
        assertEquals(3, DiplomacyTab.entries.size)
        assertEquals(3, PromotionTab.entries.size)
        assertEquals(2, HeroTab.entries.size)
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
    fun `지도에서 영지를 선택하면 상태에 반영된다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val home = vm.uiState.value.mapNodes.first { it.isHome }

        vm.selectTerritory(home.code)

        assertEquals(home.code, vm.uiState.value.selectedTerritoryCode)
        assertEquals("탕정면", vm.uiState.value.selectedTerritory?.name)
        assertEquals("탕정면", vm.uiState.value.selectedMapNode?.name)
    }

    @Test
    fun `지도에 없는 영지는 선택되지 않는다`() {
        val vm = SlgGameViewModel()
        vm.chooseLord(vm.uiState.value.lords.first().id)
        val home = vm.uiState.value.mapNodes.first { it.isHome }
        vm.selectTerritory(home.code)

        vm.selectTerritory("9999999999")

        assertEquals(home.code, vm.uiState.value.selectedTerritoryCode)
    }

    @Test
    fun `첫 화면의 선택 영지는 본진이다`() {
        val vm = SlgGameViewModel()

        assertEquals("4420033000", vm.uiState.value.selectedTerritoryCode)
        assertTrue(vm.uiState.value.selectedMapNode?.isHome == true)
        assertEquals("탕정면", vm.uiState.value.selectedMapNode?.name)
    }

    @Test
    fun `뷰모델의 탭 이동이 화면 상태에 반영된다`() {
        val vm = SlgGameViewModel()
        assertTrue(vm.navigation.value.isHome)

        vm.openTab(MainTab.DOMAIN, DomainTab.COUNCIL.ordinal)
        assertEquals(MainTab.DOMAIN, vm.navigation.value.openedTab)
        assertEquals(DomainTab.COUNCIL.ordinal, vm.navigation.value.sub)

        vm.selectSubTab(DomainTab.TECH.ordinal)
        assertEquals(DomainTab.TECH.ordinal, vm.navigation.value.sub)

        vm.backToMain()
        assertTrue(vm.navigation.value.isHome)
    }

    @Test
    fun `상단 탭 인덱스는 범위를 벗어나지 않는다`() {
        val navigation = MainNavigationState().open(MainTab.HERO, 99)

        assertEquals(HeroTab.entries.lastIndex, navigation.sub)
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
        assertTrue(alerts.all { it.targetSub >= 0 })
        assertEquals(AlertSeverity.CRITICAL, alerts.first { it.id == "alert_refugee" }.severity)
    }

    @Test
    fun `경보 칩 문구는 짧고 아이콘을 가진다`() {
        val alerts = StatusAlertBuilder.build(SlgGameViewModel().uiState.value)
        alerts.forEach { alert ->
            assertTrue("${alert.short} 칩 문구가 길다", alert.short.length <= 8)
            assertTrue("${alert.short} 아이콘 없음", alert.icon.isNotBlank())
            assertTrue("${alert.title} 제목이 길다", alert.title.length <= 20)
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
        assertEquals(MainTab.DOMAIN, billAlert.target)
        assertEquals(DomainTab.COUNCIL.ordinal, billAlert.targetSub)
    }

    @Test
    fun `군량이 바닥나면 긴급 경보가 추가된다`() {
        val state = SlgGameViewModel().uiState.value.copy(food = 320)
        val foodAlert = StatusAlertBuilder.build(state).first { it.id == "alert_food" }

        assertEquals(AlertSeverity.CRITICAL, foodAlert.severity)
        assertEquals(MainTab.DOMAIN, foodAlert.target)
    }

    @Test
    fun `우호 영지와 명성이 조건을 채우면 승격 경보가 뜬다`() {
        val base = SlgGameViewModel().uiState.value
        val ready = base.copy(alliedTerritories = 3, prestige = 88)

        assertTrue(ready.isPromotionEligible)
        val promotionAlert = StatusAlertBuilder.build(ready).first { it.id == "alert_promotion" }
        assertEquals(MainTab.DOMAIN, promotionAlert.target)
        assertEquals(DomainTab.PROMOTION.ordinal, promotionAlert.targetSub)
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
    fun `지도 탭이 화면 절반 이상을 차지한다`() {
        // 360×800dp 기준: 상단 56 + 상태 120 + 하단 탭 76 을 빼면 지도가 남는다
        assertEquals(548, HomeLayout.mapHeight(800))
        assertTrue(HomeLayout.mapHeight(800) > 800 / 2)
        assertTrue(HomeLayout.mapHeight(640) >= 320)
        assertEquals(0, HomeLayout.mapHeight(120))
    }

    @Test
    fun `글자 크기는 최소 13sp 이상으로 키웠다`() {
        assertTrue(com.game.slg.ui.SlgType.MICRO.value >= 13f)
        assertTrue(com.game.slg.ui.SlgType.LABEL.value >= 14f)
        assertTrue(com.game.slg.ui.SlgType.BODY_SMALL.value >= 15f)
        assertTrue(com.game.slg.ui.SlgType.BODY.value >= 16f)
        assertTrue(com.game.slg.ui.SlgType.TITLE.value >= 19f)
        assertTrue(com.game.slg.ui.SlgType.HERO.value >= 24f)
    }

    @Test
    fun `상단 1줄은 자원 3종만 보여 주고 상세 정보는 정보 패널로 간다`() {
        val state = SlgGameViewModel().uiState.value
        val top = MainInfoPanel.topSummary(state)
        val detail = MainInfoPanel.detailRows(state)

        assertEquals(4, top.size)
        assertTrue(top.all { it.value.length <= 7 })
        assertTrue(detail.size >= 7)
        assertTrue(detail.all { it.label.length <= 3 })
        assertEquals(MainInfoPanel.BUTTON_LABEL, "📊 정보")
        assertEquals(StatusAlertBuilder.build(state).size, MainInfoPanel.alertCount(state))
        assertTrue(MainInfoPanel.hasUrgentAlert(state))
    }

    @Test
    fun `정보 패널은 지역 연합과 영웅 수를 함께 보여 준다`() {
        val state = SlgGameViewModel().uiState.value
        val labels = MainInfoPanel.detailRows(state).map { it.label }

        assertTrue(labels.contains("연합"))
        assertTrue(labels.contains("영웅"))
    }
}
