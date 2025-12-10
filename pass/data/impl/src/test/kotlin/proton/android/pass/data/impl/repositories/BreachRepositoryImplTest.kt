/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.repositories

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.errors.CustomEmailDoesNotExistException
import proton.android.pass.data.api.errors.ItemNotFoundError
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.fakes.usecases.FakeObserveItemById
import proton.android.pass.data.fakes.usecases.FakeObserveItems
import proton.android.pass.data.impl.fakes.FakeLocalBreachDataSource
import proton.android.pass.data.impl.fakes.FakeLocalUserAccessDataDataSource
import proton.android.pass.data.impl.fakes.FakeRemoteBreachDataSource
import proton.android.pass.data.impl.responses.BreachCustomEmailApiModel
import proton.android.pass.data.impl.responses.BreachCustomEmailDetailsApiModel
import proton.android.pass.data.impl.responses.BreachCustomEmailResponse
import proton.android.pass.data.impl.responses.BreachCustomEmailsResponse
import proton.android.pass.data.impl.responses.BreachDomainPeekApiModel
import proton.android.pass.data.impl.responses.BreachEmails
import proton.android.pass.data.impl.responses.BreachEmailsResponse
import proton.android.pass.data.impl.responses.BreachProtonEmailApiModel
import proton.android.pass.data.impl.responses.Breaches
import proton.android.pass.data.impl.responses.BreachesDetailsApiModel
import proton.android.pass.data.impl.responses.BreachesResponse
import proton.android.pass.data.impl.responses.MonitorStateResponse
import proton.android.pass.data.impl.responses.Source
import proton.android.pass.data.impl.responses.UpdateGlobalMonitorStateResponse
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.AliasEmailId
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.IsDarkWebAliasMessageDismissedPreference
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.domain.breach.BreachCustomEmail as DomainBreachCustomEmail
import proton.android.pass.domain.breach.BreachProtonEmail as DomainBreachProtonEmail

internal class BreachRepositoryImplTest {

    private lateinit var instance: BreachRepositoryImpl
    private lateinit var localBreachesDataSource: FakeLocalBreachDataSource
    private lateinit var remoteBreachDataSource: FakeRemoteBreachDataSource
    private lateinit var localUserAccessDataDataSource: FakeLocalUserAccessDataDataSource
    private lateinit var observeItemById: FakeObserveItemById
    private lateinit var observeItems: FakeObserveItems
    private lateinit var internalSettings: FakeInternalSettingsRepository

    @Before
    fun setup() {
        localBreachesDataSource = FakeLocalBreachDataSource()
        remoteBreachDataSource = FakeRemoteBreachDataSource()
        localUserAccessDataDataSource = FakeLocalUserAccessDataDataSource()
        observeItemById = FakeObserveItemById()
        observeItems = FakeObserveItems()
        internalSettings = FakeInternalSettingsRepository()
        observeItems.emitValue(emptyList())

        instance = BreachRepositoryImpl(
            localUserAccessDataDataSource = localUserAccessDataDataSource,
            remoteBreachDataSource = remoteBreachDataSource,
            localBreachDataSource = localBreachesDataSource,
            observeItemById = observeItemById,
            observeItems = observeItems,
            internalSettings = internalSettings
        )
    }

    // ========== observeAllBreaches Tests ==========

    @Test
    fun `observeAllBreaches returns breaches from remote`() = runTest {
        val breachesResponse = createBreachesResponse(
            emailsCount = 5,
            customEmails = listOf(createBreachCustomEmailResponse("email1@test.com", "id1", breachCounter = 2)),
            protonEmails = listOf(createBreachProtonEmailResponse("email2@proton.me", "addr1", breachCounter = 3))
        )

        remoteBreachDataSource.setGetAllBreachesResult(Result.success(breachesResponse))
        remoteBreachDataSource.setGetBreachesForCustomEmailResult(
            Result.success(createBreachEmailsResponse(emptyList()))
        )
        remoteBreachDataSource.setGetBreachesForProtonEmailResult(
            Result.success(createBreachEmailsResponse(emptyList()))
        )

        instance.refreshBreaches(TEST_USER_ID)
        val result = instance.observeAllBreaches(TEST_USER_ID).first()

        assertThat(result.breachesCount).isEqualTo(5)
        assertThat(result.breachedCustomEmails).hasSize(1)
        assertThat(result.breachedProtonEmails).hasSize(1)
    }

    @Test
    fun `observeAllBreaches sets dark web alias message to dismissed when hasCustomDomains is false`() = runTest {
        val breachesResponse = createBreachesResponse(
            emailsCount = 0,
            hasCustomDomains = false
        )

        internalSettings.setDarkWebAliasMessageVisibility(
            IsDarkWebAliasMessageDismissedPreference.Show
        )
        remoteBreachDataSource.setGetAllBreachesResult(Result.success(breachesResponse))

        instance.refreshBreaches(TEST_USER_ID)
        instance.observeAllBreaches(TEST_USER_ID).first()

        val visibility = internalSettings.getDarkWebAliasMessageVisibility().first()
        assertThat(visibility).isEqualTo(IsDarkWebAliasMessageDismissedPreference.Dismissed)
    }

    @Test
    fun `observeAllBreaches does not dismiss message when hasCustomDomains is true`() = runTest {
        val breachesResponse = createBreachesResponse(
            emailsCount = 0,
            hasCustomDomains = true
        )

        internalSettings.setDarkWebAliasMessageVisibility(
            IsDarkWebAliasMessageDismissedPreference.Show
        )
        remoteBreachDataSource.setGetAllBreachesResult(Result.success(breachesResponse))

        instance.refreshBreaches(TEST_USER_ID)
        instance.observeAllBreaches(TEST_USER_ID).first()

        val visibility = internalSettings.getDarkWebAliasMessageVisibility().first()
        assertThat(visibility).isEqualTo(IsDarkWebAliasMessageDismissedPreference.Show)
    }

    @Test
    fun `refreshBreaches stores breach details for all breached emails`() = runTest {
        val customEmailId = CustomEmailId("custom-id")
        val addressId = AddressId("address-id")
        val shareId = ShareId("share-id")
        val itemId = ItemId("item-id")
        val aliasItem = ItemTestFactory.createAlias(
            shareId = shareId,
            itemId = itemId,
            alias = "alias@example.com",
            flags = ItemFlag.EmailBreached.value
        )

        observeItems.emit(
            FakeObserveItems.Params(
                selection = ShareSelection.AllShares,
                itemState = ItemState.Active,
                filter = ItemTypeFilter.Aliases,
                userId = TEST_USER_ID,
                itemFlags = mapOf(ItemFlag.EmailBreached to true, ItemFlag.SkipHealthCheck to false)
            ),
            listOf(aliasItem)
        )

        remoteBreachDataSource.setGetAllBreachesResult(
            Result.success(
                createBreachesResponse(
                    emailsCount = 3,
                    customEmails = listOf(
                        createBreachCustomEmailResponse(
                            email = "custom@example.com",
                            id = customEmailId.id,
                            breachCounter = 1
                        )
                    ),
                    protonEmails = listOf(
                        createBreachProtonEmailResponse(
                            email = "proton@proton.me",
                            addressId = addressId.id,
                            breachCounter = 1
                        )
                    )
                )
            )
        )
        remoteBreachDataSource.setGetBreachesForCustomEmailResult(
            Result.success(
                createBreachEmailsResponse(
                    breaches = listOf(
                        createBreachEntry(
                            id = "custom-breach",
                            email = "custom@example.com"
                        )
                    )
                )
            )
        )
        remoteBreachDataSource.setGetBreachesForProtonEmailResult(
            Result.success(
                createBreachEmailsResponse(
                    breaches = listOf(
                        createBreachEntry(
                            id = "proton-breach",
                            email = "proton@proton.me"
                        )
                    )
                )
            )
        )
        remoteBreachDataSource.setGetBreachesForAliasEmailResult(
            Result.success(
                createBreachEmailsResponse(
                    breaches = listOf(
                        createBreachEntry(
                            id = "alias-breach",
                            email = (aliasItem.itemType as ItemType.Alias).aliasEmail
                        )
                    )
                )
            )
        )

        instance.refreshBreaches(TEST_USER_ID)

        val storedCustomBreaches = localBreachesDataSource.observeCustomEmailBreaches(
            TEST_USER_ID,
            customEmailId
        ).first()
        val storedProtonBreaches = localBreachesDataSource.observeProtonEmailBreaches(
            TEST_USER_ID,
            addressId
        ).first()
        val storedAliasBreaches = localBreachesDataSource.observeAliasEmailBreaches(
            TEST_USER_ID,
            AliasEmailId(shareId, itemId)
        ).first()

        assertThat(storedCustomBreaches).hasSize(1)
        assertThat(storedProtonBreaches).hasSize(1)
        assertThat(storedAliasBreaches).hasSize(1)
    }

    // ========== observeCustomEmail Tests ==========

    @Test
    fun `observeCustomEmail returns custom email from local data source`() = runTest {
        val customEmail = DomainBreachCustomEmail(
            id = CustomEmailId("id1"),
            email = "test@example.com",
            verified = true,
            breachCount = 2,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertCustomEmail(TEST_USER_ID, customEmail)
        remoteBreachDataSource.setGetCustomEmailsResult(
            Result.success(createBreachCustomEmailsResponse(emptyList()))
        )

        val result = instance.observeCustomEmail(TEST_USER_ID, customEmail.id).first()

        assertThat(result.id).isEqualTo(customEmail.id)
        assertThat(result.email).isEqualTo(customEmail.email)
        assertThat(result.isVerified).isTrue()
        assertThat(result.breachCount).isEqualTo(2)
    }

    @Test
    fun `observeCustomEmail emits when data available after refreshBreaches`() = runTest {
        val customEmailId = CustomEmailId("id1")
        val customEmailResponse = createBreachCustomEmailResponse("test@example.com", "id1")

        remoteBreachDataSource.setGetAllBreachesResult(
            Result.success(
                createBreachesResponse(
                    emailsCount = 1,
                    customEmails = listOf(customEmailResponse)
                )
            )
        )
        remoteBreachDataSource.setGetBreachesForCustomEmailResult(
            Result.success(createBreachEmailsResponse(emptyList()))
        )

        instance.refreshBreaches(TEST_USER_ID)

        val result = instance.observeCustomEmail(TEST_USER_ID, customEmailId).first()

        assertThat(result.email).isEqualTo("test@example.com")
    }

    // ========== observeCustomEmails Tests ==========

    @Test
    fun `observeCustomEmails returns list from local data source`() = runTest {
        val customEmail1 = DomainBreachCustomEmail(
            id = CustomEmailId("id1"),
            email = "test1@example.com",
            verified = true,
            breachCount = 1,
            flags = 0,
            lastBreachTime = null
        )
        val customEmail2 = DomainBreachCustomEmail(
            id = CustomEmailId("id2"),
            email = "test2@example.com",
            verified = false,
            breachCount = 0,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertCustomEmails(TEST_USER_ID, listOf(customEmail1, customEmail2))
        remoteBreachDataSource.setGetCustomEmailsResult(
            Result.success(createBreachCustomEmailsResponse(emptyList()))
        )

        val result = instance.observeCustomEmails(TEST_USER_ID).first()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.email }).containsExactly("test1@example.com", "test2@example.com")
    }

    // ========== observeAliasEmail Tests ==========

    @Test
    fun `observeAliasEmail combines item and breach data`() = runTest {
        val shareId = ShareId("share1")
        val itemId = ItemId("item1")
        val aliasEmailId = AliasEmailId(shareId, itemId)
        val aliasEmail = "alias@example.com"

        val aliasItem = ItemTestFactory.createAlias(
            shareId = shareId,
            itemId = itemId,
            alias = aliasEmail
        )

        val breachEmail = createBreachEmail(
            id = BreachEmailId.Alias(BreachId("breach1"), shareId, itemId),
            email = aliasEmail,
            isResolved = false
        )

        observeItemById.emitValue(Result.success(aliasItem))
        localBreachesDataSource.upsertAliasEmailBreaches(TEST_USER_ID, aliasEmailId, listOf(breachEmail))

        val result = instance.observeAliasEmail(TEST_USER_ID, aliasEmailId).first()

        assertThat(result.email).isEqualTo(aliasEmail)
        assertThat(result.breachCount).isEqualTo(1)
        assertThat(result.isMonitoringDisabled).isFalse()
    }

    @Test
    fun `observeAliasEmail throws ItemNotFoundError when item not found`() = runTest {
        val shareId = ShareId("share1")
        val itemId = ItemId("item1")
        val aliasEmailId = AliasEmailId(shareId, itemId)

        observeItemById.emitValue(Result.failure(ItemNotFoundError(itemId, shareId)))

        try {
            instance.observeAliasEmail(TEST_USER_ID, aliasEmailId).first()
            assert(false) { "Expected ItemNotFoundError" }
        } catch (e: ItemNotFoundError) {
            assertThat(e.message).contains(itemId.id)
            assertThat(e.message).contains(shareId.id)
        }
    }

    @Test
    fun `observeAliasEmail calculates breach count from unresolved breaches`() = runTest {
        val shareId = ShareId("share1")
        val itemId = ItemId("item1")
        val aliasEmailId = AliasEmailId(shareId, itemId)
        val aliasEmail = "alias@example.com"

        val aliasItem = ItemTestFactory.createAlias(
            shareId = shareId,
            itemId = itemId,
            alias = aliasEmail
        )

        val resolvedBreach = createBreachEmail(
            id = BreachEmailId.Alias(BreachId("breach1"), shareId, itemId),
            email = aliasEmail,
            isResolved = true
        )
        val unresolvedBreach = createBreachEmail(
            id = BreachEmailId.Alias(BreachId("breach2"), shareId, itemId),
            email = aliasEmail,
            isResolved = false
        )

        observeItemById.emitValue(Result.success(aliasItem))
        localBreachesDataSource.upsertAliasEmailBreaches(
            TEST_USER_ID,
            aliasEmailId,
            listOf(resolvedBreach, unresolvedBreach)
        )

        val result = instance.observeAliasEmail(TEST_USER_ID, aliasEmailId).first()

        assertThat(result.breachCount).isEqualTo(1)
    }

    // ========== observeProtonEmail Tests ==========

    @Test
    fun `observeProtonEmail returns proton email report`() = runTest {
        val addressId = AddressId("addr1")
        val protonEmail = DomainBreachProtonEmail(
            addressId = addressId,
            email = "test@proton.me",
            breachCounter = 3,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertProtonEmail(TEST_USER_ID, protonEmail)
        remoteBreachDataSource.setGetBreachesForProtonEmailResult(
            Result.success(createBreachEmailsResponse(emptyList()))
        )

        val result = instance.observeProtonEmail(TEST_USER_ID, addressId).first()

        assertThat(result.addressId).isEqualTo(addressId)
        assertThat(result.email).isEqualTo("test@proton.me")
        assertThat(result.breachCount).isEqualTo(3)
    }

    // ========== observeProtonEmails Tests ==========

    @Test
    fun `observeProtonEmails returns list from local data source`() = runTest {
        val protonEmail1 = DomainBreachProtonEmail(
            addressId = AddressId("addr1"),
            email = "test1@proton.me",
            breachCounter = 1,
            flags = 0,
            lastBreachTime = null
        )
        val protonEmail2 = DomainBreachProtonEmail(
            addressId = AddressId("addr2"),
            email = "test2@proton.me",
            breachCounter = 2,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertProtonEmails(TEST_USER_ID, listOf(protonEmail1, protonEmail2))
        remoteBreachDataSource.setGetAllBreachesResult(
            Result.success(createBreachesResponse(emailsCount = 0))
        )

        val result = instance.observeProtonEmails(TEST_USER_ID).first()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.email }).containsExactly("test1@proton.me", "test2@proton.me")
    }

    // ========== observeBreachesForCustomEmail Tests ==========

    @Test
    fun `observeBreachesForCustomEmail returns breaches from local data source`() = runTest {
        val customEmailId = CustomEmailId("id1")
        val breachEmail = createBreachEmail(
            id = BreachEmailId.Custom(BreachId("breach1"), customEmailId),
            email = "test@example.com",
            isResolved = false
        )

        localBreachesDataSource.upsertCustomEmailBreaches(TEST_USER_ID, customEmailId, listOf(breachEmail))
        remoteBreachDataSource.setGetBreachesForCustomEmailResult(
            Result.success(createBreachEmailsResponse(emptyList()))
        )

        val result = instance.observeBreachesForCustomEmail(TEST_USER_ID, customEmailId).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].email).isEqualTo("test@example.com")
    }

    // ========== observeBreachesForProtonEmail Tests ==========

    @Test
    fun `observeBreachesForProtonEmail returns breaches from local data source`() = runTest {
        val addressId = AddressId("addr1")
        val breachEmail = createBreachEmail(
            id = BreachEmailId.Proton(BreachId("breach1"), addressId),
            email = "test@proton.me",
            isResolved = false
        )

        localBreachesDataSource.upsertProtonEmailBreaches(TEST_USER_ID, addressId, listOf(breachEmail))
        remoteBreachDataSource.setGetBreachesForProtonEmailResult(
            Result.success(createBreachEmailsResponse(emptyList()))
        )

        val result = instance.observeBreachesForProtonEmail(TEST_USER_ID, addressId).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].email).isEqualTo("test@proton.me")
    }

    // ========== observeBreachesForAliasEmail Tests ==========

    @Test
    fun `observeBreachesForAliasEmail returns breaches from local data source`() = runTest {
        val shareId = ShareId("share1")
        val itemId = ItemId("item1")
        val aliasEmailId = AliasEmailId(shareId, itemId)
        val breachEmail = createBreachEmail(
            id = BreachEmailId.Alias(BreachId("breach1"), shareId, itemId),
            email = "alias@example.com",
            isResolved = false
        )

        localBreachesDataSource.upsertAliasEmailBreaches(TEST_USER_ID, aliasEmailId, listOf(breachEmail))
        remoteBreachDataSource.setGetBreachesForAliasEmailResult(
            Result.success(createBreachEmailsResponse(emptyList()))
        )

        val result = instance.observeBreachesForAliasEmail(TEST_USER_ID, aliasEmailId).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].email).isEqualTo("alias@example.com")
    }

    // ========== addCustomEmail Tests ==========

    @Test
    fun `addCustomEmail adds email and stores locally`() = runTest {
        val email = "newemail@example.com"
        val customEmailResponse = BreachCustomEmailResponse(
            code = 1000,
            email = createBreachCustomEmailResponse(email, "id1")
        )

        remoteBreachDataSource.setAddCustomEmailResult(Result.success(customEmailResponse))

        val result = instance.addCustomEmail(TEST_USER_ID, email)

        assertThat(result.email).isEqualTo(email)
        assertThat(result.id.id).isEqualTo("id1")

        val stored = localBreachesDataSource.observeCustomEmail(TEST_USER_ID, result.id).first()
        assertThat(stored.email).isEqualTo(email)
    }

    // ========== verifyCustomEmail Tests ==========

    @Test
    fun `verifyCustomEmail updates local email to verified`() = runTest {
        val customEmailId = CustomEmailId("id1")
        val customEmail = DomainBreachCustomEmail(
            id = customEmailId,
            email = "test@example.com",
            verified = false,
            breachCount = 0,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertCustomEmail(TEST_USER_ID, customEmail)
        remoteBreachDataSource.setVerifyCustomEmailResult(Result.success(Unit))

        instance.verifyCustomEmail(TEST_USER_ID, customEmailId, "123456")

        val verified = localBreachesDataSource.observeCustomEmail(TEST_USER_ID, customEmailId).first()
        assertThat(verified.verified).isTrue()
    }

    @Test
    fun `verifyCustomEmail deletes email when CustomEmailDoesNotExistException thrown`() = runTest {
        val customEmailId = CustomEmailId("id1")
        val customEmail = DomainBreachCustomEmail(
            id = customEmailId,
            email = "test@example.com",
            verified = false,
            breachCount = 0,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertCustomEmail(TEST_USER_ID, customEmail)
        remoteBreachDataSource.setVerifyCustomEmailResult(
            Result.failure(CustomEmailDoesNotExistException())
        )

        instance.verifyCustomEmail(TEST_USER_ID, customEmailId, "123456")

        try {
            localBreachesDataSource.getCustomEmail(TEST_USER_ID, customEmailId)
            assert(false) { "Expected email to be deleted" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("There's no custom email with id: ${customEmailId.id}")
        }
    }

    // ========== markProtonEmailAsResolved Tests ==========

    @Test
    fun `markProtonEmailAsResolved updates remote and local state`() = runTest {
        val addressId = AddressId("addr1")
        val protonEmail = DomainBreachProtonEmail(
            addressId = addressId,
            email = "test@proton.me",
            breachCounter = 2,
            flags = 0,
            lastBreachTime = null
        )
        val breachEmail = createBreachEmail(
            id = BreachEmailId.Proton(BreachId("breach1"), addressId),
            email = "test@proton.me",
            isResolved = false
        )

        localBreachesDataSource.upsertProtonEmail(TEST_USER_ID, protonEmail)
        localBreachesDataSource.upsertProtonEmailBreaches(TEST_USER_ID, addressId, listOf(breachEmail))
        remoteBreachDataSource.setMarkProtonEmailAsResolvedResult(Result.success(Unit))

        instance.markProtonEmailAsResolved(TEST_USER_ID, addressId)

        val resolvedBreaches = localBreachesDataSource.observeProtonEmailBreaches(TEST_USER_ID, addressId).first()
        assertThat(resolvedBreaches.all { it.isResolved }).isTrue()
    }

    // ========== markAliasEmailAsResolved Tests ==========

    @Test
    fun `markAliasEmailAsResolved updates remote and local state`() = runTest {
        val shareId = ShareId("share1")
        val itemId = ItemId("item1")
        val aliasEmailId = AliasEmailId(shareId, itemId)
        val breachEmail = createBreachEmail(
            id = BreachEmailId.Alias(BreachId("breach1"), shareId, itemId),
            email = "alias@example.com",
            isResolved = false
        )

        localBreachesDataSource.upsertAliasEmailBreaches(TEST_USER_ID, aliasEmailId, listOf(breachEmail))
        remoteBreachDataSource.setMarkAliasEmailAsResolvedResult(Result.success(Unit))

        instance.markAliasEmailAsResolved(TEST_USER_ID, aliasEmailId)

        val resolvedBreaches = localBreachesDataSource.observeAliasEmailBreaches(TEST_USER_ID, aliasEmailId).first()
        assertThat(resolvedBreaches.all { it.isResolved }).isTrue()
    }

    // ========== markCustomEmailAsResolved Tests ==========

    @Test
    fun `markCustomEmailAsResolved updates remote and local state`() = runTest {
        val customEmailId = CustomEmailId("id1")
        val customEmail = DomainBreachCustomEmail(
            id = customEmailId,
            email = "test@example.com",
            verified = true,
            breachCount = 1,
            flags = 0,
            lastBreachTime = null
        )
        val breachEmail = createBreachEmail(
            id = BreachEmailId.Custom(BreachId("breach1"), customEmailId),
            email = "test@example.com",
            isResolved = false
        )

        localBreachesDataSource.upsertCustomEmail(TEST_USER_ID, customEmail)
        localBreachesDataSource.upsertCustomEmailBreaches(TEST_USER_ID, customEmailId, listOf(breachEmail))
        remoteBreachDataSource.setMarkCustomEmailAsResolvedResult(
            Result.success(
                BreachCustomEmailResponse(
                    code = 1000,
                    email = createBreachCustomEmailResponse("test@example.com", "id1")
                )
            )
        )

        instance.markCustomEmailAsResolved(TEST_USER_ID, customEmailId)

        val resolvedBreaches = localBreachesDataSource.observeCustomEmailBreaches(TEST_USER_ID, customEmailId).first()
        assertThat(resolvedBreaches.all { it.isResolved }).isTrue()
    }

    // ========== resendVerificationCode Tests ==========

    @Test
    fun `resendVerificationCode calls remote`() = runTest {
        val customEmailId = CustomEmailId("id1")
        remoteBreachDataSource.setResendVerificationCodeResult(Result.success(Unit))

        instance.resendVerificationCode(TEST_USER_ID, customEmailId)

        // Test passes if no exception is thrown
    }

    // ========== removeCustomEmail Tests ==========

    @Test
    fun `removeCustomEmail removes from remote and local`() = runTest {
        val customEmailId = CustomEmailId("id1")
        val customEmail = DomainBreachCustomEmail(
            id = customEmailId,
            email = "test@example.com",
            verified = true,
            breachCount = 0,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertCustomEmail(TEST_USER_ID, customEmail)
        remoteBreachDataSource.setRemoveCustomEmailResult(Result.success(Unit))

        instance.removeCustomEmail(TEST_USER_ID, customEmailId)

        try {
            localBreachesDataSource.getCustomEmail(TEST_USER_ID, customEmailId)
            assert(false) { "Expected email to be deleted" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("There's no custom email with id: ${customEmailId.id}")
        }
    }

    // ========== updateGlobalProtonMonitorState Tests ==========

    @Test
    fun `updateGlobalProtonMonitorState updates remote and local`() = runTest {
        val monitorResponse = UpdateGlobalMonitorStateResponse(
            code = 1000,
            monitorStateResponse = MonitorStateResponse(
                protonAddress = true,
                aliases = false
            )
        )

        remoteBreachDataSource.setUpdateGlobalProtonAddressMonitorStateResult(Result.success(monitorResponse))

        instance.updateGlobalProtonMonitorState(TEST_USER_ID, true)

        // Test passes if no exception is thrown
    }

    // ========== updateGlobalAliasMonitorState Tests ==========

    @Test
    fun `updateGlobalAliasMonitorState updates remote and local`() = runTest {
        val monitorResponse = UpdateGlobalMonitorStateResponse(
            code = 1000,
            monitorStateResponse = MonitorStateResponse(
                protonAddress = false,
                aliases = true
            )
        )

        remoteBreachDataSource.setUpdateGlobalAliasAddressMonitorStateResult(Result.success(monitorResponse))

        instance.updateGlobalAliasMonitorState(TEST_USER_ID, true)

        // Test passes if no exception is thrown
    }

    // ========== updateProtonAddressMonitorState Tests ==========

    @Test
    fun `updateProtonAddressMonitorState toggles monitoring flag`() = runTest {
        val addressId = AddressId("addr1")
        val protonEmail = DomainBreachProtonEmail(
            addressId = addressId,
            email = "test@proton.me",
            breachCounter = 0,
            flags = 0,
            lastBreachTime = null
        )

        localBreachesDataSource.upsertProtonEmail(TEST_USER_ID, protonEmail)
        remoteBreachDataSource.setUpdateProtonAddressMonitorStateResult(Result.success(Unit))

        instance.updateProtonAddressMonitorState(TEST_USER_ID, addressId, true)

        val updated = localBreachesDataSource.observeProtonEmail(TEST_USER_ID, addressId).first()
        assertThat(updated.flags).isNotEqualTo(protonEmail.flags)
    }

    // ========== Helper Functions ==========

    private fun createBreachesResponse(
        emailsCount: Int = 0,
        domainPeeks: List<BreachDomainPeekApiModel> = emptyList(),
        customEmails: List<BreachCustomEmailApiModel> = emptyList(),
        protonEmails: List<BreachProtonEmailApiModel> = emptyList(),
        hasCustomDomains: Boolean = false
    ): BreachesResponse = BreachesResponse(
        code = 1000,
        breaches = BreachesDetailsApiModel(
            emailsCount = emailsCount,
            domainPeeks = domainPeeks,
            customEmails = customEmails,
            protonEmails = protonEmails,
            hasCustomDomains = hasCustomDomains
        )
    )

    private fun createBreachCustomEmailResponse(
        email: String,
        id: String,
        verified: Boolean = false,
        breachCounter: Int = 0,
        flags: Int = 0,
        lastBreachTime: Int? = null
    ): BreachCustomEmailApiModel = BreachCustomEmailApiModel(
        customEmailId = id,
        email = email,
        verified = verified,
        breachCounter = breachCounter,
        flags = flags,
        lastBreachTime = lastBreachTime
    )

    private fun createBreachProtonEmailResponse(
        email: String,
        addressId: String,
        breachCounter: Int = 0,
        flags: Int = 0,
        lastBreachTime: Int? = null
    ): BreachProtonEmailApiModel = BreachProtonEmailApiModel(
        addressId = addressId,
        email = email,
        breachCounter = breachCounter,
        flags = flags,
        lastBreachTime = lastBreachTime
    )

    private fun createBreachCustomEmailsResponse(
        customEmails: List<BreachCustomEmailApiModel>
    ): BreachCustomEmailsResponse = BreachCustomEmailsResponse(
        code = 1000,
        emails = BreachCustomEmailDetailsApiModel(customEmails = customEmails)
    )

    private fun createBreachEmailsResponse(breaches: List<Breaches>): BreachEmailsResponse = BreachEmailsResponse(
        code = 1000,
        breachEmails = BreachEmails(
            isEligible = true,
            count = breaches.size,
            breaches = breaches
        )
    )

    private fun createBreachEntry(
        id: String,
        email: String,
        resolvedState: Int = 0,
        severity: Double = 1.0
    ): Breaches = Breaches(
        id = id,
        email = email,
        resolvedState = resolvedState,
        severity = severity,
        name = "Breach $id",
        createdAt = "2024-01-01T00:00:00Z",
        publishedAt = "2024-01-02T00:00:00Z",
        source = Source(
            isAggregated = false,
            domain = "example.com",
            category = null,
            country = null
        ),
        size = null,
        exposedData = emptyList(),
        passwordLastChars = null,
        actions = emptyList()
    )

    private fun createBreachEmail(
        id: BreachEmailId,
        email: String,
        severity: Double = 1.0,
        name: String = "Test Breach",
        createdAt: String = "2024-01-01T00:00:00Z",
        publishedAt: String = "2024-01-01T00:00:00Z",
        size: Long? = null,
        passwordLastChars: String? = null,
        exposedData: List<String> = emptyList(),
        isResolved: Boolean = false,
        actions: List<proton.android.pass.domain.breach.BreachAction> = emptyList()
    ): BreachEmail = BreachEmail(
        emailId = id,
        email = email,
        severity = severity,
        name = name,
        createdAt = createdAt,
        publishedAt = publishedAt,
        size = size,
        passwordLastChars = passwordLastChars,
        exposedData = exposedData,
        isResolved = isResolved,
        actions = actions
    )

    companion object {
        private val TEST_USER_ID = UserId("test-user-id")
    }
}
