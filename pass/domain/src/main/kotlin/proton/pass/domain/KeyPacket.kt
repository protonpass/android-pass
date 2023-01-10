package proton.pass.domain

data class KeyPacket(
    val rotationId: String,
    val keyPacket: ByteArray
)
