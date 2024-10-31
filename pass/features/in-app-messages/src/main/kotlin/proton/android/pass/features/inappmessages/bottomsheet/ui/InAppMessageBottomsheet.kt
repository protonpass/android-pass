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

package proton.android.pass.features.inappmessages.bottomsheet.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.None
import proton.android.pass.notifications.api.InAppMessage
import proton.android.pass.notifications.api.InAppMessageId
import proton.android.pass.notifications.api.InAppMessageMode

@Composable
fun InAppMessageBottomsheet(modifier: Modifier = Modifier) {
    InAppMessageContent(
        modifier = modifier,
        inAppMessage = InAppMessage(
            id = InAppMessageId(value = ""),
            mode = InAppMessageMode.Modal,
            title = "",
            message = None,
            imageUrl = None,
            ctaRoute = None,
            ctaText = None

        ),
        onCTAClick = {},
        onClose = {}
    )
}

