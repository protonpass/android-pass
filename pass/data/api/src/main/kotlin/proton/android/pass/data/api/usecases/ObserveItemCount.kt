package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.ItemCountSummary

interface ObserveItemCount {
    operator fun invoke(): Flow<LoadingResult<ItemCountSummary>>
}
