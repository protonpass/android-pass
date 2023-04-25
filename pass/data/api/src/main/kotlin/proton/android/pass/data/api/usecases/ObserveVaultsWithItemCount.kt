package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.VaultWithItemCount

interface ObserveVaultsWithItemCount {
    operator fun invoke(): Flow<List<VaultWithItemCount>>
}
