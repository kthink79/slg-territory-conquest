package com.game.slg.core.hero

/**
 * 현대 영웅(인재) 시스템 — 《단일 연방의 황혼》
 *
 * 사용자 요청: *"영웅 시스템을 도입하고 초상화 10종을 넣어줘. 초기 3명을 주고
 * 나머지는 탐색 → 설득 → 고용으로 영입하게 해줘."*
 * → 2026-11-18: 현대 영웅 1~230번 전체를 탐색·설득·고용 풀에 일괄 탑재(초상화 230장).
 * → 2026-11-19: 231~240번 10명을 더해 총 240명 · 초상화 240장.
 * → 2026-11-20: 다국적 241~250번 10명을 더해 총 250명 · 초상화 250장(origin·ethnicity 필드 추가).
 * → 2026-11-21: 251~260번(외국인 10명) · 261~280번(시트 분할 20명) · 281~303번(시트 분할 23명)을 더해
 *   총 303명 · 초상화 303장.
 *
 * · 영웅은 6개 역할(내정·기술·상업·작전·외교·정보) 중 하나를 가진다.
 * · 등급(SSR/SR/R/N)·5개 능력치(내정·기술·상업·작전·외교)·특기를 함께 들고 있다.
 * · 보유 영웅을 자기 역할 슬롯에 배치하면 영지 보너스가 실제 수치에 붙는다.
 * · 미보유 영웅은 [탐색 → 설득 → 고용] 3단계를 거쳐 영입한다.
 *
 * 안드로이드 의존성이 없어 JVM 단위 테스트로 검증한다.
 */

/** 영웅 역할 = 배치 슬롯. 앞의 4개가 영지 주 슬롯, 뒤의 2개가 외교 슬롯이다. */
enum class HeroRole(
    val label: String,
    val emoji: String,
    val accentHex: Long,
    /** 이 역할이 올려 주는 효과 이름 (슬롯 카드에 그대로 보인다) */
    val effectLabel: String,
    val primary: Boolean
) {
    ADMIN("내정", "🏛", 0xFFE2B755, "절기 자금", true),
    TECH("기술", "🛠", 0xFF64B5F6, "연구 비용", true),
    COMMERCE("상업", "💰", 0xFF81C784, "절기 보급", true),
    OPS("작전", "⚔", 0xFFE57373, "작전 전투력", true),
    DIPLOMACY("외교", "🤝", 0xFFB39DDB, "설득력", false),
    INTEL("정보", "🕵", 0xFF90A4AE, "첩보력", false);

    companion object {
        fun fromLabel(label: String): HeroRole =
            entries.firstOrNull { it.label == label } ?: ADMIN

        /** 영지 주 슬롯 4개 — 내정 / 기술 / 상업 / 작전 */
        val primarySlots: List<HeroRole> = entries.filter { it.primary }

        /** 외교 슬롯 2개 — 외교 / 정보 */
        val supportSlots: List<HeroRole> = entries.filterNot { it.primary }
    }
}

/**
 * 영웅 등급 — 로스터 303명의 SSR 42 · SR 94 · R 115 · N 52 분포를 그대로 쓴다.
 * 등급이 높을수록 배치 효과·필요 명성·고용비가 크다.
 */
enum class HeroGrade(
    val label: String,
    val accentHex: Long,
    /** 정렬용 서열 — 높을수록 상위 등급 */
    val rank: Int,
    /** 고용비 기준값 (G) */
    val hireBase: Int,
    /** 설득 필요 명성 기준값 */
    val prestigeBase: Int
) {
    SSR("SSR", 0xFFE2B755, 3, 3600, 62),
    SR("SR", 0xFFB39DDB, 2, 2600, 50),
    R("R", 0xFF7FA4D6, 1, 1600, 38),
    N("N", 0xFF90A4AE, 0, 900, 28);

    companion object {
        fun fromLabel(label: String): HeroGrade =
            entries.firstOrNull { it.label == label.trim().uppercase() } ?: R
    }
}

/**
 * 영웅 5개 능력치 — 1~100.
 * 배치 효과는 역할의 주 능력치에서 나온다 (정보 역할은 기술을 쓴다).
 */
data class HeroStats(
    val admin: Int = 50,
    val tech: Int = 50,
    val commerce: Int = 50,
    val ops: Int = 50,
    val diplomacy: Int = 50
) {
    val total: Int get() = admin + tech + commerce + ops + diplomacy

    fun primaryOf(role: HeroRole): Int = when (role) {
        HeroRole.ADMIN -> admin
        HeroRole.TECH -> tech
        HeroRole.COMMERCE -> commerce
        HeroRole.OPS -> ops
        HeroRole.DIPLOMACY -> diplomacy
        HeroRole.INTEL -> tech
    }

    /** 영웅 카드에 보이는 한 줄 — "내정 82 · 기술 48 · 상업 55 · 작전 21 · 외교 74" */
    fun summary(): String =
        "내정 $admin · 기술 $tech · 상업 $commerce · 작전 $ops · 외교 $diplomacy"

    companion object {
        val NONE = HeroStats(0, 0, 0, 0, 0)
    }
}

/** 영웅 보유·영입 단계 */
enum class HeroStage(val label: String) {
    UNDISCOVERED("미탐색"),
    SCOUTED("탐색 완료"),
    PERSUADED("설득 완료"),
    HIRED("보유 중");

    val owned: Boolean get() = this == HIRED
}

/**
 * 배치 효과 — 역할별로 하나씩만 쓰인다.
 * 수치는 % 이거나 단순 가산치다.
 */
data class HeroBonus(
    val goldPercent: Int = 0,
    val foodPercent: Int = 0,
    val researchDiscountPercent: Int = 0,
    val combatPercent: Int = 0,
    val diplomacyBonus: Int = 0,
    val intelBonus: Int = 0
) {
    operator fun plus(other: HeroBonus): HeroBonus = HeroBonus(
        goldPercent = goldPercent + other.goldPercent,
        foodPercent = foodPercent + other.foodPercent,
        researchDiscountPercent = researchDiscountPercent + other.researchDiscountPercent,
        combatPercent = combatPercent + other.combatPercent,
        diplomacyBonus = diplomacyBonus + other.diplomacyBonus,
        intelBonus = intelBonus + other.intelBonus
    )

    val isEmpty: Boolean
        get() = goldPercent == 0 && foodPercent == 0 && researchDiscountPercent == 0 &&
            combatPercent == 0 && diplomacyBonus == 0 && intelBonus == 0

    /** 화면용 한 줄 요약 — "절기 자금 +8% · 연구 -5%" */
    fun summary(): String = buildList {
        if (goldPercent != 0) add("절기 자금 +$goldPercent%")
        if (foodPercent != 0) add("절기 보급 +$foodPercent%")
        if (researchDiscountPercent != 0) add("연구 비용 -$researchDiscountPercent%")
        if (combatPercent != 0) add("작전 전투력 +$combatPercent%")
        if (diplomacyBonus != 0) add("설득력 +$diplomacyBonus")
        if (intelBonus != 0) add("첩보 +$intelBonus")
    }.ifEmpty { listOf("효과 없음") }.joinToString(" · ")

    companion object {
        val NONE = HeroBonus()

        fun of(role: HeroRole, value: Int): HeroBonus = when (role) {
            HeroRole.ADMIN -> HeroBonus(goldPercent = value)
            HeroRole.TECH -> HeroBonus(researchDiscountPercent = value)
            HeroRole.COMMERCE -> HeroBonus(foodPercent = value)
            HeroRole.OPS -> HeroBonus(combatPercent = value)
            HeroRole.DIPLOMACY -> HeroBonus(diplomacyBonus = value)
            HeroRole.INTEL -> HeroBonus(intelBonus = value)
        }
    }
}

/** 영웅 1명 — 초상화는 `slg_hero_001` ~ `slg_hero_303` 리소스로 이어진다. */
data class Hero(
    val id: String,
    val name: String,
    val role: HeroRole,
    /** 세부 직업 (카드 부제) */
    val title: String,
    /** 영입 카드에 보이는 한 줄 대사 */
    val quote: String,
    /** 배치 효과 */
    val bonus: HeroBonus,
    /** 설득에 필요한 명성 */
    val requiredPrestige: Int,
    /** 고용 비용 (G) */
    val hireCost: Int,
    /** 등급 — SSR > SR > R > N */
    val grade: HeroGrade = HeroGrade.R,
    /** 5개 능력치 (내정·기술·상업·작전·외교) */
    val stats: HeroStats = HeroStats(),
    /** 특기 — 영입 카드에 한 줄로 보인다 */
    val specialty: String = "",
    val age: Int = 0,
    /** "여" / "남" */
    val gender: String = "",
    /** 출신지 — 다국적 배치(241~260번)와 시트 분할 배치(261~303번)까지 채운다. 비어 있으면 화면에 넣지 않는다 */
    val origin: String = "",
    /** 민족 구분 — 로스터 메타데이터. 화면에는 쓰지 않고 로스터 대조에만 쓴다 */
    val ethnicity: String = ""
) {
    /** 로스터 번호 (001~303) */
    val number: Int get() = id.substringAfterLast('_').toIntOrNull() ?: 0

    /** 탑재 초상화 drawable 이름 — slg_hero_001 형식 */
    val portraitName: String get() = "slg_hero_" + number.toString().padStart(3, '0')

    /** 화면용 효과 요약 */
    val effectLabel: String get() = bonus.summary()

    /** 화면용 능력치 요약 */
    val statLine: String get() = stats.summary()

    /** "28세 · 여" */
    val profileLine: String get() = buildList {
        if (age > 0) add("${age}세")
        if (gender.isNotBlank()) add(gender)
        add(grade.label)
        if (origin.isNotBlank()) add(origin)
    }.joinToString(" · ")
}

/** 영웅 1명의 현재 기록 */
data class HeroRecord(
    val id: String,
    val stage: HeroStage = HeroStage.UNDISCOVERED,
    val affinity: Int = 0,
    /** 배치된 역할 — null 이면 미배치 */
    val assignedRole: HeroRole? = null
) {
    val owned: Boolean get() = stage.owned
}

/** 영웅 전체 상태 — 저장·테스트 대상 */
data class HeroRosterState(
    val records: Map<String, HeroRecord> = emptyMap()
) {
    fun recordOf(id: String): HeroRecord = records[id] ?: HeroRecord(id)

    private fun with(record: HeroRecord): HeroRosterState =
        copy(records = records + (record.id to record))

    val ownedHeroes: List<Hero> get() = HeroRoster.ALL.filter { recordOf(it.id).owned }

    val recruitables: List<Hero> get() = HeroRoster.ALL.filterNot { recordOf(it.id).owned }

    val ownedCount: Int get() = ownedHeroes.size

    val hiredCount: Int get() = ownedCount

    /** 슬롯에 앉은 영웅 */
    fun heroIn(role: HeroRole): Hero? = ownedHeroes.firstOrNull { recordOf(it.id).assignedRole == role }

    /** 배치된 영웅 수 */
    val placedCount: Int get() = ownedHeroes.count { recordOf(it.id).assignedRole != null }

    /** 슬롯에 앉은 영웅들의 보너스 합 */
    fun activeBonus(): HeroBonus = ownedHeroes
        .filter { recordOf(it.id).assignedRole != null }
        .fold(HeroBonus.NONE) { acc, hero -> acc + hero.bonus }

    /** 배치하지 않은 보유 영웅 */
    val unplaced: List<Hero> get() = ownedHeroes.filter { recordOf(it.id).assignedRole == null }

    /** 영입 진행 중(탐색·설득) 영웅 수 */
    val inProgressCount: Int
        get() = HeroRoster.ALL.count {
            val stage = recordOf(it.id).stage
            stage == HeroStage.SCOUTED || stage == HeroStage.PERSUADED
        }

    companion object {
        /** 게임 시작 시 함께 시작하는 기본 영웅 3명 (한서연·윤도현·차명숙) */
        val STARTING_IDS: List<String> = listOf("hero_001", "hero_002", "hero_003")

        /** 기본 3명을 슬롯에 앉힌 초기 상태 */
        fun initial(): HeroRosterState {
            var roster = HeroRosterState()
            STARTING_IDS.forEach { id ->
                val hero = HeroRoster.byId(id) ?: return@forEach
                roster = roster.with(
                    HeroRecord(id = id, stage = HeroStage.HIRED, affinity = 80, assignedRole = hero.role)
                )
            }
            return roster
        }

        /** 저장값에서 복원 — 모르는 id 는 버린다. 예전 2자리 id(`hero_02`)도 받아 준다. */
        fun fromSaves(
            owned: Collection<String>,
            placed: Map<String, String>,
            affinity: Map<String, Int> = emptyMap()
        ): HeroRosterState {
            // 옛 저장값의 id 표기를 3자리 id 로 맞춘다
            fun canonical(raw: String): String? = HeroRoster.byId(raw)?.id

            val placedById = placed.entries
                .mapNotNull { (rawId, roleName) -> canonical(rawId)?.let { it to roleName } }
                .toMap()
            val affinityById = affinity.entries
                .mapNotNull { (rawId, value) -> canonical(rawId)?.let { it to value } }
                .toMap()
            var roster = HeroRosterState()
            owned.mapNotNull { HeroRoster.byId(it) }.forEach { hero ->
                val role = placedById[hero.id]?.let { name ->
                    HeroRole.entries.firstOrNull { it.name == name }
                }
                roster = roster.with(
                    HeroRecord(
                        id = hero.id,
                        stage = HeroStage.HIRED,
                        affinity = affinityById[hero.id] ?: 80,
                        assignedRole = role
                    )
                )
            }
            return roster
        }
    }
}

/** 영웅 카탈로그 — 초상화 10장과 1:1로 대응한다. */
object HeroRoster {

    /** 탐색 1회 비용 */
    const val SCOUT_COST = 200

    /** 설득에 필요한 최소 호감도 (탐색 1회로 45가 된다) */
    const val PERSUADE_AFFINITY = 40

    /** 탐색 1회로 오르는 호감도 */
    const val SCOUT_AFFINITY = 45

    /** 설득 성공 시 호감도 */
    const val PERSUADED_AFFINITY = 70

    /** 설득 실패 시 오르는 호감도 */
    const val PERSUADE_FAIL_AFFINITY = 8

    /** 기본 10명 — 초상화 `slg_hero_001` ~ `slg_hero_010` */
    private val CORE: List<Hero> = listOf(
        Hero(
            id = "hero_001",
            name = "한서연",
            role = HeroRole.ADMIN,
            title = "지자체 행정관",
            quote = "예산은 감으로 짜는 게 아니에요. 숫자로 증명하죠.",
            bonus = HeroBonus(goldPercent = 8),
            requiredPrestige = 30,
            hireCost = 0,
            grade = HeroGrade.SR,
            stats = HeroStats(admin = 82, tech = 48, commerce = 55, ops = 21, diplomacy = 74),
            specialty = "민원 조정 · 정책 실무",
            age = 28,
            gender = "여"
        ),
        Hero(
            id = "hero_002",
            name = "윤도현",
            role = HeroRole.TECH,
            title = "반도체·AI 공학자",
            quote = "설비는 거짓말을 안 합니다. 데이터로 말해요.",
            bonus = HeroBonus(researchDiscountPercent = 12),
            requiredPrestige = 30,
            hireCost = 0,
            grade = HeroGrade.SSR,
            stats = HeroStats(admin = 44, tech = 93, commerce = 38, ops = 22, diplomacy = 41),
            specialty = "공정 수율 분석 · AI 모델 설계",
            age = 32,
            gender = "남"
        ),
        Hero(
            id = "hero_003",
            name = "차명숙",
            role = HeroRole.COMMERCE,
            title = "물류·상권 투자자",
            quote = "사람이 모이는 길에 돈이 있지. 길을 먼저 보게.",
            bonus = HeroBonus(foodPercent = 10),
            requiredPrestige = 30,
            hireCost = 0,
            grade = HeroGrade.SR,
            stats = HeroStats(admin = 51, tech = 30, commerce = 88, ops = 27, diplomacy = 69),
            specialty = "상권 분석 · 투자 판단",
            age = 54,
            gender = "여"
        ),
        Hero(
            id = "hero_004",
            name = "강태식",
            role = HeroRole.OPS,
            title = "예비역 작전참모",
            quote = "작전은 준비에서 끝난다. 나머지는 집행일 뿐이야.",
            bonus = HeroBonus(combatPercent = 12),
            requiredPrestige = 48,
            hireCost = 1300,
            grade = HeroGrade.SSR,
            stats = HeroStats(admin = 40, tech = 45, commerce = 32, ops = 90, diplomacy = 52),
            specialty = "전술 지휘 · 작전 계획",
            age = 48,
            gender = "남"
        ),
        Hero(
            id = "hero_005",
            name = "송기환",
            role = HeroRole.ADMIN,
            title = "지역 원로 정책가",
            quote = "급하게 가면 더 급해지는 법이지. 순서를 지켜.",
            bonus = HeroBonus(goldPercent = 6, foodPercent = 4),
            requiredPrestige = 40,
            hireCost = 1100,
            grade = HeroGrade.SR,
            stats = HeroStats(admin = 87, tech = 26, commerce = 44, ops = 31, diplomacy = 79),
            specialty = "원로 협의 · 정책 자문",
            age = 62,
            gender = "남"
        ),
        Hero(
            id = "hero_006",
            name = "배도윤",
            role = HeroRole.TECH,
            title = "드론·사이버 전문가",
            quote = "하늘은 이미 우리 쪽입니다. 확인해 보실래요?",
            bonus = HeroBonus(researchDiscountPercent = 8, combatPercent = 4),
            requiredPrestige = 42,
            hireCost = 1200,
            grade = HeroGrade.R,
            stats = HeroStats(admin = 28, tech = 86, commerce = 34, ops = 62, diplomacy = 30),
            specialty = "드론 운용 · 침투 해킹",
            age = 22,
            gender = "남"
        ),
        Hero(
            id = "hero_007",
            name = "임하경",
            role = HeroRole.DIPLOMACY,
            title = "협상가·조정관",
            quote = "상대의 말을 먼저 들어야 내 말이 들립니다.",
            bonus = HeroBonus(diplomacyBonus = 12),
            requiredPrestige = 55,
            hireCost = 1500,
            grade = HeroGrade.SSR,
            stats = HeroStats(admin = 60, tech = 34, commerce = 48, ops = 25, diplomacy = 91),
            specialty = "협상 조율 · 갈등 중재",
            age = 35,
            gender = "여"
        ),
        Hero(
            id = "hero_008",
            name = "백지현",
            role = HeroRole.TECH,
            title = "에너지·인프라 박사",
            quote = "전력이 곧 시간입니다. 시간을 아껴 쓰세요.",
            bonus = HeroBonus(researchDiscountPercent = 10),
            requiredPrestige = 65,
            hireCost = 1700,
            grade = HeroGrade.R,
            stats = HeroStats(admin = 57, tech = 84, commerce = 41, ops = 23, diplomacy = 50),
            specialty = "전력망 설계 · 인프라 진단",
            age = 42,
            gender = "여"
        ),
        Hero(
            id = "hero_009",
            name = "구영철",
            role = HeroRole.COMMERCE,
            title = "도시개발 전문가",
            quote = "땅값은 계획이 만듭니다. 계획은 제가 세우죠.",
            bonus = HeroBonus(goldPercent = 10, foodPercent = 4),
            requiredPrestige = 50,
            hireCost = 1600,
            grade = HeroGrade.R,
            stats = HeroStats(admin = 63, tech = 40, commerce = 82, ops = 29, diplomacy = 58),
            specialty = "개발 사업 · 시공 관리",
            age = 52,
            gender = "남"
        ),
        Hero(
            id = "hero_010",
            name = "유하린",
            role = HeroRole.INTEL,
            title = "정보분석관",
            quote = "정보는 조용한 쪽이 정확합니다. 소리는 잡음이죠.",
            bonus = HeroBonus(intelBonus = 15),
            requiredPrestige = 45,
            hireCost = 1100,
            grade = HeroGrade.R,
            stats = HeroStats(admin = 33, tech = 71, commerce = 36, ops = 68, diplomacy = 55),
            specialty = "정보 수집 · 심리 분석",
            age = 25,
            gender = "여"
        )
    )

    /** 전체 로스터 — 기본 10명 + 드라이브 로스터 011~303번 = 303명 */
    val ALL: List<Hero> = CORE + HeroExpansion.HEROES

    private val BY_ID: Map<String, Hero> = ALL.associateBy { it.id }

    fun byId(id: String): Hero? = BY_ID[id] ?: normalizedId(id)?.let { BY_ID[it] }

    /** 예전 저장값 `hero_01` 같은 2자리 id 를 3자리로 맞춘다 */
    private fun normalizedId(id: String): String? {
        val digits = id.trim().removePrefix("hero_").toIntOrNull() ?: return null
        return "hero_" + digits.toString().padStart(3, '0')
    }

    fun byName(name: String): Hero? = ALL.firstOrNull { it.name == name }

    /** 탑재된 초상화 리소스 이름 목록 (drawable-nodpi) */
    val PORTRAIT_NAMES: List<String> = ALL.map { it.portraitName }

}

/** 영웅 조작 결과 — 상태 + 자금 변화 + 안내 문구 */
data class HeroActionResult(
    val roster: HeroRosterState,
    val success: Boolean,
    val message: String,
    val goldDelta: Int = 0,
    val prestigeDelta: Int = 0
)

/**
 * 영웅 영입·배치 규칙 — 순수 함수라 화면 없이 검증한다.
 *
 * 탐색(자금 200G, 호감도 45) → 설득(명성 + 설득력 ≥ 필요 명성) → 고용(자금)
 */
object HeroEngine {

    /** 탐색 — 미보유 영웅의 신상과 접점을 알아낸다 */
    fun scout(
        roster: HeroRosterState,
        heroId: String,
        gold: Int
    ): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[탐색] 알 수 없는 인물입니다. ($heroId)")
        val record = roster.recordOf(heroId)
        if (record.owned) {
            return HeroActionResult(roster, false, "[탐색] ${hero.name}은(는) 이미 우리 리더입니다.")
        }
        if (record.stage != HeroStage.UNDISCOVERED) {
            return HeroActionResult(roster, false, "[탐색] ${hero.name}은(는) 이미 탐색을 마쳤습니다.")
        }
        if (gold < HeroRoster.SCOUT_COST) {
            return HeroActionResult(
                roster, false,
                "[탐색 불가] 자금이 부족합니다. (필요 ${HeroRoster.SCOUT_COST}G / 보유 ${gold}G)"
            )
        }
        val next = roster.copy(
            records = roster.records + (
                heroId to record.copy(stage = HeroStage.SCOUTED, affinity = HeroRoster.SCOUT_AFFINITY)
                )
        )
        return HeroActionResult(
            roster = next,
            success = true,
            message = "[탐색] ${hero.name} · ${hero.title} — 연락선을 확보했다. 호감도 ${HeroRoster.SCOUT_AFFINITY}",
            goldDelta = -HeroRoster.SCOUT_COST
        )
    }

    /** 설득 — 명성과 설득력(외교 영웅)으로 마음을 연다 */
    fun persuade(
        roster: HeroRosterState,
        heroId: String,
        prestige: Int,
        diplomacyBonus: Int = 0
    ): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[설득] 알 수 없는 인물입니다. ($heroId)")
        val record = roster.recordOf(heroId)
        if (record.owned) {
            return HeroActionResult(roster, false, "[설득] ${hero.name}은(는) 이미 우리 리더입니다.")
        }
        if (record.stage == HeroStage.UNDISCOVERED) {
            return HeroActionResult(roster, false, "[설득 불가] 먼저 탐색으로 접점을 확보하세요.")
        }
        if (record.stage == HeroStage.PERSUADED) {
            return HeroActionResult(roster, false, "[설득] ${hero.name}은(는) 이미 마음을 열었습니다. 고용만 남았습니다.")
        }
        if (record.affinity < HeroRoster.PERSUADE_AFFINITY) {
            return HeroActionResult(
                roster, false,
                "[설득 불가] 호감도 ${record.affinity} — 기준 ${HeroRoster.PERSUADE_AFFINITY} 미달입니다."
            )
        }
        val power = prestige + diplomacyBonus
        if (power < hero.requiredPrestige) {
            val next = roster.copy(
                records = roster.records + (
                    heroId to record.copy(
                        affinity = (record.affinity + HeroRoster.PERSUADE_FAIL_AFFINITY).coerceAtMost(100)
                    )
                    )
            )
            return HeroActionResult(
                roster = next,
                success = false,
                message = "[설득 실패] ${hero.name} — 명성 ${power} / 필요 ${hero.requiredPrestige}. " +
                    "명성을 더 쌓고 다시 찾아가야 합니다. (호감도 +${HeroRoster.PERSUADE_FAIL_AFFINITY})"
            )
        }
        val next = roster.copy(
            records = roster.records + (
                heroId to record.copy(
                    stage = HeroStage.PERSUADED,
                    affinity = HeroRoster.PERSUADED_AFFINITY
                )
                )
        )
        return HeroActionResult(
            roster = next,
            success = true,
            message = "[설득 성공] ${hero.name} — \"${hero.quote}\" 이제 고용 계약만 남았습니다."
        )
    }

    /** 고용 — 자금을 지급하고 보유 영웅으로 만든다. 빈 슬롯이 있으면 바로 배치한다. */
    fun hire(
        roster: HeroRosterState,
        heroId: String,
        gold: Int
    ): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[고용] 알 수 없는 인물입니다. ($heroId)")
        val record = roster.recordOf(heroId)
        if (record.owned) {
            return HeroActionResult(roster, false, "[고용] ${hero.name}은(는) 이미 우리 리더입니다.")
        }
        if (record.stage != HeroStage.PERSUADED) {
            return HeroActionResult(roster, false, "[고용 불가] 설득을 먼저 마쳐야 합니다.")
        }
        if (gold < hero.hireCost) {
            return HeroActionResult(
                roster, false,
                "[고용 불가] 자금이 부족합니다. (필요 ${hero.hireCost}G / 보유 ${gold}G)"
            )
        }
        val slotFree = roster.heroIn(hero.role) == null
        val next = roster.copy(
            records = roster.records + (
                heroId to record.copy(
                    stage = HeroStage.HIRED,
                    affinity = 80,
                    assignedRole = if (slotFree) hero.role else null
                )
                )
        )
        val tail = if (slotFree) " ${hero.role.label} 슬롯에 배치했습니다." else " 슬롯이 가득 차 대기 중입니다."
        return HeroActionResult(
            roster = next,
            success = true,
            message = "[고용 완료] ${hero.name} · ${hero.role.label} — ${hero.hireCost}G.$tail",
            goldDelta = -hero.hireCost
        )
    }

    /** 슬롯 배치 — 같은 역할에 이미 앉은 영웅은 대기로 내린다. */
    fun assign(
        roster: HeroRosterState,
        heroId: String,
        role: HeroRole
    ): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[배치] 알 수 없는 인물입니다. ($heroId)")
        if (!roster.recordOf(heroId).owned) {
            return HeroActionResult(roster, false, "[배치 불가] 아직 영입하지 않은 영웅입니다.")
        }
        if (hero.role != role) {
            return HeroActionResult(
                roster, false,
                "[배치 불가] ${hero.name}은(는) ${hero.role.label} 역할입니다. ${role.label} 슬롯에는 앉을 수 없습니다."
            )
        }
        val current = roster.recordOf(heroId)
        if (current.assignedRole == role) {
            return HeroActionResult(roster, false, "[배치] ${hero.name}은(는) 이미 ${role.label} 슬롯에 있습니다.")
        }
        var records = roster.records
        roster.heroIn(role)?.let { other ->
            records = records + (other.id to roster.recordOf(other.id).copy(assignedRole = null))
        }
        records = records + (heroId to current.copy(assignedRole = role))
        return HeroActionResult(
            roster = roster.copy(records = records),
            success = true,
            message = "[배치] ${hero.name} → ${role.label} — ${hero.effectLabel}"
        )
    }

    /** 슬롯에서 내리기 */
    fun unassign(roster: HeroRosterState, heroId: String): HeroActionResult {
        val hero = HeroRoster.byId(heroId)
            ?: return HeroActionResult(roster, false, "[배치] 알 수 없는 인물입니다. ($heroId)")
        val current = roster.recordOf(heroId)
        if (current.assignedRole == null) {
            return HeroActionResult(roster, false, "[배치] ${hero.name}은(는) 대기 중입니다.")
        }
        return HeroActionResult(
            roster = roster.copy(
                records = roster.records + (heroId to current.copy(assignedRole = null))
            ),
            success = true,
            message = "[대기] ${hero.name}을(를) 슬롯에서 내렸습니다."
        )
    }

    /** 슬롯 자동 채우기 — 빈 슬롯에 대기 영웅을 앉힌다 */
    fun fillEmptySlots(roster: HeroRosterState): HeroRosterState {
        var next = roster
        next.unplaced.forEach { hero ->
            if (next.heroIn(hero.role) == null) {
                next = assign(next, hero.id, hero.role).roster
            }
        }
        return next
    }
}

/**
 * 로스터 011~303번 한 줄을 영웅으로 만든다 — [HeroExpansion]이 쓴다.
 *
 * 배치 효과: 역할 주 능력치 ÷ 6 + 등급 보정(SSR +4 · SR +2 · R 0 · N -2), 3~24%.
 * 필요 명성: 등급 기준값 + (100 - 주 능력치) ÷ 6.
 * 고용비: 등급 기준값 + 주 능력치 × 4G (50G 단위).
 */
internal fun buildRosterHero(
    no: Int,
    name: String,
    role: HeroRole,
    grade: HeroGrade,
    age: Int,
    gender: String,
    job: String,
    specialty: String,
    admin: Int,
    tech: Int,
    commerce: Int,
    ops: Int,
    diplomacy: Int,
    quote: String,
    origin: String = "",
    ethnicity: String = ""
): Hero {
    val stats = HeroStats(admin = admin, tech = tech, commerce = commerce, ops = ops, diplomacy = diplomacy)
    val primary = stats.primaryOf(role)
    val gradeFactor = when (grade) {
        HeroGrade.SSR -> 4
        HeroGrade.SR -> 2
        HeroGrade.R -> 0
        HeroGrade.N -> -2
    }
    val effect = (primary / 6 + gradeFactor).coerceIn(3, 24)
    val prestige = (grade.prestigeBase + (100 - primary) / 6).coerceIn(1, 100)
    val hireCost = ((grade.hireBase + primary * 4) / 50 * 50).coerceAtLeast(300)
    return Hero(
        id = "hero_" + no.toString().padStart(3, '0'),
        name = name,
        role = role,
        title = job,
        quote = quote,
        bonus = HeroBonus.of(role, effect),
        requiredPrestige = prestige,
        hireCost = hireCost,
        grade = grade,
        stats = stats,
        specialty = specialty,
        age = age,
        gender = gender,
        origin = origin,
        ethnicity = ethnicity
    )
}
