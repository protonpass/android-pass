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

package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ExpandableEmailUsernameInput(
    modifier: Modifier = Modifier,
    email: String,
    username: String,
    onEvent: (LoginContentEvent) -> Unit,
    onFocusChange: (LoginField, Boolean) -> Unit,
    onAliasOptionsClick: () -> Unit,
    canUpdateUsername: Boolean,
    isEditAllowed: Boolean
) {
    val isExpanded = rememberSaveable { mutableStateOf(username.isNotEmpty()) }

    Column(
        modifier = modifier
    ) {
        EmailInput(
            email = email,
            isEditable = canUpdateUsername && isEditAllowed,
            onEmailChange = { newEmail ->
                onEvent(LoginContentEvent.OnEmailChanged(newEmail))
            },
            onFocusChange = { isFocused ->
                onFocusChange(LoginField.Email, isFocused)
            },
            leadingIcon = {
                if (isExpanded.value) {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_envelope),
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconWeak
                    )
                } else {
                    Box(
                        modifier = Modifier.clickable { isExpanded.value = true }
                    ) {
                        Icon(
                            painter = painterResource(id = CoreR.drawable.ic_proton_envelope),
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconWeak
                        )

                        Icon(
                            modifier = Modifier
                                .align(alignment = Alignment.TopEnd)
                                .size(size = 14.dp)
                                .clip(shape = CircleShape)
                                .background(color = PassTheme.colors.loginInteractionNormMinor1)
                                .padding(all = 2.dp),
                            painter = painterResource(id = CoreR.drawable.ic_proton_plus),
                            contentDescription = null,
                            tint = PassTheme.colors.loginInteractionNormMajor2
                        )
                    }
                }
            },
            trailingIcon = {
                if (canUpdateUsername) {
                    if (email.isNotEmpty()) {
                        SmallCrossIconButton(
                            enabled = isEditAllowed,
                            onClick = {
                                onEvent(LoginContentEvent.OnEmailChanged(email = ""))
                            }
                        )
                    }
                } else {
                    IconButton(
                        enabled = isEditAllowed,
                        onClick = { onAliasOptionsClick() }
                    ) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_three_dots_vertical),
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconWeak
                        )
                    }
                }
            }
        )

        AnimatedVisibility(visible = isExpanded.value) {
            Divider(color = PassTheme.colors.inputBorderNorm)

            UsernameInput(
                value = username,
                canUpdateUsername = canUpdateUsername,
                isEditAllowed = isEditAllowed,
                onChange = { newUsername ->
                    onEvent(LoginContentEvent.OnUsernameChanged(newUsername))
                },
                onAliasOptionsClick = {},
                onFocus = { isFocused ->
                    onFocusChange(LoginField.Username, isFocused)
                }
            )
        }
    }
}
