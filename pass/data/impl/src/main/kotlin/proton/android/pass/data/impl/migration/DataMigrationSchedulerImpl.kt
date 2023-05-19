package proton.android.pass.data.impl.migration

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.runBlocking
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataMigrationSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val dataMigrator: DataMigrator
) : DataMigrationScheduler {

    override fun schedule() {

        val areMigrationsNeeded = runBlocking { dataMigrator.areMigrationsNeeded() }
        if (!areMigrationsNeeded) {
            PassLogger.d(TAG, "Not scheduling DataMigrationWorker, no migrations needed")
        } else {
            PassLogger.d(TAG, "Scheduling DataMigrationWorker, migrations needed")
            val request = OneTimeWorkRequestBuilder<DataMigrationWorker>().build()
            workManager.enqueue(request)
        }
    }

    companion object {
        private const val TAG = "DataMigrationSchedulerImpl"
    }

}
