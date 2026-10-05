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

package proton.android.pass.data.impl.fakes

import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs

class FakeSrpCrypto(private val proofs: SrpProofs) : SrpCrypto {

    private val mutableProofPasswords = mutableListOf<String>()
    val proofPasswords: List<String> = mutableProofPasswords

    override suspend fun generateSrpProofs(
        username: String,
        password: ByteArray,
        version: Long,
        salt: String,
        modulus: String,
        serverEphemeral: String
    ): SrpProofs {
        mutableProofPasswords.add(password.decodeToString())
        return proofs
    }

    override suspend fun calculatePasswordVerifier(
        username: String,
        password: ByteArray,
        modulusId: String,
        modulus: String
    ): Auth {
        throw IllegalStateException("Not implemented")
    }
}
