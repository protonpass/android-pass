package me.proton.android.pass.extension

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.pass.data.extensions.shareName
import me.proton.core.pass.domain.Share
import me.proton.core.pass.presentation.components.model.ShareUiModel

fun Share.toUiModel(cryptoContext: CryptoContext): ShareUiModel =
    ShareUiModel(
        id = id,
        name = shareName(cryptoContext)
    )
