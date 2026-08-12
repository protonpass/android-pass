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

package proton.android.pass.data.impl.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

internal class InputStreamExtTest {

    @Test
    fun `fills the whole buffer when the stream returns short reads`() {
        val content = ByteArray(1000) { it.toByte() }
        val stream = ShortReadInputStream(content, maxBytesPerRead = 64)
        val buffer = ByteArray(1000)

        val read = stream.readChunk(buffer)

        assertThat(read).isEqualTo(1000)
        assertThat(buffer).isEqualTo(content)
    }

    @Test
    fun `splits a short-read stream into the same chunk count as a regular stream`() {
        val content = ByteArray(2500) { it.toByte() }
        val chunkSize = 1000

        val regularChunks = readAllChunks(ByteArrayInputStream(content), chunkSize)
        val pipedChunks = readAllChunks(ShortReadInputStream(content, 64), chunkSize)

        assertThat(pipedChunks.size).isEqualTo(3)
        assertThat(pipedChunks.map(ByteArray::toList)).isEqualTo(regularChunks.map(ByteArray::toList))
    }

    @Test
    fun `returns the remaining byte count on the last partial chunk`() {
        val stream = ShortReadInputStream(ByteArray(30), maxBytesPerRead = 7)
        val buffer = ByteArray(100)

        assertThat(stream.readChunk(buffer)).isEqualTo(30)
    }

    @Test
    fun `returns zero when the stream is already exhausted`() {
        val stream = ShortReadInputStream(ByteArray(0), maxBytesPerRead = 7)

        assertThat(stream.readChunk(ByteArray(100))).isEqualTo(0)
    }

    private fun readAllChunks(stream: InputStream, chunkSize: Int): List<ByteArray> {
        val chunks = mutableListOf<ByteArray>()
        val buffer = ByteArray(chunkSize)
        while (true) {
            val read = stream.readChunk(buffer)
            if (read == 0) break
            chunks.add(buffer.copyOf(read))
        }
        return chunks
    }

    private class ShortReadInputStream(
        private val content: ByteArray,
        private val maxBytesPerRead: Int
    ) : InputStream() {

        private var position = 0

        override fun read(): Int = if (position < content.size) content[position++].toInt() else -1

        override fun read(
            b: ByteArray,
            off: Int,
            len: Int
        ): Int {
            if (position >= content.size) return -1
            val count = minOf(len, maxBytesPerRead, content.size - position)
            content.copyInto(b, off, position, position + count)
            position += count
            return count
        }
    }
}
