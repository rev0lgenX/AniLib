package com.revolgenx.anilib.preference

import android.content.Context
import com.auth0.android.jwt.JWT
import com.google.gson.Gson
import com.pranavpandey.android.dynamic.preferences.DynamicPreferences
import com.pranavpandey.android.dynamic.utils.DynamicPackageUtils
import com.revolgenx.anilib.R
import com.revolgenx.anilib.model.UserPrefModel
import com.revolgenx.anilib.model.list.MediaListOptionModel
import com.revolgenx.anilib.type.ScoreFormat

private const val userModelKey = "user_model_key"
private const val loggedInKey = "logged_in_key"
private const val tokenKey = "token_key"
private const val titleKey = "title_key"
private const val imageQualityKey = "image_quality_key"
private const val userIdKey = "user_id_key"
private const val canShowAdultKey = "can_show_adult_key"
private const val lastNotificationKey = "last_notification_key"
private const val sharedPrefSyncKey = "sharedPrefSyncKey"


fun Context.loggedIn() = getBoolean(loggedInKey, false)
fun Context.loggedIn(logIn: Boolean) = putBoolean(loggedInKey, logIn)

fun Context.token() = getString(tokenKey, "")
fun Context.token(token: String) = putString(tokenKey, token)

fun Context.userId() = getInt(userIdKey, -1)
fun Context.userId(userId: Int) = putInt(userIdKey, userId)

fun Context.titlePref() = getString(titleKey, "0")

fun Context.imageQuality() = getString(imageQualityKey, "0")

fun Context.userName() = getUserPrefModel(this).userName
fun Context.userAvatar() = getUserPrefModel(this).avatar?.image
fun Context.userBannerImage() = getUserPrefModel(this).bannerImage
fun Context.userScoreFormat() =
    getUserPrefModel(this).mediaListOption!!.scoreFormat!!

private var userPrefModelPref: UserPrefModel? = null

fun Context.saveBasicUserDetail(userPrefModel: UserPrefModel) {
    userPrefModelPref = userPrefModel
    this.putString(userModelKey, Gson().toJson(userPrefModel))
}

fun getUserPrefModel(context: Context): UserPrefModel {
    if (userPrefModelPref == null) {
        userPrefModelPref =
            Gson().fromJson(context.getString(userModelKey, ""), UserPrefModel::class.java)
                ?: UserPrefModel().also { model ->
                    model.userName = context.getString(R.string.app_name)
                    model.mediaListOption = MediaListOptionModel().also { mediaListOptionModel ->
                        mediaListOptionModel.scoreFormat = ScoreFormat.POINT_100.ordinal
                    }
                }
    }

    return userPrefModelPref!!
}


fun Context.removeBasicUserDetail() {
    userPrefModelPref = UserPrefModel().also {
        it.userName = getString(R.string.app_name)
        it.mediaListOption = MediaListOptionModel().also { option ->
            option.scoreFormat = ScoreFormat.`$UNKNOWN`.ordinal
        }
    }
    this.putString(userModelKey, Gson().toJson(userPrefModelPref))
}

fun Context.logOut() {
    loggedIn(false)
    token("")
    userId(-1)
    removeNotification()
    removeBasicUserDetail()
}

fun Context.removeNotification() {
    setNewNotification(this)
}

fun Context.logIn(accessToken: String) {
    loggedIn(true)
    token(accessToken)
    val userId = JWT(accessToken).subject?.trim()?.toInt() ?: -1
    userId(userId)
}

fun getLastNotification(context: Context): Int {
    return context.getInt(lastNotificationKey, -1)
}

fun setNewNotification(context: Context, notifId: Int = -1) {
    context.putInt(lastNotificationKey, notifId)
}


fun canShowAdult(context: Context): Boolean {
    return context.getBoolean(canShowAdultKey, true)
}

fun isSharedPreferenceSynced(context: Context, synced: Boolean? = null) =
    if (synced == null) {
        context.getBoolean(sharedPrefSyncKey, false)
    } else {
        context.putBoolean(sharedPrefSyncKey, synced)
        synced
    }

