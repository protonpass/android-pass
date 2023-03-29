package proton.android.pass.preferences

import me.proton.android.pass.preferences.BooleanPrefProto

fun Boolean.toBooleanPrefProto() = if (this) {
    BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE
} else {
    BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE
}

fun fromBooleanPrefProto(pref: BooleanPrefProto, default: Boolean = false) =
    when (pref) {
        BooleanPrefProto.BOOLEAN_PREFERENCE_TRUE -> true
        BooleanPrefProto.BOOLEAN_PREFERENCE_FALSE -> false
        else -> default
    }
