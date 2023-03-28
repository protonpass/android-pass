package proton.android.pass.test.domain

import proton.android.pass.common.api.None
import proton.pass.domain.Share
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.SharePermission
import proton.pass.domain.SharePermissionFlag
import proton.pass.domain.ShareType
import proton.pass.domain.VaultId
import java.util.Date

object TestShare {
    fun create(
        shareId: ShareId = ShareId("123"),
        isPrimary: Boolean = false
    ): Share = Share(
        id = shareId,
        shareType = ShareType.Vault,
        targetId = "456",
        permission = SharePermission(SharePermissionFlag.Admin.value),
        vaultId = VaultId("456"),
        content = None,
        expirationTime = null,
        createTime = Date(),
        color = ShareColor.Color1,
        icon = ShareIcon.Icon1,
        isPrimary = isPrimary
    )
}
