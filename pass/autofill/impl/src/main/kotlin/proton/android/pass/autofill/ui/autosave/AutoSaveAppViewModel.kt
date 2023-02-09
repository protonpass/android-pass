package proton.android.pass.autofill.ui.autosave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class AutoSaveAppViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    val state = preferenceRepository.getThemePreference()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = runBlocking { preferenceRepository.getThemePreference().first() }
        )
}
