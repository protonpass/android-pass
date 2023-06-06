package proton.android.pass.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestPreferenceRepository @Inject constructor() : UserPreferencesRepository {

    private val biometricLockState =
        MutableStateFlow<BiometricLockState>(BiometricLockState.Disabled)
    private val themePreference = MutableStateFlow(ThemePreference.Dark)

    private val hasAuthenticated =
        MutableStateFlow<HasAuthenticated>(HasAuthenticated.Authenticated)
    private val hasCompletedOnBoarding =
        MutableStateFlow<HasCompletedOnBoarding>(HasCompletedOnBoarding.Completed)
    private val hasDismissedAutofillBanner =
        MutableStateFlow<HasDismissedAutofillBanner>(HasDismissedAutofillBanner.Dismissed)
    private val hasDismissedTrialBanner =
        MutableStateFlow<HasDismissedTrialBanner>(HasDismissedTrialBanner.Dismissed)
    private val copyTotpToClipboard =
        MutableStateFlow<CopyTotpToClipboard>(CopyTotpToClipboard.NotEnabled)
    private val clearClipboardPreference = MutableStateFlow(ClearClipboardPreference.Never)
    private val useFaviconsPreference =
        MutableStateFlow<UseFaviconsPreference>(UseFaviconsPreference.Disabled)
    private val allowScreenshotsPreference =
        MutableStateFlow<AllowScreenshotsPreference>(AllowScreenshotsPreference.Disabled)
    private val lockAppPreference = MutableStateFlow(AppLockPreference.Never)
    private val passwordGenerationPreference = MutableStateFlow(
        PasswordGenerationPreference(
            mode = PasswordGenerationMode.Words,
            randomPasswordLength = 12,
            randomHasSpecialCharacters = false,
            randomHasCapitalLetters = false,
            randomIncludeNumbers = false,
            wordsCount = 4,
            wordsSeparator = WordSeparator.Hyphen,
            wordsCapitalise = false,
            wordsIncludeNumbers = false
        )
    )

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

    override suspend fun setHasDismissedTrialBanner(state: HasDismissedTrialBanner): Result<Unit> {
        hasDismissedTrialBanner.emit(state)
        return Result.success(Unit)
    }

    override fun getHasDismissedTrialBanner(): Flow<HasDismissedTrialBanner> =
        hasDismissedTrialBanner

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

    override suspend fun setPasswordGenerationPreference(
        preference: PasswordGenerationPreference
    ): Result<Unit> {
        passwordGenerationPreference.emit(preference)
        return Result.success(Unit)
    }

    override fun getPasswordGenerationPreference(): Flow<PasswordGenerationPreference> =
        passwordGenerationPreference

    override suspend fun setAllowScreenshotsPreference(
        preference: AllowScreenshotsPreference
    ): Result<Unit> {
        allowScreenshotsPreference.emit(preference)
        return Result.success(Unit)
    }

    override fun getAllowScreenshotsPreference(): Flow<AllowScreenshotsPreference> =
        allowScreenshotsPreference

    override suspend fun clearPreferences(): Result<Unit> = Result.success(Unit)

}
