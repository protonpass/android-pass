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
        isGroupOwner: Boolean
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
        isGroupOwner: Boolean
    ): GroupInviteCryptoContext {
        PassLogger.d(TAG, "Resolving group invite crypto context:")
        PassLogger.d(TAG, "  userId=$userId, groupId=$groupId, isGroupOwner=$isGroupOwner")
        PassLogger.d(TAG, "  inviterEmail=$inviterEmail")

        val user = userRepository.getUser(userId)

        val openerKeys: List<PrivateKey> = buildList {
            if (isGroupOwner) {
                PassLogger.d(TAG, "Using group owner path - will use user keys")
                val allUserKeys = user.keys.map { it.privateKey }
                val activeUserKeys = allUserKeys.filter { it.isActive }
                PassLogger.d(TAG, "Total user keys: ${allUserKeys.size}, Active: ${activeUserKeys.size}")

                if (activeUserKeys.isEmpty()) {
                    val message = if (allUserKeys.isNotEmpty()) {
                        "User has ${allUserKeys.size} key(s) but none are active"
                    } else {
                        "User has no private keys"
                    }
                    PassLogger.w(TAG, message)
                    error("Group admin cannot access invite: $message. Please activate your keys and try again.")
                }
                addAll(activeUserKeys)
            } else {
                PassLogger.i(TAG, "Using admin path - will use organization key")
                val orgKey = fetchOrganizationPrivateKey(userId, user)
                val keyPreview = orgKey.key.take(50).replace("\n", "")
                PassLogger.d(TAG, "  Org key: $keyPreview... (primary=${orgKey.isPrimary}, active=${orgKey.isActive})")
                add(orgKey)
            }
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

    private suspend fun fetchOrganizationPrivateKey(userId: UserId, user: User): PrivateKey {
        val organizationKey = fetchWithForceRefresh(
            tag = TAG,
            initial = { organizationKeyRepository.getOrganizationKey(userId) },
            refresh = { organizationKeyRepository.getOrganizationKey(userId, true) }
        )

        if (organizationKey == null) {
            PassLogger.w(TAG, "Organization key not found for user ${userId.id}")
            error("Organization key not found. This user may not have organization access. Please sync and try again.")
        }

        return openOrganizationKey(user, organizationKey)
            .onFailure { error ->
                PassLogger.e(TAG, error, "Failed to unlock organization key for user ${userId.id}")
                throw IllegalStateException(
                    "Cannot unlock organization key. Please verify your master password and try again.",
                    error
                )
            }
            .getOrThrow()
            .first
    }

    companion object {
        private const val TAG = "ResolveGroupInviteCryptoContext"
    }
}
