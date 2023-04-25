package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.pass.domain.AliasMailbox
import proton.pass.domain.Item

data class UpdateAliasItemContent(
    val title: String,
    val note: String
)

data class UpdateAliasContent(
    val mailboxes: Option<List<AliasMailbox>>,
    val itemData: Option<UpdateAliasItemContent>
)

interface UpdateAlias {
    suspend operator fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): Item
}
