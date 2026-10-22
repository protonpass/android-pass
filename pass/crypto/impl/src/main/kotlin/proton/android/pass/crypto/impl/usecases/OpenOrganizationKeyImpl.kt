/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.toPrivateKey
import me.proton.core.key.domain.toPublicKey
import proton.android.pass.crypto.api.Constants
import proton.android.pass.crypto.api.usecases.UserKeys
import proton.android.pass.crypto.api.usecases.invites.OpenOrganizationKey
import proton.android.pass.domain.OrganizationKey
import javax.inject.Inject

class OpenOrganizationKeyImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : OpenOrganizationKey {

    override operator fun invoke(
        userKeys: UserKeys,
        organizationKey: OrganizationKey
    ): Result<Pair<PrivateKey, PublicKey>> = runCatching {
        if (!organizationKey.isPasswordless())
            error("Cannot open OrganizationKey - not passwordless")

        val privateKey = organizationKey.privateKey.takeIf { !it.isNullOrBlank() }
            ?: error("Organization private key is null")

        val token = organizationKey.token.takeIf { !it.isNullOrBlank() }
            ?: error("Organization key token is null")
        val signature = organizationKey.signature.takeIf { !it.isNullOrBlank() }
            ?: error("Organization key signature is null")

        val unlockedKey = cryptoContext.pgpCrypto.unlock(
            privateKey = privateKey,
            passphrase = userKeys.decryptAndVerifyDetached(
                token = token,
                signature = signature,
                signatureContext = Constants.SIGNATURE_CONTEXT_ORG_KEY_TOKEN,
                label = "OpenOrganizationKeyImpl: Decrypt organization key token"
            )
        )
        val privateKeyResult = cryptoContext.pgpCrypto.getArmored(
            unlockedKey.value,
            PGPHeader.PrivateKey
        )
            .toPrivateKey()
        val publicKeyResult = cryptoContext.pgpCrypto.getPublicKey(privateKey)
            .toPublicKey()

        Pair(privateKeyResult, publicKeyResult)
    }

}
