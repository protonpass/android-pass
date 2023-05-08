package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.pass.domain.ItemType
import javax.inject.Inject

class ObserveMFACountImpl @Inject constructor(
    private val observeActiveItems: ObserveActiveItems,
    private val encryptionContextProvider: EncryptionContextProvider
) : ObserveMFACount {
    override fun invoke(): Flow<Int> = observeActiveItems(ItemTypeFilter.Logins)
        .map {
            encryptionContextProvider.withEncryptionContext {
                it.map { decrypt((it.itemType as ItemType.Login).primaryTotp) }
            }.count { it.isNotBlank() }
        }
}
