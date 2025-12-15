package proton.android.pass.data.fakes.usecases

import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey

class FakeAcceptGroupInvite(
    private var result: List<EncryptedGroupInviteAcceptKey> = emptyList()
) : AcceptGroupInvite {

    var lastGroupKeys: List<PrivateAddressKey>? = null
    var lastOpenerKeys: List<PrivateKey>? = null
    var lastInviterKeys: List<PublicKey>? = null
    var lastKeys: List<EncryptedInviteKey>? = null

    override fun invoke(
        groupPrivateKeys: List<PrivateAddressKey>,
        openerKeys: List<PrivateKey>,
        inviterAddressKeys: List<PublicKey>,
        keys: List<EncryptedInviteKey>
    ): List<EncryptedGroupInviteAcceptKey> {
        lastGroupKeys = groupPrivateKeys
        lastOpenerKeys = openerKeys
        lastInviterKeys = inviterAddressKeys
        lastKeys = keys
        return result
    }

    fun setResult(result: List<EncryptedGroupInviteAcceptKey>) {
        this.result = result
    }
}
