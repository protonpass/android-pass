/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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
 */

package proton.android.pass.commonui.impl.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.dialogs.DialogButton

@Composable
fun AssociateItemDialog(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondary: (() -> Unit)? = null,
    onDismiss: () -> Unit = onCancel
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(backgroundColor = PassTheme.colors.backgroundNorm) {
            Column(
                modifier = modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = title, style = ProtonTheme.typography.headlineNorm)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = message,
                    style = ProtonTheme.typography.defaultNorm
                )
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = confirmLabel,
                    onClick = onConfirm
                )
                if (secondaryLabel != null && onSecondary != null) {
                    DialogButton(
                        modifier = Modifier.align(Alignment.End),
                        text = secondaryLabel,
                        onClick = onSecondary
                    )
                }
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = cancelLabel,
                    onClick = onCancel
                )
            }
        }
    }
}
