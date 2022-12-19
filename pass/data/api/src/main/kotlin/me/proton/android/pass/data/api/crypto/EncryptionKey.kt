package me.proton.android.pass.data.api.crypto

data class EncryptionKey(val key: ByteArray) {
    fun clear() {
        key.indices.map { idx ->
            key.set(idx, 0x00.toByte())
        }
    }
}
