package proton.android.pass.commonui.api

import androidx.lifecycle.SavedStateHandle

fun <T> SavedStateHandle.require(name: String): T =
    requireNotNull(get<T>(name)) { "Required value $name was null" }
