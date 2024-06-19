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

package proton.android.pass.features.secure.links.overview.ui.shared.widgets

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.utils.passRemainingTimeText
import proton.android.pass.domain.time.RemainingTime
import proton.android.pass.features.secure.links.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun SecureLinksOverviewWidget(
    modifier: Modifier = Modifier,
    @StringRes viewsTitleResId: Int,
    viewsText: String,
    remainingTime: RemainingTime,
    linkUrl: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            SecureLinksOverviewWidgetInfo(
                modifier = Modifier.weight(weight = 1f),
                iconResId = CoreR.drawable.ic_proton_clock,
                titleResId = R.string.secure_links_overview_widget_expiration_title,
                infoText = passRemainingTimeText(remainingTime = remainingTime).orEmpty()
            )

            SecureLinksOverviewWidgetInfo(
                modifier = Modifier.weight(weight = 1f),
                iconResId = CoreR.drawable.ic_proton_eye,
                titleResId = viewsTitleResId,
                infoText = viewsText
            )
        }

        SecureLinksOverviewWidgetLink(secureLink = linkUrl)
    }
}
