/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.crypto.fakes.context

import me.proton.core.crypto.common.aead.AeadCryptoFactory
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.srp.SrpCrypto

class FakeCryptoContext : CryptoContext {
    override val keyStoreCrypto: KeyStoreCrypto get() = error(UNAVAILABLE)
    override val aeadCryptoFactory: AeadCryptoFactory get() = error(UNAVAILABLE)
    override val pgpCrypto: PGPCrypto get() = error(UNAVAILABLE)
    override val srpCrypto: SrpCrypto get() = error(UNAVAILABLE)

    private companion object {
        const val UNAVAILABLE = "FakeCryptoContext does not provide crypto primitives"
    }
}
