package me.proton.android.pass.biometry

import android.content.Context
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.common.api.toOption
import java.lang.ref.WeakReference

class ContextHolder(private val context: Option<WeakReference<Context>>) {
    fun getContext(): Option<Context> =
        when (val ctx = context) {
            None -> None
            is Some -> ctx.value.get().toOption()
        }
}
