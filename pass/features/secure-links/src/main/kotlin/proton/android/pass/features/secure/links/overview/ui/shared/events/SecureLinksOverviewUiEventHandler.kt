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

package proton.android.pass.features.secure.links.overview.ui.shared.events

import android.content.Context
import proton.android.pass.commonui.api.AndroidUtils
import proton.android.pass.features.secure.links.shared.navigation.SecureLinksNavDestination

@Suppress("LongParameterList")
internal fun handleSecureLinksOverviewUiEvent(
    uiEvent: SecureLinksOverviewUiEvent,
    secureLinkUrl: String,
    onNavigated: (SecureLinksNavDestination) -> Unit,
    onLinkCopied: () -> Unit,
    onLinkDeleted: () -> Unit,
    context: Context
) {
    when (uiEvent) {
        is SecureLinksOverviewUiEvent.OnCloseClicked -> {
            SecureLinksNavDestination.CloseScreenWithCategory(
                itemCategory = uiEvent.itemCategory
            ).also(onNavigated)
        }

        SecureLinksOverviewUiEvent.OnCopyLinkClicked -> {
            onLinkCopied()
        }

        SecureLinksOverviewUiEvent.OnShareLinkClicked -> {
            AndroidUtils.shareTextWithThirdParties(
                context = context,
                text = secureLinkUrl
            )
        }

        SecureLinksOverviewUiEvent.OnViewAllLinksClicked -> {
            onNavigated(SecureLinksNavDestination.SecureLinksList)
        }

        is SecureLinksOverviewUiEvent.OnDeleteLinkClicked -> {
            onLinkDeleted()
        }
    }
}
