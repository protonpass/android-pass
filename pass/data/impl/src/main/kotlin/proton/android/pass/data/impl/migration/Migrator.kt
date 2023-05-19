package proton.android.pass.data.impl.migration

interface Migrator {
    val migrationName: String

    suspend fun migrate()
}
