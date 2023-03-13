package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.data.api.usecases.UpdateVault
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class TestUpdateVault @Inject constructor() : UpdateVault {

    private var result: Result<Share> = Result.failure(IllegalStateException("value not set"))

    private var value: Payload? = null

    data class Payload(
        val userId: SessionUserId?,
        val shareId: ShareId,
        val vault: NewVault
    )

    fun setResult(value: Result<Share>) {
        result = value
    }

    fun getSentValue(): Payload? = value

    override suspend fun invoke(userId: SessionUserId?, shareId: ShareId, vault: NewVault): Share {
        value = Payload(userId, shareId, vault)
        return result.fold(
            onSuccess = { it },
            onFailure = { throw it }
        )
    }
}
