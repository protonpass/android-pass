package me.proton.core.pass.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.android.pass.log.PassLogger
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.entity.PackageName
import me.proton.core.pass.domain.repositories.ItemRepository
import kotlin.Result as KResult

@HiltWorker
class AddPackageNameToItemWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted private val workerParameters: WorkerParameters,
    private val itemRepository: ItemRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        PassLogger.i(TAG, "Starting work")
        val data = getData(workerParameters.inputData)
        if (data.isFailure) {
            data.exceptionOrNull()?.let {
                PassLogger.e(TAG, it, "Error retrieving data")
            }
            return Result.failure()
        }

        val inputData = data.getOrThrow()
        val result = run(inputData)

        return if (result.isFailure) {
            val e = result.exceptionOrNull()
            PassLogger.e(TAG, e!!, "Error adding package to item [packageName=$inputData.packageName]")
            Result.failure()
        } else {
            PassLogger.i(
                TAG,
                "Added package to item [itemId=${inputData.itemId}] [packageName=${inputData.packageName}]"
            )
            Result.success()
        }
    }

    private suspend fun run(inputData: InputData): KResult<Unit> {
        PassLogger.d(
            TAG,
            "Adding package to item [itemId=${inputData.itemId}] [packageName=${inputData.packageName}]"
        )
        return kotlin.runCatching {
            itemRepository.addPackageToItem(
                inputData.shareId,
                inputData.itemId,
                inputData.packageName
            )
        }
    }

    private fun getData(inputData: Data): KResult<InputData> {
        val shareId = inputData.getString(ARG_SHARE_ID) ?: return KResult.failure(
            IllegalStateException("Missing $ARG_SHARE_ID")
        )
        val itemId = inputData.getString(ARG_ITEM_ID) ?: return KResult.failure(
            IllegalStateException("Missing $ARG_ITEM_ID")
        )
        val packageName = inputData.getString(ARG_PACKAGE_NAME) ?: return KResult.failure(
            IllegalStateException("Missing $ARG_PACKAGE_NAME")
        )

        return KResult.success(
            InputData(
                shareId = ShareId(shareId),
                itemId = ItemId(itemId),
                packageName = PackageName(packageName)
            )
        )
    }

    internal data class InputData(
        val shareId: ShareId,
        val itemId: ItemId,
        val packageName: PackageName
    )

    companion object {

        private const val TAG = "AddPackageNameToItemWorker"

        private const val ARG_SHARE_ID = "arg_share_id"
        private const val ARG_ITEM_ID = "arg_item_id"
        private const val ARG_PACKAGE_NAME = "arg_package_name"

        fun create(shareId: ShareId, itemId: ItemId, packageName: PackageName): Data =
            Data.Builder()
                .putAll(
                    mapOf(
                        ARG_SHARE_ID to shareId.id,
                        ARG_ITEM_ID to itemId.id,
                        ARG_PACKAGE_NAME to packageName.packageName
                    )
                )
                .build()
    }
}
