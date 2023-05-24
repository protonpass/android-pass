package proton.android.pass.state.fakes

import proton.android.pass.state.api.SavedStateInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSavedStateInterface @Inject constructor() : SavedStateInterface {

    private val map = mutableMapOf<String, Any?>()

    fun clear() {
        map.clear()
    }

    override fun <T> set(key: String, value: T?) {
        map[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? = map[key] as? T

    override fun <T> require(key: String): T = requireNotNull(get<T>(key)) { "Required value $key was null" }
}
