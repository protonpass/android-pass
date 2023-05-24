package proton.android.pass.commonui.api

import androidx.lifecycle.SavedStateHandle

interface SavedStateHandleProvider {
    fun get(): SavedStateHandle
}

