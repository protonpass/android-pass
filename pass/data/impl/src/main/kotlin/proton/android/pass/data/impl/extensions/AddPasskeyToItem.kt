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

package proton.android.pass.data.impl.extensions

import com.google.protobuf.ByteString
import proton.android.pass.domain.Passkey
import proton_pass_item_v1.ItemV1
import proton_pass_item_v1.ItemV1.Passkey as ProtoPasskey

fun ItemV1.Item.withPasskey(passkey: Passkey): ItemV1.Item {
    val passkeys = content.login.passkeysList.toMutableList()
    val protoPasskey = with(passkey) {
        var builder = ProtoPasskey.newBuilder()
            .setKeyId(id.value)
            .setDomain(domain)
            .setRpId(rpId)
            .setRpName(rpName)
            .setUserName(userName)
            .setUserDisplayName(userDisplayName)
            .setUserId(ByteString.copyFrom(userId.data))
            .setContent(ByteString.copyFrom(contents.data))
            .setCreateTime(createTime.epochSeconds.toInt())
            .setNote(note)
            .setUserHandle(ByteString.copyFrom(userHandle?.data))
            .setCredentialId(ByteString.copyFrom(credentialId.data))

        creationData?.let { data ->
            builder = builder.setCreationData(
                ItemV1.PasskeyCreationData.newBuilder()
                    .setOsName(data.osName)
                    .setOsVersion(data.osVersion)
                    .setDeviceName(data.deviceName)
                    .setAppVersion(data.appVersion)
                    .build()
            )
        }

        builder.build()
    }

    passkeys.add(protoPasskey)
    return this.toBuilder()
        .setContent(
            content.toBuilder()
                .setLogin(
                    content.login.toBuilder()
                        .clearPasskeys()
                        .addAllPasskeys(passkeys)
                        .build()
                )
                .build()
        )
        .build()
}
