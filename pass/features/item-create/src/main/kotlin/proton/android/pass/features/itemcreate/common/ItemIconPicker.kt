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

package proton.android.pass.features.itemcreate.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.container.BoxedIcon
import proton.android.pass.composecomponents.impl.item.icon.ItemCustomIcon
import proton.android.pass.features.itemcreate.R
import proton.android.pass.log.api.PassLogger
import me.proton.core.presentation.R as CoreR

/**
 * Lets the user pick a custom icon for the item and remove it again. The picked image is
 * processed by the caller (see `ItemIconProcessor`), this composable only renders validated icons.
 */
@Composable
internal fun ItemIconPicker(
    modifier: Modifier = Modifier,
    icon: String?,
    enabled: Boolean,
    shape: Shape = PassTheme.shapes.squircleMediumShape,
    onIconSelected: (Uri) -> Unit,
    onIconRemoved: () -> Unit,
    defaultIcon: @Composable () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onIconSelected)
    }
    val setIconDescription = stringResource(R.string.item_icon_set_content_description)
    val removeIconDescription = stringResource(R.string.item_icon_remove_content_description)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(ICON_SIZE.dp)
                .clip(shape)
                .clickable(enabled = enabled, role = Role.Button) {
                    runCatching { launcher.launch(IMAGE_MIME_TYPE_FILTER) }
                        .onFailure {
                            PassLogger.w(TAG, it)
                            PassLogger.w(TAG, "Error launching icon picker")
                        }
                }
                .semantics { contentDescription = setIconDescription }
        ) {
            ItemCustomIcon(
                icon = icon,
                size = ICON_SIZE,
                shape = shape,
                fallback = {
                    if (icon == null) {
                        defaultIcon()
                    } else {
                        // Set but not renderable (ie: coming from another client): allow removing it
                        InvalidItemIcon(shape = shape)
                    }
                }
            )
        }

        if (icon != null && enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = REMOVE_BADGE_OFFSET.dp, y = -REMOVE_BADGE_OFFSET.dp)
                    .size(REMOVE_BADGE_SIZE.dp)
                    .clip(CircleShape)
                    .background(PassTheme.colors.signalDanger)
                    .clickable(role = Role.Button, onClick = onIconRemoved)
                    .semantics { contentDescription = removeIconDescription },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(REMOVE_BADGE_ICON_SIZE.dp),
                    painter = painterResource(CoreR.drawable.ic_proton_cross_small),
                    contentDescription = null,
                    tint = PassTheme.colors.textInvert
                )
            }
        }
    }
}

@Composable
private fun InvalidItemIcon(shape: Shape) {
    BoxedIcon(
        size = ICON_SIZE,
        shape = shape,
        backgroundColor = PassTheme.colors.loginInteractionNormMinor1
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_exclamation_circle_filled),
            contentDescription = null,
            tint = PassTheme.colors.signalWarning
        )
    }
}

private const val TAG = "ItemIconPicker"
private const val IMAGE_MIME_TYPE_FILTER = "image/*"
private const val ICON_SIZE = 40
private const val REMOVE_BADGE_SIZE = 20
private const val REMOVE_BADGE_ICON_SIZE = 14
private const val REMOVE_BADGE_OFFSET = 6
