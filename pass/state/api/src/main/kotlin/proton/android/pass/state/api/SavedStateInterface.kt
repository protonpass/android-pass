package proton.android.pass.state.api

interface SavedStateInterface {
    fun <T> set(key: String, value: T?)
    fun <T> get(key: String): T?
    fun <T> require(key: String): T
}
