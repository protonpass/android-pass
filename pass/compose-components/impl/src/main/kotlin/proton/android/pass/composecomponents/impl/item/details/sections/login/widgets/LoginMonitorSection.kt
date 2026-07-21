/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.composecomponents.impl.item.details.sections.login.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.items.ItemDetailNavScope
import proton.android.pass.commonuimodels.api.items.LoginMonitorState
import proton.android.pass.commonuimodels.api.items.MonitorCheck
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.folders.ExpandCollapseIcon
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.text.Text

@Composable
internal fun LoginMonitorSection(
    modifier: Modifier = Modifier,
    monitorState: LoginMonitorState,
    canLoadExternalImages: Boolean,
    forceExpanded: Boolean = false,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) = with(monitorState) {
    val restore = isRestoreMode
    val visibleChecks = visibleChecksFor(restore, this)

    var expanded by rememberSaveable { mutableStateOf(forceExpanded) }
    val showChevron = visibleChecks.size >= MULTI_THRESHOLD
    val showRows = visibleChecks.size < MULTI_THRESHOLD || expanded

    AnimatedVisibility(
        modifier = modifier,
        visible = visibleChecks.isNotEmpty()
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(size = Radius.medium))
                .border(
                    width = 1.dp,
                    color = PassTheme.colors.inputBorderNorm,
                    shape = RoundedCornerShape(size = Radius.medium)
                )
                .padding(all = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
        ) {
            MonitorHeader(
                showChevron = showChevron,
                expanded = expanded,
                onToggle = { expanded = !expanded }
            )

            AnimatedVisibility(visible = showChevron && !expanded) {
                InlineChecksRow(
                    modifier = Modifier.padding(
                        start = 36.dp, // size ripple
                        end = 20.dp // size icon
                    ),
                    checks = visibleChecks
                )
            }

            AnimatedVisibility(visible = showRows) {
                RenderChecks(
                    modifier = Modifier.padding(start = Spacing.medium),
                    checks = visibleChecks,
                    monitorState = this@with,
                    restore = restore,
                    canLoadExternalImages = canLoadExternalImages,
                    onEvent = onEvent
                )
            }
        }
    }
}

@Composable
private fun MonitorHeader(
    showChevron: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = showChevron) {
            ExpandCollapseIcon(
                modifier = Modifier,
                expanded = expanded,
                onClick = onToggle
            )
        }
        AnimatedVisibility(visible = !showChevron) {
            Spacer(modifier = Modifier.padding(start = Spacing.medium))
        }
        Text.Body1Medium(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(id = R.string.login_item_monitor_header),
            color = PassTheme.colors.textNorm
        )
        Icon.Default(
            modifier = Modifier.size(size = 20.dp),
            id = R.drawable.ic_shield_warning,
            tint = PassTheme.colors.textWeak
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineChecksRow(checks: List<MonitorCheck>, modifier: Modifier = Modifier) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
    ) {
        checks.forEachIndexed { index, check ->
            Text.Body2Medium(
                text = stringResource(id = check.shortLabelRes()),
                color = check.titleColor()
            )
            if (index < checks.lastIndex) {
                Text.Body2Medium(
                    text = "·",
                    color = PassTheme.colors.textWeak
                )
            }
        }
    }
}

@Composable
private fun RenderChecks(
    checks: List<MonitorCheck>,
    monitorState: LoginMonitorState,
    restore: Boolean,
    canLoadExternalImages: Boolean,
    onEvent: (PassItemDetailsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        MonitorCheck.entries.forEach { check ->
            val isVisible = check in checks
            val isFirstVisible = check == checks.firstOrNull()
            AnimatedVisibility(visible = isVisible) {
                Column {
                    if (!isFirstVisible) {
                        Divider(
                            thickness = 1.dp,
                            color = PassTheme.colors.inputBorderNorm
                        )
                    }
                    val isPending = check in monitorState.pendingChecks
                    RenderCheckRow(
                        check = check,
                        monitorState = monitorState,
                        restore = restore,
                        isPending = isPending,
                        canLoadExternalImages = canLoadExternalImages,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderCheckRow(
    check: MonitorCheck,
    monitorState: LoginMonitorState,
    restore: Boolean,
    isPending: Boolean,
    canLoadExternalImages: Boolean,
    onEvent: (PassItemDetailsUiEvent) -> Unit
) {
    when (check) {
        MonitorCheck.CompromisedPassword -> LoginMonitorCompromisedPassWidget(
            modifier = Modifier.padding(vertical = Spacing.small),
            isRestoreMode = restore,
            isPending = isPending,
            canEdit = monitorState.canEdit,
            onToggleExclude = {
                onEvent(
                    PassItemDetailsUiEvent.OnToggleMonitorCheck(
                        check = MonitorCheck.CompromisedPassword,
                        skip = !restore
                    )
                )
            }
        )

        MonitorCheck.WeakPassword -> LoginMonitorInsecurePassWidget(
            modifier = Modifier.padding(vertical = Spacing.small),
            isRestoreMode = restore,
            isPending = isPending,
            canEdit = monitorState.canEdit,
            onToggleExclude = {
                onEvent(
                    PassItemDetailsUiEvent.OnToggleMonitorCheck(
                        check = MonitorCheck.WeakPassword,
                        skip = !restore
                    )
                )
            }
        )

        MonitorCheck.ReusedPassword -> LoginMonitorReusedPassWidget(
            modifier = Modifier.padding(vertical = Spacing.small),
            reusedPasswordDisplayMode = monitorState.reusedPasswordDisplayMode,
            reusedPasswordCount = monitorState.reusedPasswordCount,
            reusedPasswordItems = monitorState.reusedPasswordItems,
            canLoadExternalImages = canLoadExternalImages,
            onEvent = onEvent,
            isRestoreMode = restore,
            isPending = isPending,
            canEdit = monitorState.canEdit,
            onToggleExclude = {
                onEvent(
                    PassItemDetailsUiEvent.OnToggleMonitorCheck(
                        check = MonitorCheck.ReusedPassword,
                        skip = !restore
                    )
                )
            }
        )

        MonitorCheck.Missing2fa -> LoginMonitorMissingTwoFaWidget(
            modifier = Modifier.padding(vertical = Spacing.small),
            isRestoreMode = restore,
            isPending = isPending,
            canEdit = monitorState.canEdit,
            onToggleExclude = {
                onEvent(
                    PassItemDetailsUiEvent.OnToggleMonitorCheck(
                        check = MonitorCheck.Missing2fa,
                        skip = !restore
                    )
                )
            }
        )
    }
}

private fun visibleChecksFor(restore: Boolean, state: LoginMonitorState): List<MonitorCheck> = buildList {
    if (if (restore) state.isWeakPasswordCheckSkipped else state.isPasswordInsecure) {
        add(MonitorCheck.WeakPassword)
    }
    if (if (restore) state.isCompromisedPasswordCheckSkipped else state.isPasswordCompromised) {
        add(MonitorCheck.CompromisedPassword)
    }
    if (if (restore) state.isReusedPasswordCheckSkipped else state.isPasswordReused) {
        add(MonitorCheck.ReusedPassword)
    }
    if (if (restore) state.isMissing2faCheckSkipped else state.isMissingTwoFa) {
        add(MonitorCheck.Missing2fa)
    }
}

private fun MonitorCheck.shortLabelRes(): Int = when (this) {
    MonitorCheck.WeakPassword -> R.string.login_item_monitor_label_weak
    MonitorCheck.CompromisedPassword -> R.string.login_item_monitor_label_compromised
    MonitorCheck.ReusedPassword -> R.string.login_item_monitor_label_reused
    MonitorCheck.Missing2fa -> R.string.login_item_monitor_label_missing_2fa
}

@Composable
private fun MonitorCheck.titleColor(): Color = when (this) {
    MonitorCheck.WeakPassword,
    MonitorCheck.CompromisedPassword -> PassTheme.colors.passwordInteractionNormMajor2

    MonitorCheck.ReusedPassword -> PassTheme.colors.noteInteractionNormMajor2
    MonitorCheck.Missing2fa -> PassTheme.colors.textNorm
}

private const val MULTI_THRESHOLD = 2

@[Preview Composable]
internal fun LoginMonitorSectionMultiPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginMonitorSection(
                modifier = Modifier.padding(Spacing.medium),
                monitorState = LoginMonitorState(
                    isExcludedFromMonitor = false,
                    navigationScope = ItemDetailNavScope.Default,
                    isPasswordCompromised = true,
                    isPasswordInsecure = true,
                    isPasswordReused = true,
                    isMissingTwoFa = true,
                    reusedPasswordDisplayMode = LoginMonitorState.ReusedPasswordDisplayMode.Expanded,
                    reusedPasswordCount = 0,
                    reusedPasswordItems = persistentListOf()
                ),
                canLoadExternalImages = false,
                onEvent = {}
            )
        }
    }
}

@[Preview Composable]
internal fun LoginMonitorSectionMExpPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginMonitorSection(
                modifier = Modifier.padding(Spacing.medium),
                monitorState = LoginMonitorState(
                    isExcludedFromMonitor = false,
                    navigationScope = ItemDetailNavScope.Default,
                    isPasswordCompromised = true,
                    isPasswordInsecure = true,
                    isPasswordReused = true,
                    isMissingTwoFa = true,
                    reusedPasswordDisplayMode = LoginMonitorState.ReusedPasswordDisplayMode.Expanded,
                    reusedPasswordCount = 0,
                    reusedPasswordItems = persistentListOf()
                ),
                canLoadExternalImages = false,
                forceExpanded = true,
                onEvent = {}
            )
        }
    }
}

@[Preview Composable]
internal fun LoginMonitorSectionSinglePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginMonitorSection(
                modifier = Modifier.padding(Spacing.medium),
                monitorState = LoginMonitorState(
                    isExcludedFromMonitor = false,
                    navigationScope = ItemDetailNavScope.Default,
                    isPasswordCompromised = false,
                    isPasswordInsecure = true,
                    isPasswordReused = false,
                    isMissingTwoFa = false,
                    reusedPasswordDisplayMode = LoginMonitorState.ReusedPasswordDisplayMode.Expanded,
                    reusedPasswordCount = 0,
                    reusedPasswordItems = persistentListOf()
                ),
                canLoadExternalImages = false,
                onEvent = {}
            )
        }
    }
}
