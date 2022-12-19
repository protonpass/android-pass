package me.proton.android.pass.data.api.crypto

interface EncryptionContextProvider {
    fun <R> withContext(block: EncryptionContext.() -> R): R
    suspend fun <R> withContextSuspendable(block: suspend EncryptionContext.() -> R): R
}
