package me.proton.android.pass.ui.create.alias

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
internal fun AliasContent(
    viewState: BaseAliasViewModel.ViewState,
    @StringRes topBarTitle: Int,
    onUpClick: () -> Unit,
    onSubmit: () -> Unit,
    viewModel: BaseAliasViewModel,
    onSuccess: (ItemId) -> Unit,
    canEdit: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    val (bottomSheetContentType, setBottomSheetContentType) = remember {
        mutableStateOf(
            AliasBottomSheetContent.Suffix
        )
    }

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            BottomSheetContents(
                modelState = viewState.modelState,
                contentType = bottomSheetContentType,
                onSuffixSelect = { suffix ->
                    scope.launch {
                        bottomSheetState.hide()
                        viewModel.onSuffixChange(suffix)
                    }
                },
                onMailboxSelect = { mailbox ->
                    scope.launch {
                        bottomSheetState.hide()
                        viewModel.onMailboxChange(mailbox)
                    }
                }
            )
        }
    ) {
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
                                text = stringResource(R.string.action_save),
                                color = ProtonTheme.colors.brandNorm,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                )
            }
        ) { padding ->
            if (viewState.state == BaseAliasViewModel.State.Loading) {
                LoadingDialog()
            }
            CreateAliasForm(
                state = viewState.modelState,
                canEdit = canEdit,
                modifier = Modifier.padding(padding),
                onSuffixClick = {
                    scope.launch {
                        if (canEdit) {
                            setBottomSheetContentType(AliasBottomSheetContent.Suffix)
                            bottomSheetState.show()
                        }
                    }
                },
                onMailboxClick = {
                    scope.launch {
                        if (canEdit) {
                            setBottomSheetContentType(AliasBottomSheetContent.Mailbox)
                            bottomSheetState.show()
                        }
                    }
                },
                onTitleChange = { viewModel.onTitleChange(it) },
                onNoteChange = { viewModel.onNoteChange(it) },
                onAliasChange = { viewModel.onAliasChange(it) }
            )
            when (val state = viewState.state) {
                is BaseAliasViewModel.State.Error -> Text(text = "something went boom")
                is BaseAliasViewModel.State.Success -> onSuccess(state.itemId)
                else -> {}
            }
        }
    }
}
