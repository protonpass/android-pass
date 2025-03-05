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

package proton.android.pass.passkeys.impl

import kotlinx.datetime.Clock
import proton.android.pass.commonrust.PasskeyManager
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyId
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.passkeys.api.GeneratedPasskey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratePasskeyImpl @Inject constructor(
    private val passkeyManager: PasskeyManager,
    private val getPasskeyCreationData: GetPasskeyCreationData,
    private val clock: Clock
) : GeneratePasskey {

    override fun invoke(url: String, request: String): GeneratedPasskey {
        val sanitized = PasskeyJsonSanitizer.sanitize(request)
        passkeyManager.generatePasskey(url, sanitized).let {
            val creationData = getPasskeyCreationData()
            return GeneratedPasskey(
                passkey = Passkey(
                    rpName = it.rpName,
                    userDisplayName = it.userDisplayName,
                    userName = it.userName,
                    domain = it.domain,
                    id = PasskeyId(it.keyId),
                    rpId = it.rpId,
                    userId = ByteArrayWrapper(it.userId),
                    contents = ByteArrayWrapper(it.passkey),
                    note = "",
                    createTime = clock.now(),
                    credentialId = ByteArrayWrapper(it.credentialId),
                    userHandle = it.userHandle?.let(::ByteArrayWrapper),
                    creationData = creationData
                ),
                response = it.response
            )
        }
    }

}
