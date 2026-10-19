/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.usecases.extrapassword

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.network.domain.session.SessionId
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.accountmanager.FakeSessionManager
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult
import proton.android.pass.data.fakes.usecases.FakeGetPrimaryUserId
import proton.android.pass.data.impl.fakes.FakeExtraPasswordRepository
import proton.android.pass.data.impl.fakes.FakeRemoteExtraPasswordDataSource
import proton.android.pass.data.impl.fakes.FakeSrpCrypto
import proton.android.pass.data.impl.requests.ExtraPasswordSendSrpDataRequest
import proton.android.pass.data.impl.responses.ExtraPasswordGetSrpData
import proton.android.pass.domain.UserId
import kotlin.test.assertFailsWith

class AuthWithExtraPasswordImplTest {

    private lateinit var srpCrypto: FakeSrpCrypto
    private lateinit var remoteDataSource: FakeRemoteExtraPasswordDataSource
    private lateinit var sessionManager: FakeSessionManager
    private lateinit var listener: AuthWithExtraPasswordListenerImpl
    private lateinit var getPrimaryUserId: FakeGetPrimaryUserId
    private lateinit var extraPasswordRepository: FakeExtraPasswordRepository
    private lateinit var instance: AuthWithExtraPasswordImpl

    @Before
    fun setup() {
        srpCrypto = FakeSrpCrypto(PROOFS)
        remoteDataSource = FakeRemoteExtraPasswordDataSource().apply { setAuthData(SRP_DATA) }
        sessionManager = FakeSessionManager().apply { setSessionId(USER_ID, SESSION_ID) }
        listener = AuthWithExtraPasswordListenerImpl()
        getPrimaryUserId = FakeGetPrimaryUserId()
        extraPasswordRepository = FakeExtraPasswordRepository()
        instance = AuthWithExtraPasswordImpl(
            srpCrypto = srpCrypto,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            remoteExtraPasswordDataSource = remoteDataSource,
            sessionManager = sessionManager,
            authWithExtraPasswordListener = listener,
            getPrimaryUserId = getPrimaryUserId,
            extraPasswordRepository = extraPasswordRepository
        )
    }

    @Test
    fun correctPasswordSendsProofsStoresKeyRefreshesScopesAndNotifiesSuccess() = runTest {
        val encryptedPassword = FakeEncryptionContext.encrypt(PASSWORD)

        instance(USER_ID, encryptedPassword)

        assertThat(srpCrypto.proofPasswords).containsExactly(PASSWORD)
        assertThat(remoteDataSource.sentAuthData).containsExactly(
            USER_ID to ExtraPasswordSendSrpDataRequest(
                clientEphemeral = PROOFS.clientEphemeral,
                clientProof = PROOFS.clientProof,
                srpSessionId = SRP_DATA.srpSessionId
            )
        )
        assertThat(extraPasswordRepository.getStoredAccessKey(USER_ID)).isEqualTo(encryptedPassword)
        assertThat(sessionManager.refreshedScopes).containsExactly(SESSION_ID)
        assertThat(listener.onAuthWithExtraPassword(USER_ID)).isEqualTo(AuthWithExtraPasswordResult.Success)
    }

    @Test
    fun tooManyAttemptsNotifiesFailureAndThrows() = runTest {
        remoteDataSource.setSendAuthDataResult(Result.failure(TooManyExtraPasswordAttemptsException()))

        assertFailsWith<TooManyExtraPasswordAttemptsException> {
            instance(USER_ID, FakeEncryptionContext.encrypt(PASSWORD))
        }

        assertThat(listener.onAuthWithExtraPassword(USER_ID)).isEqualTo(AuthWithExtraPasswordResult.Failure)
        assertThat(extraPasswordRepository.getStoredAccessKey(USER_ID)).isNull()
        assertThat(sessionManager.refreshedScopes).isEmpty()
    }

    @Test
    fun wrongPasswordThrowsAndKeepsLoginWaitingForRetry() = runTest {
        remoteDataSource.setSendAuthDataResult(Result.failure(IllegalStateException("Wrong password")))
        val waitingLogin = async { listener.onAuthWithExtraPassword(USER_ID) }
        runCurrent()

        assertFailsWith<IllegalStateException> {
            instance(USER_ID, FakeEncryptionContext.encrypt(PASSWORD))
        }
        runCurrent()

        assertThat(waitingLogin.isActive).isTrue()
        assertThat(extraPasswordRepository.getStoredAccessKey(USER_ID)).isNull()

        remoteDataSource.setSendAuthDataResult(Result.success(Unit))
        instance(USER_ID, FakeEncryptionContext.encrypt(PASSWORD))

        assertThat(waitingLogin.await()).isEqualTo(AuthWithExtraPasswordResult.Success)
    }

    @Test
    fun usesPrimaryUserWhenNoUserIdGiven() = runTest {
        getPrimaryUserId.setResult(USER_ID)

        instance(null, FakeEncryptionContext.encrypt(PASSWORD))

        assertThat(remoteDataSource.authDataRequests).containsExactly(USER_ID)
        assertThat(listener.onAuthWithExtraPassword(USER_ID)).isEqualTo(AuthWithExtraPasswordResult.Success)
    }

    @Test
    fun failsWhenNoUserIdGivenAndNoPrimaryUser() = runTest {
        getPrimaryUserId.setResult(null)

        assertFailsWith<IllegalStateException> {
            instance(null, FakeEncryptionContext.encrypt(PASSWORD))
        }

        assertThat(remoteDataSource.authDataRequests).isEmpty()
    }

    private companion object {
        const val PASSWORD = "extra-password"
        val USER_ID = UserId("user-id")
        val SESSION_ID = SessionId("session-id")
        val PROOFS = SrpProofs(
            clientEphemeral = "client-ephemeral",
            clientProof = "client-proof",
            expectedServerProof = "expected-server-proof"
        )
        val SRP_DATA = ExtraPasswordGetSrpData(
            modulus = "modulus",
            serverEphemeral = "server-ephemeral",
            srpSessionId = "srp-session-id",
            version = 4,
            srpSalt = "srp-salt"
        )
    }
}
