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

package proton.android.pass.features.itemcreate.login.customfields

/*

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
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        customFields.forEachIndexed { idx, field: UICustomFieldContent ->
            val entryModifier = if (focusedField?.index == idx) {
                Modifier.focusRequester(focusRequester)
            } else {
                Modifier
            }
            val validationError = validationErrors.firstOrNull {
                when (it) {
                    is CustomFieldValidationError.EmptyField -> {
                        it.index == idx
                    }

                    is LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp -> {
                        it.index == idx
                    }
                }
            }
            val (isError, errorMessage) = when (validationError) {
                is LoginItemValidationErrors.CustomFieldValidationError.EmptyField ->
                    true to
                        stringResource(R.string.field_cannot_be_empty)

                is LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp ->
                    true to
                        stringResource(R.string.totp_create_login_field_invalid)

                null -> false to ""
            }

            CustomFieldEntry(
                modifier = entryModifier,
                passItemColors = passItemColors(ItemCategory.Login),
                entry = field,
                isError = isError,
                errorMessage = errorMessage,
                index = idx,
                canEdit = canEdit,
                onValueChange = { value ->
                    onEvent(CustomFieldEvent.OnValueChange(value, idx))
                },
                onFocusChange = { index, isFocused ->
                    val loginCustomField = when (field) {
                        is UICustomFieldContent.Hidden -> CustomFieldHidden(index)
                        is UICustomFieldContent.Text -> CustomFieldText(index)
                        is UICustomFieldContent.Totp -> CustomFieldTOTP(index)
                        is UICustomFieldContent.Date -> throw IllegalStateException("Date field not supported")
                    }
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
            AddCustomFieldButton(
                passItemColors = passItemColors(ItemCategory.Login),
                isEnabled = canEdit,
                onClick = { onEvent(CustomFieldEvent.AddCustomField) }
            )
        }
    }

    RequestFocusLaunchedEffect(
        focusRequester = focusRequester,
        requestFocus = focusedField != null
    )
}
*/
