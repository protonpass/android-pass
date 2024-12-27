/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.profile

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import proton.android.pass.commonui.api.BrowserUtils.openWebsite

@Composable
fun FeedbackBottomsheet(onNavigateEvent: (ProfileNavigation) -> Unit) {
    val context = LocalContext.current
    FeedbackBottomsheetContent(
        onSendReport = {
            onNavigateEvent(ProfileNavigation.Report)
        },
        onOpenReddit = {
            onNavigateEvent(ProfileNavigation.CloseBottomSheet)
            openWebsite(context, PASS_REDDIT)
        },
        onOpenUserVoice = {
            onNavigateEvent(ProfileNavigation.CloseBottomSheet)
            openWebsite(context, PASS_USERVOICE)
        }
    )
}

@VisibleForTesting
const val PASS_REDDIT = "https://www.reddit.com/r/ProtonPass/"

@VisibleForTesting
const val PASS_USERVOICE = "https://protonmail.uservoice.com/forums/953584-proton-pass"
