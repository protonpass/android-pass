package me.proton.android.pass.crypto.impl.usecases

import me.proton.android.pass.crypto.impl.context.TestEncryptionContextProvider
import org.apache.commons.codec.binary.Base64
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.EncryptedItemRevision
import proton.android.pass.crypto.api.usecases.OpenItem
import proton.android.pass.crypto.impl.usecases.OpenItemImpl
import proton.pass.domain.ItemType
import proton.pass.domain.Share
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.SharePermission
import proton.pass.domain.SharePermissionFlag
import proton.pass.domain.ShareType
import proton.pass.domain.VaultId
import proton.pass.domain.key.ShareKey
import java.sql.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Suppress("UnderscoresInNumericLiterals")
class OpenItemImplTest {

    private lateinit var encryptionContextProvider: EncryptionContextProvider
    private lateinit var instance: OpenItem

    @Before
    fun setUp() {
        encryptionContextProvider = TestEncryptionContextProvider(EncryptionKey.generate())
        instance = OpenItemImpl(encryptionContextProvider)
    }

    @Test
    fun canOpenItem() {
        val output = instance.open(
            response = REVISION,
            share = getShare(),
            shareKeys = listOf(getShareKey()),
        )
        val item = output.item
        assertEquals(item.revision, REVISION.revision)
        assertEquals(item.id.id, REVISION.itemId)

        assertTrue(item.itemType is ItemType.Login)
        val loginContents = item.itemType as ItemType.Login

        assertEquals(ITEM_LOGIN_USERNAME, loginContents.username)
        encryptionContextProvider.withEncryptionContext {
            assertEquals(ITEM_TITLE, decrypt(item.title))
            assertEquals(ITEM_NOTE, decrypt(item.note))
            assertEquals(ITEM_LOGIN_PASSWORD, decrypt(loginContents.password))
        }
    }

    private fun getShare(): Share {
        return Share(
            id = ShareId(SHARE_ID),
            shareType = ShareType.Vault,
            targetId = VAULT_ID,
            permission = SharePermission(SharePermissionFlag.Admin.value),
            vaultId = VaultId(VAULT_ID),
            content = None,
            expirationTime = null,
            createTime = Date(1664195804),
            color = ShareColor.Color1,
            icon = ShareIcon.Icon1
        )
    }

    private fun getShareKey(): ShareKey {
        val decodedShareKey = Base64.decodeBase64(shareKeyBase64)
        return ShareKey(
            rotation = KEY_ROTATION,
            key = encryptionContextProvider.withEncryptionContext { encrypt(decodedShareKey) },
            responseKey = shareKeyBase64,
            createTime = 1664195804
        )
    }


    companion object {
        const val VAULT_ID =
            "pIfdC0UNjR4rrdaTtEE_I92fjMgkXKMhujmz-VlvQUh5bG2V2vKdqnpLJXr_wMiC4HlNziQpo61_NCOqJKEsug=="
        const val KEY_ROTATION = 1L
        const val SHARE_ID =
            "jUjNWfdzq7X-CMrR3LyX2y7o_D6y4FIGmsuTkfdNOI4QtPyaed-kVyFcOw9XLiRpwRDKnyKtHxhrIBW7nPVZdQ=="
        const val ITEM_TITLE = "12BZDfW4zF"
        const val ITEM_NOTE = "DQl59cDg4o"
        const val ITEM_UUID = "08s4oG42nq"
        const val ITEM_LOGIN_USERNAME = "4GyGLG7YRK"
        const val ITEM_LOGIN_PASSWORD = "RFiCUSS2Sh"

        const val shareKeyBase64 = "L+J7Yyhhgvyd2+0cJidXOontWJzUa9Akz5w2flHF7W8="


        @Suppress("MaxLineLength", "UnderscoresInNumericLiterals")
        val REVISION = EncryptedItemRevision(
            itemId = "nAxHhxz0-44bRh-Fvhi7xgAFqXFrNN4FPVLh4u1Wpd6WVw2K6sOwZW7x4Pdaiy3Lhxc_70xgCIPcuzvhYHV2_A==",
            revision = 1,
            contentFormatVersion = 1,
            keyRotation = KEY_ROTATION,
            content = "bYmT1lBusC2XPxBDxdfVEe7LxpQhUrjM8b2KSSFXd/iz5RAxn0jBDLUTOqo9XZCY5X4w7RmhFuAumUivMFfE9C7U59MlOQUJDP6xWQSGfjgKn9RtadHWeRKYaw6oBQ==",
            key = "qP9qH1bo3WQmKpEOAhoD+dlaN1I0hIWdzlxP7CvmgY9gCuYcB1H+pIDQCK1jkombHhJuCGK33wMdPaz1",
            state = 1,
            aliasEmail = null,
            createTime = 1664195804,
            modifyTime = 1664195804,
            lastUseTime = 1664195804,
            revisionTime = 1664195804,
        )
    }
}


