package proton.android.pass.preferences

private const val IMMEDIATELY = 1
private const val NEVER = 2
private const val IN_ONE_MINUTE = 3
private const val IN_TWO_MINUTES = 4
private const val IN_FIVE_MINUTES = 5
private const val IN_TEN_MINUTES = 6
private const val IN_ONE_HOUR = 7
private const val IN_FOUR_HOURS = 8

enum class AppLockPreference {
    Immediately,
    Never,
    InOneMinute,
    InTwoMinutes,
    InFiveMinutes,
    InTenMinutes,
    InOneHour,
    InFourHours;

    fun value(): Int = when (this) {
        Immediately -> IMMEDIATELY
        Never -> NEVER
        InOneMinute -> IN_ONE_MINUTE
        InTwoMinutes -> IN_TWO_MINUTES
        InFiveMinutes -> IN_FIVE_MINUTES
        InTenMinutes -> IN_TEN_MINUTES
        InOneHour -> IN_ONE_HOUR
        InFourHours -> IN_FOUR_HOURS
    }

    companion object {
        fun from(value: Int): AppLockPreference = when (value) {
            IMMEDIATELY -> Immediately
            NEVER -> Never
            IN_ONE_MINUTE -> InOneMinute
            IN_TWO_MINUTES -> InTwoMinutes
            IN_FIVE_MINUTES -> InFiveMinutes
            IN_TEN_MINUTES -> InTenMinutes
            IN_ONE_HOUR -> InOneHour
            IN_FOUR_HOURS -> InFourHours
            else -> InTwoMinutes
        }
    }
}
