package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.VerifyAcceptanceSignature
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.UserAddress

class TestVerifyAcceptanceSignature(private val success: Boolean = true) :
    VerifyAcceptanceSignature {
    override fun invoke(
        acceptanceSignature: String,
        inviterAcceptanceSignature: String,
        signingKey: Armored,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>
    ) {
        if (!success) throw InvalidSignature("TEST EXCEPTION")
    }
}
