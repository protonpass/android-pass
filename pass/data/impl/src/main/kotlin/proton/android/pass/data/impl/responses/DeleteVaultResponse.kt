package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteVaultResponse(
    @SerialName("Code")
    val code: Int
)
