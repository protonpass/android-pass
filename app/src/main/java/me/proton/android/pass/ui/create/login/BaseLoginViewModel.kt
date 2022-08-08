package me.proton.android.pass.ui.create.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.proton.core.pass.domain.ItemContents
import me.proton.core.pass.domain.ItemId

abstract class BaseLoginViewModel : ViewModel() {

    val initialViewState = ViewState(
        state = State.Idle,
        modelState = ModelState(
            title = "",
            username = "",
            password = "",
            websiteAddresses = listOf(""),
            note = ""
        )
    )
    val viewState: MutableStateFlow<ViewState> = MutableStateFlow(initialViewState)

    fun onTitleChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(title = value))
    }

    fun onUsernameChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(username = value))
    }

    fun onPasswordChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(password = value))
    }

    fun onWebsiteChange(value: String, index: Int) = viewModelScope.launch {
        val addresses = viewState.value.modelState.websiteAddresses.toMutableList()
        addresses[index] = value

        viewState.value = viewState.value.copy(modelState = viewState.value.modelState.copy(websiteAddresses = addresses))
    }

    fun onAddWebsite() = viewModelScope.launch {
        val addresses = viewState.value.modelState.websiteAddresses.toMutableList()
        addresses.add("")

        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(websiteAddresses = addresses))
    }

    fun onRemoveWebsite(index: Int) = viewModelScope.launch {
        val addresses = viewState.value.modelState.websiteAddresses.toMutableList()
        addresses.removeAt(index)

        viewState.value = viewState.value.copy(modelState = viewState.value.modelState.copy(websiteAddresses = addresses))
    }

    fun onNoteChange(value: String) = viewModelScope.launch {
        viewState.value =
            viewState.value.copy(modelState = viewState.value.modelState.copy(note = value))
    }

    data class ModelState(
        val title: String,
        val username: String,
        val password: String,
        val websiteAddresses: List<String>,
        val note: String
    ) {
        fun toItemContents(): ItemContents {
            val addresses = websiteAddresses.filter { it.isNotEmpty() }
            return ItemContents.Login(
                title = title,
                note = note,
                username = username,
                password = password,
                urls = addresses
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
