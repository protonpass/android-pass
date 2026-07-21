/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.remote

import proton.android.pass.data.impl.api.CompromisedPasswordApi
import proton.android.pass.log.api.PassLogger
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream
import javax.inject.Inject

interface RemoteCompromisedPasswordDataSource {
    suspend fun getLastChange(): Long?
    suspend fun getCompromisedSuffixes(prefix: String, etag: String?): PrefixQueryResult
}

sealed interface PrefixQueryResult {
    data class Ok(val etag: String?, val suffixes: Set<String>) : PrefixQueryResult
    data object NotModified : PrefixQueryResult
    data object Error : PrefixQueryResult
}

class RemoteCompromisedPasswordDataSourceImpl @Inject constructor(
    private val api: CompromisedPasswordApi
) : RemoteCompromisedPasswordDataSource {

    override suspend fun getLastChange(): Long? = try {
        val response = api.getLastChange()
        if (!response.isSuccessful) {
            null
        } else {
            response.body()?.string()?.trim()?.toLongOrNull()
        }
    } catch (e: IOException) {
        PassLogger.w(TAG, "Failed to fetch last_change")
        PassLogger.w(TAG, e)
        null
    }

    override suspend fun getCompromisedSuffixes(prefix: String, etag: String?): PrefixQueryResult {
        val p0 = prefix.substring(0, 2)
        val p1 = prefix.substring(2, 4)
        val p2 = prefix.substring(4, 6)

        val response = try {
            api.getCompromisedSuffixes(
                p0 = p0,
                p1 = p1,
                p2 = p2,
                prefix = prefix,
                ifNoneMatch = etag?.let { stored ->
                    if (stored.startsWith("\"")) stored else "\"$stored\""
                }
            )
        } catch (e: IOException) {
            PassLogger.w(TAG, "Network error fetching compromised password data")
            PassLogger.w(TAG, e)
            return PrefixQueryResult.Error
        }

        return when {
            response.code() == HTTP_NOT_MODIFIED -> PrefixQueryResult.NotModified
            response.isSuccessful -> parseOk(response)
            else -> PrefixQueryResult.Error
        }
    }

    private fun parseOk(response: retrofit2.Response<okhttp3.ResponseBody>): PrefixQueryResult {
        val body = response.body() ?: return PrefixQueryResult.Error
        val receivedEtag = response.headers()["ETag"]
        return try {
            body.use { responseBody ->
                val bounded = BoundedInputStream(
                    GZIPInputStream(responseBody.byteStream()),
                    MAX_DECOMPRESSED_BYTES
                )
                bounded.bufferedReader().use { reader ->
                    val suffixes = reader.lineSequence()
                        .filter { it.contains(':') }
                        .map { it.substringBefore(':').uppercase() }
                        .toSet()
                    PrefixQueryResult.Ok(etag = receivedEtag, suffixes = suffixes)
                }
            }
        } catch (e: IOException) {
            PassLogger.w(TAG, "Failed to decode compromised password response")
            PassLogger.w(TAG, e)
            PrefixQueryResult.Error
        }
    }

    private class BoundedInputStream(
        private val delegate: InputStream,
        private val maxBytes: Long
    ) : InputStream() {
        private var read: Long = 0
        override fun read(): Int = delegate.read().also { if (it != -1) bump(1) }
        override fun read(
            b: ByteArray,
            off: Int,
            len: Int
        ): Int = delegate.read(b, off, len).also { if (it > 0) bump(it.toLong()) }
        override fun close() = delegate.close()
        private fun bump(n: Long) {
            read += n
            if (read > maxBytes) throw IOException("Decompressed payload exceeds $maxBytes bytes")
        }
    }

    companion object {
        private const val TAG = "RemoteCompromisedPasswordDataSource"
        private const val HTTP_NOT_MODIFIED = 304
        private const val MAX_DECOMPRESSED_BYTES = 10L * 1024 * 1024
    }
}
