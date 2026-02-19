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

package proton.android.pass.composecomponents.impl.item.details.titles

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.LocalDark
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import me.proton.core.presentation.R as CoreR

private const val ELLIPSIS = "..."

private val BorderDarkColor = Color(color = 0xFF38384C)
private val BorderLightColor = Color(color = 0xFFE3DFFA)

@Composable
fun FolderPathComposable(
    modifier: Modifier = Modifier,
    vaultName: String,
    vaultIcon: ShareIcon,
    vaultColor: ShareColor,
    forceExpand: Boolean = false,
    folderPath: List<String>
) {
    if (folderPath.isEmpty()) return

    val (isExpanded, onExpand) = rememberSaveable { mutableStateOf(forceExpand) }

    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = when (LocalDark.current) {
                    true -> BorderDarkColor
                    false -> BorderLightColor
                },
                shape = RoundedCornerShape(size = 12.dp)
            )
            .clip(RoundedCornerShape(size = 12.dp))
            .applyIf(
                condition = folderPath.size > 1,
                ifTrue = {
                    clickable { onExpand(!isExpanded) }
                }
            )
            .padding(horizontal = Spacing.medium, vertical = Spacing.mediumSmall)
            .padding(vertical = Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(16.dp)
                .padding(end = Spacing.extraSmall),
            painter = painterResource(vaultIcon.toSmallResource()),
            contentDescription = null,
            tint = vaultColor.toColor()
        )

        Text(
            text = vaultName,
            style = PassTheme.typography.body3Norm(),
            color = PassTheme.colors.textWeak
        )

        Spacer(modifier = Modifier.width(Spacing.extraSmall))

        AnimatedContent(
            targetState = isExpanded,
            label = "FolderPathAnimation"
        ) { expanded ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                when {
                    folderPath.size == 1 -> {
                        ChevronIcon()
                        FolderItem(
                            name = folderPath.first(),
                            isLast = true,
                            textColor = PassTheme.colors.textNorm
                        )
                    }

                    expanded -> {
                        folderPath.forEachIndexed { index, folderName ->
                            val isLast = index == folderPath.lastIndex
                            ChevronIcon()
                            FolderItem(
                                name = folderName,
                                isLast = isLast,
                                textColor = if (isLast) PassTheme.colors.textNorm else PassTheme.colors.textWeak
                            )
                        }
                    }

                    else -> {
                        ChevronIcon()
                        Text(
                            text = ELLIPSIS,
                            style = PassTheme.typography.body3Norm(),
                            color = PassTheme.colors.textWeak
                        )
                        ChevronIcon()
                        FolderItem(
                            name = folderPath.last(),
                            isLast = true,
                            textColor = PassTheme.colors.textNorm
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChevronIcon() {
    Icon(
        modifier = Modifier.size(12.dp),
        painter = painterResource(CoreR.drawable.ic_proton_chevron_tiny_right),
        contentDescription = null,
        tint = PassTheme.colors.textWeak
    )
}

@Composable
private fun FolderItem(
    name: String,
    isLast: Boolean,
    textColor: Color
) {
    Spacer(modifier = Modifier.width(4.dp))
    Icon(
        modifier = Modifier.size(12.dp),
        painter = painterResource(
            if (isLast) CoreR.drawable.ic_proton_folder_filled
            else CoreR.drawable.ic_proton_folders_filled
        ),
        contentDescription = null,
        tint = Color(color = 0xFFE9A944)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
        text = name,
        style = PassTheme.typography.body3Norm(),
        color = textColor
    )
}

@[Preview Composable]
internal fun FolderPathComposableSinglePreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, _) = input

    PassTheme(isDark = isDark) {
        Surface {
            FolderPathComposable(
                vaultName = "Work",
                vaultIcon = ShareIcon.Icon2,
                vaultColor = ShareColor.Color2,
                folderPath = listOf("Folder1")
            )
        }
    }
}

@[Preview Composable]
internal fun FolderPathComposableCollapsedPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            FolderPathComposable(
                vaultName = "Work",
                vaultIcon = ShareIcon.Icon2,
                vaultColor = ShareColor.Color2,
                folderPath = listOf("Folder1", "Folder2", "Folder3")
            )
        }
    }
}

@[Preview Composable]
internal fun FolderPathComposableExpandedPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            FolderPathComposable(
                vaultName = "Work",
                vaultIcon = ShareIcon.Icon2,
                vaultColor = ShareColor.Color2,
                forceExpand = true,
                folderPath = listOf("Folder1", "Folder2", "Folder3")
            )
        }
    }
}
