package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import me.proton.core.user.domain.entity.User
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import javax.inject.Inject

class TestObserveCurrentUser @Inject constructor() : ObserveCurrentUser {

    private val observeVaultsFlow: MutableSharedFlow<User> = testFlow()

    fun sendUser(user: User) = observeVaultsFlow.tryEmit(user)
    override fun invoke(): Flow<User> = observeVaultsFlow
}
