/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.commonrust.fakes

import proton.android.pass.commonrust.api.SshKeyGenerator
import proton.android.pass.domain.SshKeyComponents
import proton.android.pass.domain.SshKeyType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSshKeyGenerator @Inject constructor() : SshKeyGenerator {

    override suspend fun generateSshKey(type: SshKeyType): SshKeyComponents = SshKeyComponents(
        publicKey = "ssh-${type.name.lowercase()} fake-public-key user@host",
        privateKey = "-----BEGIN OPENSSH PRIVATE KEY-----\n" +
            "fake-private-key-${type.name.lowercase()}\n" +
            "-----END OPENSSH PRIVATE KEY-----"
    )
}
