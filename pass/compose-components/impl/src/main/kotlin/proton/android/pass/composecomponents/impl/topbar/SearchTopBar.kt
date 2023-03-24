package proton.android.pass.composecomponents.impl.topbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.icon.VaultIcon

@Composable
fun SearchTopBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    placeholderText: String,
    inSearchMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearch: () -> Unit,
    drawerIcon: @Composable () -> Unit,
    actions: (@Composable () -> Unit)? = null
) {
    val endPadding = if (actions != null) 4.dp else 16.dp
    Row(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = endPadding, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        drawerIcon()
        Spacer(modifier = Modifier.width(8.dp))
        SearchTextField(
            modifier = Modifier.weight(1f),
            searchQuery = searchQuery,
            placeholderText = placeholderText,
            inSearchMode = inSearchMode,
            onSearchQueryChange = onSearchQueryChange,
            onEnterSearch = onEnterSearch,
            onStopSearch = onStopSearch,
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            painterResource(R.drawable.ic_proton_cross),
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconWeak
                        )
                    }
                }
            } else null
        )
        actions?.invoke()
    }
}

@Preview
@Composable
fun SearchTopBarPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SearchTopBar(
                searchQuery = "",
                placeholderText = "Search in all vaults",
                onSearchQueryChange = { },
                drawerIcon = {
                    VaultIcon(
                        backgroundColor = PassPalette.MacaroniAndCheese16,
                        iconColor = PassPalette.MacaroniAndCheese100,
                        icon = R.drawable.ic_proton_house
                    )
                },
                inSearchMode = true,
                onStopSearch = {},
                onEnterSearch = {},
                actions = {
                    if (input.second) {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.ic_proton_three_dots_vertical
                                ),
                                contentDescription = null,
                                tint = PassTheme.colors.textWeak
                            )
                        }
                    }
                }
            )
        }
    }
}
