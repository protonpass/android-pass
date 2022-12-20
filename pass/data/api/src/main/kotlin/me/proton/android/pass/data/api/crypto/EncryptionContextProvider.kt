package me.proton.android.pass.data.api.crypto

interface EncryptionContextProvider {
    fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R
    suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R
}
