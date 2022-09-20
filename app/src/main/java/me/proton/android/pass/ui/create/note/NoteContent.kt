package me.proton.android.pass.ui.create.note

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.DeferredCircularProgressIndicator
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId

@ExperimentalComposeUiApi
@Composable
internal fun NoteContent(
    @StringRes topBarTitle: Int,
    @StringRes topBarActionName: Int,
    viewState: BaseNoteViewModel.ViewState,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    onSubmit: () -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(topBarTitle) },
                navigationIcon = { CrossBackIcon(onUpClick = onUpClick) },
                actions = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            onSubmit()
                        },
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Text(
                            text = stringResource(topBarActionName),
                            color = ProtonTheme.colors.brandNorm,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W500
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = viewState.state) {
            is BaseNoteViewModel.State.Idle -> CreateNoteItemForm(
                state = viewState.modelState,
                modifier = Modifier.padding(padding),
                onTitleChange = onTitleChange,
                onNoteChange = onNoteChange
            )
            is BaseNoteViewModel.State.Loading -> DeferredCircularProgressIndicator(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
            is BaseNoteViewModel.State.Error -> Text(text = "something went boom")
            is BaseNoteViewModel.State.Success -> onSuccess(state.itemId)
        }
    }
}
