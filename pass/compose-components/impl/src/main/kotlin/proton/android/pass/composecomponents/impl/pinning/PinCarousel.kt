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

package proton.android.pass.composecomponents.impl.pinning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.TransparentTextButton
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

private const val PIN_LENGTH = 5

@Composable
fun PinCarousel(
    modifier: Modifier = Modifier,
    list: ImmutableList<ItemUiModel>,
    canLoadExternalImages: Boolean,
    onItemClick: (ItemUiModel) -> Unit,
    onSeeAllClick: () -> Unit
) {
    if (list.isNotEmpty()) {
        LazyRow(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Spacer(modifier = Modifier.width(8.dp))
            }
            items(items = list.take(PIN_LENGTH), key = { it.key }) { item ->
                PinItem(
                    item = item,
                    canLoadExternalImages = canLoadExternalImages,
                    onItemClick = onItemClick
                )
            }
            if (list.size > PIN_LENGTH) {
                item {
                    TransparentTextButton(
                        text = stringResource(R.string.pinning_carousel_see_all),
                        color = PassTheme.colors.interactionNormMajor2,
                        onClick = onSeeAllClick
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PinCarouselPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PinCarousel(
                list = persistentListOf(
                    ItemUiModel(
                        id = ItemId("1"),
                        userId = UserId("user-id"),
                        shareId = ShareId("345"),
                        contents = ItemContents.Note(
                            "Item with long text and a maximum",
                            ""
                        ),
                        state = 0,
                        createTime = Clock.System.now(),
                        modificationTime = Clock.System.now(),
                        lastAutofillTime = Clock.System.now(),
                        isPinned = true,
                        revision = 1,
                        shareCount = 0
                    ),
                    ItemUiModel(
                        id = ItemId("2"),
                        userId = UserId("user-id"),
                        shareId = ShareId("345"),
                        contents = ItemContents.Login(
                            title = "Login title",
                            note = "",
                            itemEmail = "",
                            itemUsername = "",
                            password = HiddenState.Empty(""),
                            urls = listOf(),
                            packageInfoSet = setOf(),
                            primaryTotp = HiddenState.Empty(""),
                            customFields = listOf(),
                            passkeys = emptyList()
                        ),
                        state = 0,
                        createTime = Clock.System.now(),
                        modificationTime = Clock.System.now(),
                        lastAutofillTime = Clock.System.now(),
                        isPinned = true,
                        revision = 1,
                        shareCount = 0
                    )
                ),
                canLoadExternalImages = false,
                onItemClick = { _ -> },
                onSeeAllClick = { }
            )
        }
    }
}
