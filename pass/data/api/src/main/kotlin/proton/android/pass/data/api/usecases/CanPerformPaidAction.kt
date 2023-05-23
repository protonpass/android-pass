package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow

interface CanPerformPaidAction {
    operator fun invoke(): Flow<Boolean>
}
