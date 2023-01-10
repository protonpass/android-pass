package proton.android.pass.crypto.api.context

interface EncryptionContextProvider {
    fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R
    suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R
}
