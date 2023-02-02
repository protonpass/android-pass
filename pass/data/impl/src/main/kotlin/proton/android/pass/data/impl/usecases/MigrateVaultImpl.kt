package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.map
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.MigrateVault
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemState
import proton.pass.domain.ItemType
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton_pass_item_v1.ItemV1
import javax.inject.Inject

class MigrateVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val itemRepository: ItemRepository,
    private val encryptionContextProvider: EncryptionContextProvider
) : MigrateVault {

    override suspend fun invoke(origin: ShareId, dest: ShareId): LoadingResult<Unit> =
        accountManager.getPrimaryUserId()
            .filterNotNull()
            .flatMapLatest { userId ->
                onUserIdReceived(userId, origin, dest)
            }
            .first()

    private fun onUserIdReceived(
        userId: UserId,
        origin: ShareId,
        dest: ShareId
    ) = itemRepository.observeItems(
        userId = userId,
        shareSelection = ShareSelection.Share(origin),
        itemState = ItemState.Active,
        itemTypeFilter = ItemTypeFilter.All
    )
        .filterIsInstance<LoadingResult.Success<List<Item>>>()
        .map { result ->
            onObserveItemsResultReceived(result, userId, origin, dest)
        }

    private suspend fun onObserveItemsResultReceived(
        result: LoadingResult<List<Item>>,
        userId: UserId,
        origin: ShareId,
        dest: ShareId
    ): LoadingResult<Unit> = result
        .map { items ->
            shareRepository.getById(userId, dest)
                .flatMap { destShare: Share? ->
                    destShare ?: throw ShareNotAvailableError()
                    onDestinationVaultReceived(items, userId, destShare)
                }
        }
        .map {
            return shareRepository.deleteVault(userId, origin)
        }

    private suspend fun onDestinationVaultReceived(
        items: List<Item>,
        userId: UserId,
        destShare: Share
    ) = encryptionContextProvider.withEncryptionContextSuspendable {
        withContext(Dispatchers.IO) {
            recreateItems(
                items = items,
                coroutineScope = this,
                encryptionContext = this@withEncryptionContextSuspendable,
                userId = userId,
                destShare = destShare
            )
        }
    }

    private suspend fun recreateItems(
        items: List<Item>,
        coroutineScope: CoroutineScope,
        encryptionContext: EncryptionContext,
        userId: UserId,
        destShare: Share
    ): LoadingResult<Unit> {
        val results = items
            // Filter Aliases due to not being implemented in BE currently
            .filter { it.itemType !is ItemType.Alias }
            .map { item ->
                coroutineScope.async {
                    val itemContents: ItemContents = when (item.itemType) {
                        is ItemType.Alias -> throw NotImplementedError()
                        is ItemType.Login -> {
                            val decrypted = encryptionContext.decrypt(item.content)
                            val parsed = ItemV1.Item.parseFrom(decrypted)
                            ItemContents.Login(
                                title = encryptionContext.decrypt(item.title),
                                note = encryptionContext.decrypt(item.note),
                                username = parsed.content.login.username,
                                password = parsed.content.login.password,
                                urls = parsed.content.login.urlsList,
                                packageNames = parsed.platformSpecific.android.allowedAppsList
                                    .map { it.packageName }
                                    .toSet(),
                                primaryTotp = parsed.content.login.totpUri,
                                extraTotpSet = parsed.extraFieldsList
                                    .filter { it.hasTotp() }
                                    .map { it.totp.totpUri }
                                    .toSet()
                            )
                        }
                        is ItemType.Note -> ItemContents.Note(
                            title = encryptionContext.decrypt(item.title),
                            note = encryptionContext.decrypt(item.note)
                        )
                        ItemType.Password -> throw NotImplementedError()
                    }
                    itemRepository.createItem(
                        userId = userId,
                        share = destShare,
                        contents = itemContents
                    )
                }
            }
            .awaitAll()
        return if (results.any { it is LoadingResult.Error }) {
            results.filterIsInstance<LoadingResult.Error>().first()
        } else {
            LoadingResult.Success(Unit)
        }
    }
}
