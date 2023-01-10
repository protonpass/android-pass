package proton.android.pass.data.api.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.user.domain.entity.User

interface ObserveCurrentUser {
    operator fun invoke(): Flow<User?>
}
