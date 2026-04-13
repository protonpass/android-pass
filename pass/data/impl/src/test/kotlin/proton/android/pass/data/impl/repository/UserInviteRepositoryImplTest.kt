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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.FakeGroupRepository
import proton.android.pass.data.fakes.usecases.FakeObserveConfirmedInviteToken
import proton.android.pass.data.impl.crypto.EncryptUserInviteKeys
import proton.android.pass.data.impl.crypto.ReencryptUserInviteContents
import proton.android.pass.data.impl.db.entities.UserInviteEntity
import proton.android.pass.data.impl.fakes.FakeCreateNewUserInviteSignature
import proton.android.pass.data.impl.fakes.FakeEncryptShareKeysForUser
import proton.android.pass.data.impl.fakes.FakeRemoteUserInviteDataSource
import proton.android.pass.data.impl.fakes.FakeShareKeyRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.data.impl.local.LocalUserInviteDataSource
import proton.android.pass.data.impl.local.UserInviteAndKeysEntity
import proton.android.pass.data.impl.repositories.UserInviteRepositoryImpl
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.data.impl.responses.PendingUserInviteResponse
import proton.android.pass.domain.Group
import proton.android.pass.domain.GroupAddress
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.InviteToken
import proton.android.pass.domain.RecommendedGroup
import proton.android.pass.domain.ShareId
import proton.android.pass.preferences.FakeFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.FeatureFlag

class UserInviteRepositoryImplTest {

    private lateinit var repository: UserInviteRepositoryImpl
    private lateinit var groupRepository: FakeGroupRepository
    private lateinit var featureFlags: FakeFeatureFlagsPreferenceRepository
    private lateinit var remoteDataSource: FakeRemoteUserInviteDataSource

    @Before
    fun setup() {
        groupRepository = FakeGroupRepository()
        featureFlags = FakeFeatureFlagsPreferenceRepository()
        remoteDataSource = FakeRemoteUserInviteDataSource()
        featureFlags.set(FeatureFlag.PASS_GROUP_SHARE, true)

        repository = UserInviteRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDatasource = StubLocalUserInviteDataSource,
            encryptionContextProvider = FakeEncryptionContextProvider(),
            reencryptUserInviteContents = StubReencryptUserInviteContents,
            encryptUserInviteKeys = StubEncryptUserInviteKeys,
            observeConfirmedInviteToken = FakeObserveConfirmedInviteToken(),
            groupRepository = groupRepository,
            featureFlagsPreferencesRepository = featureFlags,
            shareRepository = FakeShareRepository(),
            encryptShareKeysForUser = FakeEncryptShareKeysForUser(),
            shareKeyRepository = FakeShareKeyRepository(),
            newUserInviteSignatureManager = FakeCreateNewUserInviteSignature()
        )
    }

    @Test
    fun `groups are not filtered when startsWith is null`() = runTest {
        groupRepository.groups = listOf(
            group(id = "g1", name = "Engineering", email = "engineering@proton.me"),
            group(id = "g2", name = "Marketing", email = "marketing@proton.me")
        )

        val result = repository.observeInviteRecommendations(
            userId = UserId(USER_ID),
            shareId = ShareId(SHARE_ID),
            startsWith = null
        ).first()

        val groupEmails = result.organizationItems.filterIsInstance<RecommendedGroup>().map { it.email }
        assertThat(groupEmails).containsExactly("engineering@proton.me", "marketing@proton.me")
    }

    @Test
    fun `groups are filtered by name prefix when startsWith is set`() = runTest {
        groupRepository.groups = listOf(
            group(id = "g1", name = "Engineering", email = "engineering@proton.me"),
            group(id = "g2", name = "Marketing", email = "marketing@proton.me"),
            group(id = "g3", name = "Engineering EMEA", email = "eng-emea@proton.me")
        )

        val result = repository.observeInviteRecommendations(
            userId = UserId(USER_ID),
            shareId = ShareId(SHARE_ID),
            startsWith = "eng"
        ).first()

        val groupEmails = result.organizationItems.filterIsInstance<RecommendedGroup>().map { it.email }
        assertThat(groupEmails).containsExactly("engineering@proton.me", "eng-emea@proton.me")
    }

    @Test
    fun `groups are filtered by email prefix when startsWith matches email but not name`() = runTest {
        groupRepository.groups = listOf(
            group(id = "g1", name = "Team Alpha", email = "alpha@proton.me"),
            group(id = "g2", name = "Team Beta", email = "beta@proton.me")
        )

        val result = repository.observeInviteRecommendations(
            userId = UserId(USER_ID),
            shareId = ShareId(SHARE_ID),
            startsWith = "alpha"
        ).first()

        val groupEmails = result.organizationItems.filterIsInstance<RecommendedGroup>().map { it.email }
        assertThat(groupEmails).containsExactly("alpha@proton.me")
    }

    @Test
    fun `group filter is case insensitive`() = runTest {
        groupRepository.groups = listOf(
            group(id = "g1", name = "Engineering", email = "engineering@proton.me"),
            group(id = "g2", name = "Marketing", email = "marketing@proton.me")
        )

        val result = repository.observeInviteRecommendations(
            userId = UserId(USER_ID),
            shareId = ShareId(SHARE_ID),
            startsWith = "ENG"
        ).first()

        val groupEmails = result.organizationItems.filterIsInstance<RecommendedGroup>().map { it.email }
        assertThat(groupEmails).containsExactly("engineering@proton.me")
    }

    @Test
    fun `no groups returned when startsWith matches nothing`() = runTest {
        groupRepository.groups = listOf(
            group(id = "g1", name = "Engineering", email = "engineering@proton.me"),
            group(id = "g2", name = "Marketing", email = "marketing@proton.me")
        )

        val result = repository.observeInviteRecommendations(
            userId = UserId(USER_ID),
            shareId = ShareId(SHARE_ID),
            startsWith = "xyz"
        ).first()

        val groups = result.organizationItems.filterIsInstance<RecommendedGroup>()
        assertThat(groups).isEmpty()
    }

    // region helpers

    private fun group(
        id: String,
        name: String,
        email: String
    ) = Group(
        id = GroupId(id),
        name = name,
        address = GroupAddress(id = id, email = email),
        permissions = 0,
        createTime = 0L,
        flags = 0,
        groupVisibility = 0,
        memberVisibility = 0,
        description = null
    )

    private object StubLocalUserInviteDataSource : LocalUserInviteDataSource {
        override suspend fun storeInvites(invites: List<UserInviteAndKeysEntity>) = error("not used")
        override suspend fun removeInvites(invites: List<UserInviteEntity>) = error("not used")
        override suspend fun removeInvite(userId: UserId, invite: InviteToken) = error("not used")
        override suspend fun getInvite(userId: UserId, inviteToken: InviteToken): Option<UserInviteEntity> = None
        override fun observeAllInvites(userId: UserId): Flow<List<UserInviteEntity>> = error("not used")
        override suspend fun getInviteWithKeys(
            userId: UserId,
            inviteToken: InviteToken
        ): Option<UserInviteAndKeysEntity> = None
    }

    private object StubReencryptUserInviteContents : ReencryptUserInviteContents {
        override suspend fun invoke(userId: UserId, invite: PendingUserInviteResponse) = error("not used")
    }

    private object StubEncryptUserInviteKeys : EncryptUserInviteKeys {
        override suspend fun invoke(userId: UserId, invite: UserInviteAndKeysEntity): List<InviteKeyRotation> =
            error("not used")
    }

    // endregion

    companion object {
        private const val USER_ID = "UserInviteRepositoryImplTest-UserId"
        private const val SHARE_ID = "UserInviteRepositoryImplTest-ShareId"
    }
}
