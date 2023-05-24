package proton.android.pass.commonui.fakes

import androidx.lifecycle.SavedStateHandle
import proton.android.pass.commonui.api.SavedStateHandleProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSavedStateHandleProvider @Inject constructor() : SavedStateHandleProvider {

    private val instance = SavedStateHandle()

    override fun get(): SavedStateHandle = instance
}
