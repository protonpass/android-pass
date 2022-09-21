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
import androidx.compose.runtime.LaunchedEffect
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
import me.proton.android.pass.ui.create.alias.AliasItemValidationErrors.BlankAlias
import me.proton.android.pass.ui.create.alias.AliasItemValidationErrors.BlankTitle
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.android.pass.ui.shared.uievents.ItemSavedState
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasSuffix
import me.proton.core.pass.domain.ItemId

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
internal fun AliasContent(
    uiState: CreateUpdateAliasUiState,
    @StringRes topBarTitle: Int,
    canEdit: Boolean,
    onUpClick: () -> Unit,
    onSubmit: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    onSuffixChange: (AliasSuffix) -> Unit,
    onMailboxChange: (AliasMailbox) -> Unit,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAliasChange: (String) -> Unit
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
                modelState = uiState.aliasItem,
                contentType = bottomSheetContentType,
                onSuffixSelect = { suffix ->
                    scope.launch {
                        bottomSheetState.hide()
                        onSuffixChange(suffix)
                    }
                },
                onMailboxSelect = { mailbox ->
                    scope.launch {
                        bottomSheetState.hide()
                        onMailboxChange(mailbox)
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
            if (uiState.isLoadingState == IsLoadingState.Loading) {
                LoadingDialog()
            }
            CreateAliasForm(
                state = uiState.aliasItem,
                canEdit = canEdit,
                modifier = Modifier.padding(padding),
                onTitleRequiredError = uiState.errorList.contains(BlankTitle),
                onAliasRequiredError = uiState.errorList.contains(BlankAlias),
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
                onTitleChange = { onTitleChange(it) },
                onNoteChange = { onNoteChange(it) },
                onAliasChange = { onAliasChange(it) }
            )
            LaunchedEffect(uiState.isItemSaved is ItemSavedState.Success) {
                val isItemSaved = uiState.isItemSaved
                if (isItemSaved is ItemSavedState.Success) {
                    onSuccess(isItemSaved.itemId)
                }
            }
        }
    }
}
