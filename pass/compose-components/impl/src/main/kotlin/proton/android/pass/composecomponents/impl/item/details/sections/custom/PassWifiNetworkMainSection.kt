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

package proton.android.pass.composecomponents.impl.item.details.sections.custom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent.OnWifiNetworkQRClick
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.stringhelpers.getWifiSecurityTypeText
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

private const val HIDDEN_PRIVATE_KEY_TEXT_LENGTH = 12

@Composable
fun PassWifiNetworkMainSection(
    modifier: Modifier = Modifier,
    contents: ItemContents.WifiNetwork,
    svgQR: Option<String>,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.WifiNetwork,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    RoundedCornersColumn(modifier = modifier) {
        PassItemDetailFieldRow(
            icon = CompR.drawable.ic_wifi,
            title = stringResource(R.string.item_details_wifi_network_label_ssid_name),
            subtitle = contents.ssid,
            itemColors = itemColors,
            itemDiffType = itemDiffs.ssid,
            onClick = {
                onEvent(
                    PassItemDetailsUiEvent.OnFieldClick(
                        field = ItemDetailsFieldType.PlainCopyable.SSID(contents.ssid)
                    )
                )
            }
        )

        PassDivider()

        PassItemDetailsHiddenFieldRow(
            icon = CoreR.drawable.ic_proton_key,
            title = stringResource(R.string.item_details_wifi_network_label_password),
            hiddenState = contents.password,
            hiddenTextLength = HIDDEN_PRIVATE_KEY_TEXT_LENGTH,
            needsRevealedColors = true,
            itemColors = itemColors,
            itemDiffType = itemDiffs.password,
            hiddenTextStyle = ProtonTheme.typography.defaultNorm
                .copy(fontFamily = FontFamily.Monospace),
            onClick = {
                onEvent(
                    PassItemDetailsUiEvent.OnFieldClick(
                        field = ItemDetailsFieldType.HiddenCopyable.Password(contents.password)
                    )
                )
            },
            onToggle = { isVisible ->
                onEvent(
                    PassItemDetailsUiEvent.OnHiddenFieldToggle(
                        isVisible = isVisible,
                        hiddenState = contents.password,
                        fieldType = ItemDetailsFieldType.HiddenCopyable.Password(contents.password),
                        fieldSection = ItemSection.WifiNetwork
                    )
                )
            }
        )

        PassDivider()

        PassItemDetailFieldRow(
            icon = CoreR.drawable.ic_proton_lock,
            title = stringResource(R.string.item_details_wifi_network_label_security),
            subtitle = getWifiSecurityTypeText(contents.wifiSecurityType),
            itemColors = itemColors,
            itemDiffType = itemDiffs.wifiSecurity
        )

        if (svgQR is Some) {
            PassDivider()

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onEvent(OnWifiNetworkQRClick(svgQR.value)) })
                    .padding(all = Spacing.medium)
                    .padding(vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Icon.Default(
                    modifier = Modifier.size(24.dp),
                    id = R.drawable.ic_qr_code,
                    tint = itemColors.norm
                )

                Text.Body1Regular(
                    text = stringResource(R.string.show_network_qr_code),
                    color = itemColors.majorSecondary
                )
            }
        }
    }
}

@Preview
@Composable
internal fun PassWifiNetworkMainSectionPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassWifiNetworkMainSection(
                contents = ItemContents.WifiNetwork(
                    ssid = "SSID",
                    password = HiddenState.Empty(""),
                    wifiSecurityType = WifiSecurityType.WPA,
                    note = "",
                    title = "",
                    customFields = emptyList(),
                    sectionContentList = emptyList()
                ),
                svgQR = Some(""),
                itemColors = passItemColors(ItemCategory.WifiNetwork),
                itemDiffs = ItemDiffs.WifiNetwork(),
                onEvent = {}
            )
        }
    }
}
