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

package proton.android.pass.features.security.center.darkweb.ui.customemails.dialog

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.features.security.center.R
import me.proton.core.presentation.R as CoreR

@Composable
fun CannotAddCustomEmailsDialog(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dark_web_cannot_add_custom_emails_dialog_title)) },
        text = { Text(stringResource(R.string.dark_web_cannot_add_custom_emails_dialog_subtitle)) },
        confirmButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text(text = stringResource(id = CoreR.string.presentation_alert_ok))
            }
        }
    )
}
