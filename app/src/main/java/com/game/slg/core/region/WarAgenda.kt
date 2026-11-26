package com.game.slg.core.region

/**
 * 의회 전쟁 안건 — 《단일 연방의 황혼》
 *
 * 사용자 요청(2026-11-26):
 *  *"의회 화면(CouncilScreen)에서 직접 인접 지역 또는 상위 단위 세력을 선택해 전쟁 선포·원정
 *    안건을 상정하고 표결할 수 있어야 한다. 가결 시 정식 명분 보너스(전투력 증가·사상자 감소·
 *    전리품 증가)를 부여하고 원정·병합과 연동한다."*
 *
 * · 안건은 대상(인접 지역 / 상위 단위)과 원정 명분을 담는다.
 * · 표결에서 가결되면 명분 보너스가 생기고, 그 지역에 원정을 걸 때 1회 적용된 뒤 소모된다.
 * · 병합(무력 편입)에도 같은 보너스가 붙어 전리품·사상자에 반영된다.
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 안건 대상의 층위 */
enum class WarTargetKind(val label: String, val icon: String) {
    ADJACENT("인접 지역", "🗺"),
    SUPERIOR("상위 단위", "🏛")
}

/** 가결된 전쟁 안건이 주는 정식 명분 보너스 */
data class CasusBelli(
    val combatBonusPercent: Int,
    val casualtyReductionPercent: Int,
    val lootBonusPercent: Int
) {
    val label: String
        get() = "⚔ +$combatBonusPercent% · 사상자 -$casualtyReductionPercent% · 전리품 +$lootBonusPercent%"

    val isEmpty: Boolean
        get() = combatBonusPercent == 0 && casualtyReductionPercent == 0 && lootBonusPercent == 0

    companion object {
        val NONE = CasusBelli(0, 0, 0)
    }
}

/** 의회에 오른 전쟁 안건 1건 */
data class WarAgenda(
    val id: String,
    val kind: WarTargetKind,
    val regionCode: String,
    val regionName: String,
    val targetName: String,
    val title: String,
    val requestedSeason: Int,
    val favor: Int = 0,
    val against: Int = 0,
    val ourVotes: Int = 1,
    val passed: Boolean? = null,
    val message: String? = null,
    /** 보너스를 이미 원정에 썼는지 */
    val consumed: Boolean = false
) {
    val pending: Boolean get() = passed == null

    /** 지금 바로 쓸 수 있는 명분 (가결·미소모) */
    val active: Boolean get() = passed == true && !consumed

    val totalVotes: Int get() = favor + against

    val againstPercent: Int get() = if (totalVotes == 0) 0 else against * 100 / totalVotes

    val statusLabel: String
        get() = when {
            pending -> "표결 전"
            consumed -> "명분 소모"
            passed == true -> "가결 · 명분 유효"
            else -> "부결"
        }

    val casusBelli: CasusBelli get() = if (active) WarAgendaEngine.bonusOf(this) else CasusBelli.NONE

    /** 저장용 — "id|kind|code|regionName|targetName|title|season|favor|against|votes|passed|consumed" */
    fun encode(): String = listOf(
        id, kind.name, regionCode, regionName, targetName, title, requestedSeason.toString(),
        favor.toString(), against.toString(), ourVotes.toString(),
        when (passed) {
            null -> "-"
            true -> "true"
            false -> "false"
        },
        consumed.toString()
    ).joinToString("|")
}

/**저장 문자열 → [WarAgenda] */
object WarAgendaCodec {
    fun decode(raw: String): WarAgenda? {
        val p = raw.split('|')
        val id = p.getOrNull(0)?.takeIf { it.isNotBlank() } ?: return null
        val kind = WarTargetKind.entries.firstOrNull { it.name == p.getOrNull(1) } ?: return null
        val passed = when (p.getOrNull(10)) {
            "true" -> true
            "false" -> false
            else -> null
        }
        return WarAgenda(
            id = id,
            kind = kind,
            regionCode = p.getOrNull(2) ?: "",
            regionName = p.getOrNull(3) ?: "",
            targetName = p.getOrNull(4) ?: "",
            title = p.getOrNull(5) ?: "전쟁 안건",
            requestedSeason = p.getOrNull(6)?.toIntOrNull() ?: 0,
            favor = p.getOrNull(7)?.toIntOrNull() ?: 0,
            against = p.getOrNull(8)?.toIntOrNull() ?: 0,
            ourVotes = p.getOrNull(9)?.toIntOrNull() ?: 1,
            passed = passed,
            consumed = p.getOrNull(11) == "true"
        )
    }
}

object WarAgendaEngine {

    /** 안건 상정 — 참여 리더가 많을수록 찬성표가 두텁다 */
    fun sponsor(
        kind: WarTargetKind,
        regionCode: String,
        regionName: String,
        targetName: String,
        seasonTurn: Int,
        supporters: Int,
        sequence: Int = 0
    ): WarAgenda {
        val title = when (kind) {
            WarTargetKind.ADJACENT -> "[전쟁 선포] $regionName 원정"
            WarTargetKind.SUPERIOR -> "[상위 원정] $targetName 연합 전쟁"
        }
        val id = "war_${kind.name.lowercase()}_${regionCode}_${seasonTurn}" +
            if (sequence > 0) "_$sequence" else ""
        return WarAgenda(
            id = id,
            kind = kind,
            regionCode = regionCode,
            regionName = regionName,
            targetName = targetName,
            title = title,
            requestedSeason = seasonTurn,
            favor = supporters.coerceAtLeast(0) * 2 + 2
        )
    }

    /** 표결 — 내 의결권이 찬성으로 더해진다 */
    fun vote(agenda: WarAgenda, opposingVotes: Int, ourVotes: Int): WarAgenda {
        if (!agenda.pending) return agenda
        val votes = ourVotes.coerceAtLeast(1)
        val favor = agenda.favor + votes
        val against = opposingVotes.coerceAtLeast(0)
        val passed = favor > against
        val bonus = if (passed) bonusOf(agenda.copy(favor = favor, against = against, passed = true))
        else CasusBelli.NONE
        val message = "[${agenda.title}] 찬성 $favor : 반대 $against → " +
            (if (passed) "가결 — 정식 명분 ${bonus.label}" else "부결 — 명분 없이 원정할 수 없습니다(보너스 없음)")
        return agenda.copy(
            favor = favor,
            against = against,
            ourVotes = votes,
            passed = passed,
            message = message
        )
    }

    /** 가결 표차로 정해지는 명분 보너스 */
    fun bonusOf(agenda: WarAgenda): CasusBelli {
        val margin = (agenda.favor - agenda.against).coerceAtLeast(1)
        return CasusBelli(
            combatBonusPercent = (4 + margin).coerceIn(6, 20),
            casualtyReductionPercent = (6 + margin / 2).coerceIn(8, 25),
            lootBonusPercent = (10 + margin * 2).coerceIn(12, 40)
        )
    }

    /** 이 지역에 지금 쓸 수 있는 명분 */
    fun activeFor(agendas: List<WarAgenda>, regionCode: String): WarAgenda? =
        agendas.lastOrNull { it.active && it.regionCode == regionCode }

    fun casusBelliFor(agendas: List<WarAgenda>, regionCode: String): CasusBelli =
        activeFor(agendas, regionCode)?.casusBelli ?: CasusBelli.NONE

    /** 원정 1회에 보너스를 쓰고 소모 처리 */
    fun consume(agendas: List<WarAgenda>, regionCode: String): List<WarAgenda> {
        val target = activeFor(agendas, regionCode) ?: return agendas
        return agendas.map { if (it.id == target.id) it.copy(consumed = true) else it }
    }

    /** 같은 대상·같은 종류의 대기 안건이 있는지 */
    fun hasPending(agendas: List<WarAgenda>, regionCode: String, kind: WarTargetKind): Boolean =
        agendas.any { it.pending && it.regionCode == regionCode && it.kind == kind }

    /** 절기가 지나면 최근 8건만 남긴다 */
    fun prune(agendas: List<WarAgenda>): List<WarAgenda> = agendas.takeLast(8)

    /** 상위 단위 이름 — 시·군·구(예: 충청남도 아산시) */
    fun superiorName(groupKey: String): String =
        if (groupKey.isBlank()) "상위 연합" else "$groupKey 연합"
}
