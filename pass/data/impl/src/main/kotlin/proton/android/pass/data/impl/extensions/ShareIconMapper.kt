package proton.android.pass.data.impl.extensions

import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareIcon
import proton_pass_vault_v1.VaultV1

private const val TAG = "ShareIconMapper"

fun ShareIcon.toProto(): VaultV1.VaultIcon = when (this) {
    ShareIcon.Icon1 -> VaultV1.VaultIcon.ICON1
    ShareIcon.Icon2 -> VaultV1.VaultIcon.ICON2
    ShareIcon.Icon3 -> VaultV1.VaultIcon.ICON3
    ShareIcon.Icon4 -> VaultV1.VaultIcon.ICON4
    ShareIcon.Icon5 -> VaultV1.VaultIcon.ICON5
    ShareIcon.Icon6 -> VaultV1.VaultIcon.ICON6
    ShareIcon.Icon7 -> VaultV1.VaultIcon.ICON7
    ShareIcon.Icon8 -> VaultV1.VaultIcon.ICON8
    ShareIcon.Icon9 -> VaultV1.VaultIcon.ICON9
    ShareIcon.Icon10 -> VaultV1.VaultIcon.ICON10
}

fun VaultV1.VaultIcon.toDomain(): ShareIcon = when (this) {
    VaultV1.VaultIcon.ICON_UNSPECIFIED -> ShareIcon.Icon1
    VaultV1.VaultIcon.ICON_CUSTOM -> {
        PassLogger.w(TAG, "Custom icons not supported yet")
        ShareIcon.Icon1
    }
    VaultV1.VaultIcon.ICON1 -> ShareIcon.Icon1
    VaultV1.VaultIcon.ICON2 -> ShareIcon.Icon2
    VaultV1.VaultIcon.ICON3 -> ShareIcon.Icon3
    VaultV1.VaultIcon.ICON4 -> ShareIcon.Icon4
    VaultV1.VaultIcon.ICON5 -> ShareIcon.Icon5
    VaultV1.VaultIcon.ICON6 -> ShareIcon.Icon6
    VaultV1.VaultIcon.ICON7 -> ShareIcon.Icon7
    VaultV1.VaultIcon.ICON8 -> ShareIcon.Icon8
    VaultV1.VaultIcon.ICON9 -> ShareIcon.Icon9
    VaultV1.VaultIcon.ICON10 -> ShareIcon.Icon10
    VaultV1.VaultIcon.UNRECOGNIZED -> {
        PassLogger.w(TAG, "Unrecognized icon")
        ShareIcon.Icon1
    }
}

