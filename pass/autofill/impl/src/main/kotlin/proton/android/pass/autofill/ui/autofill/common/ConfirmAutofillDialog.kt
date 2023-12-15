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

package proton.android.pass.autofill.ui.autofill.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.AutofillConfirmMode
import proton.android.pass.commonui.api.PassTheme

@Composable
fun ConfirmAutofillDialog(
    modifier: Modifier = Modifier,
    mode: AutofillConfirmMode,
    onConfirm: () -> Unit,
    onClose: () -> Unit
) {
    PassTheme(isDark = isSystemInDarkTheme()) {
        ConfirmAutofillDialogContent(
            modifier = modifier,
            mode = mode,
            onConfirm = onConfirm,
            onClose = onClose
        )
    }
}

