package proton.android.pass.preferences

sealed interface HasDismissedAutofillBanner {
    object Dismissed : HasDismissedAutofillBanner
    object NotDismissed : HasDismissedAutofillBanner

    companion object {
        fun from(value: Boolean): HasDismissedAutofillBanner = if (value) { Dismissed } else { NotDismissed }
    }
}

fun HasDismissedAutofillBanner.value(): Boolean =
    when (this) {
        HasDismissedAutofillBanner.Dismissed -> true
        HasDismissedAutofillBanner.NotDismissed -> false
    }
