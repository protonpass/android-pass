package proton.android.pass.test.crypto

import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpCrypto
import me.proton.core.crypto.common.srp.SrpProofs

@Suppress("NotImplementedDeclaration")
object TestSrpCrypto : SrpCrypto {
    override fun calculatePasswordVerifier(
        username: String,
        password: ByteArray,
        modulusId: String,
        modulus: String
    ): Auth {
        TODO("Not yet implemented")
    }

    override fun generateSrpProofs(
        username: String,
        password: ByteArray,
        version: Long,
        salt: String,
        modulus: String,
        serverEphemeral: String
    ): SrpProofs {
        TODO("Not yet implemented")
    }
}
