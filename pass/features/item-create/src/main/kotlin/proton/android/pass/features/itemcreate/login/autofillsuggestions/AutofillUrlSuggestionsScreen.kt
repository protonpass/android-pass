/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.itemcreate.login.autofillsuggestions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text as MaterialText
import androidx.compose.runtime.Composable
import proton.android.pass.composecomponents.impl.text.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.material.Surface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.text.PassTextWithInnerStyle
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.features.itemcreate.R

private enum class AutofillSuggestionsTab { Basic, Advanced }

private val AutofillUrlMode.isAdvanced: Boolean
    get() = this == AutofillUrlMode.StartWith ||
        this == AutofillUrlMode.Pattern ||
        this == AutofillUrlMode.RegularExpression ||
        this == AutofillUrlMode.ExactPath

@Composable
internal fun AutofillUrlSuggestionsScreen(
    modifier: Modifier = Modifier,
    onSave: (urlIndex: Int, mode: AutofillUrlMode) -> Unit,
    onClose: () -> Unit,
    viewModel: AutofillUrlSuggestionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    AutofillUrlSuggestionsContent(
        modifier = modifier,
        url = viewModel.url,
        initialMode = viewModel.initialMode,
        selectedMode = uiState.selectedMode,
        previewEntries = uiState.previewEntries,
        onModeSelected = viewModel::onModeSelected,
        onSave = { onSave(viewModel.urlIndex, uiState.selectedMode) },
        onClose = onClose
    )
}

@Composable
internal fun AutofillUrlSuggestionsContent(
    modifier: Modifier = Modifier,
    url: String,
    initialMode: AutofillUrlMode,
    selectedMode: AutofillUrlMode,
    previewEntries: List<Pair<String, Boolean>>,
    onModeSelected: (AutofillUrlMode) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    val displayUrl = url.removePrefix("https://").removePrefix("http://").trimEnd('/')

    var selectedTab by rememberSaveable {
        mutableStateOf(
            if (initialMode.isAdvanced) AutofillSuggestionsTab.Advanced
            else AutofillSuggestionsTab.Basic
        )
    }

    Scaffold(
        modifier = modifier.systemBarsPadding(),
        backgroundColor = PassTheme.colors.backgroundNorm,
        topBar = {
            ProtonTopAppBar(
                backgroundColor = PassTheme.colors.backgroundNorm,
                title = {},
                navigationIcon = {
                    Circle(
                        modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                        onClick = onClose
                    ) {
                        Icon(
                            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_small),
                            contentDescription = null,
                            tint = PassTheme.colors.loginInteractionNormMajor2
                        )
                    }
                },
                actions = {
                    LoadingCircleButton(
                        modifier = Modifier.padding(horizontal = Spacing.mediumSmall),
                        color = PassTheme.colors.loginInteractionNormMajor1,
                        isLoading = false,
                        text = {
                            Text.Body2Regular(
                                text = stringResource(R.string.action_save),
                                color = PassTheme.colors.textInvert
                            )
                        },
                        onClick = onSave
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.medium)
        ) {
            Text.Hero(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                text = stringResource(R.string.autofill_suggestions_title),
                color = PassTheme.colors.textNorm
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            PassTextWithInnerStyle(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                text = stringResource(R.string.autofill_suggestions_description, displayUrl),
                textStyle = ProtonTheme.typography.defaultSmallNorm.copy(
                    color = PassTheme.colors.textWeak
                ),
                innerText = displayUrl,
                innerStyle = ProtonTheme.typography.defaultSmallNorm.copy(
                    color = PassTheme.colors.textNorm
                )
            )

            Spacer(modifier = Modifier.height(Spacing.medium))

            val hasAdvancedMode = initialMode.isAdvanced || selectedMode.isAdvanced
            if (hasAdvancedMode) {
                AutofillSegmentedControl(
                    modifier = Modifier.padding(horizontal = Spacing.medium),
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> selectedTab = tab }
                )
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                },
                label = "tabContent"
            ) { tab ->
                Column {
                    when (tab) {
                        AutofillSuggestionsTab.Basic -> {
                            BasicModeOptions(
                                selectedMode = selectedMode,
                                onModeSelected = onModeSelected
                            )
                            Spacer(modifier = Modifier.height(Spacing.medium))
                            val effectiveEntries = if (url.isNotBlank() &&
                                selectedMode != AutofillUrlMode.Never
                            ) {
                                previewEntries
                            } else {
                                emptyList()
                            }
                            AnimatedContent(
                                targetState = effectiveEntries,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
                                },
                                label = "urlPreview"
                            ) { entries ->
                                Column {
                                    if (entries.isNotEmpty()) {
                                        UrlPreviewTable(
                                            modifier = Modifier.padding(horizontal = Spacing.medium),
                                            entries = entries
                                        )
                                        Spacer(modifier = Modifier.height(Spacing.medium))
                                    }
                                }
                            }
                        }

                        AutofillSuggestionsTab.Advanced -> {
                            AdvancedModeOptions(
                                selectedMode = selectedMode
                            )
                            Spacer(modifier = Modifier.height(Spacing.medium))
                        }
                    }
                }
            }

            AndroidLimitationNote(
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
        }
    }
}

@Composable
private fun AutofillSegmentedControl(
    selectedTab: AutofillSuggestionsTab,
    onTabSelected: (AutofillSuggestionsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(PassTheme.colors.loginInteractionNormMinor1)
    ) {
        AutofillSuggestionsTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val tabBackground by animateColorAsState(
                targetValue = if (isSelected) {
                    PassTheme.colors.loginInteractionNormMajor1
                } else {
                    Color.Transparent
                },
                label = "tab_bg_${tab.name}"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) {
                    PassTheme.colors.textInvert
                } else {
                    PassTheme.colors.loginInteractionNormMajor1
                },
                label = "tab_text_${tab.name}"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(tabBackground)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = Spacing.small),
                contentAlignment = Alignment.Center
            ) {
                Text.Body1Regular(
                    text = when (tab) {
                        AutofillSuggestionsTab.Basic -> stringResource(R.string.autofill_suggestions_tab_basic)
                        AutofillSuggestionsTab.Advanced -> stringResource(R.string.autofill_suggestions_tab_advanced)
                    },
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun BasicModeOptions(
    modifier: Modifier = Modifier,
    selectedMode: AutofillUrlMode,
    onModeSelected: (AutofillUrlMode) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        AutofillModeRadioRow(
            label = stringResource(R.string.autofill_suggestions_mode_default),
            badge = stringResource(R.string.autofill_suggestions_mode_default_badge),
            selected = selectedMode == AutofillUrlMode.Default,
            onSelect = { onModeSelected(AutofillUrlMode.Default) }
        )
        AutofillModeRadioRow(
            label = stringResource(R.string.autofill_suggestions_mode_exact),
            selected = selectedMode == AutofillUrlMode.Exact,
            onSelect = { onModeSelected(AutofillUrlMode.Exact) }
        )
        AutofillModeRadioRow(
            label = stringResource(R.string.autofill_suggestions_mode_never),
            selected = selectedMode == AutofillUrlMode.Never,
            onSelect = { onModeSelected(AutofillUrlMode.Never) }
        )
    }
}

@Composable
private fun AdvancedModeOptions(modifier: Modifier = Modifier, selectedMode: AutofillUrlMode) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        AutofillModeRadioRow(
            label = stringResource(R.string.autofill_suggestions_mode_start_with),
            selected = selectedMode == AutofillUrlMode.StartWith,
            enabled = false,
            onSelect = {}
        )
        AutofillModeRadioRow(
            label = stringResource(R.string.autofill_suggestions_mode_regular_expression),
            selected = selectedMode == AutofillUrlMode.RegularExpression,
            enabled = false,
            onSelect = {}
        )
        AutofillModeRadioRow(
            label = stringResource(R.string.autofill_suggestions_mode_exact_path),
            selected = selectedMode == AutofillUrlMode.ExactPath,
            enabled = false,
            onSelect = {}
        )
    }
}

@Composable
private fun AutofillModeRadioRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    badge: String? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(end = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        RadioButton(
            selected = selected,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = PassTheme.colors.loginInteractionNormMajor1,
                disabledColor = PassTheme.colors.textDisabled
            ),
            onClick = onSelect
        )
        Text.Body1Regular(
            modifier = Modifier.weight(1f),
            text = label,
            color = if (enabled) PassTheme.colors.textNorm else PassTheme.colors.textDisabled
        )
        if (badge != null) {
            Text.OverlineRegular(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(PassTheme.colors.loginInteractionNormMinor1)
                    .padding(Spacing.small),
                text = badge,
                color = PassTheme.colors.loginInteractionNormMajor1
            )
        }
    }
}

@Composable
private fun UrlPreviewTable(modifier: Modifier = Modifier, entries: List<Pair<String, Boolean>>) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PassTheme.colors.loginInteractionNormMinor2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.medium,
                    top = Spacing.medium,
                    end = Spacing.medium
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text.CaptionMedium(
                text = stringResource(R.string.autofill_suggestions_url_column)
            )
            Text.CaptionMedium(
                text = stringResource(R.string.autofill_suggestions_suggested_column)
            )
        }

        entries.forEach { (displayUrl, matches) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MaterialText(
                    modifier = Modifier.weight(1f),
                    text = displayUrl,
                    style = ProtonTheme.typography.defaultSmallNorm.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = PassTheme.colors.textNorm
                )
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(
                        if (matches) {
                            me.proton.core.presentation.R.drawable.ic_proton_checkmark_circle_filled
                        } else {
                            me.proton.core.presentation.R.drawable.ic_proton_cross_circle_filled
                        }
                    ),
                    contentDescription = null,
                    tint = if (matches) PassPalette.GreenTeal else PassTheme.colors.passwordInteractionNormMajor2
                )
            }
        }
    }
}


@Composable
private fun AndroidLimitationNote(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PassTheme.colors.inputBackgroundNorm)
            .padding(Spacing.medium)
    ) {
        Text.Body1Regular(
            text = stringResource(R.string.autofill_suggestions_android_limitation),
            color = PassTheme.colors.textNorm
        )
    }
}

class ThemedAutofillUrlSuggestionsPP :
    ThemePairPreviewProvider<AutofillUrlSuggestionsPreviewInput>(AutofillUrlSuggestionsPreviewProvider())

@Preview
@Composable
fun AutofillUrlSuggestionsContentPreview(
    @PreviewParameter(ThemedAutofillUrlSuggestionsPP::class) input: Pair<Boolean, AutofillUrlSuggestionsPreviewInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AutofillUrlSuggestionsContent(
                url = input.second.url,
                initialMode = input.second.selectedMode,
                selectedMode = input.second.selectedMode,
                previewEntries = input.second.previewEntries,
                onModeSelected = {},
                onSave = {},
                onClose = {}
            )
        }
    }
}
