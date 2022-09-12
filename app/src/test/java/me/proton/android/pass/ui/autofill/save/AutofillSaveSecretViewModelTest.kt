package me.proton.android.pass.ui.autofill.save

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import me.proton.android.pass.log.e
import me.proton.android.pass.ui.autofill.save.AutofillSaveSecretViewModel.State
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountDetails
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.autofill.service.entities.SecretSaveInfo
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue
import me.proton.core.pass.domain.usecases.AddSecret
import me.proton.core.pass.domain.usecases.GetAddressesForUserId
import me.proton.core.pass.domain.usecases.ObserveAccounts
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.util.kotlin.Logger
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AutofillSaveSecretViewModelTest {

    lateinit var viewModel: AutofillSaveSecretViewModel
    lateinit var getAddressesForUserId: GetAddressesForUserId
    lateinit var addSecret: AddSecret
    lateinit var observeAccounts: ObserveAccounts
    lateinit var accountsFlow: MutableStateFlow<List<Account>>
    lateinit var logger: Logger

    init {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

    @Before
    fun setup() {
        addSecret = mockk()
        getAddressesForUserId = mockk()
        observeAccounts = mockk()
        logger = mockk()
        accountsFlow = MutableStateFlow(
            listOf(
                Account(
                    UserId("user_id"),
                    "Username",
                    "email",
                    AccountState.Ready,
                    sessionId = null,
                    sessionState = null,
                    AccountDetails(null, null)
                )
            )
        )

        every { logger.e(any()) }.answers { }
        coEvery { observeAccounts() }.answers { accountsFlow.asStateFlow() }
        coEvery { getAddressesForUserId(any()) }.answers {
            val userId = firstArg<UserId>()
            val address = if (userId.id == "user_id") {
                makeUserAddress(userId)
            } else {
                makeUserAddress(userId, addressId = AddressId("other_address_id"))
            }
            listOf(address)
        }
        coEvery { addSecret(any(), any(), any(), any(), any(), any()) }.answers {}

        viewModel = AutofillSaveSecretViewModel(
            addSecret,
            getAddressesForUserId,
            observeAccounts
        )
    }

    @Test
    fun `save fun calls addAddress use case`() = runBlockingTest {
        val address = makeUserAddress()
        val secretInfo = makeSecretInfo()
        viewModel.save(address, secretInfo)

        coVerify { addSecret(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `save fun sets state to Success state`() = runBlockingTest {
        val address = makeUserAddress()
        val secretInfo = makeSecretInfo()

        viewModel.save(address, secretInfo)

        coVerify { addSecret(any(), any(), any(), any(), any(), any()) }
        Assert.assertTrue(viewModel.state.value is State.Success)
    }

    @Test
    fun `Exceptions are handled in save`() = runBlockingTest {
        val address = makeUserAddress()
        val secretInfo = makeSecretInfo()
        coEvery { addSecret(any(), any(), any(), any(), any(), any()) }
            .throws(IllegalStateException("Some exception"))

        viewModel.save(address, secretInfo)
    }

    private fun makeSecretInfo() = SecretSaveInfo(
        "Some secret",
        "some.package.name",
        SecretType.Email,
        SecretValue.Single("jorge.martin@proton.me")
    )

    private fun makeUserAddress(
        userId: UserId = UserId("user_id"),
        addressId: AddressId = AddressId("address_id"),
        email: String = "jorge.martin@proton.me",
        canSend: Boolean = true,
        canReceive: Boolean = true,
        enabled: Boolean = true,
        order: Int = 0,
        keys: List<UserAddressKey> = emptyList()
    ) = UserAddress(
        userId,
        addressId,
        email,
        canSend = canSend,
        canReceive = canReceive,
        enabled = enabled,
        order = order,
        keys = keys,
        signedKeyList = null
    )
}
