/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.report.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.report.ui.ReportReason
import proton.android.pass.passkeys.api.PasskeySupport

@Stable
internal data class ReportState(
    internal val passkeySupportOption: Option<PasskeySupport>,
    internal val reportEvent: ReportEvent,
    internal val reportReasonOption: Option<ReportReason>,
    private val isLoadingState: IsLoadingState,
    private val formValidationErrors: ImmutableList<ReportValidationError>
) {
    val isLoading: Boolean
        get() = isLoadingState is IsLoadingState.Loading
    val emailErrors: List<EmailError>
        get() = formValidationErrors.filterIsInstance<EmailError>()
    val descriptionErrors: List<DescriptionError>
        get() = formValidationErrors.filterIsInstance<DescriptionError>()

    companion object {
        val Initial = ReportState(
            passkeySupportOption = None,
            reportEvent = ReportEvent.Idle,
            reportReasonOption = None,
            isLoadingState = IsLoadingState.NotLoading,
            formValidationErrors = persistentListOf()
        )
    }
}

sealed interface ReportValidationError

interface EmailError : ReportValidationError
interface DescriptionError : ReportValidationError

object EmailBlank : EmailError
object EmailInvalid : EmailError
object DescriptionBlank : DescriptionError
object DescriptionTooShort : DescriptionError
object DescriptionTooLong : DescriptionError
