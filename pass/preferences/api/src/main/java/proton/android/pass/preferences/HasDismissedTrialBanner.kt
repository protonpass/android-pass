package proton.android.pass.preferences

sealed interface HasDismissedTrialBanner {
    object Dismissed : HasDismissedTrialBanner
    object NotDismissed : HasDismissedTrialBanner

    companion object {
        fun from(value: Boolean): HasDismissedTrialBanner = if (value) { Dismissed } else { NotDismissed }
    }
}

fun HasDismissedTrialBanner.value(): Boolean =
    when (this) {
        HasDismissedTrialBanner.Dismissed -> true
        HasDismissedTrialBanner.NotDismissed -> false
    }
