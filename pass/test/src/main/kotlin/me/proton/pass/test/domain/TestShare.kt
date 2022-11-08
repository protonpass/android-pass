package me.proton.pass.test.domain

import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.SharePermission
import me.proton.pass.domain.SharePermissionFlag
import me.proton.pass.domain.ShareType
import me.proton.pass.domain.VaultId
import me.proton.pass.domain.key.SigningKey

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
            nameKeyId = null,
            expirationTime = null,
            createTime = java.util.Date(),
            keys = emptyList()
        )
}
