package proton.android.pass.crypto.api.error

class BadTagException(override val message: String, override val cause: Throwable) : RuntimeException(message)
