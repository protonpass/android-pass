package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.pass.domain.Item

interface GetItemByAliasEmail {
    suspend operator fun invoke(userId: UserId? = null, aliasEmail: String): Item?
}
