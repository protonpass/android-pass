/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.log.api.PassLogger

@HiltWorker
class UpdateAutofillItemWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val itemRepository: ItemRepository,
    private val shareRepository: ShareRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting $TAG attempt $runAttemptCount")
        return getData(workerParameters.inputData)
            .map { inputData: InputData -> executeWork(inputData) }
            .fold(
                onSuccess = { _ ->
                    Result.success().also {
                        PassLogger.i(TAG, "Completed $TAG work")
                    }
                },
                onFailure = { throwable ->
                    Result.failure().also { PassLogger.w(TAG, throwable) }
                }
            )
    }

    private suspend fun executeWork(inputData: InputData) = getUserID(inputData)
        .map { userID ->
            if (inputData.shouldAssociate) {
                updateItemWithPackageNameOrUrl(userID, inputData)
            } else {
                updateLastUsed(userID, inputData)
            }
        }

    private suspend fun updateItemWithPackageNameOrUrl(userId: UserId, inputData: InputData) = safeRunCatching {
        val message = "Adding package and url to item [itemId=${inputData.itemId}]" +
            " [packageInfo=${inputData.packageInfo}] " +
            " [url=${inputData.url}]"
        PassLogger.d(TAG, message)
        itemRepository.addPackageAndUrlToItem(
            userId = userId,
            shareId = inputData.shareId,
            itemId = inputData.itemId,
            packageInfo = inputData.packageInfo,
            url = inputData.url
        )
    }
        .map { updateLastUsed(userId = userId, inputData = inputData) }
        .fold(
            onSuccess = {
                PassLogger.i(
                    TAG,
                    "Successfully added package or url and updated last used item"
                )
            },
            onFailure = {
                PassLogger.w(TAG, "Failed to add package or url and update last used item")
                PassLogger.w(TAG, it)
            }
        )

    private suspend fun getUserID(inputData: InputData) = safeRunCatching {
        val userIdOption = itemRepository.findUserId(inputData.shareId, inputData.itemId)
        userIdOption.value() ?: throw IllegalStateException("User not found")
    }

    private suspend fun updateLastUsed(userId: UserId, inputData: InputData) {
        PassLogger.d(TAG, "Start update last used")
        return safeRunCatching {
            val share = shareRepository.getById(userId, inputData.shareId)
            itemRepository.updateItemLastUsed(share.vaultId, inputData.itemId)
        }.fold(
            onSuccess = {
                PassLogger.d(TAG, "Completed update last used")
            },
            onFailure = {
                PassLogger.w(TAG, "Failed update last used")
                PassLogger.w(TAG, it)
            }
        )
    }

    private fun getData(inputData: Data): kotlin.Result<InputData> {
        val shareId = inputData.getString(ARG_SHARE_ID) ?: return kotlin.Result.failure(
            IllegalStateException("Missing $ARG_SHARE_ID")
        )
        val itemId = inputData.getString(ARG_ITEM_ID) ?: return kotlin.Result.failure(
            IllegalStateException("Missing $ARG_ITEM_ID")
        )
        val packageName = inputData.getString(ARG_PACKAGE_NAME).toOption().map { PackageName(it) }
        val appName = inputData.getString(ARG_APP_NAME).toOption().map { AppName(it) }
        val packageInfo = packageName.map {
            PackageInfo(it, appName.value() ?: AppName(it.value))
        }
        val url = inputData.getString(ARG_URL).toOption()
        val shouldAssociate = inputData.getBoolean(ARG_SHOULD_ASSOCIATE, false)
        if (url.isEmpty() && packageName.isEmpty() && appName.isEmpty()) {
            return kotlin.Result.failure(
                IllegalStateException("Did not receive neither package name nor url")
            )
        }

        return kotlin.Result.success(
            InputData(
                shareId = ShareId(shareId),
                itemId = ItemId(itemId),
                packageInfo = packageInfo,
                url = url,
                shouldAssociate = shouldAssociate
            )
        )
    }

    internal data class InputData(
        val shareId: ShareId,
        val itemId: ItemId,
        val packageInfo: Option<PackageInfo>,
        val url: Option<String>,
        val shouldAssociate: Boolean
    )

    companion object {

        private const val TAG = "AddPackageNameToItemWorker"

        private const val ARG_SHARE_ID = "arg_share_id"
        private const val ARG_ITEM_ID = "arg_item_id"
        private const val ARG_PACKAGE_NAME = "arg_package_name"
        private const val ARG_APP_NAME = "arg_app_name"
        private const val ARG_URL = "arg_url"
        private const val ARG_SHOULD_ASSOCIATE = "arg_should_associate"

        fun create(data: UpdateAutofillItemData): Data {
            val extras = mutableMapOf(
                ARG_SHARE_ID to data.shareId.id,
                ARG_ITEM_ID to data.itemId.id,
                ARG_SHOULD_ASSOCIATE to data.shouldAssociate
            )

            val packageInfoOption = data.packageInfo
            if (packageInfoOption is Some && packageInfoOption.value.packageName.value.isNotBlank()) {
                extras[ARG_PACKAGE_NAME] = packageInfoOption.value.packageName.value
                extras[ARG_APP_NAME] = packageInfoOption.value.appName.value
            }

            val url = data.url
            if (url is Some && url.value.isNotBlank()) {
                extras[ARG_URL] = url.value
            }

            return Data.Builder()
                .putAll(extras.toMap())
                .build()
        }
    }
}
