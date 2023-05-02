package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ClearUserData
import javax.inject.Inject

class ClearUserDataImpl @Inject constructor(
    private val shareRepository: ShareRepository
) : ClearUserData {

    override suspend fun invoke(userId: UserId) = withContext(Dispatchers.IO) {
        shareRepository.deleteSharesForUser(userId)
    }

}
