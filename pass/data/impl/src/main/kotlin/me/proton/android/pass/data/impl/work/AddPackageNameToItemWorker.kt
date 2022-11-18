package me.proton.android.pass.data.impl.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.android.pass.data.api.repositories.ItemRepository
import me.proton.android.pass.data.api.usecases.UpdateAutofillItemData
import me.proton.android.pass.log.PassLogger
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.flatMap
import me.proton.pass.common.api.toOption
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.common.api.Result as KResult

@HiltWorker
class AddPackageNameToItemWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val itemRepository: ItemRepository
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting work")
        val res = getData(workerParameters.inputData).flatMap { run(it) }

        return when (res) {
            is KResult.Error -> {
                PassLogger.e(
                    TAG,
                    res.exception!!,
                    "Error adding package to item"
                )
                Result.failure()
            }
            is KResult.Success -> {
                PassLogger.i(
                    TAG,
                    "Added package to item [itemId=${res.data.id}]"
                )
                Result.success()
            }
            KResult.Loading -> {
                // no-op, should never happen
                Result.failure()
            }
        }
    }

    private suspend fun run(inputData: InputData): KResult<Item> {
        PassLogger.d(
            TAG,
            "Adding package to item [itemId=${inputData.itemId}] [packageName=${inputData.packageName}]"
        )
        return itemRepository.addPackageAndUrlToItem(
            inputData.shareId,
            inputData.itemId,
            inputData.packageName,
            inputData.url
        )
    }

    private fun getData(inputData: Data): KResult<InputData> {
        val shareId = inputData.getString(ARG_SHARE_ID) ?: return KResult.Error(
            IllegalStateException("Missing $ARG_SHARE_ID")
        )
        val itemId = inputData.getString(ARG_ITEM_ID) ?: return KResult.Error(
            IllegalStateException("Missing $ARG_ITEM_ID")
        )
        val packageName = inputData.getString(ARG_PACKAGE_NAME).toOption().map { PackageName(it) }
        val url = inputData.getString(ARG_URL).toOption()

        if (url.isEmpty() && packageName.isEmpty()) {
            return KResult.Error(
                IllegalStateException("Did not receive neither package name nor url")
            )
        }

        return KResult.Success(
            InputData(
                shareId = ShareId(shareId),
                itemId = ItemId(itemId),
                packageName = packageName,
                url
            )
        )
    }

    internal data class InputData(
        val shareId: ShareId,
        val itemId: ItemId,
        val packageName: Option<PackageName>,
        val url: Option<String>
    )

    companion object {

        private const val TAG = "AddPackageNameToItemWorker"

        private const val ARG_SHARE_ID = "arg_share_id"
        private const val ARG_ITEM_ID = "arg_item_id"
        private const val ARG_PACKAGE_NAME = "arg_package_name"
        private const val ARG_URL = "arg_url"

        fun create(shareId: ShareId, itemId: ItemId, data: UpdateAutofillItemData): Data {
            val extras = mutableMapOf(
                ARG_SHARE_ID to shareId.id,
                ARG_ITEM_ID to itemId.id
            )

            if (data.packageName is Some) {
                val packageName = (data.packageName as Some<PackageName>).value
                extras.put(ARG_PACKAGE_NAME, packageName.packageName)
            }

            if (data.url is Some) {
                val url = (data.url as Some<String>).value
                extras.put(ARG_URL, url)
            }

            return Data.Builder()
                .putAll(extras.toMap())
                .build()
        }
    }
}
