/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.navigation.api

import android.content.Context
import android.os.Bundle
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.DialogNavigator
import com.google.accompanist.navigation.animation.AnimatedComposeNavigator
import me.proton.core.crypto.android.keystore.AndroidKeyStoreCrypto
import me.proton.core.crypto.common.keystore.KeyStoreCrypto

@Composable
fun rememberAnimatedNavController(
    vararg navigators: Navigator<out NavDestination>
): NavHostController {
    val context = LocalContext.current
    return rememberSaveable(
        inputs = navigators,
        saver = NavControllerSaver(context, AndroidKeyStoreCrypto.default)
    ) { createNavController(context) }
        .apply {
            for (navigator in navigators) {
                navigatorProvider.addNavigator(navigator)
            }
        }
}

@OptIn(ExperimentalAnimationApi::class)
fun createNavController(context: Context) =
    NavHostController(context).apply {
        navigatorProvider.addNavigator(AnimatedComposeNavigator())
        navigatorProvider.addNavigator(DialogNavigator())
    }

/**
 * Saver to save and restore the NavController across config change and process death.
 */
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

