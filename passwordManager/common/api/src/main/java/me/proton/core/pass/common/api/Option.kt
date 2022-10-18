package me.proton.core.pass.common.api

sealed interface Option<out A> {

    fun isEmpty(): Boolean

    fun isNotEmpty(): Boolean = !isEmpty()

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
}

data class Some<out T>(val value: T) : Option<T> {

    override fun isEmpty(): Boolean = false

    override fun toString(): String = "Option.Some($value)"

    companion object {
        @PublishedApi
        internal val unit: Option<Unit> = Some(Unit)
    }
}
