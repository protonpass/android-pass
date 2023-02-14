package proton.android.pass.test.domain

import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PublicKey
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.SharePermission
import proton.pass.domain.SharePermissionFlag
import proton.pass.domain.ShareType
import proton.pass.domain.VaultId
import proton.pass.domain.key.SigningKey
import java.util.Date

object TestShare {
    fun create(): Share =
        Share(
            id = ShareId("123"),
            shareType = ShareType.Vault,
            targetId = "456",
            permission = SharePermission(SharePermissionFlag.Admin.value),
            vaultId = VaultId("456"),
            signingKey = SigningKey(
                ArmoredKey.Public(
                    armored = "",
                    PublicKey(
                        "",
                        isPrimary = true,
                        isActive = true,
                        canEncrypt = true,
                        canVerify = true
                    )
                )
            ),
            content = null,
            contentRotationId = null,
            expirationTime = null,
            createTime = Date(),
        )
}
