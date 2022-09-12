package me.proton.core.pass.domain

data class KeyPacket(
    val rotationId: String,
    val keyPacket: ByteArray
)
