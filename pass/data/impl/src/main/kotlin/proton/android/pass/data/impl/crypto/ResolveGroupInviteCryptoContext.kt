package proton.android.pass.data.impl.crypto

import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import proton.android.pass.crypto.api.usecases.invites.OpenOrganizationKey
import proton.android.pass.data.api.repositories.GroupRepository
import proton.android.pass.data.api.usecases.GetAllKeysByAddress
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.OrganizationKey
import proton.android.pass.domain.repositories.OrganizationKeyRepository
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

data class GroupInviteCryptoContext(
    val user: User,
    val groupPrivateKeys: List<PrivateAddressKey>,
    val openerKeys: List<PrivateKey>,
    val inviterPublicKeys: List<PublicKey>
)

interface ResolveGroupInviteCryptoContext {
    suspend operator fun invoke(
        userId: UserId,
        groupId: GroupId,
        inviterEmail: String,
        isGroupAdmin: Boolean
    ): GroupInviteCryptoContext
}

class ResolveGroupInviteCryptoContextImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val organizationKeyRepository: OrganizationKeyRepository,
    private val openOrganizationKey: OpenOrganizationKey,
    private val getAllKeysByAddress: GetAllKeysByAddress
) : ResolveGroupInviteCryptoContext {

    override suspend fun invoke(
        userId: UserId,
        groupId: GroupId,
        inviterEmail: String,
        isGroupAdmin: Boolean
    ): GroupInviteCryptoContext {
        val user = userRepository.getUser(userId)

        val openerKeys: List<PrivateKey> = if (isGroupAdmin) {
            user.keys.map { it.privateKey }.ifEmpty { error("User does not have a private key") }
        } else {
            val organizationKey: OrganizationKey = fetchWithForceRefresh(
                tag = TAG,
                initial = { organizationKeyRepository.getOrganizationKey(userId) },
                refresh = { organizationKeyRepository.getOrganizationKey(userId, true) }
            ) ?: error("Organization key not found")

            val (organizationPrivateKey, _) = openOrganizationKey(user, organizationKey)
                .getOrElse {
                    PassLogger.w(TAG, "Failed to open organization key")
                    PassLogger.w(TAG, it)
                    throw it
                }
            listOf(organizationPrivateKey)
        }

        val group = fetchWithForceRefresh(
            tag = TAG,
            initial = { groupRepository.retrieveGroup(userId, groupId) },
            refresh = { groupRepository.retrieveGroup(userId, groupId, true) }
        ) ?: error("Group not found")

        val groupPrivateKeys = group.address?.keys ?: error("Group doesn't have private keys")

        val inviterPublicKeys = getAllKeysByAddress(inviterEmail)
            .getOrElse {
                PassLogger.w(TAG, "Could not get inviter address keys")
                PassLogger.w(TAG, it)
                throw it
            }
            .map { it.publicKey }

        return GroupInviteCryptoContext(
            user = user,
            groupPrivateKeys = groupPrivateKeys,
            openerKeys = openerKeys,
            inviterPublicKeys = inviterPublicKeys
        )
    }

    companion object {
        private const val TAG = "ResolveGroupInviteCryptoContext"
    }
}
