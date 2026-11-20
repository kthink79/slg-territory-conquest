package com.game.slg.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.game.slg.R
import com.game.slg.core.hero.Hero
import com.game.slg.core.hero.HeroGrade
import com.game.slg.core.hero.HeroRole
import com.game.slg.core.hero.HeroRoster
import com.game.slg.core.hero.HeroStage

/**
 * 영웅 탭 — 보유 영웅 배치(배치)와 미보유 영웅 영입(영입).
 *
 * 초상화 250장(2:3)은 `drawable-nodpi/slg_hero_001~250.jpg` 로 탑재되어 있고,
 * 카드에서는 `ContentScale.Fit` 으로 잘리지 않게 보여 준다.
 */
@Composable
fun HeroScreen(
    state: SlgGameState,
    onScout: (String) -> Unit,
    onPersuade: (String) -> Unit,
    onHire: (String) -> Unit,
    onAssign: (String, HeroRole) -> Unit,
    onUnassign: (String) -> Unit,
    tabIndex: Int = 0,
    onSearch: () -> Unit = {},
    onAcceptVisit: () -> Unit = {},
    onDeclineVisit: () -> Unit = {}
) {
    SlgScreenFrame {
        when (HeroTab.entries[tabIndex.coerceIn(0, HeroTab.entries.lastIndex)]) {
            HeroTab.PLACE -> HeroPlaceSection(state, onAssign, onUnassign)
            HeroTab.RECRUIT -> HeroRecruitSection(
                state = state,
                onScout = onScout,
                onPersuade = onPersuade,
                onHire = onHire,
                onSearch = onSearch,
                onAcceptVisit = onAcceptVisit,
                onDeclineVisit = onDeclineVisit
            )
        }
    }
}

/** 초상화 리소스 — slg_hero_001 ~ slg_hero_250 (drawable-nodpi, 540x810 JPEG) */
private val portraitResIds: Map<String, Int> by lazy {
    R.drawable::class.java.fields
        .filter { it.name.startsWith("slg_hero_") }
        .associate { it.name to it.getInt(null) }
}

/** 영웅 id(`hero_07` · `hero_007`)를 3자리 초상화 이름으로 맞춘다 */
fun heroPortraitRes(heroId: String): Int {
    val number = heroId.substringAfterLast('_').toIntOrNull() ?: return R.drawable.slg_event_scout
    return portraitResIds["slg_hero_" + number.toString().padStart(3, '0')]
        ?: R.drawable.slg_event_scout
}

/** 2:3 세로 초상화 — 잘리지 않게 Fit */
@Composable
fun HeroPortrait(hero: Hero, widthDp: Int = 88, modifier: Modifier = Modifier) {
    val heightDp = widthDp * 3 / 2
    Box(
        modifier = modifier
            .width(widthDp.dp)
            .height(heightDp.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SlgColors.SurfaceAlt)
            .border(1.dp, Color(hero.role.accentHex).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
    ) {
        Image(
            painter = painterResource(heroPortraitRes(hero.id)),
            contentDescription = hero.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(heightDp.dp)
        )
    }
}

// ────────────────────────────────────────────────────────────────
// 배치
// ────────────────────────────────────────────────────────────────

@Composable
private fun HeroPlaceSection(
    state: SlgGameState,
    onAssign: (String, HeroRole) -> Unit,
    onUnassign: (String) -> Unit
) {
    val roster = state.heroes
    val bonus = state.heroBonus
    var unplacedShown by remember { mutableStateOf(12) }

    SlgSectionCard(
        title = "배치 보너스",
        badge = "${roster.placedCount}명 배치",
        accent = SlgColors.Violet
    ) {
        Text(
            bonus.summary(),
            color = if (bonus.isEmpty) SlgColors.Muted else SlgColors.Text,
            fontSize = SlgType.BODY_SMALL,
            fontWeight = FontWeight.Bold,
            lineHeight = SlgLine.BODY
        )
        Text(
            "보유 ${roster.ownedCount}명 · 배치 ${roster.placedCount}명 · 대기 ${roster.unplaced.size}명",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
    }

    SlgSectionCard(title = "영지 슬롯", badge = "내정·기술·상업·작전", accent = SlgColors.Brass) {
        HeroRole.primarySlots.forEach { role ->
            HeroSlotRow(state = state, role = role, onUnassign = onUnassign)
        }
    }

    SlgSectionCard(title = "외교 슬롯", badge = "외교·정보", accent = SlgColors.Steel) {
        HeroRole.supportSlots.forEach { role ->
            HeroSlotRow(state = state, role = role, onUnassign = onUnassign)
        }
    }

    if (roster.unplaced.isNotEmpty()) {
        SlgSectionCard(
            title = "대기 영웅",
            badge = "${roster.unplaced.size}명",
            accent = SlgColors.Success
        ) {
            roster.unplaced.take(unplacedShown).forEach { hero ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlgColors.SurfaceAlt)
                        .padding(horizontal = 9.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroPortrait(hero = hero, widthDp = 40)
                    Spacer(Modifier.width(9.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(hero.name, color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                        Text(
                            "${hero.role.label} · ${hero.effectLabel}",
                            color = SlgColors.Muted,
                            fontSize = SlgType.MICRO,
                            lineHeight = SlgLine.MICRO
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(hero.role.accentHex).copy(alpha = 0.2f))
                            .border(1.dp, Color(hero.role.accentHex).copy(alpha = 0.7f), RoundedCornerShape(9.dp))
                            .clickable { onAssign(hero.id, hero.role) }
                            .padding(horizontal = 11.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "배치",
                            color = Color(hero.role.accentHex),
                            fontSize = SlgType.LABEL,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Text(
                "배치는 역할이 같은 슬롯에만 앉습니다. 이미 앉은 영웅은 대기로 내려갑니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            if (roster.unplaced.size > unplacedShown) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SlgColors.Surface)
                        .clickable { unplacedShown += 12 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "더 보기 — 대기 ${roster.unplaced.size - unplacedShown}명",
                        color = SlgColors.Success,
                        fontSize = SlgType.MICRO,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }

    SlgExpand(
        summary = "슬롯별 효과 정리",
        detail = "내정 = 절기 자금 · 기술 = 연구 비용 할인 · 상업 = 절기 보급 · 작전 = 작전 전투력 · " +
            "외교 = 설득력(영웅 설득 명성 +) · 정보 = 첩보력. 효과는 배치한 동안 계속 적용됩니다.",
        accent = SlgColors.Violet
    )
}

@Composable
private fun HeroSlotRow(state: SlgGameState, role: HeroRole, onUnassign: (String) -> Unit) {
    val hero = state.heroes.heroIn(role)
    val accent = Color(role.accentHex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(if (hero != null) accent.copy(alpha = 0.12f) else SlgColors.SurfaceAlt)
            .border(
                1.dp,
                if (hero != null) accent.copy(alpha = 0.5f) else SlgColors.Line.copy(alpha = 0.6f),
                RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 9.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 78.dp, height = 32.dp)
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${role.emoji} ${role.label}",
                color = accent,
                fontSize = SlgType.MICRO,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        if (hero == null) {
            Column(modifier = Modifier.weight(1f)) {
                Text("빈 슬롯", color = SlgColors.Muted, fontSize = SlgType.LABEL, fontWeight = FontWeight.Bold)
                Text(
                    "${role.effectLabel} 대기 중 · 영입에서 채우세요",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
            }
        } else {
            HeroPortrait(hero = hero, widthDp = 38)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(hero.name, color = SlgColors.Text, fontSize = SlgType.LABEL, fontWeight = FontWeight.ExtraBold)
                Text(
                    hero.effectLabel,
                    color = accent,
                    fontSize = SlgType.MICRO,
                    lineHeight = SlgLine.MICRO
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(SlgColors.Surface)
                    .clickable { onUnassign(hero.id) }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Text("내리기", color = SlgColors.Muted, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────
// 영입 — 탐색 → 설득 → 고용
// ────────────────────────────────────────────────────────────────

/** 영입 화면에 한 번에 보여 주는 카드 수 — 250명 로스터를 페이지로 나눈다 */
private const val RECRUIT_PAGE = 8

@Composable
private fun HeroRecruitSection(
    state: SlgGameState,
    onScout: (String) -> Unit,
    onPersuade: (String) -> Unit,
    onHire: (String) -> Unit,
    onSearch: () -> Unit,
    onAcceptVisit: () -> Unit,
    onDeclineVisit: () -> Unit
) {
    val roster = state.heroes
    val diplomacy = state.heroBonus.diplomacyBonus
    val known = state.knownHeroes.filterNot { roster.recordOf(it.id).owned }
    var roleFilter by remember { mutableStateOf<HeroRole?>(null) }
    var gradeFilter by remember { mutableStateOf<HeroGrade?>(null) }
    var shown by remember { mutableStateOf(RECRUIT_PAGE) }
    val undiscovered = state.undiscoveredHeroCount

    val filtered = known
        .filter { roleFilter == null || it.role == roleFilter }
        .filter { gradeFilter == null || it.grade == gradeFilter }
        .sortedWith(compareByDescending<Hero> { it.grade.rank }.thenByDescending { it.stats.total })

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SlgSummaryChip("💰", "${state.gold}", SlgColors.Brass, Modifier.weight(1f))
        SlgSummaryChip("⭐", "명성 ${state.prestige}", SlgColors.Violet, Modifier.weight(1f))
        SlgSummaryChip("🤝", "설득 +$diplomacy", SlgColors.Steel, Modifier.weight(1f))
    }

    // 찾아온 영웅 — 스스로 합류를 청하는 방문 이벤트
    state.heroVisit?.let { visit ->
        SlgSectionCard(title = "찾아온 영웅", badge = visit.season.substringBefore(" ("), accent = SlgColors.Success) {
            Text(visit.summary, color = SlgColors.Text, fontSize = SlgType.BODY_SMALL, fontWeight = FontWeight.Bold)
            Text(
                visit.requestText,
                color = SlgColors.Text.copy(alpha = 0.85f),
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
            SlgChoiceCard(
                title = "합류 수락",
                icon = "🤝",
                effects = listOf(Effect("✓", "무상 합류", SlgColors.Success)),
                accent = SlgColors.Success,
                onClick = onAcceptVisit
            )
            SlgChoiceCard(
                title = "거절",
                icon = "✋",
                effects = listOf(Effect("⭐", "-1", SlgColors.Danger)),
                accent = SlgColors.Muted,
                onClick = onDeclineVisit
            )
        }
    }

    SlgSectionCard(title = "영웅 탐색", badge = "지역·절기 랜덤", accent = SlgColors.Brass) {
        Text(
            "${HeroRoster.ALL.size}명 전체 명부는 공개되지 않습니다. ${state.seasonName.substringBefore(" (")} " +
                "${state.regionName} 일대를 훑어 재야 영웅을 만나십시오.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO,
            lineHeight = SlgLine.MICRO
        )
        SlgChoiceCard(
            title = "🔎 영웅 탐색 · ${com.game.slg.core.hero.HeroDiscoveryEngine.SEARCH_COST}G",
            icon = "🔎",
            effects = listOf(
                Effect("🎲", "랜덤 발견", SlgColors.Brass),
                Effect("👥", "미발견 ${undiscovered}명", SlgColors.Muted)
            ),
            accent = SlgColors.Brass,
            enabled = state.gold >= com.game.slg.core.hero.HeroDiscoveryEngine.SEARCH_COST && undiscovered > 0,
            onClick = onSearch
        )
        state.heroSearchMessage?.let { ResultBox(it, SlgColors.Brass) }
    }

    SlgExpand(
        summary = "영입 순서 — 탐색 → 설득 → 고용",
        detail = "① 탐색 — 자금을 쓰고 지역·절기에 맞는 재야 영웅을 만납니다(만난 영웅만 명부에 남습니다).\n" +
            "② 설득 — 명성(＋외교 영웅 설득력)이 필요 명성 이상이면 성공합니다.\n" +
            "③ 고용 — 자금을 지급하면 보유 영웅이 되고, 빈 슬롯에 자동 배치됩니다.\n" +
            "명성·민심·절기 조건을 채우면 영웅이 스스로 찾아와 합류를 청하기도 합니다.",
        accent = SlgColors.Violet
    )

    if (state.heroMessage != null && state.heroVisit == null) {
        ResultBox(state.heroMessage, SlgColors.Violet)
    }

    // 미발견 영웅 — 실루엣으로만 보인다
    if (undiscovered > 0) {
        SlgSectionCard(title = "미발견 영웅", badge = "${undiscovered}명", accent = SlgColors.Line) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((0 until minOf(undiscovered, 10)).toList()) { index ->
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(96.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(SlgColors.SurfaceAlt)
                            .border(1.dp, SlgColors.Line.copy(alpha = 0.6f), RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("?", color = SlgColors.Muted, fontSize = SlgType.TITLE, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                "아직 만나지 못한 영웅은 이름·능력치·초상화가 모두 비공개입니다. 탐색해야 드러납니다.",
                color = SlgColors.Muted,
                fontSize = SlgType.MICRO,
                lineHeight = SlgLine.MICRO
            )
        }
    }

    if (known.isEmpty()) {
        SlgSectionCard(title = "만난 영웅", badge = "0명", accent = SlgColors.Steel) {
            Text(
                "아직 만난 재야 영웅이 없습니다. 위의 영웅 탐색으로 한 명씩 만나 보십시오.",
                color = SlgColors.Muted,
                fontSize = SlgType.BODY_SMALL
            )
        }
        return
    }

    SlgSectionCard(
        title = "만난 영웅",
        badge = "${filtered.size}/${known.size}명",
        accent = SlgColors.Violet
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                HeroFilterChip(
                    label = "전체 역할",
                    selected = roleFilter == null,
                    accent = SlgColors.Violet
                ) {
                    roleFilter = null
                    shown = RECRUIT_PAGE
                }
            }
            items(HeroRole.entries.toList(), key = { it.name }) { role ->
                HeroFilterChip(
                    label = "${role.emoji} ${role.label}",
                    selected = roleFilter == role,
                    accent = Color(role.accentHex)
                ) {
                    roleFilter = role
                    shown = RECRUIT_PAGE
                }
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                HeroFilterChip(
                    label = "전체 등급",
                    selected = gradeFilter == null,
                    accent = SlgColors.Violet
                ) {
                    gradeFilter = null
                    shown = RECRUIT_PAGE
                }
            }
            items(HeroGrade.entries.toList(), key = { it.name }) { grade ->
                HeroFilterChip(
                    label = grade.label,
                    selected = gradeFilter == grade,
                    accent = Color(grade.accentHex)
                ) {
                    gradeFilter = grade
                    shown = RECRUIT_PAGE
                }
            }
        }
        Text(
            "만난 영웅만 보입니다. 설득·고용으로 데려오십시오.",
            color = SlgColors.Muted,
            fontSize = SlgType.MICRO
        )
    }

    if (filtered.isEmpty()) {
        Text(
            "조건에 맞는 영웅이 없습니다. 필터를 바꿔 보세요.",
            color = SlgColors.Muted,
            fontSize = SlgType.BODY_SMALL
        )
        return
    }

    // 넓은 화면(태블릿·폴더블)에서는 2열, 폰에서는 1열 — 2:3 초상화 비율은 그대로
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = SlgLayout.heroColumns(maxWidth.value)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filtered.take(shown).chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { hero ->
                        Box(modifier = Modifier.weight(1f)) {
                            RecruitCard(
                                hero = hero,
                                state = state,
                                stage = roster.recordOf(hero.id).stage,
                                affinity = roster.recordOf(hero.id).affinity,
                                onScout = onScout,
                                onPersuade = onPersuade,
                                onHire = onHire
                            )
                        }
                    }
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }

    if (filtered.size > shown) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SlgColors.SurfaceAlt)
                .clickable { shown += RECRUIT_PAGE }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "더 보기 — 남은 ${filtered.size - shown}명",
                color = SlgColors.Violet,
                fontSize = SlgType.BODY_SMALL,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/** 역할·등급 필터 칩 */
@Composable
private fun HeroFilterChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.24f) else SlgColors.SurfaceAlt)
            .border(
                1.dp,
                if (selected) accent.copy(alpha = 0.8f) else SlgColors.Line,
                RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) accent else SlgColors.Muted,
            fontSize = SlgType.MICRO,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun RecruitCard(
    hero: Hero,
    state: SlgGameState,
    stage: HeroStage,
    affinity: Int,
    onScout: (String) -> Unit,
    onPersuade: (String) -> Unit,
    onHire: (String) -> Unit
) {
    val accent = Color(hero.role.accentHex)
    val discovered = stage != HeroStage.UNDISCOVERED
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlgColors.Surface)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(11.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            HeroPortrait(hero = hero, widthDp = 96)
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(hero.name, color = SlgColors.Text, fontSize = SlgType.TITLE, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(6.dp))
                    SlgPill("${hero.role.emoji} ${hero.role.label}", accent)
                    Spacer(Modifier.width(4.dp))
                    SlgPill(hero.grade.label, Color(hero.grade.accentHex))
                }
                Text(
                    "${hero.title} · ${hero.profileLine}",
                    color = SlgColors.Muted,
                    fontSize = SlgType.MICRO
                )
                Text(
                    hero.statLine,
                    color = SlgColors.Steel,
                    fontSize = SlgType.MICRO,
                    maxLines = 2,
                    lineHeight = SlgLine.MICRO
                )
                Text(
                    hero.effectLabel,
                    color = accent,
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.Bold,
                    lineHeight = SlgLine.MICRO
                )
                if (discovered) {
                    Text(
                        "특기 · ${hero.specialty}",
                        color = SlgColors.Text.copy(alpha = 0.85f),
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO,
                        maxLines = 2
                    )
                    Text(
                        "“${hero.quote}”",
                        color = SlgColors.Text.copy(alpha = 0.85f),
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO,
                        maxLines = 3
                    )
                    Text(
                        "호감도 $affinity · 필요 명성 ${hero.requiredPrestige}",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO
                    )
                } else {
                    Text(
                        "탐색하면 신상과 접점을 알 수 있습니다.",
                        color = SlgColors.Muted,
                        fontSize = SlgType.MICRO,
                        lineHeight = SlgLine.MICRO
                    )
                }
            }
        }

        HeroStageBar(stage = stage)

        when (stage) {
            HeroStage.UNDISCOVERED -> HeroActionButton(
                label = "🔎 탐색 · ${HeroRoster.SCOUT_COST}G",
                enabled = state.gold >= HeroRoster.SCOUT_COST,
                accent = SlgColors.Brass,
                onClick = { onScout(hero.id) }
            )

            HeroStage.SCOUTED -> {
                val power = state.prestige + state.heroBonus.diplomacyBonus
                HeroActionButton(
                    label = if (power >= hero.requiredPrestige) {
                        "🗣 설득 (명성 $power / ${hero.requiredPrestige})"
                    } else {
                        "🗣 설득 — 명성 ${hero.requiredPrestige} 필요 (현재 $power)"
                    },
                    enabled = true,
                    accent = SlgColors.Violet,
                    onClick = { onPersuade(hero.id) }
                )
            }

            HeroStage.PERSUADED -> HeroActionButton(
                label = "✍ 고용 · ${hero.hireCost}G",
                enabled = state.gold >= hero.hireCost,
                accent = SlgColors.Success,
                onClick = { onHire(hero.id) }
            )

            HeroStage.HIRED -> HeroActionButton(
                label = "보유 중",
                enabled = false,
                accent = SlgColors.Muted,
                onClick = {}
            )
        }
    }
}

/** 탐색 → 설득 → 고용 3단계 진행 표시 */
@Composable
private fun HeroStageBar(stage: HeroStage) {
    val steps = listOf(HeroStage.SCOUTED to "탐색", HeroStage.PERSUADED to "설득", HeroStage.HIRED to "고용")
    val reached = when (stage) {
        HeroStage.UNDISCOVERED -> -1
        HeroStage.SCOUTED -> 0
        HeroStage.PERSUADED -> 1
        HeroStage.HIRED -> 2
    }
    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        steps.forEachIndexed { index, (_, label) ->
            val done = index <= reached
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (done) SlgColors.Violet.copy(alpha = 0.22f) else SlgColors.SurfaceAlt
                    )
                    .border(
                        1.dp,
                        if (done) SlgColors.Violet.copy(alpha = 0.6f) else SlgColors.Line,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${if (done) "✓" else (index + 1).toString()} $label",
                    color = if (done) SlgColors.Violet else SlgColors.Muted,
                    fontSize = SlgType.MICRO,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun HeroActionButton(
    label: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(if (enabled) accent.copy(alpha = 0.92f) else SlgColors.SurfaceAlt)
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (enabled) Color(0xFF10131A) else SlgColors.Muted,
            fontSize = SlgType.BODY_SMALL,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

/** 보유 영웅 한 줄 — 헤더용 요약 (지도 화면 등에서 재사용) */
@Composable
fun HeroMiniRow(hero: Hero, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(Color(hero.role.accentHex))
        )
        Spacer(Modifier.width(5.dp))
        Text(hero.name, color = SlgColors.Text, fontSize = SlgType.MICRO, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text(hero.role.label, color = SlgColors.Muted, fontSize = SlgType.MICRO)
    }
}
