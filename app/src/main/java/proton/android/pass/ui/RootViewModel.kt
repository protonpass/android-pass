package proton.android.pass.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    fun getRoute(): RootNavigation {
        return runBlocking {
            val needsAuth = needsBiometricAuth().first()
            if (needsAuth) {
                return@runBlocking RootNavigation.Auth
            }
            val hasCompletedOnBoarding = preferenceRepository.getHasCompletedOnBoarding().first()
            if (!hasCompletedOnBoarding.value()) {
                return@runBlocking RootNavigation.OnBoarding
            }
            RootNavigation.Home
        }
    }
}
