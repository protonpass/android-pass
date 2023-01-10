package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.error.InvalidAddressSignature
import proton.android.pass.crypto.api.error.InvalidSignature
import proton.android.pass.crypto.api.usecases.VerifyAcceptanceSignature
import proton.android.pass.log.api.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import me.proton.core.key.domain.getArmored
import me.proton.core.key.domain.getBase64Decoded
import me.proton.core.key.domain.useKeys
import me.proton.core.key.domain.verifyData
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

class VerifyAcceptanceSignatureImpl @Inject constructor(
    private val cryptoContext: CryptoContext
) : VerifyAcceptanceSignature {
    override fun invoke(
        acceptanceSignature: String,
        inviterAcceptanceSignature: String,
        signingKey: Armored,
        userAddress: UserAddress,
        inviterKeys: List<PublicKey>
    ) {
        userAddress.useKeys(cryptoContext) {
            // Check Signing Key Signature
            val signingKeyFingerprint = Utils.getPrimaryV5Fingerprint(cryptoContext, signingKey)
            val armoredAcceptanceSignature = getArmored(
                getBase64Decoded(acceptanceSignature),
                PGPHeader.Signature
            )
            val verified =
                verifyData(signingKeyFingerprint.encodeToByteArray(), armoredAcceptanceSignature)
            if (!verified) {
                val e = InvalidSignature("Acceptance signature")
                PassLogger.e(TAG, e, "Acceptance signature failed to verify")
                throw e
            }

            // Check inviter acceptance signature
            val publicKeyRing = PublicKeyRing(inviterKeys)
            val armoredInviterAcceptanceSignature =
                getArmored(getBase64Decoded(inviterAcceptanceSignature), PGPHeader.Signature)
            val inviterVerified = publicKeyRing.verifyData(
                cryptoContext,
                signingKeyFingerprint.encodeToByteArray(),
                armoredInviterAcceptanceSignature
            )
            if (!inviterVerified) {
                val e = InvalidAddressSignature()
                PassLogger.e(TAG, e, "Share inviterAcceptanceSignature failed to verify")
                throw e
            }
        }
    }

    companion object {
        private const val TAG = "VerifyAcceptanceSignatureImpl"
    }
}
