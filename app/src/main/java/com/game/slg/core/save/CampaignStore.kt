package com.game.slg.core.save

/**
 * 진행 저장 — 앱을 다시 열어도 인트로를 반복하지 않고 이어서 플레이한다.
 *
 * 전체 상태를 저장하지 않고, 재개에 필요한 최소값(지역·호족·레벨·자원·시설·승격)만 담는다.
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
    val councilAuthorityBonus: Int = 0
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
