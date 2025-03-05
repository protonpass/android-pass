/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.commonuimodels.api

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyCreationData
import proton.android.pass.domain.PasskeyId

@Stable
@Parcelize
@Serializable
data class UIPasskeyCreationData(
    val osName: String,
    val osVersion: String,
    val deviceName: String,
    val appVersion: String
) : Parcelable {
    fun toDomain() = PasskeyCreationData(
        osName = osName,
        osVersion = osVersion,
        deviceName = deviceName,
        appVersion = appVersion
    )

    companion object {
        fun from(creationData: PasskeyCreationData) = with(creationData) {
            UIPasskeyCreationData(
                osName = osName,
                osVersion = osVersion,
                deviceName = deviceName,
                appVersion = appVersion
            )
        }
    }
}

@Stable
@Parcelize
@Serializable
data class UIPasskeyContent(
    val id: String,
    val domain: String,
    val rpId: String?,
    val rpName: String,
    val userName: String,
    val userDisplayName: String,
    val userId: ByteArray,
    val contents: ByteArray,
    val createTime: Int,
    val note: String,
    val userHandle: ByteArray?,
    val credentialId: ByteArray,
    val creationData: UIPasskeyCreationData?
) : Parcelable {

    @Suppress("ReturnCount")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UIPasskeyContent

        if (id != other.id) return false
        if (domain != other.domain) return false
        if (rpId != other.rpId) return false
        if (rpName != other.rpName) return false
        if (userName != other.userName) return false
        if (userDisplayName != other.userDisplayName) return false
        if (!userId.contentEquals(other.userId)) return false
        if (createTime != other.createTime) return false
        if (note != other.note) return false
        if (creationData != other.creationData) return false
        return contents.contentEquals(other.contents)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + domain.hashCode()
        result = 31 * result + rpId.hashCode()
        result = 31 * result + rpName.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + userDisplayName.hashCode()
        result = 31 * result + userId.contentHashCode()
        result = 31 * result + contents.contentHashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + note.hashCode()
        result = 31 * result + creationData.hashCode()
        return result
    }

    fun toDomain(): Passkey = Passkey(
        id = PasskeyId(id),
        domain = domain,
        rpId = rpId,
        rpName = rpName,
        userName = userName,
        userDisplayName = userDisplayName,
        userId = ByteArrayWrapper(userId),
        contents = ByteArrayWrapper(contents),
        createTime = Instant.fromEpochSeconds(createTime.toLong()),
        note = note,
        credentialId = ByteArrayWrapper(credentialId),
        userHandle = userHandle?.let(::ByteArrayWrapper),
        creationData = creationData?.toDomain()
    )

    companion object {
        fun from(passkey: Passkey) = with(passkey) {
            UIPasskeyContent(
                id = id.value,
                domain = domain,
                rpId = rpId,
                rpName = rpName,
                userName = userName,
                userDisplayName = userDisplayName,
                contents = contents.data,
                createTime = createTime.epochSeconds.toInt(),
                note = note,
                userId = userId.data,
                credentialId = credentialId.data,
                userHandle = userHandle?.data,
                creationData = creationData?.let { UIPasskeyCreationData.from(it) }
            )
        }
    }
}

