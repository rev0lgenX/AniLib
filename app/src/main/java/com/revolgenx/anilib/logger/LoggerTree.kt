package com.revolgenx.anilib.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class LoggerTree: Timber.DebugTree(){
    init {
        FirebaseCrashlytics.getInstance()
            .setCrashlyticsCollectionEnabled(false)
    }
}
