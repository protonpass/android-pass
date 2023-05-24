package proton.android.pass.commonui.impl

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import proton.android.pass.commonui.api.SavedStateHandleProvider
import javax.inject.Inject

@ViewModelScoped
class SavedStateHandleProviderImpl @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : SavedStateHandleProvider {
    override fun get(): SavedStateHandle = savedStateHandle
}
