package proton.android.pass.common.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class ABBucketAssignerTest {

    @Test
    fun `same UUID should always return the same bucket`() {
        val numBuckets = 2
        val uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        val bucket1 = ABBucketAssigner.getBucket(numBuckets, uuid)
        val bucket2 = ABBucketAssigner.getBucket(numBuckets, uuid)

        assertThat(bucket1).isEqualTo(bucket2)
    }

    @Test
    fun `different UUIDs should distribute across buckets`() {
        val numBuckets = 2
        val iterations = 100
        val seenBuckets = mutableSetOf<Int>()

        repeat(iterations) {
            val uuid = UUID.randomUUID()
            seenBuckets.add(ABBucketAssigner.getBucket(numBuckets, uuid))
        }

        assertThat(seenBuckets.size).isGreaterThan(1)
    }

    @Test
    fun `should always return a valid bucket within range`() {
        val numBuckets = 10

        repeat(100) {
            val uuid = UUID.randomUUID()
            val bucket = ABBucketAssigner.getBucket(numBuckets, uuid)
            assertThat(bucket).isAtLeast(0)
            assertThat(bucket).isLessThan(numBuckets)
        }
    }

    @Test
    fun `should handle single bucket case correctly`() {
        val uuid = UUID.randomUUID()

        val bucket = ABBucketAssigner.getBucket(1, uuid)

        assertThat(bucket).isEqualTo(0)
    }

    @Test
    fun `should handle a large number of buckets`() {
        val numBuckets = 1000

        repeat(100) {
            val uuid = UUID.randomUUID()
            val bucket = ABBucketAssigner.getBucket(numBuckets, uuid)
            assertThat(bucket).isAtLeast(0)
            assertThat(bucket).isLessThan(numBuckets)
        }
    }

}
