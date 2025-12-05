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

package proton.android.pass.passkeys.fakes

import kotlinx.datetime.Clock
import proton.android.pass.domain.ByteArrayWrapper
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyCreationData
import proton.android.pass.domain.PasskeyId
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.passkeys.api.GeneratedPasskey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGeneratePasskey @Inject constructor() : GeneratePasskey {

    private var result: Result<GeneratedPasskey> = run {
        val passkey = GeneratedPasskey(
            passkey = Passkey(
                id = PasskeyId("passkeyId"),
                domain = "domain",
                rpId = null,
                rpName = "rpName",
                contents = ByteArrayWrapper(byteArrayOf(7, 8, 9)),
                userName = "userName",
                userDisplayName = "userDisplayName",
                userId = ByteArrayWrapper(byteArrayOf(1, 2, 3)),
                note = "note",
                createTime = Clock.System.now(),
                credentialId = ByteArrayWrapper(byteArrayOf(4, 5, 6)),
                userHandle = ByteArrayWrapper(byteArrayOf(10, 11, 12)),
                creationData = PasskeyCreationData(
                    osName = "osName",
                    osVersion = "osVersion",
                    deviceName = "deviceName",
                    appVersion = "appVersion"
                )
            ),
            response = ""
        )
        Result.success(passkey)
    }

    fun setResult(value: Result<GeneratedPasskey>) {
        result = value
    }

    override fun invoke(url: String, request: String): GeneratedPasskey = result.getOrThrow()
}
