package me.proton.android.pass.extension

import androidx.lifecycle.SavedStateHandle

fun <T : Any> SavedStateHandle.require(key: String) =
    get<T>(key) ?: throw IllegalArgumentException("$key is required")
