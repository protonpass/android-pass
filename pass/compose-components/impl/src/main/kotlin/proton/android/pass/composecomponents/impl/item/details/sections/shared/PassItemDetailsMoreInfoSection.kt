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

package proton.android.pass.composecomponents.impl.item.details.sections.shared

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.overlineStrongNorm
import me.proton.core.compose.theme.overlineWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.form.ChevronDownIcon
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultId
import me.proton.core.presentation.R as CoreR

private const val CHEVRON_ROTATION_DEGREES_COLLAPSED = 0f
private const val CHEVRON_ROTATION_DEGREES_EXPANDED = -180f
private const val CHEVRON_ROTATION_DEGREES_ANIMATION_LABEL = "chevronRotationDegreesAnimationLabel"

private const val IS_MORE_INFO_EXPANDED_DEFAULT = false

@Composable
fun PassItemDetailsMoreInfoSection(
    modifier: Modifier = Modifier,
    itemId: ItemId,
    shareId: ShareId,
    vaultId: VaultId
) {
    var chevronRotationDegrees by remember { mutableFloatStateOf(CHEVRON_ROTATION_DEGREES_COLLAPSED) }
    var isMoreInfoExpanded by remember { mutableStateOf(IS_MORE_INFO_EXPANDED_DEFAULT) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        PassItemDetailsMoreInfoHeader(
            chevronRotationDegrees = chevronRotationDegrees,
            onClick = {
                isMoreInfoExpanded = !isMoreInfoExpanded

                chevronRotationDegrees = if (isMoreInfoExpanded) {
                    CHEVRON_ROTATION_DEGREES_EXPANDED
                } else {
                    CHEVRON_ROTATION_DEGREES_COLLAPSED
                }
            }
        )

        AnimatedVisibility(visible = isMoreInfoExpanded) {
            PassItemDetailsMoreInfoContent(
                itemId = itemId,
                shareId = shareId,
                vaultId = vaultId
            )
        }
    }
}

@Composable
private fun PassItemDetailsMoreInfoHeader(
    modifier: Modifier = Modifier,
    chevronRotationDegrees: Float,
    onClick: () -> Unit
) {
    val animatedChevronRotationDegrees by animateFloatAsState(
        targetValue = chevronRotationDegrees,
        label = CHEVRON_ROTATION_DEGREES_ANIMATION_LABEL
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(size = Radius.small))
            .clickable { onClick() }
            .padding(vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_info_circle),
            contentDescription = stringResource(
                id = R.string.item_details_shared_section_more_info_header_icon_content_description
            ),
            tint = ProtonTheme.colors.iconWeak
        )

        PassItemDetailsMoreInfoText(
            text = stringResource(id = R.string.item_details_shared_section_more_info_header_title)
        )

        ChevronDownIcon(
            modifier = Modifier
                .size(size = Spacing.medium)
                .rotate(degrees = animatedChevronRotationDegrees),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )
    }
}

@Composable
private fun PassItemDetailsMoreInfoContent(
    modifier: Modifier = Modifier,
    itemId: ItemId,
    shareId: ShareId,
    vaultId: VaultId
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        PassItemDetailsMoreInfoRow(
            titleResId = R.string.item_details_shared_section_more_info_item_id,
            value = itemId.id
        )

        PassItemDetailsMoreInfoRow(
            titleResId = R.string.item_details_shared_section_more_info_share_id,
            value = shareId.id
        )

        PassItemDetailsMoreInfoRow(
            titleResId = R.string.item_details_shared_section_more_info_vault_id,
            value = vaultId.id
        )
    }
}

@Composable
private fun PassItemDetailsMoreInfoRow(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    value: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
    ) {
        PassItemDetailsMoreInfoText(
            text = stringResource(id = titleResId)
        )

        PassItemDetailsMoreInfoText(
            text = value,
            style = ProtonTheme.typography.overlineWeak
        )
    }
}

@Composable
private fun PassItemDetailsMoreInfoText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = ProtonTheme.typography.overlineStrongNorm
) {
    SelectionContainer {
        Text(
            modifier = modifier,
            text = text,
            style = style,
            color = ProtonTheme.colors.textWeak
        )
    }
}

@[Preview Composable]
internal fun PassItemDetailsMoreInfoPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassItemDetailsMoreInfoSection(
                itemId = ItemId(id = "UD09090ikodfjd0ulhj267sjk3lsdfjkls345djsdfdsf=="),
                shareId = ShareId(id = "OPPOSDkljksd__kjksdfjkj23r4343434Ju343434ookj=="),
                vaultId = VaultId(id = "dsdsd090iksdsds7sjk3lsdfjkls345djsdfdsf")
            )
        }
    }
}
