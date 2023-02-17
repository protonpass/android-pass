package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option

@Stable
data class MoreInfoUiState(
    val now: Instant,
    val lastAutofilled: Option<Instant>,
    val lastModified: Instant,
    val numRevisions: Long,
    val createdTime: Instant
) {
    companion object {
        val Initial = MoreInfoUiState(
            now = Instant.fromEpochSeconds(0),
            lastAutofilled = None,
            lastModified = Instant.fromEpochSeconds(0),
            numRevisions = 0,
            createdTime = Instant.fromEpochSeconds(0)
        )
    }
}
