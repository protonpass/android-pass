package proton.android.pass.common.api

import kotlin.text.Typography.ellipsis

fun String.ellipsize(size: Int) = take(size) + if (length > size) ellipsis else ""
