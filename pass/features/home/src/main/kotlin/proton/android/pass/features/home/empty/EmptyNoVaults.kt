/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.home.empty

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.PassCircleButton
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.home.R


@Composable
internal fun EmptyNoVaults(
    modifier: Modifier = Modifier,
    canCreateVault: Boolean,
    onCreateVaultsClick: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = 0.1f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image.Default(
                modifier = Modifier.size(120.dp),
                id = R.drawable.ic_vault_lock
            )

            Text.Subheadline(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.home_empty_no_vaults_title),
                textAlign = TextAlign.Center
            )

            if (canCreateVault) {
                PassCircleButton(
                    modifier = Modifier,
                    text = stringResource(R.string.vault_drawer_create_vault),
                    backgroundColor = PassTheme.colors.interactionNormMinor1,
                    textColor = PassTheme.colors.interactionNormMajor2,
                    onClick = onCreateVaultsClick,
                    contentHorizontalPadding = Spacing.large,
                    fillMaxWidth = false
                )
            } else {
                Row(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = PassTheme.colors.textWeak,
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon.Default(
                        modifier = Modifier.size(18.dp),
                        id = me.proton.core.presentation.compose.R.drawable.ic_proton_question_circle,
                        tint = PassTheme.colors.textWeak
                    )

                    Text.Body3Regular(
                        text = stringResource(id = R.string.home_empty_no_vaults_subtitle),
                        color = PassTheme.colors.textWeak,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun EmptyNoVaultsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            EmptyNoVaults(
                canCreateVault = false,
                onCreateVaultsClick = {}
            )
        }
    }
}

@Preview
@Composable
fun EmptyNoVaultsCanCreatePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            EmptyNoVaults(
                canCreateVault = true,
                onCreateVaultsClick = {}
            )
        }
    }
}

