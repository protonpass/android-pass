package proton.android.pass.preferences

sealed interface HasAuthenticated {
    object Authenticated : HasAuthenticated
    object NotAuthenticated : HasAuthenticated

    companion object {
        fun from(value: Boolean): HasAuthenticated = if (value) { Authenticated } else { NotAuthenticated }
    }
}

fun HasAuthenticated.value(): Boolean =
    when (this) {
        HasAuthenticated.Authenticated -> true
        HasAuthenticated.NotAuthenticated -> false
    }
