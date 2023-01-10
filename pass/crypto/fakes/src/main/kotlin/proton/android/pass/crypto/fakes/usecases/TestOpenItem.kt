package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.OpenItem
import me.proton.core.key.domain.entity.key.PublicKey
import proton.pass.domain.Item
import proton.pass.domain.Share
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey

class TestOpenItem : OpenItem {

    private var item: Item? = null

    fun setItem(value: Item) {
        item = value
    }

    override fun open(
        response: EncryptedItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item = item ?: throw IllegalStateException("item not set")
}
