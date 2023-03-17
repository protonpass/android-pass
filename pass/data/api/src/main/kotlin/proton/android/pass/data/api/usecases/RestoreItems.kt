package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId

interface RestoreItems {
    operator fun invoke(userId: UserId? = null): Flow<Unit>
}
