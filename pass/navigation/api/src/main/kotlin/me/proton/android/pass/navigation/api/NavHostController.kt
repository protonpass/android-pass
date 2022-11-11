package me.proton.android.pass.navigation.api

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.DialogNavigator
import com.google.accompanist.navigation.animation.AnimatedComposeNavigator
import me.proton.core.crypto.android.keystore.AndroidKeyStoreCrypto
import me.proton.core.crypto.common.keystore.KeyStoreCrypto

@ExperimentalAnimationApi
@Composable
fun rememberAnimatedNavController(): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(saver = NavControllerSaver(context, AndroidKeyStoreCrypto.default)) {
        createNavController(context)
    }
}

@ExperimentalAnimationApi
fun createNavController(context: Context) =
    NavHostController(context).apply {
        navigatorProvider.addNavigator(AnimatedComposeNavigator())
        navigatorProvider.addNavigator(DialogNavigator())
    }

/**
 * Saver to save and restore the NavController across config change and process death.
 */
@ExperimentalAnimationApi
private fun NavControllerSaver(
    context: Context,
    keyStoreCrypto: KeyStoreCrypto
): Saver<NavHostController, *> = Saver(
    save = { keyStoreCrypto.encrypt(it.saveState()) },
    restore = { createNavController(context).apply { restoreState(keyStoreCrypto.decrypt(it)) } }
)

internal fun KeyStoreCrypto.encrypt(bundle: Bundle?) = Bundle().apply {
    if (bundle == null) {
        return@apply
    }

    val parcel = android.os.Parcel.obtain()
    bundle.writeToParcel(parcel, 0)
    val base64 = String(android.util.Base64.encode(parcel.marshall(), android.util.Base64.NO_WRAP))
    putString(PROTON_NAV_KEY, encrypt(base64))
    parcel.recycle()
}

internal fun KeyStoreCrypto.decrypt(bundle: Bundle): Bundle {
    val encrypted = bundle.getString(PROTON_NAV_KEY) ?: return Bundle()
    val bytes = android.util.Base64.decode(decrypt(encrypted), android.util.Base64.NO_WRAP)
    val parcel = android.os.Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)
    val decrypted = Bundle.CREATOR.createFromParcel(parcel)
    parcel.recycle()
    return decrypted
}

private const val PROTON_NAV_KEY = "proton.nav.bundle"

