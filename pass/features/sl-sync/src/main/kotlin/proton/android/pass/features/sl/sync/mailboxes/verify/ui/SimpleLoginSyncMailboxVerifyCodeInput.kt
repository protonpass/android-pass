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

package proton.android.pass.features.sl.sync.mailboxes.verify.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf

@Composable
internal fun SimpleLoginSyncMailboxVerifyCodeInput(
    modifier: Modifier = Modifier,
    verificationCode: String,
    verificationCodeLength: Int,
    canEnterVerificationCode: Boolean,
    onVerificationCodeChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    var focusedField by remember { mutableIntStateOf(verificationCode.length) }

    val clipboardManager = LocalClipboardManager.current

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = verificationCode,
        onValueChange = { newVerificationCode ->
            if (newVerificationCode.length <= verificationCodeLength) {
                onVerificationCodeChange(newVerificationCode)
                focusedField = newVerificationCode.length
                return@BasicTextField
            }

            clipboardManager.getText()?.text.let { clipboardText ->
                if (clipboardText == newVerificationCode.takeLast(verificationCodeLength)) {
                    onVerificationCodeChange(clipboardText)
                    focusedField = clipboardText.length
                }
            }
        },
        readOnly = !canEnterVerificationCode,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.None
        ),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                repeat(verificationCodeLength) { index ->
                    val verificationCodeDigit = if (index < verificationCode.length) {
                        verificationCode[index].toString()
                    } else ""

                    Text(
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(size = Radius.small))
                            .border(
                                width = 1.dp,
                                color = PassTheme.colors.inputBorderStrong,
                                shape = RoundedCornerShape(size = Radius.small)
                            )
                            .applyIf(
                                condition = index == focusedField,
                                ifTrue = {
                                    background(color = PassTheme.colors.inputBackgroundNorm)
                                },
                                ifFalse = {
                                    background(color = PassTheme.colors.inputBackgroundStrong)
                                }
                            )
                            .width(width = Spacing.large)
                            .padding(all = Spacing.small),
                        text = verificationCodeDigit,
                        style = ProtonTheme.typography.subheadlineNorm,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}
