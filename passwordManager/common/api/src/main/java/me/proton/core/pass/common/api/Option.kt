package me.proton.core.pass.common.api

sealed interface Option<out A> {

    fun isEmpty(): Boolean

    fun isNotEmpty(): Boolean = !isEmpty()

    fun <R> map(block: (A) -> R): Option<R>

    companion object {

        @JvmStatic
        fun <A> fromNullable(a: A?): Option<A> = if (a != null) Some(a) else None

        @JvmStatic
        operator fun <A> invoke(a: A): Option<A> = Some(a)
    }
}

object None : Option<Nothing> {
    override fun isEmpty(): Boolean = true

    override fun toString(): String = "Option.None"

    override fun <R> map(block: (Nothing) -> R): Option<R> = None
}

data class Some<out T>(val value: T) : Option<T> {

    override fun isEmpty(): Boolean = false

    override fun toString(): String = "Option.Some($value)"

    override fun <R> map(block: (T) -> R): Option<R> = Some(block(value))

    companion object {
        @PublishedApi
        internal val unit: Option<Unit> = Some(Unit)
    }
}

fun <T> T?.toOption(): Option<T> = this?.let { Some(it) } ?: None
