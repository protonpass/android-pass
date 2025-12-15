package proton.android.pass.data.impl.crypto

import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.KeyFlags
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicAddressKey
import me.proton.core.key.domain.entity.key.PublicKey
import proton.android.pass.crypto.api.usecases.invites.EncryptedGroupInviteAcceptKey
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeAcceptGroupInvite
import proton.android.pass.data.impl.db.entities.GroupInviteEntity
import proton.android.pass.data.impl.db.entities.GroupInviteKeyEntity
import proton.android.pass.data.impl.local.GroupInviteAndKeysEntity
import proton.android.pass.data.impl.requests.invites.InviteKeyRotation
import proton.android.pass.data.impl.responses.invites.GroupInviteApiModel
import proton.android.pass.data.impl.responses.invites.KeyApiModel
import proton.android.pass.data.impl.responses.invites.VaultDataApiModel
import proton.android.pass.domain.GroupId
import proton.android.pass.test.domain.GroupTestFactory
import proton.android.pass.test.domain.UserTestFactory
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ReencryptGroupInviteContentsTest {

    private val user = UserTestFactory.create(userId = USER_ID)
    private val group = GroupTestFactory.create(id = GROUP_ID, email = GROUP_EMAIL)

    @Test
    fun reencryptContentUsesFirstKey() = runTest {
        val acceptGroupInvite = FakeAcceptGroupInvite(
            listOf(
                keyWithRotation(1),
                keyWithRotation(3)
            )
        )

        val sut = ReencryptGroupInviteContentsImpl(
            acceptGroupInvite = acceptGroupInvite,
            inviteContentReencrypter = InviteContentReencrypter(FakeEncryptionContextProvider()),
            resolveGroupInviteCryptoContext = FakeResolveGroupInviteCryptoContext(defaultCryptoContext())
        )

        val invite = groupInviteApiModel(
            keys = listOf(KeyApiModel(1, "k1"), KeyApiModel(3, "k3")),
            vaultData = VaultDataApiModel(
                content = "",
                contentKeyRotation = 0,
                contentFormatVersion = 1,
                memberCount = 0,
                itemCount = 0
            )
        )

        val result = sut.invoke(USER_ID, invite)

        val expectedLocalKey = FakeEncryptionContext.encrypt(byteArrayOf(1.toByte()))
        assertContentEquals(expectedLocalKey.array, result.localEncryptedKey.array)
        assertEquals(1, acceptGroupInvite.lastInviterKeys?.size)
    }

    @Test
    fun encryptKeysPreservesOrder() = runTest {
        val acceptGroupInvite = FakeAcceptGroupInvite(
            listOf(
                keyWithRotation(5),
                keyWithRotation(2)
            )
        )

        val sut = EncryptGroupInviteKeysImpl(
            acceptGroupInvite = acceptGroupInvite,
            resolveGroupInviteCryptoContext = FakeResolveGroupInviteCryptoContext(defaultCryptoContext())
        )

        val inviteEntity = GroupInviteEntity(
            inviteId = "invite-id",
            userId = USER_ID.id,
            inviterUserId = "inviter-user",
            inviterEmail = INVITER_EMAIL,
            invitedGroupId = GROUP_ID.id,
            invitedEmail = GROUP_EMAIL,
            targetType = 0,
            targetId = "target-id",
            remindersSent = 0,
            inviteToken = "token",
            invitedAddressId = "address-id",
            memberCount = 0,
            itemCount = 0,
            shareContent = "",
            shareContentKeyRotation = 0,
            shareContentFormatVersion = 1,
            createTime = 0,
            isGroupOwner = false,
            encryptedContent = FakeEncryptionContext.encrypt(ByteArray(0))
        )
        val groupKeys = listOf(
            GroupInviteKeyEntity("invite-id", "k5", 5, 0),
            GroupInviteKeyEntity("invite-id", "k2", 2, 0)
        )

        val result = sut.invoke(USER_ID, GroupInviteAndKeysEntity(inviteEntity, groupKeys))

        val rotations = result.map(InviteKeyRotation::keyRotation)
        assertEquals(listOf(5L, 2L), rotations)
    }

    private fun keyWithRotation(rotation: Long): EncryptedGroupInviteAcceptKey = EncryptedGroupInviteAcceptKey(
        keyRotation = rotation,
        key = "key-$rotation",
        localEncryptedKey = FakeEncryptionContext.encrypt(byteArrayOf(rotation.toByte()))
    )

    private fun inviterKeys(): List<PublicAddressKey> = listOf(
        PublicAddressKey(
            email = INVITER_EMAIL,
            flags = KeyFlags.NotCompromised or KeyFlags.NotObsolete,
            publicKey = PublicKey(
                key = INVITER_PUBLIC_KEY,
                isPrimary = true,
                isActive = true,
                canEncrypt = true,
                canVerify = true
            )
        )
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

    private fun defaultCryptoContext() = GroupInviteCryptoContext(
        user = user,
        groupPrivateKeys = group.address!!.keys!!,
        openerKeys = listOf(orgKeyPair().first),
        inviterPublicKeys = inviterKeys().map { it.publicKey }
    )

    private class FakeResolveGroupInviteCryptoContext(
        private val ctx: GroupInviteCryptoContext
    ) : ResolveGroupInviteCryptoContext {
        override suspend fun invoke(
            userId: UserId,
            groupId: GroupId,
            inviterEmail: String,
            isGroupAdmin: Boolean
        ): GroupInviteCryptoContext = ctx
    }

    private fun groupInviteApiModel(keys: List<KeyApiModel>, vaultData: VaultDataApiModel?) = GroupInviteApiModel(
        inviteId = "invite-id",
        inviterUserId = "inviter-user",
        inviterEmail = INVITER_EMAIL,
        invitedGroupId = GROUP_ID.id,
        invitedEmail = GROUP_EMAIL,
        targetType = 0,
        targetId = "target-id",
        remindersSent = 0,
        inviteToken = "token",
        invitedAddressId = "address-id",
        keys = keys,
        vaultData = vaultData,
        isGroupOwner = false,
        createTime = 0
    )

    companion object {
        private val USER_ID = UserId("user-id")
        private val GROUP_ID = GroupId("group-id")
        private const val GROUP_EMAIL = "group@test.com"
        private const val INVITER_EMAIL = "inviter@test.com"
        private const val INVITER_PUBLIC_KEY = "inviter-public-key"
    }
}
