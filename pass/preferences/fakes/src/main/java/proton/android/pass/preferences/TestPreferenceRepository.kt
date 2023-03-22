package proton.android.pass.preferences

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class TestPreferenceRepository @Inject constructor() : UserPreferencesRepository {

    private val biometricLockState = MutableSharedFlow<BiometricLockState>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    private val themePreference = MutableSharedFlow<ThemePreference>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    private val hasAuthenticated = MutableSharedFlow<HasAuthenticated>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    private val hasCompletedOnBoarding = MutableSharedFlow<HasCompletedOnBoarding>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    private val hasDismissedAutofillBanner = MutableSharedFlow<HasDismissedAutofillBanner>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    private val copyTotpToClipboard = MutableSharedFlow<CopyTotpToClipboard>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    private val clearClipboardPreference = MutableSharedFlow<ClearClipboardPreference>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )

    override suspend fun setBiometricLockState(state: BiometricLockState): Result<Unit> {
        biometricLockState.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getBiometricLockState(): Flow<BiometricLockState> = biometricLockState

    override suspend fun setHasAuthenticated(state: HasAuthenticated): Result<Unit> {
        hasAuthenticated.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getHasAuthenticated(): Flow<HasAuthenticated> = hasAuthenticated

    override suspend fun setHasCompletedOnBoarding(state: HasCompletedOnBoarding): Result<Unit> {
        hasCompletedOnBoarding.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getHasCompletedOnBoarding(): Flow<HasCompletedOnBoarding> = hasCompletedOnBoarding

    override suspend fun setThemePreference(theme: ThemePreference): Result<Unit> {
        themePreference.tryEmit(theme)
        return Result.success(Unit)
    }

    override fun getThemePreference(): Flow<ThemePreference> = themePreference

    override suspend fun setHasDismissedAutofillBanner(state: HasDismissedAutofillBanner): Result<Unit> {
        hasDismissedAutofillBanner.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getHasDismissedAutofillBanner(): Flow<HasDismissedAutofillBanner> =
        hasDismissedAutofillBanner

    override suspend fun setCopyTotpToClipboardEnabled(state: CopyTotpToClipboard): Result<Unit> {
        copyTotpToClipboard.tryEmit(state)
        return Result.success(Unit)
    }

    override fun getCopyTotpToClipboardEnabled(): Flow<CopyTotpToClipboard> = copyTotpToClipboard

    override suspend fun setClearClipboardPreference(clearClipboard: ClearClipboardPreference): Result<Unit> {
        clearClipboardPreference.tryEmit(clearClipboard)
        return Result.success(Unit)
    }

    override fun getClearClipboardPreference(): Flow<ClearClipboardPreference> =
        clearClipboardPreference

    override suspend fun clearPreferences(): Result<Unit> = Result.success(Unit)

}
