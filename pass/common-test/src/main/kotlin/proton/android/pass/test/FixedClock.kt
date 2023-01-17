package proton.android.pass.test

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class FixedClock(val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
