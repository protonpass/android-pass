package proton.android.pass.composecomponents.impl.topbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import proton.android.pass.commonui.api.ThemePreviewProvider
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
    drawerIcon: @Composable () -> Unit
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        drawerIcon()
        SearchTextField(
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
    }
}

@Preview
@Composable
fun SearchTopBarPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    PassTheme(isDark = isDarkMode) {
        Surface {
            SearchTopBar(
                searchQuery = "",
                placeholderText = "Search in all vaults",
                onSearchQueryChange = { },
                drawerIcon = {
                    VaultIcon(
                        backgroundColor = PassPalette.Yellow16,
                        iconColor = PassPalette.Yellow100,
                        icon = R.drawable.ic_proton_house
                    )
                },
                inSearchMode = true,
                onStopSearch = {},
                onEnterSearch = {}
            )
        }
    }
}
