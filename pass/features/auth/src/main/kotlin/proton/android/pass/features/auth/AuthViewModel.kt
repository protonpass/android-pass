/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.auth

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.Account
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
import proton.android.pass.biometry.UnlockMethod
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
import proton.android.pass.features.auth.AuthSnackbarMessage.AuthExtraPasswordError
import proton.android.pass.features.auth.AuthSnackbarMessage.AuthTooManyAttemptsError
import proton.android.pass.features.auth.PinConstants.MAX_PIN_ATTEMPTS
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.AppLockTypePreference
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
    private val encryptionContextProvider: EncryptionContextProvider,
    private val authWithExtraPassword: AuthWithExtraPassword,
    private val checkLocalExtraPassword: CheckLocalExtraPassword,
    private val removeExtraPassword: RemoveExtraPassword,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val appDispatchers: AppDispatchers,
    hasExtraPassword: HasExtraPassword,
    observeUserEmail: ObserveUserEmail,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    internal val origin = savedStateHandleProvider.get()
        .get<AuthOrigin>(AuthOriginNavArgId.key)
        ?: AuthOrigin.AUTO_LOCK
    internal val userId: Option<UserId> = savedStateHandleProvider.get()
        .get<String>(CommonNavArgId.UserId.key)
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

    private val accountSwitcherFlow: Flow<AccountSwitcherState> =
        if (origin == AuthOrigin.AUTO_LOCK) {
            combine(
                accountManager.getAccounts(AccountState.Ready),
                accountManager.getPrimaryAccount()
            ) { accounts: List<Account>, primaryAccount: Account? ->
                val orderedAccounts = orderAccountsByPrimary(accounts, primaryAccount)
                AccountSwitcherState(orderedAccounts.toPersistentMap())
            }
        } else {
            flowOf(AccountSwitcherState(persistentMapOf()))
        }

    private val currentUserId = combine(
        flowOf(userId.value()),
        accountManager.getPrimaryAccount().map { it?.userId }
    ) { userId, primaryAccountUserId ->
        userId ?: primaryAccountUserId
    }.filterNotNull()

    val state: StateFlow<AuthState> = combineN(
        eventFlow,
        formContentFlow,
        currentUserId.flatMapLatest { observeUserEmail(it).asLoadingResult() },
        authMethodFlow,
        currentUserId.mapLatest(hasExtraPassword::invoke).asLoadingResult(),
        accountSwitcherFlow,
        currentUserId,
        internalSettingsRepository.getPinAttemptsCount(),
        currentUserId.flatMapLatest(::getRemainingPasswordAttempts)
    ) { event, formContent, userEmail, authMethod, hasExtraPassword,
        accountSwitcherState, currentUserId, pinAttemptsCount, remainingPasswordAttemptCount ->
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
                remainingPasswordAttempts = remainingPasswordAttemptCount,
                passwordError = formContent.passwordError,
                address = address,
                authMethod = authMethod,
                accountSwitcherState = accountSwitcherState,
                showExtraPassword = shouldShowExtraPassword(hasExtraPassword),
                showPinOrBiometry = pinAttemptsCount < MAX_PIN_ATTEMPTS &&
                    origin == AuthOrigin.AUTO_LOCK,
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

    private fun getRemainingPasswordAttempts(userId: UserId): Flow<Option<Int>> =
        internalSettingsRepository.getMasterPasswordAttemptsCount(userId)
            .map(::calculateRemainingAttempts)

    private fun calculateRemainingAttempts(attempts: Int): Option<Int> {
        val remainingAttempts = MAX_WRONG_PASSWORD_ATTEMPTS - attempts
        return if (remainingAttempts > 0 && remainingAttempts != MAX_WRONG_PASSWORD_ATTEMPTS) {
            remainingAttempts.toOption()
        } else {
            None
        }
    }

    private suspend fun orderAccountsByPrimary(accounts: List<Account>, primaryAccount: Account?) =
        primaryAccount?.let { primary ->
            listOf(primary) + accounts.filter { it.userId != primary.userId }
        }?.associate { account ->
            account.userId to createAccountItem(account, primaryAccount)
        } ?: accounts.associate { account ->
            account.userId to createAccountItem(account, primaryAccount)
        }

    private suspend fun createAccountItem(
        account: Account,
        primaryAccount: Account?
    ): proton.android.pass.features.auth.AccountItem {
        val email = userManager.getUser(account.userId).email.orEmpty()
        val isPrimary = primaryAccount?.userId == account.userId
        return proton.android.pass.features.auth.AccountItem(
            email = email,
            isPrimary = isPrimary
        )
    }

    fun onAccountSwitch(userId: UserId) = viewModelScope.launch {
        accountManager.setAsPrimary(userId)
    }

    private fun shouldShowExtraPassword(hasExtraPassword: LoadingResult<Boolean>) = when (hasExtraPassword) {
        is LoadingResult.Error ->
            if (hasExtraPassword.exception is UserIdNotAvailableError &&
                origin == AuthOrigin.EXTRA_PASSWORD_LOGIN
            ) {
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
                passwordError = None
            )
        }
    }

    fun onSubmit(hasExtraPassword: Boolean) {
        val password = formContentFlow.value.password
        if (isPasswordEmpty(password)) return
        formContentFlow.update {
            it.copy(
                isPasswordVisible = false, // Hide password on submit by default
                isLoadingState = IsLoadingState.Loading,

                // Hide errors by default
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
            val userId = currentUserId.firstOrNull() ?: throw UserIdNotAvailableError()
            storeAuthSuccessful(UnlockMethod.Password(userId))
            updateAuthEventFlow(AuthEvent.Success(origin))
        }
    }


    private suspend fun removeExtraPasswordOnAuthenticated() {
        runCatching { removeExtraPassword(userId.value()) }
            .onSuccess {
                PassLogger.i(TAG, "Removed extra password successfully")
                val userId = currentUserId.firstOrNull() ?: throw UserIdNotAvailableError()
                storeAuthSuccessful(UnlockMethod.Password(userId))
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
                snackbarDispatcher(AuthTooManyAttemptsError)
                withContext(appDispatchers.default) {
                    delay(WRONG_PASSWORD_DELAY_SECONDS)
                }
                state.value.content.userId.value()?.let {
                    updateAuthEventFlow(AuthEvent.ForceSignOut(it))
                }
            }

            is WrongExtraPasswordException -> {
                withContext(appDispatchers.default) {
                    delay(WRONG_PASSWORD_DELAY_SECONDS)
                }
                formContentFlow.update {
                    it.copy(passwordError = PasswordError.IncorrectPassword.some())
                }
                PassLogger.w(TAG, "Wrong extra password")
            }

            is WrongLocalCheckExtraPasswordException -> {
                PassLogger.w(TAG, "Wrong local extra password")
                val userId = currentUserId.firstOrNull() ?: throw UserIdNotAvailableError()
                val currentFailedAttempts = internalSettingsRepository
                    .getMasterPasswordAttemptsCount(userId)
                    .first()
                val remainingAttempts = MAX_WRONG_PASSWORD_ATTEMPTS - currentFailedAttempts - 1
                if (remainingAttempts <= 0) {
                    snackbarDispatcher(AuthTooManyAttemptsError)
                    withContext(appDispatchers.default) {
                        delay(WRONG_PASSWORD_DELAY_SECONDS)
                    }
                    setIncorrectPasswordData(userId, currentFailedAttempts)
                    updateAuthEventFlow(AuthEvent.ForceSignOut(userId))
                } else {
                    withContext(appDispatchers.default) {
                        delay(WRONG_PASSWORD_DELAY_SECONDS)
                    }
                    setIncorrectPasswordData(userId, currentFailedAttempts)
                }
            }

            else -> {
                PassLogger.w(TAG, "Error performing authentication")
                PassLogger.w(TAG, err)
                snackbarDispatcher(AuthExtraPasswordError)
            }
        }
    }

    private fun setIncorrectPasswordData(userId: UserId, currentFailedAttempts: Int) {
        internalSettingsRepository.setMasterPasswordAttemptsCount(
            userId = userId,
            count = currentFailedAttempts + 1
        )
        formContentFlow.update {
            it.copy(passwordError = PasswordError.IncorrectPassword.some())
        }
    }

    private fun isPasswordEmpty(password: String): Boolean = if (password.isEmpty()) {
        formContentFlow.update {
            it.copy(passwordError = PasswordError.EmptyPassword.some())
        }
        true
    } else {
        false
    }

    private fun submitMasterPassword(password: String) {
        viewModelScope.launch {
            runCatching { checkMasterPassword(password = password.encodeToByteArray()) }
                .onSuccess { isPasswordRight ->
                    if (isPasswordRight) {
                        onAuthenticatedWithMasterPassword()
                    } else {
                        val userId = currentUserId.firstOrNull() ?: throw UserIdNotAvailableError()
                        val currentFailedAttempts = internalSettingsRepository
                            .getMasterPasswordAttemptsCount(userId)
                            .first()
                        val remainingAttempts =
                            MAX_WRONG_PASSWORD_ATTEMPTS - currentFailedAttempts - 1
                        if (remainingAttempts <= 0) {
                            snackbarDispatcher(AuthTooManyAttemptsError)
                            PassLogger.w(TAG, "Too many wrong attempts, logging user out")
                            withContext(appDispatchers.default) {
                                delay(WRONG_PASSWORD_DELAY_SECONDS)
                            }
                            internalSettingsRepository.setMasterPasswordAttemptsCount(
                                userId = userId,
                                count = currentFailedAttempts + 1
                            )
                            updateAuthEventFlow(AuthEvent.ForceSignOut(userId))
                        } else {
                            withContext(appDispatchers.default) {
                                delay(WRONG_PASSWORD_DELAY_SECONDS)
                            }
                            formContentFlow.update {
                                it.copy(passwordError = PasswordError.IncorrectPassword.some())
                            }
                            internalSettingsRepository.setMasterPasswordAttemptsCount(
                                userId = userId,
                                count = currentFailedAttempts + 1
                            )
                            PassLogger.i(
                                TAG,
                                "Wrong password. Remaining attempts: $remainingAttempts"
                            )
                        }
                    }
                }
                .onFailure { err ->
                    PassLogger.w(TAG, "Error checking master password")
                    PassLogger.w(TAG, err)
                }
            formContentFlow.update { it.copy(isLoadingState = IsLoadingState.NotLoading) }
        }
    }

    private fun onAuthenticatedWithMasterPassword() {
        viewModelScope.launch {
            val userId = currentUserId.firstOrNull() ?: throw UserIdNotAvailableError()
            storeAuthSuccessful(UnlockMethod.Password(userId))
        }
        formContentFlow.update { it.copy(password = "", isPasswordVisible = false) }
        updateAuthEventFlow(AuthEvent.Success(origin))
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
        val canUseAlternative =
            internalSettingsRepository.getPinAttemptsCount().first() < MAX_PIN_ATTEMPTS
        if (canUseAlternative) {
            val newAuthEvent = when (preferenceRepository.getAppLockTypePreference().first()) {
                AppLockTypePreference.None -> AuthEvent.Unknown
                AppLockTypePreference.Biometrics -> AuthEvent.EnterBiometrics
                AppLockTypePreference.Pin -> AuthEvent.EnterPin(origin)
            }
            updateAuthEventFlow(newAuthEvent)
        }
    }

    internal fun clearEvent() = viewModelScope.launch {
        updateAuthEventFlow(AuthEvent.Unknown)
    }

    internal fun onBiometricsRequired(contextHolder: ClassHolder<Context>) = viewModelScope.launch {
        when (biometryManager.getBiometryStatus()) {
            BiometryStatus.NotAvailable,
            BiometryStatus.NotEnrolled -> {}

            BiometryStatus.CanAuthenticate -> {
                val biometricLockState = preferenceRepository.getAppLockState().first()
                when (biometricLockState) {
                    AppLockState.Enabled -> openBiometrics(contextHolder)
                    AppLockState.Disabled -> updateAuthEventFlow(AuthEvent.Success(origin))
                }
            }
        }
    }

    private suspend fun openBiometrics(contextHolder: ClassHolder<Context>) {
        PassLogger.i(TAG, "Launching Biometry")
        biometryManager.launch(contextHolder, BiometryType.AUTHENTICATE)
            .collect { result ->
                PassLogger.i(TAG, "Biometry result: $result")
                when (result) {
                    BiometryResult.Success -> {
                        storeAuthSuccessful(UnlockMethod.PinOrBiometrics)
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
        val passwordError: Option<PasswordError> = None
    )

    companion object {
        private const val TAG = "AuthViewModel"

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val WRONG_PASSWORD_DELAY_SECONDS = 2000L

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        const val MAX_WRONG_PASSWORD_ATTEMPTS = 3
    }
}
