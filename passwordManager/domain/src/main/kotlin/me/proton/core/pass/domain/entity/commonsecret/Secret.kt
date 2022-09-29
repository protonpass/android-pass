package me.proton.core.pass.domain.entity.commonsecret

data class Secret(
    val id: String?,
    val userId: String,
    val addressId: String,
    val name: String,
    val type: SecretType,
    val isUploaded: Boolean,
    val contents: SecretValue,
    val associatedUris: List<String>
)
