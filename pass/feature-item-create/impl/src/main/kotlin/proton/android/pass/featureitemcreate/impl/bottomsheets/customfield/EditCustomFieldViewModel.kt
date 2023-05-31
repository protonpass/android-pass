package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@HiltViewModel
class EditCustomFieldViewModel @Inject constructor(
    private val draftRepository: DraftRepository,
    private val savedStateHandle: SavedStateHandleProvider
) : ViewModel() {

    private val index = savedStateHandle.get().require<Int>(CustomFieldIndexNavArgId.key)
    private val title = savedStateHandle.get().require<String>(CustomFieldTitleNavArgId.key).let {
        NavParamEncoder.decode(it)
    }

    private val eventStateFlow: MutableStateFlow<EditCustomFieldEvent> =
        MutableStateFlow(EditCustomFieldEvent.Unknown)

    val eventState: StateFlow<EditCustomFieldEvent> = eventStateFlow

    fun onEdit() {
        eventStateFlow.update { EditCustomFieldEvent.EditField(index = index, title = title) }
    }

    fun onRemove() {
        draftRepository.save(DRAFT_REMOVE_CUSTOM_FIELD_KEY, index)
        eventStateFlow.update { EditCustomFieldEvent.RemovedField }
    }
}

sealed interface EditCustomFieldEvent {
    object Unknown : EditCustomFieldEvent
    data class EditField(val index: Int, val title: String) : EditCustomFieldEvent
    object RemovedField : EditCustomFieldEvent
}
