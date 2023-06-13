package proton.android.pass.featurefeatureflags.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val ffRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {

    val state = ffRepository.get<Boolean>(FeatureFlag.CUSTOM_FIELDS_ENABLED)
        .map { mapOf(FeatureFlag.CUSTOM_FIELDS_ENABLED to it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun <T> override(featureFlag: FeatureFlag, value: T) = viewModelScope.launch {
        ffRepository.set(featureFlag = featureFlag, value = value)
    }
}
