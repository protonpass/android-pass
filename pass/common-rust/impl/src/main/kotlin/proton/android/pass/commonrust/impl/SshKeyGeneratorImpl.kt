/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.commonrust.impl

import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.commonrust.SshKeyManager
import proton.android.pass.commonrust.api.SshKeyGenerator
import proton.android.pass.domain.SshKeyComponents
import proton.android.pass.domain.SshKeyType
import javax.inject.Inject
import proton.android.pass.commonrust.SshKeyType as RustSshKeyType

class SshKeyGeneratorImpl @Inject constructor(
    private val appDispatchers: AppDispatchers,
    private val sshKeyManager: SshKeyManager
) : SshKeyGenerator {

    override suspend fun generateSshKey(type: SshKeyType): SshKeyComponents = withContext(appDispatchers.default) {
        val rustType = when (type) {
            SshKeyType.ED25519 -> RustSshKeyType.ED25519
            SshKeyType.RSA_2048 -> RustSshKeyType.RSA2048
            SshKeyType.RSA_4096 -> RustSshKeyType.RSA4096
        }

        withTimeout(GENERATION_TIMEOUT_MS) {
            val result = sshKeyManager.generateSshKey(
                comment = "",
                keyType = rustType,
                passphrase = null
            )

            val components = SshKeyComponents(
                publicKey = result.publicKey,
                privateKey = result.privateKey
            )

            validateSshKeyComponents(type, components)
            components
        }
    }

    private fun validateSshKeyComponents(type: SshKeyType, components: SshKeyComponents) {
        require(components.publicKey.isNotBlank()) {
            "Generated public key is empty for type $type"
        }
        require(components.privateKey.isNotBlank()) {
            "Generated private key is empty for type $type"
        }

        // Validate public key format
        val expectedPrefix = when (type) {
            SshKeyType.ED25519 -> "ssh-ed25519 "
            SshKeyType.RSA_2048, SshKeyType.RSA_4096 -> "ssh-rsa "
        }
        require(components.publicKey.startsWith(expectedPrefix)) {
            "Invalid public key format for $type: expected to start with '$expectedPrefix'"
        }

        // Validate private key format
        val expectedPrivateKeyHeader = when (type) {
            SshKeyType.ED25519 -> "-----BEGIN OPENSSH PRIVATE KEY-----"
            SshKeyType.RSA_2048, SshKeyType.RSA_4096 -> "-----BEGIN OPENSSH PRIVATE KEY-----"
        }
        require(components.privateKey.startsWith(expectedPrivateKeyHeader)) {
            "Invalid private key format for $type: expected to start with '$expectedPrivateKeyHeader'"
        }
    }

    companion object {
        private const val GENERATION_TIMEOUT_MS = 30_000L // 30 seconds
    }
}
