package proton.android.pass.state.impl

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import proton.android.pass.state.api.SavedStateInterface
import javax.inject.Inject

@ViewModelScoped
class SavedStateInterfaceImpl @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : SavedStateInterface {

    override fun <T> set(key: String, value: T?) {
        savedStateHandle[key] = value
    }

    override fun <T> get(key: String): T? = savedStateHandle[key]

    override fun <T> require(key: String): T =
        requireNotNull(get<T>(key)) { "Required value $key was null" }
}
