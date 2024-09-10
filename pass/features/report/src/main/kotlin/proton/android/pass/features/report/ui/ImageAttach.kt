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

package proton.android.pass.features.report.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.features.report.R
import proton.android.pass.features.report.navigation.ReportNavContentEvent
import me.proton.core.presentation.compose.R as CoreR

@Composable
internal fun ImageAttach(
    modifier: Modifier = Modifier,
    images: Set<Uri>,
    onEvent: (ReportNavContentEvent) -> Unit
) {
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        if (it != null) {
            onEvent(ReportNavContentEvent.OnImageSelected(it))
        }
    }
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TransparentTextButton(
                text = stringResource(R.string.attach_image_button),
                prefixIcon = CoreR.drawable.ic_proton_plus,
                onClick = {
                    runCatching {
                        pickMedia.launch(PickVisualMediaRequest(ImageOnly))
                    }
                }
            )
        }
        Column {
            images.forEach { image ->
                ImageAttachedItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    image = image,
                    onEvent = onEvent
                )
            }
        }
    }
}
