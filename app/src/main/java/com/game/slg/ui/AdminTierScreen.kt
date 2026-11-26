package com.game.slg.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.game.slg.core.region.AdminActionKind
import com.game.slg.core.region.AdminMenu
import com.game.slg.core.region.AdminTier

/**
 * 행정 단위 4단계 — 읍·면·동 → 시·군·구 → 도·광역시 → 국가·연방.
 *
 * 사용자 요청(2026-11-26): *"단위가 승격될수록 해당 단계에 맞는 관리 메뉴가 실제로 늘어나고
 * 사용 가능해야 한다."*
 *
 * 화면은 ① 단계 사다리 ② 승격 조건 카드 ③ 지금 쓸 수 있는 관리 메뉴(아이콘 그리드)
 * ④ 다음 단계에서 열릴 메뉴 순서로 압축한다. 긴 설명문은 넣지 않는다.
 */
@Composable
fun AdminTierScreen(
    state: SlgGameState,
    onPromote: () -> Unit,
    onPerform: (String) -> Unit
) {
    val current = state.adminTier
    val evaluation = state.adminEvaluation
    SlgScreenFrame {
        // ① 단계 사다리 — 4칸
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AdminTier.entries.forEach { tier ->
                val reached = tier.order <= current.order
                val isCurrent = tier == current
                val accent = when {
                    isCurrent -> SlgColors.Brass
                    reached -> SlgColors.Success
                    else -> SlgColors.Muted
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(accent.copy(alpha = if (isCurrent) 0.22f else 0.08f))
                        .border(
                            1.dp,
                            accent.copy(alpha = if (isCurrent) 0.85f else 0.35f),
                            RoundedCornerShape(11.dp)
                        )
                        .padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(tier.icon, fontSize = SlgType.BODY)
                    Text(
                        tier.short,
                        color = if (reached) accent else SlgColors.Muted,
                        fontSize = SlgType.LABEL,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // ② 현재 단위 · 승격 조건
        SlgSectionCard(
            title = "${current.icon} ${current.label}",
            badge = if (current.isFinal) "최종" else "다음 ${current.next?.label ?: ""}",
            accent = SlgColors.Brass
        ) {
            Text(
                current.detail,
                color = SlgColors.Muted,
                fontSize = SlgType.LABEL,
                lineHeight = SlgLine.MICRO
            )
            val next = evaluation.next
            if (next != null) {
                SlgStatBar(
                    ratio = evaluation.progressRatio,
                    color = if (evaluation.isEligible) SlgColors.Success else SlgColors.Brass,
                    modifier = Modifier.fillMaxWidth(),
                    height = 8
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AdminRequirementPill("🗺", evaluation.territories, next.requireTerritories, SlgColors.Steel)
                    AdminRequirementPill("🤝", evaluation.allied, next.requireAllied, SlgColors.Success)
                    AdminRequirementPill("⭐", evaluation.prestige, next.requirePrestige, SlgColors.Brass)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AdminRequirementPill("🗳", evaluation.authority, next.requireAuthority, SlgColors.Violet)
                    AdminRequirementPill("💰", state.gold, next.promoteCost, SlgColors.Brass)
                }
                SlgIconAction(
                    icon = "🏢",
                    label = if (evaluation.isEligible) "승격" else "조건 미달",
                    accent = SlgColors.Brass,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = evaluation.isEligible,
                    onClick = onPromote
                )
            } else {
                Text(
                    "행정 사다리의 마지막 단계입니다 — 중앙 정부 부처가 모두 열렸습니다.",
                    color = SlgColors.Success,
                    fontSize = SlgType.LABEL
                )
            }
            state.adminMessage?.let { ResultBox(it, SlgColors.Brass) }
        }

        // ③ 지금 쓸 수 있는 관리 메뉴
        SlgSectionCard(
            title = "관리 메뉴",
            badge = "${state.adminMenus.size}개",
            accent = SlgColors.Steel
        ) {
            val groups = state.adminMenus.groupBy { it.tier }
            AdminTier.entries.forEach { tier ->
                val menus = groups[tier].orEmpty()
                if (menus.isEmpty()) return@forEach
                Text(
                    "${tier.icon} ${tier.label}",
                    color = if (tier == current) SlgColors.Brass else SlgColors.Muted,
                    fontSize = SlgType.LABEL,
                    fontWeight = FontWeight.Bold
                )
                SlgIconMenuGrid(
                    entries = menus.map { menu -> menu.toEntry(state) },
                    columns = 4,
                    onSelect = { entry -> onPerform(entry.id) }
                )
            }
        }

        // ④ 다음 단계에서 열릴 메뉴 — 자물쇠로 미리 보여 준다
        if (state.nextAdminMenus.isNotEmpty()) {
            SlgSectionCard(
                title = "${current.next?.label ?: ""}에서 열림",
                badge = "${state.nextAdminMenus.size}개",
                accent = SlgColors.Line
            ) {
                SlgIconMenuGrid(
                    entries = state.nextAdminMenus.map { menu ->
                        SlgIconEntry(
                            id = menu.id,
                            icon = menu.icon,
                            label = menu.label,
                            enabled = false,
                            tone = SlgColors.Muted
                        )
                    },
                    columns = 4,
                    onSelect = {}
                )
            }
        }

        SlgLogBox(state.log, maxLines = 3)
    }
}

/** 조건 칩 — 달성/미달을 색으로만 보여 준다 */
@Composable
private fun AdminRequirementPill(icon: String, value: Int, required: Int, accent: Color) {
    val done = value >= required
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (done) accent.copy(alpha = 0.18f) else SlgColors.SurfaceAlt)
            .border(
                1.dp,
                if (done) accent.copy(alpha = 0.55f) else SlgColors.Line,
                RoundedCornerShape(50)
            )
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = SlgType.MICRO)
        Spacer(Modifier.width(3.dp))
        Text(
            "%,d".format(value) + "/" + "%,d".format(required),
            color = if (done) accent else SlgColors.Muted,
            fontSize = SlgType.MICRO,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/** 관리 메뉴 → 아이콘 그리드 칸 */
private fun AdminMenu.toEntry(state: SlgGameState): SlgIconEntry {
    val affordable = state.gold >= cost
    val enabled = when (kind) {
        AdminActionKind.NAVIGATE -> true
        AdminActionKind.EFFECT -> affordable
    }
    return SlgIconEntry(
        id = id,
        icon = icon,
        label = label,
        enabled = enabled,
        tone = if (kind == AdminActionKind.NAVIGATE) SlgColors.Steel else SlgColors.Brass,
        footnote = if (cost > 0) "${cost}G" else null
    )
}
