package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.pass.domain.ItemType
import proton.pass.domain.ShareSelection
import javax.inject.Inject

class ObserveMFACountImpl @Inject constructor(
    private val observeItems: ObserveItems,
    private val encryptionContextProvider: EncryptionContextProvider
) : ObserveMFACount {
    override fun invoke(): Flow<Int> = observeItems(
        selection = ShareSelection.AllShares,
        itemState = null,
        filter = ItemTypeFilter.Logins
    )
        .map {
            encryptionContextProvider.withEncryptionContext {
                it.map { decrypt((it.itemType as ItemType.Login).primaryTotp) }
            }.count { it.isNotBlank() }
        }
}
