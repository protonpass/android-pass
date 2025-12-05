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

package proton.android.pass.features.security.center.sentinel.darkweb.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.user.domain.entity.AddressId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.breach.CustomEmailSuggestion
import proton.android.pass.data.fakes.usecases.breach.BreachCustomEmailMother
import proton.android.pass.data.fakes.usecases.breach.FakeAddBreachCustomEmail
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachAliasEmails
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachCustomEmails
import proton.android.pass.data.fakes.usecases.breach.FakeObserveBreachProtonEmails
import proton.android.pass.data.fakes.usecases.breach.FakeObserveCustomEmailSuggestions
import proton.android.pass.data.fakes.usecases.breach.FakeObserveGlobalMonitorState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.breach.AliasData
import proton.android.pass.domain.breach.AliasKeyId
import proton.android.pass.domain.breach.BreachEmail
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.BreachProtonEmail
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiState
import proton.android.pass.features.security.center.darkweb.presentation.CustomEmailUiStatus
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebCustomEmailsState
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebEmailBreachState
import proton.android.pass.features.security.center.darkweb.presentation.DarkWebViewModel
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState
import proton.android.pass.features.security.center.shared.ui.DateUtils
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule

class DarkWebViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: DarkWebViewModel

    private lateinit var observeBreachProtonEmails: FakeObserveBreachProtonEmails
    private lateinit var observeBreachCustomEmails: FakeObserveBreachCustomEmails
    private lateinit var observeCustomEmailSuggestions: FakeObserveCustomEmailSuggestions
    private lateinit var observeGlobalMonitorState: FakeObserveGlobalMonitorState
    private lateinit var observeBreachAliasEmails: FakeObserveBreachAliasEmails

    @Before
    fun setUp() {
        observeBreachProtonEmails = FakeObserveBreachProtonEmails()
        observeBreachCustomEmails = FakeObserveBreachCustomEmails()
        observeCustomEmailSuggestions = FakeObserveCustomEmailSuggestions()
        observeGlobalMonitorState = FakeObserveGlobalMonitorState()
        observeBreachAliasEmails = FakeObserveBreachAliasEmails()
        instance = DarkWebViewModel(
            observeBreachProtonEmails = observeBreachProtonEmails,
            observeBreachAliasEmails = observeBreachAliasEmails,
            observeBreachCustomEmails = observeBreachCustomEmails,
            observeCustomEmailSuggestions = observeCustomEmailSuggestions,
            observeGlobalMonitorState = observeGlobalMonitorState,
            telemetryManager = FakeTelemetryManager(),
            addBreachCustomEmail = FakeAddBreachCustomEmail(),
            snackbarDispatcher = FakeSnackbarDispatcher()
        )
    }


    @Test
    fun `emits breach proton emails`() = runTest {
        val emails = listOf("email1", "email2").map {
            BreachProtonEmail(
                addressId = AddressId(it),
                email = it,
                breachCounter = 1,
                flags = 0,
                lastBreachTime = 0
            )
        }
        val breaches = emails.map { breachEmail ->
            EmailBreachUiState(
                id = BreachEmailId.Proton(
                    id = BreachId(breachEmail.email),
                    addressId = breachEmail.addressId
                ),
                email = breachEmail.email,
                count = breachEmail.breachCounter,
                breachDate = breachEmail.lastBreachTime?.let(DateUtils::formatDate)?.getOrNull(),
                isMonitored = true
            )
        }

        observeBreachProtonEmails.emit(emails)

        instance.state.test {
            val state = awaitItem()
            val expected = DarkWebEmailBreachState.Success(
                enabledMonitoring = true,
                emails = breaches.toPersistentList()
            )
            assertThat(state.protonEmailState).isEqualTo(expected)
        }
    }

    @Test
    fun `emits breach aliases`() = runTest {
        val now = Clock.System.now().toString()
        val email1 = "email1"
        val email2 = "email2"
        val emails = listOf(email1, email2).map {
            BreachEmail(
                emailId = BreachEmailId.Alias(BreachId(""), ShareId(it), ItemId(it)),
                email = it,
                severity = 1.0,
                name = it,
                createdAt = now,
                publishedAt = now,
                size = 2,
                passwordLastChars = null,
                exposedData = emptyList(),
                isResolved = false,
                actions = emptyList()
            )
        }
        val breaches = emails.map { breachEmail ->
            EmailBreachUiState(
                id = breachEmail.emailId,
                email = breachEmail.email,
                count = breachEmail.size!!.toInt(),
                breachDate = now.let(DateUtils::formatDate).getOrNull(),
                isMonitored = true
            )
        }
        val aliasKey1 = AliasKeyId(
            shareId = ShareId(email1),
            itemId = ItemId(email1),
            alias = email1
        )
        val aliasKey2 = AliasKeyId(
            shareId = ShareId(email2),
            itemId = ItemId(email2),
            alias = email2
        )
        val list = listOf(
            BreachEmail(
                emailId = BreachEmailId.Alias(
                    BreachId(""),
                    ShareId(email1),
                    ItemId(email1)
                ),
                email = email1,
                severity = 1.0,
                name = email1,
                createdAt = Clock.System.now().toString(),
                publishedAt = Clock.System.now().toString(),
                size = 2,
                passwordLastChars = null,
                exposedData = emptyList(),
                isResolved = false,
                actions = emptyList()
            ),
            BreachEmail(
                emailId = BreachEmailId.Alias(
                    BreachId(""),
                    ShareId(email2),
                    ItemId(email2)
                ),
                email = email2,
                severity = 1.0,
                name = email2,
                createdAt = Clock.System.now().toString(),
                publishedAt = Clock.System.now().toString(),
                size = 2,
                passwordLastChars = null,
                exposedData = emptyList(),
                isResolved = false,
                actions = emptyList()
            )
        )
        val map = mapOf(
            aliasKey1 to AliasData(list, true),
            aliasKey2 to AliasData(list, true)
        )
        observeBreachAliasEmails.emit(map)

        instance.state.test {
            val state = awaitItem()
            val expected = DarkWebEmailBreachState.Success(
                enabledMonitoring = true,
                emails = breaches.toPersistentList()
            )
            assertThat(state.aliasEmailState).isEqualTo(expected)
        }
    }

    @Test
    fun `emits breach custom emails`() = runTest {
        val emails = listOf("email1", "email2").map {
            CustomEmailSuggestion(
                email = it,
                usedInLoginsCount = 2
            )
        }

        val addedVerified = BreachCustomEmailMother.random().copy(verified = true)
        val addedNotVerified = BreachCustomEmailMother.random().copy(verified = false)
        val alreadyAdded = listOf(addedVerified, addedNotVerified)

        val expectedAlreadyAdded = listOf(
            CustomEmailUiState(
                email = addedVerified.email,
                status = CustomEmailUiStatus.Verified(
                    id = addedVerified.id,
                    breachesDetected = addedVerified.breachCount
                )
            ),
            CustomEmailUiState(
                email = addedNotVerified.email,
                status = CustomEmailUiStatus.Unverified(
                    id = addedNotVerified.id
                )
            )
        )


        val expectedSuggestions = emails.map { breachEmail ->
            CustomEmailUiState(
                email = breachEmail.email,
                status = CustomEmailUiStatus.Suggestion(
                    usedInLoginsCount = breachEmail.usedInLoginsCount,
                    isLoadingState = IsLoadingState.NotLoading
                )
            )
        }

        observeBreachCustomEmails.emit(alreadyAdded)
        observeCustomEmailSuggestions.emitResult(emails)

        instance.state.test {
            val state = awaitItem()
            val expected = DarkWebCustomEmailsState.Success(
                emails = expectedAlreadyAdded.toPersistentList(),
                suggestions = expectedSuggestions.toPersistentList()
            )
            assertThat(state.customEmailState).isEqualTo(expected)
        }
    }

    @Suppress("LongMethod")
    @Test
    fun `does not send duplicates for custom emails`() = runTest {
        // Test data
        val protonAddress1 = "some@proton.address1"
        val protonAddress2 = "some@proton.address2"
        val protonAddressesData = listOf(protonAddress1, protonAddress2)

        val aliasEmail1 = "some@alias.address1"
        val aliasEmail2 = "some@alias.address2"
        val aliasesData = listOf(aliasEmail1, aliasEmail2)

        val customEmail1 = "some@custom.address1"
        val customEmail2 = "some@custom.address2"
        val customEmail3 = "some@custom.address3"
        val customEmailsData = listOf(customEmail1, customEmail2, customEmail3)

        val suggestion1 = "some@suggestion.test1"
        val suggestion2 = "some@suggestion.test2"

        val aliasKey1 = AliasKeyId(
            shareId = ShareId(aliasEmail1),
            itemId = ItemId(aliasEmail1),
            alias = aliasEmail1
        )
        val aliasKey2 = AliasKeyId(
            shareId = ShareId(aliasEmail2),
            itemId = ItemId(aliasEmail2),
            alias = aliasEmail2
        )
        val list = listOf(
            BreachEmail(
                emailId = BreachEmailId.Alias(
                    BreachId(""),
                    ShareId(aliasEmail1),
                    ItemId(aliasEmail1)
                ),
                email = aliasEmail1,
                severity = 1.0,
                name = aliasEmail1,
                createdAt = Clock.System.now().toString(),
                publishedAt = Clock.System.now().toString(),
                size = 2,
                passwordLastChars = null,
                exposedData = emptyList(),
                isResolved = false,
                actions = emptyList()
            ),
            BreachEmail(
                emailId = BreachEmailId.Alias(
                    BreachId(""),
                    ShareId(aliasEmail2),
                    ItemId(aliasEmail2)
                ),
                email = aliasEmail2,
                severity = 1.0,
                name = aliasEmail2,
                createdAt = Clock.System.now().toString(),
                publishedAt = Clock.System.now().toString(),
                size = 2,
                passwordLastChars = null,
                exposedData = emptyList(),
                isResolved = false,
                actions = emptyList()
            )
        )
        val map = mapOf(
            aliasKey1 to AliasData(list, true),
            aliasKey2 to AliasData(list, true)
        )
        observeBreachAliasEmails.emit(map)

        // Setup proton addresses
        val protonAddressEmails = protonAddressesData.map {
            BreachProtonEmail(
                addressId = AddressId(it),
                email = it,
                breachCounter = 1,
                flags = 0,
                lastBreachTime = 0
            )
        }
        val protonAddressBreaches = protonAddressEmails.map { breachEmail ->
            EmailBreachUiState(
                id = BreachEmailId.Proton(
                    id = BreachId(breachEmail.email),
                    addressId = breachEmail.addressId
                ),
                email = breachEmail.email,
                count = breachEmail.breachCounter,
                breachDate = breachEmail.lastBreachTime?.let(DateUtils::formatDate)?.getOrNull(),
                isMonitored = true
            )
        }
        observeBreachProtonEmails.emit(protonAddressEmails)

        // Setup aliases
        val now = Clock.System.now().toString()
        val aliasEmails = aliasesData.map {
            BreachEmail(
                emailId = BreachEmailId.Alias(BreachId(""), ShareId(it), ItemId(it)),
                email = it,
                severity = 1.0,
                name = it,
                createdAt = now,
                publishedAt = now,
                size = 2,
                passwordLastChars = null,
                exposedData = emptyList(),
                isResolved = false,
                actions = emptyList()

            )
        }
        val aliasBreaches = aliasEmails.map { breachEmail ->
            EmailBreachUiState(
                id = breachEmail.emailId,
                email = breachEmail.email,
                count = breachEmail.size!!.toInt(),
                breachDate = now.let(DateUtils::formatDate).getOrNull(),
                isMonitored = true
            )
        }

        // Setup added custom emails
        val customEmailBreaches = customEmailsData.map {
            BreachCustomEmailMother.random().copy(email = it, verified = true)
        }
        observeBreachCustomEmails.emit(customEmailBreaches)


        // Setup suggestions
        val suggestionUsedInLoginsCount = 2
        val suggestions = listOf(
            suggestion1,

            // Replicate this one 3 times to check dedup
            suggestion2,
            suggestion2,
            suggestion2,

            protonAddress1,
            aliasEmail1,
            customEmail1,
            customEmail2,
            customEmail3
        )
        val suggestionBreaches = suggestions.map {
            CustomEmailSuggestion(
                email = it,
                usedInLoginsCount = suggestionUsedInLoginsCount
            )
        }
        observeCustomEmailSuggestions.emitResult(suggestionBreaches)


        // Test
        val expectedProtonBreachState = DarkWebEmailBreachState.Success(
            enabledMonitoring = true,
            emails = protonAddressBreaches.toPersistentList()
        )
        val expectedProtonAliasState = DarkWebEmailBreachState.Success(
            enabledMonitoring = true,
            emails = aliasBreaches.toPersistentList()
        )

        val expectedCustomEmailsState = DarkWebCustomEmailsState.Success(
            emails = customEmailBreaches.map {
                CustomEmailUiState(
                    email = it.email,
                    status = CustomEmailUiStatus.Verified(
                        id = it.id,
                        breachesDetected = it.breachCount
                    )
                )
            }.toPersistentList(),

            // Only suggestion1 and suggestion2 should appear
            // The others are duplicated
            suggestions = listOf(suggestion1, suggestion2).map {
                CustomEmailUiState(
                    email = it,
                    status = CustomEmailUiStatus.Suggestion(
                        usedInLoginsCount = suggestionUsedInLoginsCount,
                        isLoadingState = IsLoadingState.NotLoading
                    )
                )
            }.toPersistentList()
        )
        instance.state.test {
            val state = awaitItem()

            assertThat(state.protonEmailState).isEqualTo(expectedProtonBreachState)
            assertThat(state.aliasEmailState).isEqualTo(expectedProtonAliasState)
            assertThat(state.customEmailState).isEqualTo(expectedCustomEmailsState)
        }

    }

}
