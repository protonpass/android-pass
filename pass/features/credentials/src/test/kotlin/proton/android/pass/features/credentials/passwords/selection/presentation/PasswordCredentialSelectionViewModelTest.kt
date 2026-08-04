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

package proton.android.pass.features.credentials.passwords.selection.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.biometry.FakeNeedsBiometricAuth
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.fakes.repositories.FakeAssetLinkRepository
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeHasActiveAccount
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.assetlink.AssetLink
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.notifications.fakes.FakeToastManager
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.features.credentials.shared.passwords.search.PasswordCallerContext
import proton.android.pass.features.credentials.shared.passwords.search.PasswordOriginResolver
import proton.android.pass.features.credentials.shared.passwords.search.StoredPasswordAppAssociationAuthorizer

internal class PasswordCredentialSelectionViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var encryptionContextProvider: RecordingEncryptionContextProvider
    private lateinit var toastManager: FakeToastManager
    private lateinit var assetLinkRepository: FakeAssetLinkRepository
    private lateinit var itemRepository: FakeItemRepository
    private lateinit var getItemById: FakeGetItemById
    private lateinit var dalVerifier: FakeDigitalAssetLinksVerifier
    private lateinit var viewModel: PasswordCredentialSelectionViewModel

    @Before
    internal fun setUp() {
        encryptionContextProvider = RecordingEncryptionContextProvider()
        toastManager = FakeToastManager()
        assetLinkRepository = FakeAssetLinkRepository()
        itemRepository = FakeItemRepository()
        getItemById = FakeGetItemById()
        dalVerifier = FakeDigitalAssetLinksVerifier()

        viewModel = PasswordCredentialSelectionViewModel(
            userPreferenceRepository = FakePreferenceRepository(),
            needsBiometricAuth = FakeNeedsBiometricAuth(),
            appConfig = FakeAppConfig(),
            accountManager = FakeAccountManager(),
            encryptionContextProvider = encryptionContextProvider,
            toastManager = toastManager,
            internalSettingsRepository = FakeInternalSettingsRepository(),
            telemetryManager = FakeTelemetryManager(),
            assetLinkRepository = assetLinkRepository,
            verifyDigitalAssetLinksForCredentialSharing = dalVerifier,
            getItemById = getItemById,
            hasActiveAccount = FakeHasActiveAccount(),
            itemRepository = itemRepository,
            storedPasswordAppAssociationAuthorizer = StoredPasswordAppAssociationAuthorizer(),
            passwordOriginResolver = PasswordOriginResolver()
        )
    }

    @Test
    internal fun `WHEN stored signed association matches THEN credential is returned without DAL`() = runTest {
        val selectedItem = createLoginItemUiModel(
            username = "alice",
            password = "s3cret",
            urls = emptyList()
        )
        getItemById.emit(
            Result.success(
                ItemTestFactory.createLogin(
                    shareId = selectedItem.shareId,
                    itemId = selectedItem.id
                ).copy(
                    packageInfoSet = setOf(
                        PackageInfo(
                            packageName = PackageName("com.example.app"),
                            appName = AppName("Example"),
                            hashes = setOf("certificate")
                        )
                    )
                )
            )
        )
        viewModel.onUpdateRequest(createSelectRequest(callingPackageName = "com.example.app"))

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(selectedItem)

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            val event =
                state.event as PasswordCredentialSelectionStateEvent.SendCredentialResponse
            assertThat(event.id).isEqualTo("alice")
            assertThat(event.password).isEqualTo("s3cret")
            assertThat(dalVerifier.invocations).isEmpty()
        }
    }

    @Test
    internal fun `WHEN cached DAL matches selected login THEN credential is returned`() = runTest {
        assetLinkRepository.setAssetLinks(
            packageName = "com.example.credentialapp",
            assetLinks = listOf(assetLink("https://login.example.test"))
        )

        viewModel.onUpdateRequest(
            createSelectRequest(
                callingPackageName = "com.example.credentialapp"
            )
        )

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(
                createLoginItemUiModel(
                    username = "alice",
                    password = "s3cret",
                    urls = listOf("https://login.example.test/login?source=app#section")
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            val event =
                state.event as PasswordCredentialSelectionStateEvent.SendCredentialResponse

            assertThat(event.id).isEqualTo("alice")
            assertThat(event.password).isEqualTo("s3cret")
            assertThat(dalVerifier.invocations).isEmpty()
        }

    }

    @Test
    internal fun `WHEN live DAL authorizes selected origin THEN credential is returned`() = runTest {
        assetLinkRepository.setAssetLinks(
            packageName = "com.attacker.app",
            assetLinks = listOf(assetLink("https://different.example"))
        )
        dalVerifier.result = true

        viewModel.onUpdateRequest(
            createSelectRequest(
                callingPackageName = "com.attacker.app"
            )
        )
        assertThat(dalVerifier.invocations).isEmpty()

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(
                createLoginItemUiModel(
                    username = "alice",
                    password = "s3cret",
                    urls = listOf("https://login.example.test/login?source=app#section")
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            val event =
                state.event as PasswordCredentialSelectionStateEvent.SendCredentialResponse
            assertThat(event.id).isEqualTo("alice")
            assertThat(event.password).isEqualTo("s3cret")
            assertThat(dalVerifier.invocations).containsExactly(
                DalVerificationInvocation(
                    website = "https://login.example.test",
                    packageName = "com.attacker.app",
                    certificateFingerprints = setOf("certificate")
                )
            )
        }
    }

    @Test
    internal fun `WHEN live DAL rejects selected origin THEN request is cancelled before decryption`() = runTest {
        dalVerifier.result = false

        viewModel.onUpdateRequest(
            createSelectRequest(callingPackageName = "com.attacker.app")
        )

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(
                createLoginItemUiModel(
                    username = "alice",
                    password = "s3cret",
                    urls = listOf("https://login.example.test/login")
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            assertThat(state.event).isEqualTo(PasswordCredentialSelectionStateEvent.Cancel)
            assertThat(dalVerifier.invocations).hasSize(1)
            assertThat(encryptionContextProvider.withEncryptionContextInvocations).isEqualTo(0)
        }
    }

    @Test
    internal fun `WHEN native selection has no URL THEN association is requested without verifier or decryption`() =
        runTest {
            viewModel.onUpdateRequest(
                createSelectRequest(callingPackageName = "com.attacker.app")
            )

            viewModel.stateFlow.test {
                skipItems(1)

                viewModel.onItemSelected(
                    createLoginItemUiModel(
                        username = "alice",
                        password = "s3cret",
                        urls = emptyList()
                    )
                )

                val state = awaitItem() as PasswordCredentialSelectionState.Ready
                assertThat(state.associationCandidate).isNotNull()
                assertThat(dalVerifier.invocations).isEmpty()
                assertThat(encryptionContextProvider.withEncryptionContextInvocations).isEqualTo(0)
            }
        }

    @Test
    internal fun `WHEN association is confirmed THEN signing hashes are stored before response`() = runTest {
        val selectedItem = createLoginItemUiModel(
            username = "alice",
            password = "s3cret",
            urls = emptyList()
        )
        itemRepository.setItem(
            ItemTestFactory.createLogin(
                shareId = selectedItem.shareId,
                itemId = selectedItem.id
            )
        )
        viewModel.onUpdateRequest(createSelectRequest(callingPackageName = "com.example.app"))

        viewModel.stateFlow.test {
            skipItems(1)
            viewModel.onItemSelected(selectedItem)
            assertThat((awaitItem() as PasswordCredentialSelectionState.Ready).associationCandidate).isNotNull()

            viewModel.onAssociationConfirmed()
            val response = awaitItem() as PasswordCredentialSelectionState.Ready
            assertThat(response.event)
                .isInstanceOf(PasswordCredentialSelectionStateEvent.SendCredentialResponse::class.java)
            assertThat(itemRepository.getAddPackageAndUrlToItemMemory()).containsExactly(
                FakeItemRepository.AddPackageAndUrlToItemPayload(
                    userId = selectedItem.userId,
                    shareId = selectedItem.shareId,
                    itemId = selectedItem.id,
                    packageInfo = PackageInfo(
                        packageName = PackageName("com.example.app"),
                        appName = AppName("com.example.app"),
                        hashes = setOf("certificate")
                    ).some(),
                    url = None
                )
            )
        }
    }

    @Test
    internal fun `WHEN trusted browser selects login without URL THEN credential is returned`() = runTest {
        viewModel.onUpdateRequest(
            createSelectRequest(
                callingPackageName = "com.android.chrome",
                callerContext = PasswordCallerContext.Browser(origin = "https://login.example.test")
            )
        )

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(
                createLoginItemUiModel(
                    username = "alice",
                    password = "s3cret",
                    urls = emptyList()
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            val event =
                state.event as PasswordCredentialSelectionStateEvent.SendCredentialResponse
            assertThat(event.id).isEqualTo("alice")
            assertThat(event.password).isEqualTo("s3cret")
            assertThat(dalVerifier.invocations).isEmpty()
        }
    }

    @Test
    internal fun `WHEN trusted browser selects matching origin THEN credential is returned`() = runTest {
        viewModel.onUpdateRequest(
            createSelectRequest(
                callingPackageName = "com.android.chrome",
                callerContext = PasswordCallerContext.Browser(origin = "https://login.example.test")
            )
        )

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(
                createLoginItemUiModel(
                    username = "alice",
                    password = "s3cret",
                    urls = listOf("https://Login.Example.Test/login?source=browser#section")
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            val event =
                state.event as PasswordCredentialSelectionStateEvent.SendCredentialResponse
            assertThat(event.id).isEqualTo("alice")
            assertThat(event.password).isEqualTo("s3cret")
            assertThat(dalVerifier.invocations).isEmpty()
        }
    }

    @Test
    internal fun `WHEN trusted browser selects different origin THEN request is cancelled`() = runTest {
        viewModel.onUpdateRequest(
            createSelectRequest(
                callingPackageName = "com.android.chrome",
                callerContext = PasswordCallerContext.Browser(origin = "https://login.example.test")
            )
        )

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(
                createLoginItemUiModel(
                    username = "alice",
                    password = "s3cret",
                    urls = listOf("https://attacker.example/login")
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            assertThat(state.event).isEqualTo(PasswordCredentialSelectionStateEvent.Cancel)
            assertThat(dalVerifier.invocations).isEmpty()
            assertThat(encryptionContextProvider.withEncryptionContextInvocations).isEqualTo(0)
        }
    }

    @Test
    internal fun `WHEN caller context is absent THEN selection fails closed`() = runTest {
        viewModel.onUpdateRequest(
            createSelectRequest(
                callingPackageName = "com.attacker.app",
                callerContext = null
            )
        )

        viewModel.stateFlow.test {
            skipItems(1)

            viewModel.onItemSelected(
                createLoginItemUiModel(
                    username = "alice",
                    password = "s3cret",
                    urls = listOf("https://login.example.test/login")
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            assertThat(state.event).isEqualTo(PasswordCredentialSelectionStateEvent.Cancel)
            assertThat(dalVerifier.invocations).isEmpty()
        }
    }

    @Test
    internal fun `WHEN no active account for userId THEN onAuthPerformed denies request`() = runTest {
        val hasActiveAccount = FakeHasActiveAccount()
        hasActiveAccount.setResult(false)

        val viewModelWithoutAccount = PasswordCredentialSelectionViewModel(
            userPreferenceRepository = FakePreferenceRepository(),
            needsBiometricAuth = FakeNeedsBiometricAuth(),
            appConfig = FakeAppConfig(),
            accountManager = FakeAccountManager(),
            encryptionContextProvider = encryptionContextProvider,
            toastManager = toastManager,
            internalSettingsRepository = FakeInternalSettingsRepository(),
            telemetryManager = FakeTelemetryManager(),
            assetLinkRepository = assetLinkRepository,
            verifyDigitalAssetLinksForCredentialSharing = dalVerifier,
            getItemById = getItemById,
            hasActiveAccount = hasActiveAccount,
            itemRepository = itemRepository,
            storedPasswordAppAssociationAuthorizer = StoredPasswordAppAssociationAuthorizer(),
            passwordOriginResolver = PasswordOriginResolver()
        )

        viewModelWithoutAccount.onUpdateRequest(
            createSelectRequest(callingPackageName = "com.example.app")
        )

        viewModelWithoutAccount.stateFlow.test {
            skipItems(1)

            viewModelWithoutAccount.onAuthPerformed(
                PasswordCredentialSelectionRequest.Use(
                    title = "Select a password",
                    suggestion = Suggestion.PackageName("com.example.app"),
                    userId = UserId("user-id"),
                    username = "alice",
                    encryptedPassword = FakeEncryptionContext.encrypt("s3cret")
                )
            )

            val state = awaitItem() as PasswordCredentialSelectionState.Ready
            assertThat(state.event).isEqualTo(PasswordCredentialSelectionStateEvent.Cancel)
        }
    }

    private fun createSelectRequest(
        callingPackageName: String,
        callerContext: PasswordCallerContext? = PasswordCallerContext.Native(
            packageName = callingPackageName,
            certificateFingerprints = setOf("certificate")
        )
    ): PasswordCredentialSelectionRequest.Select = PasswordCredentialSelectionRequest.Select(
        title = "Select a password",
        suggestion = Suggestion.PackageName(callingPackageName),
        callerContext = callerContext
    )

    private fun assetLink(website: String) = AssetLink(
        website = website,
        packages = setOf(AssetLink.Package("unused", emptySet()))
    )

    private fun createLoginItemUiModel(
        username: String,
        password: String,
        urls: List<String>
    ): ItemUiModel {
        val loginContents = ItemContents.Login.create(
            password = HiddenState.Revealed(
                encrypted = FakeEncryptionContext.encrypt(password),
                clearText = password
            ),
            primaryTotp = HiddenState.Empty(FakeEncryptionContext.encrypt(""))
        ).copy(
            itemUsername = username,
            urls = urls
        )

        return ItemUiModel(
            id = ItemId("item-id"),
            shareId = ShareId("share-id"),
            userId = UserId("user-id"),
            contents = loginContents,
            state = 0,
            createTime = Instant.fromEpochSeconds(0),
            modificationTime = Instant.fromEpochSeconds(0),
            lastAutofillTime = null,
            isPinned = false,
            pinTime = null,
            category = ItemCategory.Login,
            revision = 1,
            shareCount = 0,
            shareType = ShareType.Vault
        )
    }

    private class FakeDigitalAssetLinksVerifier : VerifyDigitalAssetLinksForCredentialSharing {

        var result = false

        val invocations = mutableListOf<DalVerificationInvocation>()

        override suspend fun invoke(
            website: String,
            packageName: String,
            certificateFingerprints: Set<String>
        ): Boolean {
            invocations += DalVerificationInvocation(website, packageName, certificateFingerprints)
            return result
        }
    }

    private data class DalVerificationInvocation(
        val website: String,
        val packageName: String,
        val certificateFingerprints: Set<String>
    )

    private class RecordingEncryptionContextProvider : EncryptionContextProvider {

        var withEncryptionContextInvocations = 0

        override fun <R> withEncryptionContext(block: EncryptionContext.() -> R): R {
            withEncryptionContextInvocations++
            return block(FakeEncryptionContext)
        }

        override fun <R> withEncryptionContext(key: EncryptionKey, block: EncryptionContext.() -> R): R =
            block(FakeEncryptionContext)

        override suspend fun <R> withEncryptionContextSuspendable(block: suspend EncryptionContext.() -> R): R =
            block(FakeEncryptionContext)

        override suspend fun <R> withEncryptionContextSuspendable(
            key: EncryptionKey,
            block: suspend EncryptionContext.() -> R
        ): R = block(FakeEncryptionContext)
    }

}
