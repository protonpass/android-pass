/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.common.UICustomFieldContent
import proton.android.pass.featureitemcreate.impl.login.LoginCustomField
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors
import me.proton.core.presentation.R as CoreR

@Composable
internal fun CustomFieldsContent(
    modifier: Modifier = Modifier,
    customFields: ImmutableList<UICustomFieldContent>,
    canUseCustomFields: Boolean,
    validationErrors: ImmutableList<LoginItemValidationErrors.CustomFieldValidationError>,
    focusedField: LoginCustomField?,
    canEdit: Boolean,
    onEvent: (CustomFieldEvent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        customFields.forEachIndexed { idx, field: UICustomFieldContent ->
            val entryModifier = if (focusedField?.index == idx) {
                Modifier.focusRequester(focusRequester)
            } else {
                Modifier
            }
            val validationError = validationErrors.firstOrNull {
                when (it) {
                    is LoginItemValidationErrors.CustomFieldValidationError.EmptyField -> {
                        it.index == idx
                    }

                    is LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp -> {
                        it.index == idx
                    }
                }
            }

            CustomFieldEntry(
                modifier = entryModifier,
                entry = field,
                validationError = validationError,
                index = idx,
                canEdit = canEdit,
                onValueChange = { value ->
                    onEvent(CustomFieldEvent.OnValueChange(value, idx))
                },
                onFocusChange = { loginCustomField, isFocused ->
                    onEvent(CustomFieldEvent.FocusRequested(loginCustomField, isFocused))
                },
                onOptionsClick = {
                    onEvent(
                        CustomFieldEvent.OnCustomFieldOptions(
                            index = idx,
                            currentLabel = field.label
                        )
                    )
                }
            )
        }

        if (canUseCustomFields) {
            TransparentTextButton(
                text = stringResource(R.string.create_login_add_custom_field_button),
                icon = CoreR.drawable.ic_proton_plus,
                iconContentDescription = null,
                color = PassTheme.colors.loginInteractionNormMajor2,
                onClick = { onEvent(CustomFieldEvent.AddCustomField) }
            )
        }
    }

    RequestFocusLaunchedEffect(
        focusRequester = focusRequester,
        requestFocus = focusedField != null
    )
}
