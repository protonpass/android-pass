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

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.item.PassPasswordStrengthItem
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailTOTPFieldRow
import proton.android.pass.composecomponents.impl.item.details.rows.PassItemDetailsHiddenFieldRow
import proton.android.pass.composecomponents.impl.item.details.sections.shared.PassItemDetailMainSectionContainer
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemDiffType
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.TotpState
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

private const val HIDDEN_PASSWORD_TEXT_LENGTH = 12

@Composable
internal fun PassLoginItemDetailMainSection(
    modifier: Modifier = Modifier,
    email: String,
    username: String,
    password: HiddenState,
    passwordStrength: PasswordStrength,
    primaryTotp: TotpState?,
    itemColors: PassItemColors,
    itemDiffs: ItemDiffs.Login,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    val sections = mutableListOf<@Composable () -> Unit>()

    if (email.isNotBlank()) {
        sections.add {
            PassItemDetailFieldRow(
                icon = CoreR.drawable.ic_proton_envelope,
                title = stringResource(R.string.item_details_login_section_email_title),
                subtitle = email,
                itemColors = itemColors,
                itemDiffType = itemDiffs.email,
                onClick = {
                    onEvent(
                        PassItemDetailsUiEvent.OnSectionClick(
                            section = email,
                            field = ItemDetailsFieldType.Plain.Email
                        )
                    )
                }
            )
        }
    }

    if (username.isNotBlank()) {
        sections.add {
            PassItemDetailFieldRow(
                icon = CoreR.drawable.ic_proton_user,
                title = stringResource(R.string.item_details_login_section_username_title),
                subtitle = username,
                itemColors = itemColors,
                itemDiffType = itemDiffs.username,
                onClick = {
                    onEvent(
                        PassItemDetailsUiEvent.OnSectionClick(
                            section = username,
                            field = ItemDetailsFieldType.Plain.Username
                        )
                    )
                }
            )
        }
    }

    if (password !is HiddenState.Empty) {
        sections.add {
            PassItemDetailsHiddenFieldRow(
                icon = CoreR.drawable.ic_proton_key,
                title = stringResource(R.string.item_details_login_section_password_title),
                hiddenState = password,
                hiddenTextLength = HIDDEN_PASSWORD_TEXT_LENGTH,
                needsRevealedColors = true,
                itemColors = itemColors,
                itemDiffType = itemDiffs.password,
                hiddenTextStyle = ProtonTheme.typography.defaultNorm
                    .copy(fontFamily = FontFamily.Monospace),
                onClick = {
                    onEvent(
                        PassItemDetailsUiEvent.OnHiddenFieldClick(
                            state = password,
                            field = ItemDetailsFieldType.Hidden.Password
                        )
                    )
                },
                onToggle = { isVisible ->
                    onEvent(
                        PassItemDetailsUiEvent.OnHiddenFieldToggle(
                            isVisible = isVisible,
                            hiddenState = password,
                            fieldType = ItemDetailsFieldType.Hidden.Password,
                            fieldSection = ItemSection.Login
                        )
                    )
                },
                contentInBetween = { PassPasswordStrengthItem(passwordStrength = passwordStrength) }
            )
        }
    }

    primaryTotp?.let { totp ->
        sections.add {
            PassItemDetailTOTPFieldRow(
                totp = totp,
                icon = CoreR.drawable.ic_proton_lock,
                title = stringResource(CompR.string.item_details_login_section_primary_totp_title),
                itemColors = itemColors,
                itemDiffType = itemDiffs.totp,
                onEvent = onEvent
            )
        }
    }

    PassItemDetailMainSectionContainer(
        modifier = modifier,
        sections = sections.toPersistentList()
    )
}

@[Preview Composable]
internal fun PassLoginItemDetailMainDiffPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassLoginItemDetailMainSection(
                email = "user@email.com",
                username = "username",
                password = HiddenState.Concealed(""),
                passwordStrength = PasswordStrength.Vulnerable,
                primaryTotp = TotpState.Visible(
                    code = "123456",
                    remainingSeconds = 25,
                    totalSeconds = 30
                ),
                itemColors = passItemColors(ItemCategory.Login),
                itemDiffs = ItemDiffs.Login(
                    email = ItemDiffType.Field,
                    username = ItemDiffType.Field,
                    password = ItemDiffType.Content,
                    totp = ItemDiffType.Field
                ),
                onEvent = {}
            )
        }
    }
}
