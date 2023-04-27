package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import proton.android.pass.common.api.FlowUtils.testFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestPreferenceRepository @Inject constructor() : UserPreferencesRepository {

    private val biometricLockState = testFlow<BiometricLockState>()
    private val themePreference = MutableStateFlow(ThemePreference.Dark)

    private val hasAuthenticated = testFlow<HasAuthenticated>()
    private val hasCompletedOnBoarding = testFlow<HasCompletedOnBoarding>()
    private val hasDismissedAutofillBanner = testFlow<HasDismissedAutofillBanner>()
    private val copyTotpToClipboard = testFlow<CopyTotpToClipboard>()
    private val clearClipboardPreference = testFlow<ClearClipboardPreference>()
    private val useFaviconsPreference = testFlow<UseFaviconsPreference>()
    private val lockAppPreference = testFlow<AppLockPreference>()

    override suspend fun setBiometricLockState(state: BiometricLockState): Result<Unit> {
        biometricLockState.emit(state)
        return Result.success(Unit)
    }

    override fun getBiometricLockState(): Flow<BiometricLockState> = biometricLockState

    override suspend fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> {
        hasAuthenticated.emit(state)
        return Result.success(Unit)
    }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> = hasAuthenticated

    override suspend fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> {
        hasCompletedOnBoarding.emit(state)
        return Result.success(Unit)
    }

    override fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding> = hasCompletedOnBoarding

    override suspend fun setThemePreference(theme: ThemePreference): Result<Unit> {
        themePreference.emit(theme)
        return Result.success(Unit)
    }

    override fun getThemePreference(): Flow<ThemePreference> = themePreference

    override suspend fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> {
        hasDismissedAutofillBanner.emit(state)
        return Result.success(Unit)
    }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> =
        hasDismissedAutofillBanner

    override suspend fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> {
        copyTotpToClipboard.emit(state)
        return Result.success(Unit)
    }

    override fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard> = copyTotpToClipboard

    override suspend fun setClearClipboardPreference(clearClipboard: ClearClipboardPreference): Result<Unit> {
        clearClipboardPreference.emit(clearClipboard)
        return Result.success(Unit)
    }

    override fun getClearClipboardPreference(): Flow<ClearClipboardPreference> =
        clearClipboardPreference

    override suspend fun setUseFaviconsPreference(useFavicons: UseFaviconsPreference): Result<Unit> {
        useFaviconsPreference.emit(useFavicons)
        return Result.success(Unit)
    }

    override fun getUseFaviconsPreference(): Flow<UseFaviconsPreference> = useFaviconsPreference

    override suspend fun setAppLockPreference(preference: AppLockPreference): Result<Unit> {
        lockAppPreference.emit(preference)
        return Result.success(Unit)
    }

    override fun getAppLockPreference(): Flow<AppLockPreference> = lockAppPreference

    override suspend fun clearPreferences(): Result<Unit> = Result.success(Unit)

}
