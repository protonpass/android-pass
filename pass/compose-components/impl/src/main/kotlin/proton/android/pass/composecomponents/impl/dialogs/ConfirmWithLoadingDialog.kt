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

package proton.android.pass.composecomponents.impl.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.DialogProperties
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import kotlin.math.max

@Composable
fun ConfirmWithLoadingDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    isLoading: Boolean,
    isConfirmActionDestructive: Boolean,
    isConfirmEnabled: Boolean = true,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    extraButtons: (@Composable () -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = show,
        isLoading = isLoading,
        isConfirmActionDestructive = isConfirmActionDestructive,
        isConfirmEnabled = isConfirmEnabled,
        title = title,
        content = {
            ProtonAlertDialogText(
                text = message
            )
        },
        extraButtons = extraButtons,
        confirmText = confirmText,
        cancelText = cancelText,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        onCancel = onCancel
    )
}

@Composable
fun ConfirmWithLoadingDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    isLoading: Boolean,
    isConfirmActionDestructive: Boolean,
    isConfirmEnabled: Boolean = true,
    title: String,
    content: @Composable () -> Unit,
    extraButtons: (@Composable () -> Unit)? = null,
    confirmText: String,
    cancelText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    LoadingDialog(
        modifier = modifier,
        show = show,
        title = title,
        content = content,
        onDismiss = onDismiss,
        buttons = {
            DialogButton(text = cancelText, isEnabled = !isLoading, onClick = onCancel)

            extraButtons?.invoke()

            DialogButton(
                text = confirmText,
                textColor = dialogConfirmColor(isConfirmEnabled, isConfirmActionDestructive),
                isEnabled = isConfirmEnabled,
                isLoading = isLoading,
                onClick = onConfirm
            )
        }
    )
}

@Composable
fun dialogConfirmColor(isConfirmEnabled: Boolean = true, isConfirmActionDestructive: Boolean) = if (isConfirmEnabled) {
    if (isConfirmActionDestructive) {
        PassTheme.colors.signalDanger
    } else {
        PassTheme.colors.interactionNormMajor2
    }
} else {
    PassTheme.colors.textDisabled
}

@Composable
fun LoadingDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    title: String,
    content: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return
    BackHandler { onDismiss() }
    AlertDialog(
        modifier = modifier
            .fillMaxWidth(fraction = ALERT_DIALOG_WIDTH_FRACTION)
            .widthIn(max = MAX_ALERT_DIALOG_WIDTH),
        shape = ProtonTheme.shapes.medium,
        backgroundColor = ProtonTheme.colors.backgroundNorm,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        title = {
            Text(
                text = title,
                style = ProtonTheme.typography.headlineNorm,
                color = ProtonTheme.colors.textNorm
            )
        },
        text = content,
        onDismissRequest = onDismiss,
        buttons = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = Spacing.small,
                        vertical = Spacing.extraSmall / 2
                    )
            ) {
                AlertDialogFlowRow(
                    mainAxisSpacing = Spacing.small,
                    crossAxisSpacing = Spacing.mediumSmall
                ) {
                    buttons()
                }
            }
        }
    )
}

// Copied from AndroidAlertDialog.Kt
@Composable
private fun AlertDialogFlowRow(
    mainAxisSpacing: Dp,
    crossAxisSpacing: Dp,
    content: @Composable () -> Unit
) {
    Layout(content) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        val childConstraints = Constraints(maxWidth = constraints.maxWidth)

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
                placeable.width <= constraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.roundToPx()
            }
            // Ensures that confirming actions appear above dismissive actions.
            @Suppress("ListIterator")
            sequences.add(0, currentSequence.toList())
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        measurables.fastForEach { measurable ->
            // Ask the child for its preferred size.
            val placeable = measurable.measure(childConstraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.roundToPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = if (constraints.maxWidth != Constraints.Infinity) {
            constraints.maxWidth
        } else {
            max(mainAxisSpace, constraints.minWidth)
        }
        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        val layoutWidth = mainAxisLayoutSize

        val layoutHeight = crossAxisLayoutSize

        layout(layoutWidth, layoutHeight) {
            sequences.fastForEachIndexed { i, placeables ->
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].width +
                        if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                }
                val arrangement = Arrangement.Bottom
                // Handle vertical direction
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
                with(arrangement) {
                    arrange(mainAxisLayoutSize, childrenMainAxisSizes, mainAxisPositions)
                }
                placeables.fastForEachIndexed { j, placeable ->
                    placeable.place(
                        x = mainAxisPositions[j],
                        y = crossAxisPositions[i]
                    )
                }
            }
        }
    }
}

private const val ALERT_DIALOG_WIDTH_FRACTION = 0.9f

// Mobile alert on desktop is 560dp wide
// https://material.io/components/dialogs#specs
private val MAX_ALERT_DIALOG_WIDTH = 560.dp
