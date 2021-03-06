package com.revolgenx.anilib.ui.viewmodel

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

abstract class BaseViewModel : ViewModel() {
    protected val compositeDisposable: CompositeDisposable = CompositeDisposable()


    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}
