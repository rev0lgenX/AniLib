package com.revolgenx.anilib.common.ext

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavBackStackEntry
import cafe.adriel.voyager.androidx.AndroidScreenLifecycleOwner
import cafe.adriel.voyager.core.lifecycle.ScreenLifecycleOwner
import org.koin.androidx.viewmodel.ext.android.toExtras
import org.koin.androidx.viewmodel.resolveViewModel
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope


@OptIn(KoinInternalApi::class)
inline fun <reified T : ViewModel> ScreenLifecycleOwner.koinViewModel(
    qualifier: Qualifier? = null,
    key: String? = null,
    extras: CreationExtras = defaultExtras(this as AndroidScreenLifecycleOwner),
    scope: Scope = GlobalContext.get().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null,
): T {
    return (this as AndroidScreenLifecycleOwner).viewModel(
        qualifier = qualifier,
        key = key,
        extras = extras,
        scope = scope,
        parameters = parameters
    )
}

@OptIn(KoinInternalApi::class)
inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModel(
    qualifier: Qualifier? = null,
    key: String? = null,
    extras: CreationExtras = defaultExtras(this),
    scope: Scope = GlobalContext.get().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null,
): T {
    val currentBundle = (this as? NavBackStackEntry)?.arguments?.toExtras(this)
    return resolveViewModel(
        T::class, this.viewModelStore, key, currentBundle ?: extras, qualifier, scope, parameters
    )
}


@OptIn(KoinInternalApi::class)
fun defaultExtras(viewModelStoreOwner: ViewModelStoreOwner): CreationExtras = when {
    //TODO To be fully verified
    viewModelStoreOwner is NavBackStackEntry && viewModelStoreOwner.arguments != null -> viewModelStoreOwner.arguments?.toExtras(
        viewModelStoreOwner
    ) ?: CreationExtras.Empty

    viewModelStoreOwner is HasDefaultViewModelProviderFactory -> {
        viewModelStoreOwner.defaultViewModelCreationExtras
    }

    else -> {
        CreationExtras.Empty
    }
}