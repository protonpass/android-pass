package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Vault

interface ObserveVaults {
    operator fun invoke(): Flow<LoadingResult<List<Vault>>>
}
