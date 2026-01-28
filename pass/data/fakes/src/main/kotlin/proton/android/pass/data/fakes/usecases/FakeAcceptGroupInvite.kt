package proton.android.pass.data.fakes.usecases

import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.usecases.invites.AcceptGroupInvite
import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedInviteKey

class FakeAcceptGroupInvite(
    private var result: List<EncryptedGroupInviteAcceptKey> = emptyList()
) : AcceptGroupInvite {

    var lastUser: User? = null
    var lastGroupKeys: List<PrivateAddressKey>? = null
    var lastUnlockedOrganizationKey: UnlockedKey? = null
    var lastInviterKeys: List<PublicKey>? = null
    var lastKeys: List<EncryptedInviteKey>? = null
    var lastIsGroupOwner: Boolean? = null

    override fun invoke(
        user: User,
        groupPrivateKeys: List<PrivateAddressKey>,
        unlockedOrganizationKey: UnlockedKey?,
        inviterAddressKeys: List<PublicKey>,
        keys: List<EncryptedInviteKey>,
        isGroupOwner: Boolean
    ): List<EncryptedGroupInviteAcceptKey> {
        lastUser = user
        lastGroupKeys = groupPrivateKeys
        lastUnlockedOrganizationKey = unlockedOrganizationKey
        lastInviterKeys = inviterAddressKeys
        lastKeys = keys
        lastIsGroupOwner = isGroupOwner
        return result
    }

    fun setResult(result: List<EncryptedGroupInviteAcceptKey>) {
        this.result = result
    }
}
