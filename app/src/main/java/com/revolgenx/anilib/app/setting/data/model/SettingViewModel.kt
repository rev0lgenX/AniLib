package com.revolgenx.anilib.app.setting.data.model

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.revolgenx.anilib.common.preference.storeMediaOptions
import com.revolgenx.anilib.app.setting.data.field.MediaSettingField
import com.revolgenx.anilib.app.setting.data.field.MediaSettingMutateField
import com.revolgenx.anilib.common.repository.util.Resource
import com.revolgenx.anilib.app.setting.service.SettingService
import com.revolgenx.anilib.common.viewmodel.BaseViewModel

class SettingViewModel(private val settingService: SettingService) : BaseViewModel() {

    val mediaOptionLiveData: MutableLiveData<Resource<UserOptionsModel>> =
        MutableLiveData()

    val saveMediaOptionLiveData = MutableLiveData<Resource<Boolean>>()

    val mediaSettingField = MediaSettingField()

    fun getMediaSetting() {
        mediaOptionLiveData.value = Resource.loading()
        settingService.getMediaSetting(mediaSettingField, compositeDisposable) {
            if (it is Resource.Success) {
                storeMediaOptions(it.data)
            }

            mediaOptionLiveData.value = it
        }
    }

    fun setMediaSetting(field: MediaSettingMutateField) {
        saveMediaOptionLiveData.value = Resource.loading()
        settingService.saveMediaSetting(field, compositeDisposable) {
            if (it is Resource.Success) {
                storeMediaOptions(field.model)
            }
            saveMediaOptionLiveData.value = it
        }
    }

}