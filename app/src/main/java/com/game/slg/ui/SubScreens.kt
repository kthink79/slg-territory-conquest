package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.game.slg.core.model.PromotionStage

/**
 * 서브 메뉴 상세 화면 공통 틀 — ① «메인» ② 제목 ③ 탭 바 ④ 본문.
 * 탭 선택은 화면(메뉴)별로 기억된다.
 */
@Composable
fun SubScreenHost(
    menu: SubMenu,
    onBack: () -> Unit,
    content: @Composable (Int) -> Unit
) {
    val tabs = ScreenTabs.labelsOf(menu)
    var tabIndex by rememberSaveable(menu.id) { mutableIntStateOf(0) }
    val accent = Color(menu.accentHex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlgColors.Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0E1218))
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SlgBackButton(onBack = onBack)
            Spacer(Modifier.weight(1f))
            Text(
                "${menu.emoji} ${menu.title}",
                color = SlgColors.Text,
                fontSize = SlgType.BODY,
                fontWeight = FontWeight.Bold
            )
        }
        if (tabs.isNotEmpty()) {
            SlgTabBar(
                tabs = tabs,
                selectedIndex = tabIndex,
                accent = accent,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                onSelect = { tabIndex = it }
            )
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            content(tabIndex)
        }
    }
}

private fun branchAccent(tab: TechTab): Color = when (tab) {
    TechTab.ADMIN -> SlgColors.Success
    TechTab.SCIENCE -> Color(0xFF64B5F6)
    TechTab.CULTURE -> SlgColors.Violet
    TechTab.MILITARY -> SlgColors.Danger
}

/**
 * 서브 메뉴 「기술」 — 4대 계통 탭. 선택한 계통만 보여 준다.
 * 계통도는 Canvas 로 그리고, 연구 항목은 「이름 + 효과 1줄 + 비용」 만 보인다.
 */
@Composable
fun TechTreeScreen(
    state: SlgGameState,
    onResearch: (String) -> Unit,
    tabIndex: Int = 0
) {
    val tab = TechTab.entries[tabIndex.coerceIn(0, TechTab.entries.lastIndex)]
    SlgScreenFrame {
        val items = SlgWorld.TECH_ITEMS.filter { it.branch == tab.branch }.sortedBy { it.stage }
        val researched = items.map { state.researchedTechs.contains(it.id) }
            val done = researched.count { it }
            val accent = branchAccent(tab)
            val availableStage = items.indexOfFirst { !state.researchedTechs.contains(it.id) }
                .let { first ->
                    if (first <= 0) first
                    else if (state.researchedTechs.contains(items[first - 1].id)) first else -1
                }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SlgColors.Surface)
                    .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
                    .padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SlgPill(tab.branch, accent, filled = done == items.size)
                    Spacer(Modifier.width(6.dp))
                    Text("$done/${items.size}", color = accent, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    SlgPill("완료 $done/${SlgWorld.TECH_ITEMS.size}", SlgColors.Muted)
                }
                SlgStatBar(ratio = done.toFloat() / items.size.coerceAtLeast(1), color = accent, height = 5)
                SlgTechBranchCanvas(
                    stageNames = items.map { it.name },
                    researched = researched,
                    availableStage = availableStage,
                    accent = accent,
                    modifier = Modifier.fillMaxWidth().height(122.dp)
                )
                items.forEachIndexed { index, tech ->
                    val isDone = researched[index]
                    val prereqDone = index == 0 || researched[index - 1]
                    val canResearch = !isDone && prereqDone && state.gold >= tech.cost
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                if (isDone) accent.copy(alpha = 0.12f) else SlgColors.SurfaceAlt
                            )
                            .clickable(enabled = canResearch) { onResearch(tech.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isDone) accent else Color(0xFF262D3A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isDone) "✓" else "${index + 1}",
                                color = if (isDone) Color(0xFF10131A) else SlgColors.Muted,
                                fontSize = SlgType.MICRO,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(7.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                tech.name,
                                color = if (isDone) accent else SlgColors.Text,
                                fontSize = SlgType.LABEL,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                tech.effect,
                                color = SlgColors.Muted,
                                fontSize = SlgType.MICRO,
                                maxLines = 1
                            )
                        }
                        SlgPill("${tech.cost}G", if (state.gold >= tech.cost) SlgColors.Brass else SlgColors.Muted)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            when {
                                isDone -> "완료"
                                !prereqDone -> "선행"
                                !canResearch -> "부족"
                                else -> "연구"
                            },
                            color = when {
                                isDone -> accent
                                canResearch -> Color(0xFF10131A)
                                else -> SlgColors.Muted
                            },
                            fontSize = SlgType.MICRO,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (canResearch) accent else Color(0xFF262D3A))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
                SlgExpand(
                    summary = "${tab.branch} 계통 설명",
                    detail = items.joinToString("\n") { "${it.stage}단계 ${it.name} — ${it.description}" },
                    accent = accent
                )
            }
    }
}

/** 서브 메뉴 「사건」 — 탭 3개: 유민 / 상인 / 정찰대 (선택한 사건만 보인다) */
@Composable
fun EventScenarioScreen(
    state: SlgGameState,
    onChooseEventA: (Boolean) -> Unit,
    onChooseEventB: (Boolean) -> Unit,
    onChooseRefugee: (String) -> Unit,
    tabIndex: Int = 0
) {
    val pendingCount = listOf(state.refugeeChoice, state.eventAChoice, state.eventBChoice)
        .count { it == null }
    SlgScreenFrame {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SlgPill("D+14", SlgColors.Stone)
            Spacer(Modifier.width(5.dp))
            SlgPill(
                if (pendingCount == 0) "모두 처리" else "미결 ${pendingCount}건",
                if (pendingCount == 0) SlgColors.Success else SlgColors.Danger
            )
        }
        when (EventTab.entries[tabIndex.coerceIn(0, EventTab.entries.lastIndex)]) {
            EventTab.REFUGEE -> RefugeeCrisisCard(state = state, onChooseRefugee = onChooseRefugee)
            EventTab.MERCHANT -> EventMerchantCard(state = state, onChooseEventA = onChooseEventA)
            EventTab.SCOUT -> EventScoutCard(state = state, onChooseEventB = onChooseEventB)
        }
        SlgSectionCard(title = "사건 기록", accent = SlgColors.Muted) {
            SlgLogBox(state.log, maxLines = 3)
        }
    }
}

/** 서브 메뉴 「로드맵」 — 승격 탭 3개: 로드맵 / 조건 / 경로 */
@Composable
fun PromotionScreen(
    state: SlgGameState,
    onPromote: () -> Unit,
    onDismissCeremony: () -> Unit,
    tabIndex: Int = 0
) {
    SlgScreenFrame {
        when (PromotionTab.entries[tabIndex.coerceIn(0, PromotionTab.entries.lastIndex)]) {
            PromotionTab.ROADMAP -> {
                PromotionCeremonyBanner(state = state, onDismissCeremony = onDismissCeremony)
                PromotionRoadmapCard(state = state, onPromote = onPromote)
            }

            PromotionTab.CONDITIONS -> {
                SlgSectionCard(
                    title = "승격 조건",
                    badge = state.promotionStage.displayName,
                    accent = SlgColors.Steel
                ) {
                    PromotionStage.entries.forEach { stage ->
                        val achieved = stage.order <= state.promotionStage.order
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    if (achieved) SlgColors.Brass.copy(alpha = 0.12f) else SlgColors.SurfaceAlt
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (achieved) "●" else "○",
                                color = if (achieved) SlgColors.Brass else SlgColors.Line,
                                fontSize = SlgType.LABEL
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                stage.displayName,
                                color = if (achieved) SlgColors.Brass else SlgColors.Text,
                                fontSize = SlgType.LABEL,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text(
                                "🤝 ${stage.requiredAlliedTerritories} · ⭐ ${stage.requiredPrestige}",
                                color = SlgColors.Muted,
                                fontSize = SlgType.MICRO
                            )
                        }
                    }
                }
            }

            PromotionTab.PATH -> {
                SlgSectionCard(title = "승격 경로", accent = SlgColors.Brass) {
                    listOf(
                        Triple("🏙", "거점 호족", "${state.regionName}에서 시작"),
                        Triple("🏛", "연합 대표", "우호 거점 2곳 · 명성 80"),
                        Triple("👑", "광역 통합군주", "우호 거점 3곳 · 명성 95")
                    ).forEach { (icon, title, caption) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SlgColors.SurfaceAlt)
                                .padding(horizontal = 9.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(icon, fontSize = SlgType.TITLE)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    title,
                                    color = SlgColors.Text,
                                    fontSize = SlgType.LABEL,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(caption, color = SlgColors.Muted, fontSize = SlgType.MICRO)
                            }
                        }
                    }
                    SlgExpand(
                        summary = "우호·동맹 영지를 얻는 방법",
                        detail = "① 인접 물류 상인회 야간 협정(사건 탭 「상인」) ② 경계 세력 속국 조공 조약 " +
                            "③ 이웃 자치구 상호방위 조약 — 조약은 「외교」 화면에서 체결합니다.",
                        accent = SlgColors.Steel
                    )
                }
            }
        }
    }
}
