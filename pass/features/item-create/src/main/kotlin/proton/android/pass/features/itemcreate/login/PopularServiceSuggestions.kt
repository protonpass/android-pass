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

package proton.android.pass.features.itemcreate.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.popularservices.PopularService
import kotlin.math.roundToInt

private const val SUGGESTIONS_WIDTH_FRACTION = 0.9f
private const val SUGGESTIONS_KEY_SEPARATOR = "|"
private const val SUGGESTIONS_MAX_VISIBLE_ITEMS = 3.5f
private val SUGGESTIONS_ROW_HEIGHT = 56.dp
private val SUGGESTIONS_DIVIDER_HEIGHT = 1.dp
private val SUGGESTIONS_MAX_HEIGHT =
    (SUGGESTIONS_ROW_HEIGHT + SUGGESTIONS_DIVIDER_HEIGHT) * SUGGESTIONS_MAX_VISIBLE_ITEMS
private val SUGGESTIONS_ELEVATION = 6.dp
private val SUGGESTIONS_CORNER_RADIUS = 12.dp

private val SUGGESTIONS_TITLE_OVERLAP = 8.dp

/**
 * A floating autocomplete popup anchored right below the title field. It does not displace the
 * form (it renders in its own window) and is not focusable, so the keyboard stays up while typing.
 */
@Composable
internal fun PopularServiceSuggestionsPopup(
    services: ImmutableList<PopularService>,
    anchorPosition: IntOffset,
    anchorSize: IntSize,
    canLoadExternalImages: Boolean,
    onServiceSelected: (PopularService) -> Unit
) {
    if (services.isEmpty() || anchorSize.width == 0) return

    val density = LocalDensity.current
    val overlapPx = with(density) { SUGGESTIONS_TITLE_OVERLAP.roundToPx() }
    val popupWidthPx = (anchorSize.width * SUGGESTIONS_WIDTH_FRACTION).roundToInt()
    val horizontalInsetPx = (anchorSize.width - popupWidthPx) / 2

    Popup(
        popupPositionProvider = remember(anchorPosition, anchorSize, overlapPx, horizontalInsetPx) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = IntOffset(
                    x = anchorPosition.x + horizontalInsetPx,
                    y = anchorPosition.y + anchorSize.height - overlapPx
                )
            }
        },
        properties = PopupProperties(focusable = false)
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            PopularServiceSuggestionsContent(
                services = services,
                modifier = Modifier
                    .width(with(density) { popupWidthPx.toDp() }),
                canLoadExternalImages = canLoadExternalImages,
                onServiceSelected = onServiceSelected
            )
        }
    }
}

@Composable
private fun PopularServiceSuggestionsContent(
    services: ImmutableList<PopularService>,
    modifier: Modifier = Modifier,
    canLoadExternalImages: Boolean = false,
    onServiceSelected: (PopularService) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(SUGGESTIONS_CORNER_RADIUS),
        color = PassTheme.colors.backgroundNorm,
        shadowElevation = SUGGESTIONS_ELEVATION
    ) {
        LazyColumn(modifier = Modifier.heightIn(max = SUGGESTIONS_MAX_HEIGHT)) {
            itemsIndexed(
                items = services,
                key = { _, service ->
                    "${service.title}$SUGGESTIONS_KEY_SEPARATOR${service.urls.firstOrNull().orEmpty()}"
                }
            ) { index, service ->
                PopularServiceRow(
                    service = service,
                    canLoadExternalImages = canLoadExternalImages,
                    onClick = { onServiceSelected(service) }
                )
                if (index < services.lastIndex) {
                    PassDivider()
                }
            }
        }
    }
}

@Composable
private fun PopularServiceRow(
    service: PopularService,
    canLoadExternalImages: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SUGGESTIONS_ROW_HEIGHT)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoginIcon(
            text = service.title,
            websites = service.urls,
            packageName = null,
            canLoadExternalImages = canLoadExternalImages
        )
        Column {
            Text.Body1Regular(text = service.title)
            val displayUrl = remember(service) {
                service.urls.firstOrNull()?.let { UrlSanitizer.getDomain(it).getOrNull() }
            }
            displayUrl?.let { Text.Body3Weak(text = it) }
        }
    }
}

internal class PopularServicesPreviewProvider :
    PreviewParameterProvider<ImmutableList<PopularService>> {
    override val values: Sequence<ImmutableList<PopularService>> = sequenceOf(
        persistentListOf(
            PopularService(title = "Facebook", urls = listOf("https://www.facebook.com")),
            PopularService(title = "Fast.com", urls = listOf("https://fast.com")),
            PopularService(title = "Figma", urls = listOf("https://www.figma.com")),
            PopularService(title = "Tiktok", urls = listOf("https://www.tiktok.com"))
        )
    )
}

internal class ThemeAndPopularServicesProvider :
    ThemePairPreviewProvider<ImmutableList<PopularService>>(PopularServicesPreviewProvider())

@Preview
@Composable
internal fun PopularServiceSuggestionsContentPreview(
    @PreviewParameter(ThemeAndPopularServicesProvider::class)
    input: Pair<Boolean, ImmutableList<PopularService>>
) {
    PassTheme(isDark = input.first) {
        Surface {
            PopularServiceSuggestionsContent(
                services = input.second,
                modifier = Modifier.padding(Spacing.medium),
                onServiceSelected = {}
            )
        }
    }
}
