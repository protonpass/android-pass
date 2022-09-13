package me.proton.android.pass.ui.create.alias

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.CrossBackIcon
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.android.pass.ui.shared.ProtonFormInput
import me.proton.android.pass.ui.shared.ProtonTextField
import me.proton.android.pass.ui.shared.ProtonTextTitle
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

internal typealias OnTextChange = (String) -> Unit

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun CreateAliasView(
    shareId: ShareId,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    viewModel: CreateAliasViewModel = hiltViewModel()
) {
    val viewState by rememberFlowWithLifecycle(viewModel.viewState)
        .collectAsState(viewModel.initialViewState)

    LaunchedEffect(Unit) {
        viewModel.onStart(shareId)
    }

    AliasView(
        viewState = viewState,
        topBarTitle = R.string.title_create_alias,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.createAlias(shareId) },
        viewModel = viewModel,
        canEdit = true
    )
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun UpdateAliasView(
    shareId: ShareId,
    itemId: ItemId,
    onUpClick: () -> Unit,
    onSuccess: (ItemId) -> Unit,
    viewModel: UpdateAliasViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.onStart(shareId, itemId)
    }

    val viewState by rememberFlowWithLifecycle(viewModel.viewState)
        .collectAsState(viewModel.initialViewState)

    AliasView(
        viewState = viewState,
        topBarTitle = R.string.title_edit_alias,
        onUpClick = onUpClick,
        onSuccess = onSuccess,
        onSubmit = { viewModel.updateAlias() },
        viewModel = viewModel,
        canEdit = false
    )
}

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
private fun AliasView(
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
    val (bottomSheetContentType, setBottomSheetContentType) = remember { mutableStateOf(AliasBottomSheetContent.Suffix) }

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
            CreateAliasScreen(
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

@Composable
private fun CreateAliasScreen(
    modifier: Modifier = Modifier,
    state: BaseAliasViewModel.ModelState,
    canEdit: Boolean,
    onTitleChange: OnTextChange,
    onAliasChange: OnTextChange,
    onNoteChange: OnTextChange,
    onSuffixClick: () -> Unit,
    onMailboxClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TitleInput(value = state.title, onChange = onTitleChange)
        Spacer(Modifier.padding(vertical = 8.dp))
        AliasSection(
            state = state,
            onChange = onAliasChange,
            onSuffixClick = onSuffixClick,
            canEdit = canEdit
        )
        Spacer(Modifier.padding(vertical = 8.dp))
        MailboxSection(
            state = state,
            onMailboxClick = onMailboxClick
        )
        NoteInput(value = state.note, onChange = onNoteChange)
    }
}

@Composable
private fun TitleInput(
    value: String,
    onChange: (String) -> Unit
) {
    ProtonFormInput(
        title = R.string.field_title_title,
        placeholder = R.string.field_title_hint,
        value = value,
        onChange = onChange,
        required = true,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun NoteInput(value: String, onChange: (String) -> Unit) {
    ProtonFormInput(
        title = R.string.field_note_title,
        placeholder = R.string.field_note_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 28.dp),
        singleLine = false,
        moveToNextOnEnter = false
    )
}

@Composable
private fun AliasSection(
    state: BaseAliasViewModel.ModelState,
    canEdit: Boolean,
    onChange: (String) -> Unit,
    onSuffixClick: () -> Unit
) {
    ProtonTextTitle(R.string.field_alias_title)
    ProtonTextField(
        value = state.alias,
        onChange = onChange,
        modifier = Modifier.padding(top = 8.dp),
        editable = canEdit
    )
    AliasSelector(
        state = state,
        modifier = Modifier.padding(top = 8.dp),
        onClick = onSuffixClick
    )
    if (state.aliasToBeCreated != null) {
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = stringResource(R.string.alias_you_are_about_to_create_alias),
                fontSize = 10.sp,
                color = ProtonTheme.colors.textWeak
            )
            Text(
                text = state.aliasToBeCreated,
                fontSize = 10.sp,
                color = ProtonTheme.colors.brandNorm,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}

@Composable
private fun MailboxSection(
    state: BaseAliasViewModel.ModelState,
    onMailboxClick: () -> Unit
) {
    ProtonTextTitle(R.string.field_mailboxes_title)
    MailboxSelector(
        state = state,
        modifier = Modifier.padding(top = 8.dp),
        onClick = onMailboxClick
    )
}

@Composable
private fun AliasSelector(
    state: BaseAliasViewModel.ModelState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val value = if (state.selectedSuffix != null) {
        state.selectedSuffix.suffix
    } else {
        ""
    }
    Selector(
        text = value,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun MailboxSelector(
    state: BaseAliasViewModel.ModelState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val value = if (state.selectedMailbox != null) {
        state.selectedMailbox.email
    } else {
        ""
    }
    Selector(
        text = value,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
private fun Selector(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextField(
        readOnly = true,
        enabled = false,
        value = text,
        onValueChange = {},
        trailingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_proton_chevron_right),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
        },
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = TextFieldDefaults.textFieldColors(
            textColor = ProtonTheme.colors.textNorm,
            disabledTextColor = ProtonTheme.colors.textNorm,
            backgroundColor = ProtonTheme.colors.backgroundSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
