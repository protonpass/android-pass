package me.proton.android.pass.extension

import me.proton.android.pass.data.impl.extensions.shareName
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.pass.domain.Share
import me.proton.pass.presentation.components.model.ShareUiModel

fun Share.toUiModel(cryptoContext: CryptoContext): ShareUiModel =
    ShareUiModel(
        id = id,
        name = shareName(cryptoContext)
    )
