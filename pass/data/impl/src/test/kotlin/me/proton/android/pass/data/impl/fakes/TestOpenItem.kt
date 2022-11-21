package me.proton.android.pass.data.impl.fakes

import me.proton.android.pass.data.impl.crypto.OpenItem
import me.proton.android.pass.data.impl.responses.ItemRevision
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.pass.domain.Item
import me.proton.pass.domain.Share
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey

class TestOpenItem : OpenItem {

    private var item: Item? = null

    fun setItem(value: Item) {
        item = value
    }

    override fun open(
        response: ItemRevision,
        share: Share,
        verifyKeys: List<PublicKey>,
        vaultKeys: List<VaultKey>,
        itemKeys: List<ItemKey>
    ): Item = item ?: throw IllegalStateException("item not set")
}
