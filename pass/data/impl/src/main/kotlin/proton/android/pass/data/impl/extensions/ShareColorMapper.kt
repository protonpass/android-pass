package proton.android.pass.data.impl.extensions

import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareColor
import proton_pass_vault_v1.VaultV1

private const val TAG = "ShareColorMapper"

fun ShareColor.toProto(): VaultV1.VaultColor = when (this) {
    ShareColor.Color1 -> VaultV1.VaultColor.COLOR1
    ShareColor.Color2 -> VaultV1.VaultColor.COLOR2
    ShareColor.Color3 -> VaultV1.VaultColor.COLOR3
    ShareColor.Color4 -> VaultV1.VaultColor.COLOR4
    ShareColor.Color5 -> VaultV1.VaultColor.COLOR5
    ShareColor.Color6 -> VaultV1.VaultColor.COLOR6
    ShareColor.Color7 -> VaultV1.VaultColor.COLOR7
    ShareColor.Color8 -> VaultV1.VaultColor.COLOR8
    ShareColor.Color9 -> VaultV1.VaultColor.COLOR9
    ShareColor.Color10 -> VaultV1.VaultColor.COLOR10
}

fun VaultV1.VaultColor.toDomain(): ShareColor = when (this) {
    VaultV1.VaultColor.COLOR_UNSPECIFIED -> ShareColor.Color1
    VaultV1.VaultColor.COLOR_CUSTOM -> {
        PassLogger.w(TAG, "Custom colors not supported yet")
        ShareColor.Color1
    }
    VaultV1.VaultColor.COLOR1 -> ShareColor.Color1
    VaultV1.VaultColor.COLOR2 -> ShareColor.Color2
    VaultV1.VaultColor.COLOR3 -> ShareColor.Color3
    VaultV1.VaultColor.COLOR4 -> ShareColor.Color4
    VaultV1.VaultColor.COLOR5 -> ShareColor.Color5
    VaultV1.VaultColor.COLOR6 -> ShareColor.Color6
    VaultV1.VaultColor.COLOR7 -> ShareColor.Color7
    VaultV1.VaultColor.COLOR8 -> ShareColor.Color8
    VaultV1.VaultColor.COLOR9 -> ShareColor.Color9
    VaultV1.VaultColor.COLOR10 -> ShareColor.Color10
    VaultV1.VaultColor.UNRECOGNIZED -> {
        PassLogger.w(TAG, "Unrecognized color")
        ShareColor.Color1
    }
}
