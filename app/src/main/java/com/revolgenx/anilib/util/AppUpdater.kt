package com.revolgenx.anilib.util

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.FragmentManager
import com.github.javiersantos.appupdater.AppUpdaterUtils
import com.github.javiersantos.appupdater.enums.AppUpdaterError
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.javiersantos.appupdater.objects.Update
import com.revolgenx.anilib.BuildConfig
import com.revolgenx.anilib.R
import com.revolgenx.anilib.constant.GITHUB_NAME
import com.revolgenx.anilib.constant.PROJECT_NAME
import com.revolgenx.anilib.dialog.LoadingDialog
import com.revolgenx.anilib.dialog.MessageDialog
import com.revolgenx.anilib.preference.getUpdateVersion
import com.revolgenx.anilib.preference.setUpdateVersion
import com.revolgenx.anilib.ui.view.makeToast
import timber.log.Timber

object AppUpdater {
    fun startAppUpdater(
        context: Context,
        fragmentManager: FragmentManager,
        manualCheck: Boolean = false,
    ) {
        val loadingDialog = if (manualCheck) {
            LoadingDialog.createLoadingDialog(
                context.getString(R.string.checking_for_update)
            )
        } else null

        loadingDialog?.show(fragmentManager, LoadingDialog::class.java.simpleName)

        AppUpdaterUtils(context)
            .setUpdateFrom(UpdateFrom.GITHUB)
            .setGitHubUserAndRepo(GITHUB_NAME, PROJECT_NAME)
            .withListener(object : AppUpdaterUtils.UpdateListener {
                override fun onSuccess(update: Update?, isUpdateAvailable: Boolean?) {
                    loadingDialog?.dismiss()
                    if (update != null) {
                        Timber.d(update.latestVersion)
                        if (update.latestVersion > getUpdateVersion(context) || (isUpdateAvailable == true && manualCheck)) {
                            setUpdateVersion(update.latestVersion)
                            with(MessageDialog.Companion.Builder()) {
                                titleRes = R.string.new_update_available
                                messageRes = R.string.new_update_description
                                positiveTextRes = R.string.update_
                                negativeTextRes = R.string.no_thanks
                                build().let {
                                    it.onButtonClickedListener = { _, which ->
                                        if (which == DialogInterface.BUTTON_POSITIVE) {
                                            openUpdateLink(context)
                                        }
                                    }
                                    it.show(
                                        fragmentManager,
                                        AppUpdater::class.java.simpleName
                                    )
                                }
                            }
                        } else {
                            if (manualCheck) {
                                with(MessageDialog.Companion.Builder()) {
                                    titleRes = R.string.no_update_available
                                    messageRes = R.string.no_update_description
                                    positiveTextRes = R.string.done
                                    build().show(
                                        fragmentManager,
                                        AppUpdater::class.java.simpleName
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onFailed(error: AppUpdaterError?) {
                    Timber.d("success updatesu failed")
                    Timber.d(error.toString())
                    loadingDialog?.dismiss()
                    if (manualCheck) {
                        context.makeToast(R.string.checking_for_update_failed)
                    }
                }
            }).start()
    }

    fun openUpdateLink(context: Context) {
        val updateUrl =
            context.getString(
                if (BuildConfig.APPLICATION_ID.contains(
                        "ultra",
                        true
                    )
                ) R.string.site_url else R.string.playstore
            )
        context.openLink(updateUrl)
    }
}