package proton.android.pass.data.api.repositories

interface TelemetryRepository {
    suspend fun storeEntry(
        event: String,
        dimensions: Map<String, String>
    )

    suspend fun sendEvents()
}
