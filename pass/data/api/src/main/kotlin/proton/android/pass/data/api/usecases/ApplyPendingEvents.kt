package proton.android.pass.data.api.usecases

import proton.android.pass.common.api.Result

interface ApplyPendingEvents {
    suspend operator fun invoke(): Result<Unit>
}
