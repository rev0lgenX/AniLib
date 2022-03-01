package com.revolgenx.anilib.common.preference

import android.content.Context
import androidx.work.WorkManager
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import com.pranavpandey.android.dynamic.preferences.DynamicPreferences
import com.pranavpandey.android.dynamic.theme.DynamicPalette
import com.revolgenx.anilib.R
import com.revolgenx.anilib.app.theme.ThemeController
import com.revolgenx.anilib.airing.data.field.AiringMediaField
import com.revolgenx.anilib.app.setting.data.model.MediaListOptionModel
import com.revolgenx.anilib.app.setting.data.model.UserOptionsModel
import com.revolgenx.anilib.notification.service.NotificationWorker
import com.revolgenx.anilib.type.ScoreFormat
import com.revolgenx.anilib.type.UserTitleLanguage
import com.revolgenx.anilib.user.data.model.UserModel
import com.revolgenx.anilib.util.shortcutAction

private const val userModelKey = "save_user_model_key"
private const val loggedInKey = "logged_in_key"
private const val tokenKey = "token_key"
private const val titleKey = "title_key"
private const val imageQualityKey = "image_quality_key"
private const val userIdKey = "user_id_key"
private const val canShowAdultKey = "can_show_adult_key"
private const val lastNotificationKey = "last_notification_key"
private const val sharedPrefSyncKey = "sharedPrefSyncKey"
private const val updateProfileColorKey = "updateProfileColorKey"
private const val recentAnimeListStatusKey = "recentAnimeListStatusKey"
private const val recentMangaListStatusKey = "recentMangaListStatusKey"
private const val media_info_or_list_editor_key = "media_info_or_list_editor_key"


private const val recent_anime_list_status_key = "recent_anime_list_status_key"
private const val recent_manga_list_status_key = "recent_manga_list_status_key"

object UserPreference{
    val userId get() = DynamicPreferences.getInstance().load(userIdKey, -1)

}

fun Context.loggedIn() = getBoolean(loggedInKey, false)
fun Context.loggedIn(logIn: Boolean) = putBoolean(loggedInKey, logIn)

fun Context.token() = getString(tokenKey, "")
fun Context.token(token: String) = putString(tokenKey, token)

fun Context.userId(userId: Int) = putInt(userIdKey, userId)

fun Context.titlePref() = getString(titleKey, "0")
fun titlePref(context: Context, pref: String) = context.putString(titleKey, pref)

fun Context.imageQuality() = getString(imageQualityKey, "0")

fun Context.userName() = getUserPrefModel(this).name
fun Context.userScoreFormat() =
    getUserPrefModel(this).mediaListOptions!!.scoreFormat!!

private var userModel: UserModel? = null

fun Context.saveBasicUserDetail(userPrefModel: UserModel) {
    userModel = userPrefModel
    this.putString(userModelKey, Gson().toJson(userPrefModel))

    if (shouldUpdateProfileColor(this)) {
        shouldUpdateProfileColor(this, false)
//        userPrefModel.mediaOptions?.profileColor?.let {
//            saveUserAccentColor(it)
//        }
    }
}

fun saveUserAccentColor(it: String) {
    val colors = DynamicPalette.MATERIAL_COLORS
    val accentColorToSave = when (it) {
        "blue" -> {
            colors[5]
        }
        "purple" -> {
            colors[2]
        }
        "pink" -> {
            colors[1]
        }
        "orange" -> {
            colors[14]
        }
        "red" -> {
            colors[0]
        }
        "green" -> {
            colors[9]
        }
        "gray" -> {
            colors[17]
        }
        else -> {
            colors[5]
        }
    }
    ThemeController.accentColor = accentColorToSave
}

fun getUserPrefModel(context: Context): UserModel {
    if (userModel == null) {
        userModel =
            Gson().fromJson(context.getString(userModelKey, ""), UserModel::class.java)
                ?: UserModel().also { model ->
                    model.name = context.getString(R.string.app_name)
                }
    }

    if (userModel!!.options == null) {
        userModel!!.options =
            UserOptionsModel(UserTitleLanguage.ROMAJI.ordinal, false, false, null)
    }

    if (userModel!!.mediaListOptions == null) {
        userModel!!.mediaListOptions = MediaListOptionModel().also { mediaListOptionModel ->
            mediaListOptionModel.scoreFormat = ScoreFormat.POINT_100.ordinal
        }
    }

    return userModel!!
}


private fun removeBasicUserDetail(context: Context) {
    userModel = UserModel().also {
        it.name = context.getString(R.string.app_name)
        it.options = UserOptionsModel(UserTitleLanguage.ROMAJI.ordinal, false, false, null)
    }
    context.putString(userModelKey, Gson().toJson(userModel))
}

fun Context.logOut() {
    loggedIn(false)
    token("")
    userId(-1)
    titlePref(this, "0")
    removeNotification(this)
    removeBasicUserDetail(this)
    removeAiringField(this)
    shortcutAction(this) {
        it.removeAllDynamicShortcuts()
    }
    removeNotificationWorker(this)
}

private fun removeNotificationWorker(context: Context){
    WorkManager.getInstance(context)
        .cancelUniqueWork(NotificationWorker.NOTIFICATION_WORKER_TAG)
}

private fun removeNotification(context: Context) {
    setNewNotification(context)
}

private fun removeAiringField(context: Context) {
    storeDiscoverAiringField(context, AiringMediaField())
    storeAiringField(context, AiringMediaField())
}

fun Context.logIn(accessToken: String) {
    loggedIn(true)
    token(accessToken)
    val userId = JWT(accessToken).subject?.trim()?.toInt() ?: -1
    userId(userId)
    titlePref(this, "3")
    shortcutAction(this) {
        it.removeAllDynamicShortcuts()
    }
    shouldUpdateProfileColor(this, true)
}

private fun shouldUpdateProfileColor(context: Context, update: Boolean) {
    context.putBoolean(updateProfileColorKey, update)
}

private fun shouldUpdateProfileColor(context: Context) =
    context.getBoolean(updateProfileColorKey, false)


fun getLastNotification(context: Context): Int {
    return context.getInt(lastNotificationKey, -1)
}

fun setNewNotification(context: Context, notifId: Int = -1) {
    context.putInt(lastNotificationKey, notifId)
}


fun canShowAdult(context: Context): Boolean {
    return if (userEnabledAdultContent(context)) {
        context.getBoolean(canShowAdultKey, false)
    } else {
        false
    }
}

fun userEnabledAdultContent(context: Context): Boolean {
    return getStoredMediaOptions(context).displayAdultContent
}

fun isSharedPreferenceSynced(context: Context, synced: Boolean? = null) =
    if (synced == null) {
        context.getBoolean(sharedPrefSyncKey, false)
    } else {
        context.putBoolean(sharedPrefSyncKey, synced)
        synced
    }


fun storeMediaOptions(context: Context, model: UserOptionsModel?) {
    if (model == null) return

    val userPrefModel = getUserPrefModel(context)
    userPrefModel.options = model
    context.saveBasicUserDetail(userPrefModel)
}

fun getStoredMediaOptions(context: Context): UserOptionsModel {
    return getUserPrefModel(context).options!!
}

fun animeListStatusHistory() = dynamicPreferences.load(recent_anime_list_status_key, "All")
fun animeListStatusHistory(value: String) = dynamicPreferences.save(recent_anime_list_status_key, value.trim())

fun mangaListStatusHistory() = dynamicPreferences.load(recent_manga_list_status_key, "All")
fun mangaListStatusHistory(value:String) = dynamicPreferences.save(recent_manga_list_status_key, value.trim())

fun openMediaInfoOrListEditor() = dynamicPreferences.load(media_info_or_list_editor_key, false)