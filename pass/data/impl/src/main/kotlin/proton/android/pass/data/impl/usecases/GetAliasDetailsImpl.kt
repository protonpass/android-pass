package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.usecases.GetAliasDetails
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.pass.domain.AliasDetails
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class GetAliasDetailsImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val aliasRepository: AliasRepository
) : GetAliasDetails {

    override fun invoke(shareId: ShareId, itemId: ItemId): Flow<AliasDetails> =
        observeCurrentUser()
            .flatMapLatest { aliasRepository.getAliasDetails(it.userId, shareId, itemId) }
}

