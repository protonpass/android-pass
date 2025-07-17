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

package proton.android.pass.features.report.navigation

import android.net.Uri
import proton.android.pass.features.report.ui.ReportReason

sealed interface ReportNavContentEvent {

    data object Close : ReportNavContentEvent
    data object OpenAutofillSettings : ReportNavContentEvent
    data object SubmitReport : ReportNavContentEvent
    data object CancelReason : ReportNavContentEvent
    data object OpenTestPage : ReportNavContentEvent

    @JvmInline
    value class OnReasonChange(val value: ReportReason) : ReportNavContentEvent

    @JvmInline
    value class OnEmailChange(val value: String) : ReportNavContentEvent

    @JvmInline
    value class OnDescriptionChange(val value: String) : ReportNavContentEvent

    @JvmInline
    value class OnSendLogsChange(val value: Boolean) : ReportNavContentEvent

    @JvmInline
    value class OnImagesSelected(val value: Set<Uri>) : ReportNavContentEvent

    @JvmInline
    value class OnImageRemoved(val value: Uri) : ReportNavContentEvent
}
