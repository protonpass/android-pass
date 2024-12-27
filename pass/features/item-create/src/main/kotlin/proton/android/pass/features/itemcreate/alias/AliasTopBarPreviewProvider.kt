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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

class AliasTopBarPreviewProvider : PreviewParameterProvider<AliasTopBarInput> {
    override val values: Sequence<AliasTopBarInput>
        get() = sequence {
            for (isDraft in listOf(false, true)) {
                for (isButtonEnabled in listOf(IsButtonEnabled.Enabled, IsButtonEnabled.Disabled)) {
                    yield(
                        AliasTopBarInput(
                            isDraft = isDraft,
                            buttonEnabled = isButtonEnabled,
                            isLoadingState = IsLoadingState.NotLoading
                        )
                    )
                }
            }
            yield(
                AliasTopBarInput(
                    isDraft = false,
                    buttonEnabled = IsButtonEnabled.Enabled,
                    isLoadingState = IsLoadingState.Loading
                )
            )
        }
}

data class AliasTopBarInput(
    val isDraft: Boolean,
    val buttonEnabled: IsButtonEnabled,
    val isLoadingState: IsLoadingState
)
