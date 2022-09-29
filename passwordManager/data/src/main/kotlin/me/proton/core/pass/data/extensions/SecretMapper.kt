package me.proton.core.pass.data.extensions

import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.proton.core.pass.domain.entity.commonsecret.Secret
import me.proton.core.pass.domain.entity.commonsecret.SecretType
import me.proton.core.pass.domain.entity.commonsecret.SecretValue
import me.proton.core.pass.data.db.entities.LoginSecretContents
import me.proton.core.pass.data.db.entities.SecretEntity
import me.proton.core.util.kotlin.serialize

fun Secret.toEntity(): SecretEntity {
    val secretContents: String = when (contents) {
        is SecretValue.Login -> {
            val loginContents = contents as SecretValue.Login
            val loginSecretContents = LoginSecretContents(
                loginContents.identity,
                loginContents.password
            )
            loginSecretContents.serialize(null)
        }
        is SecretValue.Single -> (contents as SecretValue.Single).contents
    }
    return SecretEntity(
        // TODO: remove once these are persisted on backend
        id ?: UUID.randomUUID().toString(),
        userId,
        addressId,
        name,
        type.value,
        isUploaded,
        secretContents,
        associatedUris.joinToString(";")
    )
}

fun SecretEntity.toSecret(): Secret {
    val secretType = SecretType.map[type] ?: SecretType.Other
    val secretContents = when (secretType) {
        SecretType.Login -> {
            val login: LoginSecretContents = Json.decodeFromString(contents)
            SecretValue.Login(login.identity, login.password)
        }
        else -> SecretValue.Single(contents)
    }
    return Secret(
        id,
        userId,
        addressId,
        name,
        secretType,
        isUploaded,
        secretContents,
        associatedUris.split(";")
    )
}
