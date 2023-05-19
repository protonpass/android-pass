package proton.android.pass.data.impl.migration

interface DataMigrator {
    suspend fun areMigrationsNeeded(): Boolean
    fun run(): Result<Unit>
}
