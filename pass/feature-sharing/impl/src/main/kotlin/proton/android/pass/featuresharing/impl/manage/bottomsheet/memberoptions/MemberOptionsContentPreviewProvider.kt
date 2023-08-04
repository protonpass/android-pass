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

package proton.android.pass.featuresharing.impl.manage.bottomsheet.memberoptions

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

class MemberOptionsContentPreviewProvider : PreviewParameterProvider<MemberOptionInput> {
    override val values = sequence {
        for (showTransfer in listOf(true, false)) {
            yield(MemberOptionInput(showTransferOwnership = showTransfer))
        }
        for (option in listOf(LoadingOption.Admin, LoadingOption.RemoveMember)) {
            yield(
                MemberOptionInput(
                    loadingOption = option,
                    isLoading = IsLoadingState.Loading
                )
            )
        }
    }
}

class ThemeMemberOptionsPreviewProvider : ThemePairPreviewProvider<MemberOptionInput>(
    MemberOptionsContentPreviewProvider()
)

data class MemberOptionInput(
    val showTransferOwnership: Boolean = true,
    val loadingOption: LoadingOption? = null,
    val isLoading: IsLoadingState = IsLoadingState.NotLoading,
)
