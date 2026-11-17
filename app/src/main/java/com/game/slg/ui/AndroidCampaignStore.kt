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
            councilAuthorityBonus = prefs.getInt(KEY_AUTHORITY, 0)
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
    }
}
