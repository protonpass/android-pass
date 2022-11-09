package me.proton.android.pass.preferences

sealed interface HasCompletedOnBoarding {
    object Completed : HasCompletedOnBoarding
    object NotCompleted : HasCompletedOnBoarding

    companion object {
        fun from(value: Boolean): HasCompletedOnBoarding = if (value) { Completed } else { NotCompleted }
    }
}

fun HasCompletedOnBoarding.value(): Boolean =
    when (this) {
        HasCompletedOnBoarding.Completed -> true
        HasCompletedOnBoarding.NotCompleted -> false
    }
