/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
