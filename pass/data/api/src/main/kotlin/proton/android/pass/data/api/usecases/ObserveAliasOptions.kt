package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.AliasOptions
import proton.pass.domain.ShareId

interface ObserveAliasOptions {
    operator fun invoke(shareId: ShareId): Flow<AliasOptions>
}
