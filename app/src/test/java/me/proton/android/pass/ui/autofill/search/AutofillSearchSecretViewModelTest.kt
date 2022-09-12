package me.proton.android.pass.ui.autofill.search

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import me.proton.android.pass.log.e
import me.proton.android.pass.ui.autofill.search.AutofillSearchSecretViewModel.State
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue
import me.proton.core.pass.domain.usecases.GetAddressById
import me.proton.core.pass.domain.usecases.SearchSecretWithUri
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.util.kotlin.Logger
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AutofillSearchSecretViewModelTest {

    lateinit var viewModel: AutofillSearchSecretViewModel
    lateinit var searchSecretWithUri: SearchSecretWithUri
    lateinit var getAddressById: GetAddressById
    lateinit var logger: Logger

    private val testDispatcher = TestCoroutineDispatcher()

    init {
        Dispatchers.setMain(testDispatcher)
    }

    @Before
    fun setup() {
        searchSecretWithUri = mockk()
        getAddressById = mockk()
        logger = mockk()

        every { logger.e(any()) }.answers { }

        viewModel = AutofillSearchSecretViewModel(searchSecretWithUri, getAddressById)
    }

    @Test
    fun `searchByPackageName fun updates state's isSearching and results`() = runBlockingTest {
        testDispatcher.pauseDispatcher()
        coEvery { searchSecretWithUri(any(), any()) }.answers { listOf(makeSecret()) }
        coEvery { getAddressById(any(), any()) }.answers { makeAddress("address_id") }

        viewModel.searchByPackageName("some.package.name")
        Assert.assertTrue(viewModel.state.value is State.Searching)
        testDispatcher.resumeDispatcher()

        coVerify { searchSecretWithUri(any(), any()) }
        val readyState = viewModel.state.value as? State.Ready
        Assert.assertNotNull(readyState)
        Assert.assertTrue(readyState?.results.orEmpty().isNotEmpty())
    }

    @Test
    fun `searchByPackageName handles exception and sets isSearching to false`() = runBlockingTest {
        testDispatcher.pauseDispatcher()
        coEvery { searchSecretWithUri(any(), any()) }.throws(IllegalStateException("Some exception"))

        viewModel.searchByPackageName("some.package.name")
        Assert.assertTrue(viewModel.state.value is State.Searching)
        testDispatcher.resumeDispatcher()

        coVerify { searchSecretWithUri(any(), any()) }
        Assert.assertTrue(viewModel.state.value is State.Idle)
    }

    private fun makeSecret() = Secret(
        "secret_id",
        "user_id",
        "address_id",
        "Some name",
        SecretType.Other,
        false,
        SecretValue.Single("some secret"),
        associatedUris = emptyList()
    )

    private fun makeAddress(addressId: String) = UserAddress(
        userId = UserId("user_id"),
        addressId = AddressId(addressId),
        email = "test@proton.me",
        displayName = "Some email",
        canSend = true,
        canReceive = true,
        enabled = true,
        order = 0,
        keys = emptyList(),
        signedKeyList = null
    )
}
