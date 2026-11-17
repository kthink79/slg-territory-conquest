package com.game.slg.ui

import android.content.Context
import com.game.slg.core.save.CampaignSave
import com.game.slg.core.save.CampaignStore

/**
 * SharedPreferences 기반 진행 저장소.
 * 인트로 시청 여부와 재개용 캠페인 최소값을 보관한다.
 */
class AndroidCampaignStore(context: Context) : CampaignStore {

    private val prefs = context.applicationContext
        .getSharedPreferences("slg_campaign", Context.MODE_PRIVATE)

    override fun isIntroSeen(): Boolean = prefs.getBoolean(KEY_INTRO_SEEN, false)

    override fun setIntroSeen(seen: Boolean) {
        prefs.edit().putBoolean(KEY_INTRO_SEEN, seen).apply()
    }

    override fun load(): CampaignSave? {
        val regionCode = prefs.getString(KEY_REGION, null) ?: return null
        val lordId = prefs.getString(KEY_LORD, null) ?: return null
        return CampaignSave(
            regionCode = regionCode,
            lordId = lordId,
            level = prefs.getInt(KEY_LEVEL, 1),
            xp = prefs.getInt(KEY_XP, 0),
            gold = prefs.getInt(KEY_GOLD, 250),
            food = prefs.getInt(KEY_FOOD, 300),
            troops = prefs.getInt(KEY_TROOPS, 100),
            morale = prefs.getInt(KEY_MORALE, 60),
            prestige = prefs.getInt(KEY_PRESTIGE, 40),
            facilities = prefs.getStringSet(KEY_FACILITIES, emptySet()).orEmpty(),
            researchedTechs = prefs.getStringSet(KEY_TECHS, emptySet()).orEmpty(),
            alliedTerritories = prefs.getInt(KEY_ALLIED, 0),
            promotionStage = prefs.getString(KEY_STAGE, "DISTRICT_LORD") ?: "DISTRICT_LORD",
            councilAuthorityBonus = prefs.getInt(KEY_AUTHORITY, 0),
            spawnTurns = prefs.getInt(KEY_SPAWN_TURNS, 0),
            ownedHeroes = prefs.getStringSet(KEY_HEROES, null)?.orEmpty()
                ?: com.game.slg.core.hero.HeroRosterState.STARTING_IDS.toSet(),
            heroPlacements = prefs.getStringSet(KEY_HERO_SLOTS, emptySet()).orEmpty()
                .mapNotNull { entry ->
                    val parts = entry.split(':')
                    if (parts.size == 2) parts[0] to parts[1] else null
                }
                .toMap(),
            heroAffinity = prefs.getStringSet(KEY_HERO_AFFINITY, emptySet()).orEmpty()
                .mapNotNull { entry ->
                    val parts = entry.split(':')
                    parts.getOrNull(0)?.let { id -> id to (parts.getOrNull(1)?.toIntOrNull() ?: 80) }
                }
                .toMap()
        )
    }

    override fun save(save: CampaignSave?) {
        val editor = prefs.edit()
        if (save == null) {
            editor.clear().putBoolean(KEY_INTRO_SEEN, true).apply()
            return
        }
        editor
            .putString(KEY_REGION, save.regionCode)
            .putString(KEY_LORD, save.lordId)
            .putInt(KEY_LEVEL, save.level)
            .putInt(KEY_XP, save.xp)
            .putInt(KEY_GOLD, save.gold)
            .putInt(KEY_FOOD, save.food)
            .putInt(KEY_TROOPS, save.troops)
            .putInt(KEY_MORALE, save.morale)
            .putInt(KEY_PRESTIGE, save.prestige)
            .putStringSet(KEY_FACILITIES, save.facilities)
            .putStringSet(KEY_TECHS, save.researchedTechs)
            .putInt(KEY_ALLIED, save.alliedTerritories)
            .putString(KEY_STAGE, save.promotionStage)
            .putInt(KEY_AUTHORITY, save.councilAuthorityBonus)
            .putInt(KEY_SPAWN_TURNS, save.spawnTurns)
            .putStringSet(KEY_HEROES, save.ownedHeroes)
            .putStringSet(
                KEY_HERO_SLOTS,
                save.heroPlacements.map { (id, role) -> "$id:$role" }.toSet()
            )
            .putStringSet(
                KEY_HERO_AFFINITY,
                save.heroAffinity.map { (id, value) -> "$id:$value" }.toSet()
            )
            .apply()
    }

    companion object {
        private const val KEY_INTRO_SEEN = "intro_seen"
        private const val KEY_REGION = "region_code"
        private const val KEY_LORD = "lord_id"
        private const val KEY_LEVEL = "level"
        private const val KEY_XP = "xp"
        private const val KEY_GOLD = "gold"
        private const val KEY_FOOD = "food"
        private const val KEY_TROOPS = "troops"
        private const val KEY_MORALE = "morale"
        private const val KEY_PRESTIGE = "prestige"
        private const val KEY_FACILITIES = "facilities"
        private const val KEY_TECHS = "techs"
        private const val KEY_ALLIED = "allied"
        private const val KEY_STAGE = "promotion_stage"
        private const val KEY_AUTHORITY = "council_authority"
        private const val KEY_SPAWN_TURNS = "spawn_turns"
        private const val KEY_HEROES = "owned_heroes"
        private const val KEY_HERO_SLOTS = "hero_slots"
        private const val KEY_HERO_AFFINITY = "hero_affinity"
    }
}
