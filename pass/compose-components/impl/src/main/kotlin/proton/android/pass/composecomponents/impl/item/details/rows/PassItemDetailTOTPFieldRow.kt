/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.masks.TextMask.TotpCode
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.SectionTitle
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent.OnFieldClick
import proton.android.pass.composecomponents.impl.progress.PassTotpProgress
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.TotpState
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun PassItemDetailTOTPFieldRow(
    modifier: Modifier = Modifier,
    totp: TotpState,
    icon: Int? = null,
    title: String,
    itemColors: PassItemColors,
    itemDiffType: ItemDiffType,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    when (totp) {
        TotpState.Limited -> TotpUpgradeContent(onUpgrade = { onEvent(PassItemDetailsUiEvent.OnUpgrade) })
        TotpState.Empty -> PassItemDetailMaskedFieldRow(
            modifier = modifier,
            icon = icon,
            title = title,
            itemColors = itemColors,
            itemDiffType = itemDiffType
        )

        is TotpState.Visible -> PassItemDetailMaskedFieldRow(
            modifier = modifier,
            icon = icon,
            title = title,
            maskedSubtitle = TotpCode(totp.code),
            itemColors = itemColors,
            itemDiffType = itemDiffType,
            onClick = { onEvent(OnFieldClick(ItemDetailsFieldType.PlainCopyable.TotpCode(totp.code))) },
            contentInBetween = {
                PassTotpProgress(
                    remainingSeconds = totp.remainingSeconds,
                    totalSeconds = totp.totalSeconds
                )
            }
        )

        TotpState.Hidden -> {}
    }
}

@Composable
fun TotpUpgradeContent(
    modifier: Modifier = Modifier,
    label: String = stringResource(id = R.string.mfa_limit_reached),
    onUpgrade: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.medium,
                top = Spacing.mediumSmall,
                end = Spacing.mediumSmall,
                bottom = Spacing.mediumSmall
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_lock),
            contentDescription = null,
            tint = PassTheme.colors.loginInteractionNorm
        )
        Column(modifier = Modifier.padding(start = Spacing.small)) {
            SectionTitle(
                modifier = Modifier.padding(start = Spacing.small),
                text = label
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(Spacing.small))
                    .clickable(onClick = onUpgrade)
                    .padding(Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                Text(
                    text = stringResource(CompR.string.upgrade),
                    style = ProtonTheme.typography.defaultNorm,
                    color = PassTheme.colors.interactionNormMajor2
                )
                Icon(
                    modifier = Modifier.size(Spacing.medium),
                    painter = painterResource(CoreR.drawable.ic_proton_arrow_out_square),
                    contentDescription = null,
                    tint = PassTheme.colors.interactionNormMajor2
                )
            }
        }
    }
}
