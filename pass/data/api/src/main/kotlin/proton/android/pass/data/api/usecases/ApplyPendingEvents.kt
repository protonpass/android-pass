package proton.android.pass.data.api.usecases

import proton.android.pass.common.api.LoadingResult

interface ApplyPendingEvents {
    suspend operator fun invoke(): LoadingResult<Unit>
}
