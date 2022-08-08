package me.proton.android.pass.ui.create.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ItemId

abstract class BaseNoteViewModel : ViewModel() {

    val initialViewState = ViewState(
        state = State.Idle,
        modelState = ModelState(
            title = "",
            note = ""
        )
    )
    val viewState: MutableStateFlow<ViewState> = MutableStateFlow(initialViewState)

    fun onTitleChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(title = value))
    }

    fun onNoteChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(note = value))
    }

    data class ModelState(
        val title: String,
        val note: String
    ) {
        fun toItemContents(): ItemContents {
            return ItemContents.Note(
                title = title,
                note = note,
            )
        }
    }

    data class ViewState(
        val state: State,
        val modelState: ModelState
    )

    sealed class State {
        object Loading : State()
        object Idle : State()
        data class Error(val message: String) : State()
        data class Success(val itemId: ItemId) : State()
    }
}
