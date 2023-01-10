package proton.android.pass.test

import androidx.lifecycle.SavedStateHandle

object TestSavedStateHandle {
    fun create(): SavedStateHandle = SavedStateHandle(mapOf())
}
