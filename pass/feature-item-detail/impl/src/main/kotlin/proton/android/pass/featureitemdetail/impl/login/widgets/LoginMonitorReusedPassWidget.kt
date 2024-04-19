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

package proton.android.pass.featureitemdetail.impl.login.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Clock
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionStrongNorm
import proton.android.pass.common.api.ellipsize
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.body3Bold
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.login.LoginMonitorState
import proton.android.pass.featureitemdetail.impl.login.LoginMonitorWidget

private const val REUSED_LOGIN_ITEM_ICON_SIZE = 24
private const val REUSED_LOGIN_ITEM_TITLE_MAX_LENGTH = 20

@Composable
internal fun LoginMonitorReusedPassWidget(
    modifier: Modifier = Modifier,
    reusedPasswordDisplayMode: LoginMonitorState.ReusedPasswordDisplayMode,
    reusedPasswordCount: Int,
    reusedPasswordItems: ImmutableList<ItemUiModel>,
    canLoadExternalImages: Boolean
) {
    LoginMonitorWidget(
        modifier = modifier,
        titleResId = R.string.login_item_monitor_widget_reused_pass_title,
        subtitleResId = R.string.login_item_monitor_widget_reused_pass_subtitle,
        additionalContent = {
            when (reusedPasswordDisplayMode) {
                LoginMonitorState.ReusedPasswordDisplayMode.Compact -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
                    ) {
                        Text(
                            modifier = Modifier
                                .clip(shape = RoundedCornerShape(size = Radius.small))
                                .background(color = PassTheme.colors.noteInteractionNormMinor1)
                                .padding(all = Spacing.small),
                            text = stringResource(
                                id = R.string.login_item_monitor_widget_reused_pass_count_compact_leading,
                                reusedPasswordCount
                            ),
                            color = PassTheme.colors.noteInteractionNormMajor2,
                            style = PassTheme.typography.body3Bold()
                        )

                        Text(
                            text = stringResource(
                                id = R.string.login_item_monitor_widget_reused_pass_count_compact_trailing
                            ),
                            color = PassTheme.colors.noteInteractionNormMajor2,
                            style = PassTheme.typography.body3Norm()
                        )
                    }
                }

                LoginMonitorState.ReusedPasswordDisplayMode.Expanded -> {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.login_item_monitor_widget_reused_pass_count_expanded,
                            count = reusedPasswordCount, reusedPasswordCount
                        ),
                        color = PassTheme.colors.noteInteractionNormMajor2,
                        style = PassTheme.typography.body3Norm()
                    )

                    ReusedPasswordCarousel(
                        reusedPasswordItems = reusedPasswordItems,
                        canLoadExternalImages = canLoadExternalImages
                    )
                }
            }
        }
    )
}

@Composable
private fun ReusedPasswordCarousel(
    modifier: Modifier = Modifier,
    reusedPasswordItems: ImmutableList<ItemUiModel>,
    canLoadExternalImages: Boolean
) {
    if (reusedPasswordItems.isNotEmpty()) {
        LazyRow(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            items(
                items = reusedPasswordItems,
                key = { reusedPasswordItem -> reusedPasswordItem.id.id }
            ) { reusedPasswordItem ->
                ReusedPasswordCarouselItem(
                    item = reusedPasswordItem,
                    canLoadExternalImages = canLoadExternalImages
                )
            }
        }
    }
}

@Composable
private fun ReusedPasswordCarouselItem(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    canLoadExternalImages: Boolean
) = with(item.contents as ItemContents.Login) {
    Row(
        modifier = modifier
            .roundedContainer(
                backgroundColor = PassTheme.colors.noteInteractionNormMinor1,
                borderColor = PassTheme.colors.noteInteractionNormMinor1
            )
            .padding(Spacing.small),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoginIcon(
            modifier = Modifier.size(REUSED_LOGIN_ITEM_ICON_SIZE.dp),
            text = title,
            shape = PassTheme.shapes.squircleSmallShape,
            canLoadExternalImages = canLoadExternalImages,
            size = REUSED_LOGIN_ITEM_ICON_SIZE,
            favIconPadding = 2.dp,
            website = urls.firstOrNull(),
            packageName = packageInfoSet.firstOrNull()?.packageName?.value,
            backgroundColor = PassTheme.colors.loginInteractionNormMinor2
        )

        Text(
            text = item.contents.title.ellipsize(REUSED_LOGIN_ITEM_TITLE_MAX_LENGTH),
            style = ProtonTheme.typography.captionStrongNorm
        )
    }
}

@[Preview Composable Suppress("FunctionMaxLength")]
internal fun LoginMonitorReusedPassWidgetCompactPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginMonitorReusedPassWidget(
                reusedPasswordDisplayMode = LoginMonitorState.ReusedPasswordDisplayMode.Compact,
                reusedPasswordCount = 8,
                reusedPasswordItems = persistentListOf(),
                canLoadExternalImages = false
            )
        }
    }
}

@[Preview Composable Suppress("FunctionMaxLength")]
internal fun LoginMonitorReusedPassWidgetExpandedPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            LoginMonitorReusedPassWidget(
                reusedPasswordDisplayMode = LoginMonitorState.ReusedPasswordDisplayMode.Expanded,
                reusedPasswordCount = 1,
                reusedPasswordItems = persistentListOf(
                    ItemUiModel(
                        id = ItemId("123"),
                        shareId = ShareId("345"),
                        contents = ItemContents.Login(
                            title = "Proton",
                            note = "",
                            username = "",
                            password = HiddenState.Concealed(""),
                            urls = emptyList(),
                            packageInfoSet = emptySet(),
                            primaryTotp = HiddenState.Concealed(""),
                            customFields = emptyList(),
                            passkeys = emptyList()
                        ),
                        state = 0,
                        createTime = Clock.System.now(),
                        modificationTime = Clock.System.now(),
                        lastAutofillTime = Clock.System.now(),
                        isPinned = false
                    )
                ),
                canLoadExternalImages = false
            )
        }
    }
}
