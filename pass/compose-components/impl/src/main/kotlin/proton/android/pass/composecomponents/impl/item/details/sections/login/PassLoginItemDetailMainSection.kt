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

package proton.android.pass.composecomponents.impl.item.details.sections.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.utils.ProtonItemColors
import proton.android.pass.domain.HiddenState
import me.proton.core.presentation.R as CoreR

private const val HIDDEN_PASSWORD_TEXT_LENGTH = 12

@Composable
internal fun PassLoginItemDetailMainSection(
    modifier: Modifier = Modifier,
    username: String,
    password: HiddenState,
    itemColors: ProtonItemColors,
    onSectionClick: (String) -> Unit,
    onHiddenSectionClick: (HiddenState) -> Unit,
    onHiddenSectionToggle: (Boolean, HiddenState, ItemDetailsFieldType.Hidden) -> Unit,
) {
    val sections = mutableListOf<@Composable (() -> Unit)?>()

    sections.add {
        PassItemDetailFieldRow(
            icon = painterResource(CoreR.drawable.ic_proton_user),
            title = stringResource(R.string.item_details_login_section_username_title),
            subtitle = username,
            itemColors = itemColors,
            onClick = { onSectionClick(username) },
        ).takeIf { username.isNotBlank() }
    }

    sections.add {
        PassItemDetailsHiddenFieldRow(
            icon = painterResource(CoreR.drawable.ic_proton_key),
            title = stringResource(R.string.item_details_login_section_password_title),
            hiddenState = password,
            hiddenTextLength = HIDDEN_PASSWORD_TEXT_LENGTH,
            needsRevealedColors = true,
            itemColors = itemColors,
            hiddenTextStyle = ProtonTheme.typography.defaultNorm
                .copy(fontFamily = FontFamily.Monospace),
            onClick = { onHiddenSectionClick(password) },
            onToggle = { isVisible ->
                onHiddenSectionToggle(isVisible, password, ItemDetailsFieldType.Hidden.Password)
            },
        ).takeIf { password !is HiddenState.Empty }
    }

    RoundedCornersColumn(modifier = modifier) {
        sections
            .filterNotNull()
            .forEachIndexed { index, block ->
                block()

                if (index < sections.lastIndex) {
                    PassDivider()
                }
            }
    }
}
