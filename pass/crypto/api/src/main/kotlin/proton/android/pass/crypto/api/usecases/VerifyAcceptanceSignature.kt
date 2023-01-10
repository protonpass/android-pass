package proton.android.pass.crypto.api.usecases

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.UserAddress

interface VerifyAcceptanceSignature {
    operator fun invoke(
        acceptanceSignature: String,
        inviterAcceptanceSignature: String,
        signingKey: Armored,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>
    )
}
