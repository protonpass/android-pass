package proton.android.pass.test.crypto

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.srp.SrpCrypto

object TestCryptoContext : CryptoContext {
    override val keyStoreCrypto: KeyStoreCrypto
        get() = TestKeyStoreCrypto

    override val pgpCrypto: PGPCrypto
        get() = TestPGPCrypto

    override val srpCrypto: SrpCrypto
        get() = TestSrpCrypto
}

