package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.pass.domain.Item
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGetItemByAliasEmail @Inject constructor() : GetItemByAliasEmail {

    private var result: Item? = null

    fun setResult(value: Item?) {
        result = value
    }

    override suspend fun invoke(userId: UserId?, aliasEmail: String): Item? = result

}
