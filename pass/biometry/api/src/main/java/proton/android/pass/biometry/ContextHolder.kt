package proton.android.pass.biometry

import android.content.Context
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import java.lang.ref.WeakReference

class ContextHolder(private val context: Option<WeakReference<Context>>) {
    fun getContext(): Option<Context> =
        when (val ctx = context) {
            None -> None
            is Some -> ctx.value.get().toOption()
        }

    companion object {
        fun fromContext(context: Context): ContextHolder = ContextHolder(Some(WeakReference(context)))
    }
}
