package proton.android.pass.composecomponents.impl.topbar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder

@Composable
fun SearchTextField(
    modifier: Modifier = Modifier,
    searchQuery: String,
    placeholderText: String,
    inSearchMode: Boolean,
    trailingIcon: @Composable (() -> Unit)? = null,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearch: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(inSearchMode) {
        if (inSearchMode) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }
    BackHandler(enabled = inSearchMode) {
        onStopSearch()
    }
    ProtonTextField(
        modifier = modifier
            .focusRequester(focusRequester)
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .applyIf(
                condition = !inSearchMode,
                ifTrue = { background(PassTheme.colors.searchBarBackground) }
            ),
        value = searchQuery,
        singleLine = true,
        placeholder = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!inSearchMode) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_magnifier),
                        contentDescription = stringResource(R.string.search_topbar_icon_content_description),
                        tint = PassTheme.colors.textWeak
                    )
                }
                ProtonTextFieldPlaceHolder(text = placeholderText)
            }
        },
        textStyle = ProtonTheme.typography.defaultNorm,
        onFocusChange = {
            if (it && !inSearchMode) {
                onEnterSearch()
            }
        },
        onChange = onSearchQueryChange,
        trailingIcon = trailingIcon
    )
}
