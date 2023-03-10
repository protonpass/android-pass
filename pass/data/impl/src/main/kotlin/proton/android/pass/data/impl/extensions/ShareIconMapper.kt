package proton.android.pass.data.impl.extensions

import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareIcon
import proton_pass_vault_v1.VaultV1

private const val TAG = "ShareIconMapper"

@Suppress("ComplexMethod")
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
    ShareIcon.Icon11 -> VaultV1.VaultIcon.ICON11
    ShareIcon.Icon12 -> VaultV1.VaultIcon.ICON12
    ShareIcon.Icon13 -> VaultV1.VaultIcon.ICON13
    ShareIcon.Icon14 -> VaultV1.VaultIcon.ICON14
    ShareIcon.Icon15 -> VaultV1.VaultIcon.ICON15
    ShareIcon.Icon16 -> VaultV1.VaultIcon.ICON16
    ShareIcon.Icon17 -> VaultV1.VaultIcon.ICON17
    ShareIcon.Icon18 -> VaultV1.VaultIcon.ICON18
    ShareIcon.Icon19 -> VaultV1.VaultIcon.ICON19
    ShareIcon.Icon20 -> VaultV1.VaultIcon.ICON20
    ShareIcon.Icon21 -> VaultV1.VaultIcon.ICON21
    ShareIcon.Icon22 -> VaultV1.VaultIcon.ICON22
    ShareIcon.Icon23 -> VaultV1.VaultIcon.ICON23
    ShareIcon.Icon24 -> VaultV1.VaultIcon.ICON24
    ShareIcon.Icon25 -> VaultV1.VaultIcon.ICON25
    ShareIcon.Icon26 -> VaultV1.VaultIcon.ICON26
    ShareIcon.Icon27 -> VaultV1.VaultIcon.ICON27
    ShareIcon.Icon28 -> VaultV1.VaultIcon.ICON28
    ShareIcon.Icon29 -> VaultV1.VaultIcon.ICON29
    ShareIcon.Icon30 -> VaultV1.VaultIcon.ICON30
}

@Suppress("ComplexMethod")
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
    VaultV1.VaultIcon.ICON11 -> ShareIcon.Icon11
    VaultV1.VaultIcon.ICON12 -> ShareIcon.Icon12
    VaultV1.VaultIcon.ICON13 -> ShareIcon.Icon13
    VaultV1.VaultIcon.ICON14 -> ShareIcon.Icon14
    VaultV1.VaultIcon.ICON15 -> ShareIcon.Icon15
    VaultV1.VaultIcon.ICON16 -> ShareIcon.Icon16
    VaultV1.VaultIcon.ICON17 -> ShareIcon.Icon17
    VaultV1.VaultIcon.ICON18 -> ShareIcon.Icon18
    VaultV1.VaultIcon.ICON19 -> ShareIcon.Icon19
    VaultV1.VaultIcon.ICON20 -> ShareIcon.Icon20
    VaultV1.VaultIcon.ICON21 -> ShareIcon.Icon21
    VaultV1.VaultIcon.ICON22 -> ShareIcon.Icon22
    VaultV1.VaultIcon.ICON23 -> ShareIcon.Icon23
    VaultV1.VaultIcon.ICON24 -> ShareIcon.Icon24
    VaultV1.VaultIcon.ICON25 -> ShareIcon.Icon25
    VaultV1.VaultIcon.ICON26 -> ShareIcon.Icon26
    VaultV1.VaultIcon.ICON27 -> ShareIcon.Icon27
    VaultV1.VaultIcon.ICON28 -> ShareIcon.Icon28
    VaultV1.VaultIcon.ICON29 -> ShareIcon.Icon29
    VaultV1.VaultIcon.ICON30 -> ShareIcon.Icon30
    VaultV1.VaultIcon.UNRECOGNIZED -> {
        PassLogger.w(TAG, "Unrecognized icon")
        ShareIcon.Icon1
    }
}

