package me.proton.pass.test

import androidx.lifecycle.SavedStateHandle

object TestSavedStateHandle {
    fun create(): SavedStateHandle = SavedStateHandle(mapOf())
}
