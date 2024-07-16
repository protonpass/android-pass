/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featureauth.impl

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.BiometryType
import proton.android.pass.biometry.StoreAuthSuccessful
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.errors.WrongExtraPasswordException
import proton.android.pass.data.api.usecases.CheckMasterPassword
import proton.android.pass.data.api.usecases.ObserveUserEmail
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.CheckLocalExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.HasExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.RemoveExtraPassword
import proton.android.pass.featureauth.impl.AuthSnackbarMessage.AuthExtraPasswordError
import proton.android.pass.featureauth.impl.AuthSnackbarMessage.AuthExtraPasswordTooManyAttemptsError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.UserIdNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val preferenceRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val checkMasterPassword: CheckMasterPassword,
    private val storeAuthSuccessful: StoreAuthSuccessful,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val appDispatchers: AppDispatchers,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val authWithExtraPassword: AuthWithExtraPassword,
    private val checkLocalExtraPassword: CheckLocalExtraPassword,
    private val removeExtraPassword: RemoveExtraPassword,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    hasExtraPassword: HasExtraPassword,
    observeUserEmail: ObserveUserEmail,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    internal val origin = savedStateHandleProvider.get()
        .get<AuthOrigin>(AuthOriginNavArgId.key)
        ?: AuthOrigin.AUTO_LOCK
    internal val userId: Option<UserId> = savedStateHandleProvider.get()
        .get<String>(UserIdNavArgId.key)
        ?.let(::UserId)
        .let(UserId?::toOption)

    private val eventFlow: MutableStateFlow<Option<AuthEvent>> = MutableStateFlow(None)
    private val formContentFlow: MutableStateFlow<FormContents> = MutableStateFlow(FormContents())

    private val authMethodFlow: Flow<Option<AuthMethod>> = preferenceRepository
        .getAppLockTypePreference()
        .map {
            when (it) {
                AppLockTypePreference.None -> None
                AppLockTypePreference.Biometrics -> AuthMethod.Fingerprint.some()
                AppLockTypePreference.Pin -> AuthMethod.Pin.some()
            }
        }
        .distinctUntilChanged()

    private val accountSwitcherFlow = combine(
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.ACCOUNT_SWITCH_V1)
            .map { it && origin == AuthOrigin.AUTO_LOCK },
        combine(
            accountManager.getAccounts(AccountState.Ready),
            accountManager.getPrimaryAccount()
        ) { accounts, primaryAccount ->
            accounts.associate { account ->
                account.userId to AccountItem(
                    email = userManager.getUser(account.userId).email.orEmpty(),
                    isPrimary = primaryAccount?.userId == account.userId
                )
            }.toPersistentMap()
        },
        ::AccountSwitcherState
    )

    private val currentUserId = combine(
        flowOf(userId.value()),
        accountManager.getPrimaryAccount().map { it?.userId }
    ) { userId, primaryAccountUserId ->
        userId ?: primaryAccountUserId
    }

    val state: StateFlow<AuthState> = combineN(
        eventFlow,
        formContentFlow,
        currentUserId.flatMapLatest { observeUserEmail(it).asLoadingResult() },
        authMethodFlow,
        currentUserId.mapLatest(hasExtraPassword::invoke).asLoadingResult(),
        accountSwitcherFlow,
        currentUserId
    ) { event, formContent, userEmail, authMethod, hasExtraPassword, accountSwitcherState, currentUserId ->
        val address = when (userEmail) {
            LoadingResult.Loading -> None
            is LoadingResult.Error -> {
                PassLogger.w(TAG, "Error loading userEmail")
                PassLogger.w(TAG, userEmail.exception)
                None
            }

            is LoadingResult.Success -> userEmail.data.some()
        }

        AuthState(
            event = event,
            content = AuthStateContent(
                userId = currentUserId.toOption(),
                password = formContent.password,
                isLoadingState = formContent.isLoadingState,
                isPasswordVisible = formContent.isPasswordVisible,
                error = formContent.error,
                passwordError = formContent.passwordError,
                address = address,
                authMethod = authMethod,
                accountSwitcherState = accountSwitcherState,
                showExtraPassword = shouldShowExtraPassword(hasExtraPassword),
                showPinOrBiometry = origin == AuthOrigin.AUTO_LOCK,
                showLogout = origin != AuthOrigin.EXTRA_PASSWORD_LOGIN,
                showBackNavigation = origin == AuthOrigin.EXTRA_PASSWORD_CONFIGURE ||
                    origin == AuthOrigin.EXTRA_PASSWORD_REMOVE
            )
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AuthState.Initial
        )

    fun onAccountSwitch(userId: UserId) = viewModelScope.launch {
        accountManager.setAsPrimary(userId)
    }

    private fun shouldShowExtraPassword(hasExtraPassword: LoadingResult<Boolean>) = when (hasExtraPassword) {
        is LoadingResult.Error ->
            if (hasExtraPassword.exception is UserIdNotAvailableError && origin == AuthOrigin.EXTRA_PASSWORD_LOGIN) {
                LoadingResult.Success(true)
            } else {
                hasExtraPassword
            }

        LoadingResult.Loading -> LoadingResult.Loading
        is LoadingResult.Success -> when (origin) {
            AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY,
            AuthOrigin.AUTO_LOCK -> hasExtraPassword

            AuthOrigin.EXTRA_PASSWORD_CONFIGURE -> LoadingResult.Success(false)
            AuthOrigin.EXTRA_PASSWORD_LOGIN,
            AuthOrigin.EXTRA_PASSWORD_REMOVE -> LoadingResult.Success(true)
        }
    }

    fun onPasswordChanged(value: String) = viewModelScope.launch {
        formContentFlow.update {
            it.copy(
                password = value,

                // Hide errors on password change
                passwordError = None,
                error = None
            )
        }
    }

    fun onSubmit(hasExtraPassword: Boolean) {
        val password = formContentFlow.value.password
        if (!isPasswordValid(password)) return
        formContentFlow.update {
            it.copy(
                isPasswordVisible = false, // Hide password on submit by default
                isLoadingState = IsLoadingState.Loading,

                // Hide errors by default
                error = None,
                passwordError = None
            )
        }
        if (hasExtraPassword) {
            submitExtraPassword(password)
        } else {
            submitMasterPassword(password)
        }
    }

    private fun submitExtraPassword(password: String) {
        viewModelScope.launch {
            runCatching {
                val encryptedPassword = encryptionContextProvider.withEncryptionContext {
                    encrypt(password)
                }
                if (origin == AuthOrigin.AUTO_LOCK) {
                    val isPasswordRight = checkLocalExtraPassword(
                        userId = userId.value(),
                        password = encryptedPassword
                    )
                    if (!isPasswordRight) throw WrongLocalCheckExtraPasswordException()
                } else {
                    authWithExtraPassword(
                        userId = userId.value(),
                        password = encryptedPassword
                    )
                }
            }
                .onSuccess { onAuthenticatedWithExtraPasswordSuccess() }
                .onFailure { onAuthenticatedWithExtraPasswordFailed(it) }
            formContentFlow.update { it.copy(isLoadingState = IsLoadingState.NotLoading) }
        }
    }

    private suspend fun onAuthenticatedWithExtraPasswordSuccess() {
        PassLogger.i(TAG, "Extra password success")
        if (origin == AuthOrigin.EXTRA_PASSWORD_REMOVE) {
            removeExtraPasswordOnAuthenticated()
        } else {
            storeAuthSuccessful()
            updateAuthEventFlow(AuthEvent.Success(origin))
        }
    }


    private suspend fun removeExtraPasswordOnAuthenticated() {
        runCatching { removeExtraPassword(userId.value()) }
            .onSuccess {
                PassLogger.i(TAG, "Removed extra password successfully")
                storeAuthSuccessful()
                updateAuthEventFlow(AuthEvent.Success(origin))
            }
            .onFailure { err ->
                PassLogger.w(TAG, "Error removing extra password")
                PassLogger.w(TAG, err)
                snackbarDispatcher(AuthExtraPasswordError)
            }
    }

    private suspend fun onAuthenticatedWithExtraPasswordFailed(err: Throwable) {
        when (err) {
            is TooManyExtraPasswordAttemptsException -> {
                PassLogger.w(TAG, "Too many attempts")
                snackbarDispatcher(AuthExtraPasswordTooManyAttemptsError)
                delay(WRONG_PASSWORD_DELAY_SECONDS)
                state.value.content.userId.value()?.let {
                    updateAuthEventFlow(AuthEvent.ForceSignOut(it))
                }
            }

            is WrongExtraPasswordException -> {
                PassLogger.w(TAG, "Wrong extra password")
                formContentFlow.update {
                    it.copy(error = AuthError.WrongPassword(None).some())
                }
            }

            is WrongLocalCheckExtraPasswordException -> {
                PassLogger.w(TAG, "Wrong local extra password")
                val remainingAttempts = incrementAttemptAndReturnRemaining()
                if (remainingAttempts <= 0) {
                    snackbarDispatcher(AuthExtraPasswordTooManyAttemptsError)
                    delay(WRONG_PASSWORD_DELAY_SECONDS)
                    state.value.content.userId.value()?.let {
                        updateAuthEventFlow(AuthEvent.ForceSignOut(it))
                    }
                } else {
                    formContentFlow.update {
                        it.copy(error = AuthError.WrongPassword(remainingAttempts.some()).some())
                    }
                }
            }

            else -> {
                PassLogger.w(TAG, "Error performing authentication")
                PassLogger.w(TAG, err)
                snackbarDispatcher(AuthExtraPasswordError)
            }
        }
    }

    private fun isPasswordValid(password: String): Boolean = if (password.isEmpty()) {
        formContentFlow.update {
            it.copy(passwordError = PasswordError.EmptyPassword.some())
        }
        false
    } else {
        true
    }

    private fun submitMasterPassword(password: String) {
        viewModelScope.launch {
            runCatching { checkMasterPassword(password = password.encodeToByteArray()) }
                .onSuccess { isPasswordRight ->
                    if (isPasswordRight) {
                        onAuthenticatedWithMasterPassword()
                    } else {
                        withContext(appDispatchers.default) {
                            delay(WRONG_PASSWORD_DELAY_SECONDS)
                        }

                        val remainingAttempts = incrementAttemptAndReturnRemaining()

                        if (remainingAttempts <= 0) {
                            PassLogger.w(TAG, "Too many wrong attempts, logging user out")
                            state.value.content.userId.value()?.let {
                                updateAuthEventFlow(AuthEvent.ForceSignOut(it))
                            }
                        } else {
                            PassLogger.i(
                                TAG,
                                "Wrong password. Remaining attempts: $remainingAttempts"
                            )
                            formContentFlow.update {
                                it.copy(
                                    error = AuthError.WrongPassword(remainingAttempts.some()).some()
                                )
                            }
                        }
                    }
                }
                .onFailure { err ->
                    PassLogger.w(TAG, "Error checking master password")
                    PassLogger.w(TAG, err)
                    formContentFlow.update { it.copy(error = AuthError.UnknownError.some()) }
                }
            formContentFlow.update { it.copy(isLoadingState = IsLoadingState.NotLoading) }
        }
    }

    private fun onAuthenticatedWithMasterPassword() {
        storeAuthSuccessful()
        formContentFlow.update { it.copy(password = "", isPasswordVisible = false) }
        updateAuthEventFlow(AuthEvent.Success(origin))
    }

    private suspend fun incrementAttemptAndReturnRemaining(): Int {
        val currentFailedAttempts = internalSettingsRepository
            .getMasterPasswordAttemptsCount()
            .first()

        internalSettingsRepository.setMasterPasswordAttemptsCount(
            currentFailedAttempts + 1
        )
        return MAX_WRONG_PASSWORD_ATTEMPTS - currentFailedAttempts - 1
    }

    fun onSignOut() = viewModelScope.launch {
        state.value.content.userId.value()?.let {
            updateAuthEventFlow(AuthEvent.SignOut(it))
        }
    }

    fun onTogglePasswordVisibility(value: Boolean) = viewModelScope.launch {
        formContentFlow.update { it.copy(isPasswordVisible = value) }
    }

    internal fun onAuthMethodRequested() = viewModelScope.launch {
        if (origin != AuthOrigin.AUTO_LOCK) return@launch
        val newAuthEvent = when (preferenceRepository.getAppLockTypePreference().first()) {
            AppLockTypePreference.None -> AuthEvent.Unknown
            AppLockTypePreference.Biometrics -> AuthEvent.EnterBiometrics
            AppLockTypePreference.Pin -> AuthEvent.EnterPin(origin)
        }

        updateAuthEventFlow(newAuthEvent)
    }

    internal fun clearEvent() = viewModelScope.launch {
        updateAuthEventFlow(AuthEvent.Unknown)
    }

    internal fun onBiometricsRequired(contextHolder: ClassHolder<Context>) = viewModelScope.launch {
        val newAuthEvent = when (biometryManager.getBiometryStatus()) {
            BiometryStatus.NotAvailable,
            BiometryStatus.NotEnrolled -> AuthEvent.Success(origin)

            BiometryStatus.CanAuthenticate -> {
                val biometricLockState = preferenceRepository.getAppLockState().first()
                if (biometricLockState == AppLockState.Enabled) {
                    // If there is biometry available, and the user has it enabled, perform auth
                    openBiometrics(contextHolder)
                    return@launch
                }
                // If there is biometry available, but the user does not have it enabled
                // we should proceed
                AuthEvent.Success(origin)
            }
        }

        updateAuthEventFlow(newAuthEvent)
    }

    private suspend fun openBiometrics(contextHolder: ClassHolder<Context>) {
        PassLogger.i(TAG, "Launching Biometry")
        biometryManager.launch(contextHolder, BiometryType.AUTHENTICATE)
            .collect { result ->
                PassLogger.i(TAG, "Biometry result: $result")
                when (result) {
                    BiometryResult.Success -> {
                        formContentFlow.update { it.copy(password = "", isPasswordVisible = false) }
                        updateAuthEventFlow(AuthEvent.Success(origin))
                    }

                    is BiometryResult.Error -> {
                        PassLogger.w(TAG, "BiometryResult=Error: cause ${result.cause}")
                        when (result.cause) {
                            BiometryAuthError.Canceled,
                            BiometryAuthError.UserCanceled,
                            BiometryAuthError.NegativeButton -> {
                            }

                            else -> updateAuthEventFlow(AuthEvent.Failed)
                        }
                    }

                    // User can retry
                    BiometryResult.Failed -> {}

                    is BiometryResult.FailedToStart -> {
                        updateAuthEventFlow(AuthEvent.Failed)
                    }
                }
            }
    }

    private fun updateAuthEventFlow(newAuthEvent: AuthEvent) {
        eventFlow.update { newAuthEvent.some() }
    }

    private data class FormContents(
        val password: String = "",
        val isPasswordVisible: Boolean = false,
        val isLoadingState: IsLoadingState = IsLoadingState.NotLoading,
        val error: Option<AuthError> = None,
        val passwordError: Option<PasswordError> = None
    )

    companion object {
        private const val TAG = "AuthViewModel"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val WRONG_PASSWORD_DELAY_SECONDS = 2000L

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val MAX_WRONG_PASSWORD_ATTEMPTS = 5
    }
}
