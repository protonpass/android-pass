package proton.android.pass.featureitemcreate.impl.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_TITLE_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldIndexNavArgId
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldTitleNavArgId
import proton.android.pass.featureitemcreate.impl.common.CustomFieldIndexTitle
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@HiltViewModel
class EditCustomFieldNameViewModel @Inject constructor(
    private val draftRepository: DraftRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val customFieldIndex: Int = savedStateHandleProvider
        .get()
        .require(CustomFieldIndexNavArgId.key)

    private val customFieldTitle: String = savedStateHandleProvider
        .get()
        .require<String>(CustomFieldTitleNavArgId.key)
        .let { NavParamEncoder.decode(it) }

    private val eventFlow: MutableStateFlow<CustomFieldEvent> =
        MutableStateFlow(CustomFieldEvent.Unknown)
    private val nameFlow: MutableStateFlow<String> = MutableStateFlow(customFieldTitle)

    val state: StateFlow<CustomFieldNameUiState> = combine(
        eventFlow,
        nameFlow
    ) { event, value ->
        CustomFieldNameUiState(
            value = value,
            canConfirm = value.isNotEmpty(),
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CustomFieldNameUiState.Initial
    )

    fun onNameChanged(name: String) {
        nameFlow.update { name }
    }

    fun onSave() = viewModelScope.launch {
        draftRepository.save(
            key = DRAFT_CUSTOM_FIELD_TITLE_KEY,
            value = CustomFieldIndexTitle(
                title = nameFlow.value,
                index = customFieldIndex
            )
        )
        eventFlow.update { CustomFieldEvent.Close }
    }
}
