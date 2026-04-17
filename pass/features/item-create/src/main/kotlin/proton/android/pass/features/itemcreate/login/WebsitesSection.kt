/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.itemcreate.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonuimodels.api.UIAutofillUrl
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.login.WebsiteSectionEvent.AddWebsite
import proton.android.pass.features.itemcreate.login.WebsiteSectionEvent.OpenAutofillSuggestions
import proton.android.pass.features.itemcreate.login.WebsiteSectionEvent.RemoveWebsite
import proton.android.pass.features.itemcreate.login.WebsiteSectionEvent.WebsiteValueChanged

@Composable
internal fun AutofillUrlMode.label(): String = when (this) {
    AutofillUrlMode.Default -> stringResource(R.string.autofill_suggestions_mode_default)
    AutofillUrlMode.Exact -> stringResource(R.string.autofill_suggestions_mode_exact)
    AutofillUrlMode.Never -> stringResource(R.string.autofill_suggestions_mode_never)
    AutofillUrlMode.StartWith -> stringResource(R.string.autofill_suggestions_mode_start_with)
    AutofillUrlMode.RegularExpression -> stringResource(R.string.autofill_suggestions_mode_regular_expression)
    AutofillUrlMode.ExactPath -> stringResource(R.string.autofill_suggestions_mode_exact_path)
    AutofillUrlMode.Pattern -> stringResource(R.string.autofill_suggestions_mode_regular_expression)
}

@Suppress("ComplexMethod")
@Composable
internal fun WebsitesSection(
    modifier: Modifier = Modifier,
    websites: ImmutableList<String>,
    autofillUrls: ImmutableList<UIAutofillUrl>,
    websitesWithErrors: ImmutableList<Int>,
    focusLastWebsite: Boolean,
    isEditAllowed: Boolean,
    isAutofillUrlRegexEnabled: Boolean,
    onWebsiteSectionEvent: (WebsiteSectionEvent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    Row(
        modifier = modifier
            .roundedContainerNorm()
            .padding(Spacing.none, Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.padding(
                Spacing.mediumSmall,
                Spacing.none,
                Spacing.none,
                Spacing.none
            ),
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_earth),
            contentDescription = "",
            tint = if (websitesWithErrors.isNotEmpty()) {
                PassTheme.colors.signalDanger
            } else {
                ProtonTheme.colors.iconWeak
            }
        )
        Column {
            websites.forEachIndexed { idx, value ->
                val textFieldModifier = if (idx < websites.count() - 1) {
                    Modifier
                } else {
                    Modifier.focusRequester(focusRequester)
                }

                val autofillUrl = autofillUrls.getOrNull(idx)
                val mode = autofillUrl?.mode ?: AutofillUrlMode.Default
                val showModeLabel = value.isNotBlank() && isAutofillUrlRegexEnabled
                val showOptionsMenu = value.isNotBlank() && isEditAllowed && isAutofillUrlRegexEnabled

                ProtonTextField(
                    modifier = Modifier.heightIn(min = 48.dp),
                    textFieldModifier = textFieldModifier.fillMaxWidth(),
                    isError = websitesWithErrors.contains(idx),
                    errorMessage = stringResource(id = R.string.field_website_address_invalid),
                    value = value,
                    editable = isEditAllowed,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri
                    ),
                    textStyle = ProtonTheme.typography.defaultNorm(isEditAllowed),
                    moveToNextOnEnter = false,
                    onChange = {
                        if (it.isBlank() && websites.size > 1) {
                            onWebsiteSectionEvent(RemoveWebsite(idx))
                        } else {
                            onWebsiteSectionEvent(WebsiteValueChanged(it, idx))
                        }
                    },
                    label = if (idx == 0) {
                        {
                            ProtonTextFieldLabel(
                                text = stringResource(id = R.string.field_website_address_title),
                                isError = websitesWithErrors.isNotEmpty()
                            )
                        }
                    } else {
                        null
                    },
                    placeholder = {
                        ProtonTextFieldPlaceHolder(text = stringResource(id = R.string.field_website_address_hint))
                    },
                    trailingIcon = if (isEditAllowed) {
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (showOptionsMenu) {
                                    IconButton(
                                        modifier = Modifier.size(24.dp),
                                        onClick = {
                                            onWebsiteSectionEvent(
                                                OpenAutofillSuggestions(
                                                    idx
                                                )
                                            )
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical
                                            ),
                                            contentDescription = null,
                                            tint = PassTheme.colors.textHint
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Spacing.extraSmall))
                                }
                                if (value.isNotEmpty()) {
                                    SmallCrossIconButton {
                                        if (value.isNotBlank() && websites.size > 1) {
                                            onWebsiteSectionEvent(RemoveWebsite(idx))
                                        } else {
                                            onWebsiteSectionEvent(WebsiteValueChanged("", idx))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        null
                    }
                )

                if (showModeLabel) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = Spacing.medium)
                            .background(
                                PassTheme.colors.inputBackgroundStrong,
                                PassTheme.shapes.squircleSmallShape
                            )
                            .padding(Spacing.small),
                        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                    ) {
                        Text.CaptionRegular(
                            text = mode.label(),
                            color = PassTheme.colors.loginInteractionNormMajor2
                        )
                        if (!mode.isSupported) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                            ) {
                                Icon.Default(
                                    modifier = Modifier.size(12.dp),
                                    id = me.proton.core.presentation.R.drawable.ic_proton_exclamation_circle,
                                    tint = PassTheme.colors.signalWarning
                                )
                                Text.CaptionRegular(
                                    text = stringResource(R.string.field_website_skipped_autofill),
                                    color = PassTheme.colors.signalWarning
                                )
                            }
                        }
                    }

                }
            }

            // If we receive focusLastWebsite, call requestFocus
            LaunchedEffect(focusLastWebsite) {
                if (focusLastWebsite) {
                    focusRequester.requestFocus()
                }
            }

            val shouldShowAddWebsiteButton = (
                websites.count() == 1 && websites.last()
                    .isNotEmpty() || websites.count() > 1
                ) && isEditAllowed
            AnimatedVisibility(shouldShowAddWebsiteButton) {
                val ableToAddNewWebsite = websites.lastOrNull()?.isNotEmpty() ?: false
                Button(
                    enabled = ableToAddNewWebsite,
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    ),
                    contentPadding = PaddingValues(0.dp),
                    onClick = { onWebsiteSectionEvent(AddWebsite) },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        disabledBackgroundColor = Color.Transparent,
                        contentColor = ProtonTheme.colors.brandNorm,
                        disabledContentColor = ProtonTheme.colors.interactionDisabled
                    )
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stringResource(R.string.field_website_add_another),
                        style = ProtonTheme.typography.defaultNorm,
                        color = ProtonTheme.colors.brandNorm
                    )
                }
            }
        }
    }
}

class ThemedWebsitesSectionPP :
    ThemePairPreviewProvider<WebsitesPreviewParameter>(WebsitesSectionPreviewProvider())

@Preview
@Composable
fun WebsitesSectionPreview(
    @PreviewParameter(ThemedWebsitesSectionPP::class) input: Pair<Boolean, WebsitesPreviewParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            WebsitesSection(
                websites = input.second.websites,
                autofillUrls = persistentListOf(),
                focusLastWebsite = false,
                isEditAllowed = input.second.isEditAllowed,
                isAutofillUrlRegexEnabled = true,
                websitesWithErrors = persistentListOf(),
                onWebsiteSectionEvent = {}
            )
        }
    }
}

class ThemedWebsitesSectionErrorsPP :
    ThemePairPreviewProvider<WebsitesPreviewParameter>(WebsitesSectionPreviewProvider(withErrors = true))

@Preview
@Composable
fun WebsitesSectionWithErrorsPreview(
    @PreviewParameter(ThemedWebsitesSectionErrorsPP::class) input: Pair<Boolean, WebsitesPreviewParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            WebsitesSection(
                websites = input.second.websites,
                autofillUrls = persistentListOf(),
                focusLastWebsite = false,
                isEditAllowed = input.second.isEditAllowed,
                isAutofillUrlRegexEnabled = true,
                websitesWithErrors = input.second.websitesWithErrors,
                onWebsiteSectionEvent = {}
            )
        }
    }
}
