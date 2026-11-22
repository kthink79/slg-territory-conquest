package com.game.slg.core.save

import com.game.slg.core.hero.HeroRosterState
import com.game.slg.core.region.RegionUnion

/**
 * 진행 저장 — 앱을 다시 열어도 인트로를 반복하지 않고 이어서 플레이한다.
 *
 * 전체 상태를 저장하지 않고, 재개에 필요한 최소값(지역·리더·레벨·자원·시설·승격)만 담는다.
 * 사건·의회 안건 같은 1회성 시나리오는 재개 시 새로 시작한다.
 */
data class CampaignSave(
    val regionCode: String,
    val lordId: String,
    val level: Int,
    val xp: Int,
    val gold: Int,
    val food: Int,
    val troops: Int,
    val morale: Int,
    val prestige: Int,
    val facilities: Set<String> = emptySet(),
    val researchedTechs: Set<String> = emptySet(),
    val alliedTerritories: Int = 0,
    val promotionStage: String = "DISTRICT_LORD",
    val councilAuthorityBonus: Int = 0,
    /** 남은 스폰 보호 턴 (지역 연합) */
    val spawnTurns: Int = RegionUnion.DEFAULT_TURNS,
    /** 보유 영웅 id — 기본 3명 */
    val ownedHeroes: Set<String> = HeroRosterState.STARTING_IDS.toSet(),
    /** 영웅 id → 배치된 역할 이름 */
    val heroPlacements: Map<String, String> = emptyMap(),
    /** 영웅 id → 호감도 */
    val heroAffinity: Map<String, Int> = emptyMap(),
    /** 편입 영지 — "code|name|population|status|morale|development|troops|food|gold|defense|viceroyId|viceroyName|viceroyRole|tribute|absorbedBy" */
    val territories: List<String> = emptyList(),
    /** 용병 부대 — "id|name|troops|hireCost|upkeep|contract|origin" */
    val mercenaries: List<String> = emptyList(),
    /** 무기 기술 이름 → 레벨 */
    val weaponTech: Map<String, Int> = emptyMap(),
    /** LORD / HERO */
    val startMode: String = "LORD",
    /** 영웅 스타트 경력 — "heroId|heroName|lordName|loyalty|merit|privateTroops|employerLordId" */
    val heroCareer: String? = null,
    /** 적 리더 세력 상태 — "factionId|troops|incorporated" (교전으로 깎은 병력·편입 상태 유지) */
    val factionStates: List<String> = emptyList(),
    /** 처분 대기 영웅 — "heroId|lordId|lordName" */
    val pendingDisposition: List<String> = emptyList(),
    /** 보유하지 않은 영웅의 진행 기록 — heroId → "STAGE|affinity" */
    val heroDiscovery: Map<String, String> = emptyMap()
)

interface CampaignStore {
    fun isIntroSeen(): Boolean
    fun setIntroSeen(seen: Boolean)
    fun load(): CampaignSave?
    fun save(save: CampaignSave?)
}

/** 테스트·미리보기용 메모리 저장소 */
class InMemoryCampaignStore(
    private var introSeen: Boolean = false,
    private var save: CampaignSave? = null
) : CampaignStore {
    override fun isIntroSeen(): Boolean = introSeen
    override fun setIntroSeen(seen: Boolean) { introSeen = seen }
    override fun load(): CampaignSave? = save
    override fun save(save: CampaignSave?) { this.save = save }
}
