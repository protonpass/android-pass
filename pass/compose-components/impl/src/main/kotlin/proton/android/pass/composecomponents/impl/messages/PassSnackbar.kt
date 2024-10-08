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

package proton.android.pass.composecomponents.impl.messages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonSnackbarHost
import me.proton.core.compose.component.ProtonSnackbarHostState
import me.proton.core.compose.component.ProtonSnackbarType
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.notifications.api.SnackbarType

@Composable
fun PassSnackbar(
    modifier: Modifier = Modifier,
    type: ProtonSnackbarType,
    message: String,
    actionLabel: String? = null,
    performAction: () -> Unit = {},
    dismiss: () -> Unit = {}
) {
    Snackbar(
        modifier = modifier.padding(Spacing.mediumSmall),
        content = { Text(message) },
        action = {
            Row {
                if (!actionLabel.isNullOrBlank()) {
                    TextButton(onClick = { performAction() }) {
                        Text(text = actionLabel)
                    }
                }
                IconButton(
                    onClick = { dismiss() },
                    content = {
                        Icon(
                            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_small),
                            contentDescription = null,
                            tint = ProtonTheme.colors.textInverted
                        )
                    }
                )
            }
        },
        actionOnNewLine = false,
        shape = ProtonTheme.shapes.medium,
        backgroundColor = when (type) {
            ProtonSnackbarType.SUCCESS -> ProtonTheme.colors.notificationSuccess
            ProtonSnackbarType.WARNING -> ProtonTheme.colors.notificationWarning
            ProtonSnackbarType.ERROR -> ProtonTheme.colors.notificationError
            ProtonSnackbarType.NORM -> ProtonTheme.colors.notificationNorm
        },
        contentColor = ProtonTheme.colors.textInverted,
        elevation = 6.dp
    )
}


@Composable
fun PassSnackbarHost(modifier: Modifier = Modifier, snackbarHostState: PassSnackbarHostState) {
    ProtonSnackbarHost(
        modifier = modifier,
        hostState = snackbarHostState.protonSnackbarHostState,
        snackbar = { data ->
            PassSnackbar(
                type = snackbarHostState.protonSnackbarHostState.type,
                message = data.message,
                actionLabel = data.actionLabel,
                performAction = { data.performAction() },
                dismiss = { data.dismiss() }
            )
        }
    )
}

@Composable
fun rememberPassSnackbarHostState(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
): PassSnackbarHostState = remember {
    PassSnackbarHostState(ProtonSnackbarHostState(snackbarHostState))
}

@Stable
class PassSnackbarHostState(
    val protonSnackbarHostState: ProtonSnackbarHostState = ProtonSnackbarHostState()
) {
    suspend fun showSnackbar(
        type: SnackbarType,
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) = protonSnackbarHostState.showSnackbar(
        type = when (type) {
            SnackbarType.SUCCESS -> ProtonSnackbarType.SUCCESS
            SnackbarType.WARNING -> ProtonSnackbarType.WARNING
            SnackbarType.ERROR -> ProtonSnackbarType.ERROR
            SnackbarType.NORM -> ProtonSnackbarType.NORM
        },
        message = message,
        actionLabel = actionLabel,
        duration = duration
    )
}

private val previewSnackbarData = object : SnackbarData {
    override val actionLabel: String? = null
    override val duration: SnackbarDuration = SnackbarDuration.Indefinite
    override val message: String = "This is a snackbar"
    override fun dismiss() = Unit
    override fun performAction() = Unit
}

class ThemeSnackbarPreviewProvider :
    ThemePairPreviewProvider<ProtonSnackbarType>(SnackbarTypePreviewProvider())

@Preview
@Composable
fun PassSnackbarPreview(
    @PreviewParameter(ThemeSnackbarPreviewProvider::class) input: Pair<Boolean, ProtonSnackbarType>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PassSnackbar(
                type = input.second,
                message = previewSnackbarData.message,
                actionLabel = previewSnackbarData.actionLabel,
                performAction = previewSnackbarData::performAction,
                dismiss = previewSnackbarData::dismiss
            )
        }
    }
}
