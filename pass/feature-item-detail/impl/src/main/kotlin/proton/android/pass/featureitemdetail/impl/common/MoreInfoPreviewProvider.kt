package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import proton.android.pass.common.api.toOption

class MoreInfoPreviewProvider : PreviewParameterProvider<MoreInfoPreview> {
    override val values: Sequence<MoreInfoPreview>
        get() = sequenceOf(
            MoreInfoPreview(showMoreInfo = false, uiState = uiState()),
            MoreInfoPreview(showMoreInfo = true, uiState = uiState()),
            MoreInfoPreview(showMoreInfo = true, uiState = uiState(lastAutofilled = ONE_HOUR_AGO)),
        )

    private fun uiState(lastAutofilled: Long? = null): MoreInfoUiState {
        return MoreInfoUiState(
            now = Instant.fromEpochSeconds(NOW),
            lastAutofilled = lastAutofilled.toOption().map(Instant::fromEpochSeconds),
            lastModified = Instant.fromEpochSeconds(ONE_DAY_AGO),
            numRevisions = 3,
            createdTime = Instant.fromEpochSeconds(ONE_WEEK_AGO)
        )
    }

    @Suppress("UnderscoresInNumericLiterals")
    companion object {
        private const val NOW = 1676641715L // Friday, February 17 2023 13:48:35 UTC
        private const val ONE_HOUR_AGO = 1676638115L // Friday, February 17 2023 12:48:35 UTC
        private const val ONE_DAY_AGO = 1676555315L // Thursday, February 16 2023 13:48:35 UTC
        private const val ONE_WEEK_AGO = 1676036915L // Friday, February 10 2023 13:48:35 UTC
    }
}

data class MoreInfoPreview(
    val uiState: MoreInfoUiState,
    val showMoreInfo: Boolean
)
