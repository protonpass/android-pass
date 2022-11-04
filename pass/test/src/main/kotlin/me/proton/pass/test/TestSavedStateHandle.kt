package me.proton.pass.test.core

import androidx.lifecycle.SavedStateHandle

object TestSavedStateHandle {
    fun create(): SavedStateHandle = SavedStateHandle(mapOf())
}
