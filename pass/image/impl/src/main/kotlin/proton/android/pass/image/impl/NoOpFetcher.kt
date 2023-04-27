package proton.android.pass.image.impl

import coil.fetch.FetchResult
import coil.fetch.Fetcher

class NoOpFetcher : Fetcher {
    override suspend fun fetch(): FetchResult? = null
}
