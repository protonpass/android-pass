package proton.android.pass.data.impl.crypto

import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyFlags
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.UserKey
import proton.android.pass.account.fakes.FakeUserRepository
import proton.android.pass.data.fakes.repositories.FakeGroupRepository
import proton.android.pass.data.fakes.usecases.FakeGetAllKeysByAddress
import proton.android.pass.data.fakes.usecases.FakeOpenOrganizationKey
import proton.android.pass.data.fakes.usecases.FakeOrganizationKeyRepository
import proton.android.pass.domain.GroupId
import proton.android.pass.test.domain.GroupTestFactory
import proton.android.pass.test.domain.UserTestFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResolveGroupInviteCryptoContextTest {

    private val user = UserTestFactory.create(userId = USER_ID)
    private val group = GroupTestFactory.create(id = GROUP_ID, email = GROUP_EMAIL)
    private val inviterKeys = listOf(
        PublicAddressKey(
            email = INVITER_EMAIL,
            flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
            publicKey = PublicKey(
                key = "inviter-pub",
                isPrimary = true,
                isActive = true,
                canEncrypt = true,
                canVerify = true
            )
        )
    )

    @Test
    fun returnsExpectedContext() = runTest {
        val resolver = resolver(
            groupRepo = FakeGroupRepository().apply { groups = listOf(group) },
            orgRepo = FakeOrganizationKeyRepository(),
            getKeys = FakeGetAllKeysByAddress().apply {
                setResult(Result.success(inviterKeys))
            }
        )

        val ctx = resolver(
            userId = USER_ID,
            groupId = GROUP_ID,
            inviterEmail = INVITER_EMAIL,
            isGroupAdmin = false
        )

        assertEquals(group.address?.keys, ctx.groupPrivateKeys)
        assertEquals(inviterKeys.map { it.publicKey }, ctx.inviterPublicKeys)
        assertEquals("org-priv", ctx.openerKey.key)
        assertEquals(user, ctx.user)
    }

    @Test
    fun throwsWhenOrganizationKeyMissing() = runTest {
        val resolver = resolver(
            orgRepo = FakeOrganizationKeyRepository().apply { organizationKey = null }
        )

        assertFailsWith<IllegalStateException> {
            resolver(USER_ID, GROUP_ID, INVITER_EMAIL, false)
        }
    }

    @Test
    fun throwsWhenGroupMissing() = runTest {
        val resolver = resolver(
            groupRepo = FakeGroupRepository().apply {
                groups = emptyList()
            }
        )

        assertFailsWith<IllegalStateException> {
            resolver(USER_ID, GROUP_ID, INVITER_EMAIL, false)
        }
    }

    @Test
    fun throwsWhenInviterKeysFail() = runTest {
        val resolver = resolver(
            getKeys = FakeGetAllKeysByAddress().apply {
                setResult(Result.failure(IllegalStateException("no keys")))
            }
        )

        assertFailsWith<IllegalStateException> {
            resolver(USER_ID, GROUP_ID, INVITER_EMAIL, false)
        }
    }

    @Test
    fun groupAdminUsesUserKey() = runTest {
        val userKey = PrivateKey(
            key = "user-priv",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = null
        )
        val userWithKey = user.copy(
            keys = listOf(
                UserKey(
                    userId = USER_ID,
                    version = 1,
                    activation = null,
                    active = true,
                    keyId = KeyId("kid"),
                    privateKey = userKey
                )
            )
        )

        val resolver = ResolveGroupInviteCryptoContextImpl(
            userRepository = FakeUserRepository().apply { setUser(userWithKey) },
            groupRepository = FakeGroupRepository().apply { groups = listOf(group) },
            organizationKeyRepository = FakeOrganizationKeyRepository(),
            openOrganizationKey = FakeOpenOrganizationKey(Result.success(orgKeyPair())),
            getAllKeysByAddress = FakeGetAllKeysByAddress().apply { setResult(Result.success(inviterKeys)) }
        )

        val ctx = resolver(USER_ID, GROUP_ID, INVITER_EMAIL, true)
        assertEquals(listOf(userKey), ctx.openerKeys)
    }

    private fun resolver(
        groupRepo: FakeGroupRepository = FakeGroupRepository().apply { groups = listOf(group) },
        orgRepo: FakeOrganizationKeyRepository = FakeOrganizationKeyRepository(),
        openOrg: FakeOpenOrganizationKey = FakeOpenOrganizationKey(Result.success(orgKeyPair())),
        getKeys: FakeGetAllKeysByAddress = FakeGetAllKeysByAddress().apply {
            setResult(Result.success(inviterKeys))
        },
        userRepo: FakeUserRepository = FakeUserRepository().apply { setUser(user) }
    ) = ResolveGroupInviteCryptoContextImpl(
        userRepository = userRepo,
        groupRepository = groupRepo,
        organizationKeyRepository = orgRepo,
        openOrganizationKey = openOrg,
        getAllKeysByAddress = getKeys
    )

    private fun orgKeyPair(): Pair<PrivateKey, PublicKey> = Pair(
        PrivateKey(
            key = "org-priv",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = null
        ),
        PublicKey(
            key = "org-pub",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )
    )

    companion object {
        private val USER_ID = UserId("user-id")
        private val GROUP_ID = GroupId("group-id")
        private const val GROUP_EMAIL = "group@test.com"
        private const val INVITER_EMAIL = "inviter@test.com"
    }
}
