package proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName

@HiltWorker
class UpdateAutofillItemWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val itemRepository: ItemRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting work")
        return getData(workerParameters.inputData)
            .fold(
                onSuccess = { inputData ->
                    run(inputData).also { PassLogger.i(TAG, "Completed work") }
                },
                onFailure = { throwable ->
                    Result.failure().also { PassLogger.w(TAG, throwable) }
                }
            )
    }

    private suspend fun run(inputData: InputData): Result =
        if (inputData.shouldAssociate) {
            updateItemWithPackageNameOrUrl(inputData)
        } else {
            updateLastUsed(inputData)
        }

    private suspend fun updateItemWithPackageNameOrUrl(
        inputData: InputData
    ): Result {
        val message = "Adding package and url to item [itemId=${inputData.itemId}]" +
            " [packageInfo=${inputData.packageInfo}] " +
            " [url=${inputData.url}]"
        PassLogger.d(TAG, message)

        return runCatching {
            itemRepository.addPackageAndUrlToItem(
                shareId = inputData.shareId,
                itemId = inputData.itemId,
                packageInfo = inputData.packageInfo,
                url = inputData.url
            )
        }.mapCatching {
            updateLastUsed(inputData)
        }.fold(
            onSuccess = {
                val successMessage =
                    "Successfully added package or url and updated last used item"
                PassLogger.i(TAG, successMessage)
                Result.success()
            },
            onFailure = {
                PassLogger.e(TAG, it, "Failed to add package or url and update last used item")
                Result.failure()
            }
        )
    }

    private suspend fun updateLastUsed(inputData: InputData): Result {
        PassLogger.d(TAG, "Start update last used")
        return runCatching {
            itemRepository.updateItemLastUsed(
                inputData.shareId,
                inputData.itemId
            )
        }.fold(
            onSuccess = {
                PassLogger.d(TAG, "Completed update last used")
                Result.success()
            },
            onFailure = {
                PassLogger.w(TAG, it, "Failed update last used")
                Result.failure()
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
                ARG_SHOULD_ASSOCIATE to data.shouldAssociate,
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
