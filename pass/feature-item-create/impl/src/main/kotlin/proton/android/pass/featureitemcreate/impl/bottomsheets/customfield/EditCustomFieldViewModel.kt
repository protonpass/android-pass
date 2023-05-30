package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import javax.inject.Inject

@HiltViewModel
class EditCustomFieldViewModel @Inject constructor(
    private val draftRepository: DraftRepository
) : ViewModel() {

    fun onRemove(index: Int) {
        draftRepository.save(DRAFT_REMOVE_CUSTOM_FIELD_KEY, index)
    }

}
