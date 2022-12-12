package me.proton.android.pass.data.impl.crypto

import me.proton.android.pass.data.impl.responses.ItemRevision
import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.key.domain.entity.key.ArmoredKey
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.SharePermission
import me.proton.pass.domain.SharePermissionFlag
import me.proton.pass.domain.ShareType
import me.proton.pass.domain.VaultId
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.SigningKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.test.crypto.TestKeyStoreCrypto
import org.junit.Before
import org.junit.Test
import java.sql.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class OpenItemTest {

    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = TestKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto(),
    )

    private lateinit var instance: OpenItem

    @Before
    fun setUp() {
        instance = OpenItemImpl(cryptoContext)
    }

    @Test
    fun canOpenItem() {
        val item = instance.open(
            REVISION,
            getShare(),
            listOf(getUserPublicKey()),
            listOf(getVaultKey()),
            listOf(getItemKey())
        )
        assertEquals(item.revision, REVISION.revision)
        assertEquals(item.id.id, REVISION.itemId)
        assertEquals(ITEM_TITLE, item.title.decrypt(cryptoContext.keyStoreCrypto))
        assertEquals(ITEM_NOTE, item.note.decrypt(cryptoContext.keyStoreCrypto))

        assertTrue(item.itemType is ItemType.Login)

        val loginContents = item.itemType as ItemType.Login
        assertEquals(ITEM_USERNAME, loginContents.username)
        assertEquals(ITEM_PASSWORD, loginContents.password.decrypt(cryptoContext.keyStoreCrypto))
    }

    @Suppress("UnderscoresInNumericLiterals")
    fun getShare(): Share {
        return Share(
            id = ShareId(SHARE_ID),
            shareType = ShareType.Vault,
            targetId = VAULT_ID,
            permission = SharePermission(SharePermissionFlag.Admin.value),
            vaultId = VaultId(VAULT_ID),
            signingKey = getSigningKey(),
            content = null,
            nameKeyId = ROTATION_ID,
            expirationTime = null,
            createTime = Date(1664195804),
            keys = listOf(getVaultKey())
        )
    }

    fun getSigningKey(): SigningKey {
        val decodedSigningKeyPassphrase = cryptoContext.pgpCrypto.getBase64Decoded(signingKeyPassphrase)
        val encryptedSigningKeyPassphrase =
            PlainByteArray(decodedSigningKeyPassphrase).encrypt(cryptoContext.keyStoreCrypto)
        val signingKey = ArmoredKey.Private(
            armored = signingKeyContents,
            key = PrivateKey(
                key = signingKeyContents,
                isPrimary = true,
                passphrase = encryptedSigningKeyPassphrase
            )
        )
        return SigningKey(signingKey)
    }

    fun getItemKey(): ItemKey {
        val encryptedItemKeyPassphrase =
            PlainByteArray(itemKeyPassphrase.encodeToByteArray()).encrypt(cryptoContext.keyStoreCrypto)
        return ItemKey(
            rotationId = ROTATION_ID,
            key = ArmoredKey.Private(
                armored = itemKeyContents,
                key = PrivateKey(
                    key = itemKeyContents,
                    isPrimary = true,
                    passphrase = encryptedItemKeyPassphrase
                )
            ),
            encryptedKeyPassphrase = encryptedItemKeyPassphrase
        )
    }

    fun getVaultKey(): VaultKey {
        val encryptedVaultKeyPassphrase =
            PlainByteArray(vaultKeyPassphrase.encodeToByteArray()).encrypt(cryptoContext.keyStoreCrypto)
        return VaultKey(
            rotationId = ROTATION_ID,
            rotation = 1,
            key = ArmoredKey.Private(
                armored = vaultKeyContents,
                key = PrivateKey(
                    key = vaultKeyContents,
                    isPrimary = true,
                    passphrase = encryptedVaultKeyPassphrase
                )
            ),
            encryptedKeyPassphrase = encryptedVaultKeyPassphrase
        )
    }

    fun getUserPublicKey(): PublicKey =
        PublicKey(
            key = userPublicKey,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true
        )

    companion object {
        const val VAULT_ID = "pIfdC0UNjR4rrdaTtEE_I92fjMgkXKMhujmz-VlvQUh5bG2V2vKdqnpLJXr_wMiC4HlNziQpo61_NCOqJKEsug=="
        const val ROTATION_ID =
            "pgw8hP-bgsLFI-9ZO2GfZOzhY0ovR5Tv0QDIsMORjsN8SFej5GDQijaKwQnTVsEgD6LzOzUy3uLDCwZDXeRAfg=="
        const val SHARE_ID = "jUjNWfdzq7X-CMrR3LyX2y7o_D6y4FIGmsuTkfdNOI4QtPyaed-kVyFcOw9XLiRpwRDKnyKtHxhrIBW7nPVZdQ=="
        const val ITEM_TITLE = "wYx9ab8ORZ"
        const val ITEM_NOTE = "DlSpTebMmG"
        const val ITEM_USERNAME = "XQLijDkZuu"
        const val ITEM_PASSWORD = "X8kQ1UjgtF"

        val userPublicKey = """-----BEGIN PGP PUBLIC KEY BLOCK-----
Version: ProtonMail

xjMEYuOCtxYJKwYBBAHaRw8BAQdAa8djJvy27uMU1BXXazU75UzWcVvXRphD
ApwNNlpU2yPNMWNhcmxvc3Rlc3RAcHJvdG9uLmJsYWNrIDxjYXJsb3N0ZXN0
QHByb3Rvbi5ibGFjaz7CjAQQFgoAHQUCYuOCtwQLCQcIAxUICgQWAAIBAhkB
AhsDAh4BACEJEKb7XT4q85emFiEE1LgT1a0jMp9YQ41vpvtdPirzl6Yr1gD9
GVLamVQeCJVInz0Kyj+4adlYe95CGRx/XSv8oWZ61EgA/24pR2XXRqsGYwUV
+L7u+rUeDWg0GepJWXKByERaTa4FwqgEEBYIAFoFAlzZcwMJkNTSGgVRp7Ls
FiEENhVDvw2lbaYMs9qU1NIaBVGnsuwsHFRlc3QgT3BlblBHUCBDQSA8dGVz
dC1vcGVucGdwLWNhQHByb3Rvbi5tZT4FgwcHhtgAADo3AP931a/jGllSPt1Q
kylNHRA/VLyHdIA/n/N47SlVVPR3ogD9ExtQ2ENWAXKHCYpovsjhhBSkjR5d
MVtFL+CzBUkFbgHOOARi44K3EgorBgEEAZdVAQUBAQdAZwHSHO5SEwTbuLZZ
2bj8oDxaqgcedaggazbt7An0lCcDAQgHwngEGBYIAAkFAmLjgrcCGwwAIQkQ
pvtdPirzl6YWIQTUuBPVrSMyn1hDjW+m+10+KvOXpisLAP4yninUoVr3GT9i
OM9ZlmyMPeK6TiAjFxFWCmp3vvpeaQEAjnAmPcXDCBxNx2LZUD7YSxXtLGI5
L7zh72zXbbDHbgU=
=EDtZ
-----END PGP PUBLIC KEY BLOCK-----
""".trimIndent()

        const val signingKeyPassphrase = "ZTJvdWM4UXh0bGJ0eDN3UDBnWlYzTHp5YXJEYW1Pb2Y="
        val signingKeyContents = """-----BEGIN PGP PRIVATE KEY BLOCK-----
Comment: https://gopenpgp.org
Version: GopenPGP 2.2.4

xVgEYzGv/RYJKwYBBAHaRw8BAQdAp3jH7uCNtZGc6LihDZcIKrjeEZvirDGRre5M
p9dyDXoAAP9bJnrFbgq4GPDsVJVQ133OGP3QLT2tm0ZCryYbdjfuwRCpzSlWYXVs
dFNpZ25pbmdLZXkgPHZhdWx0X3NpZ25pbmdAcHJvdG9uLmNoPsKMBBMWCAA+BQJj
Ma/9CZByDFaXVxoTPhahBO+NvCQYFfAX7iRCPHIMVpdXGhM+AhsDAh4BAhkBAwsJ
BwIVCAMWAAICIgEAABW4AP0WJeGIXH4w7xTU0Ss+pSWdnx0pobGN+OhQ1JzJ3djE
ZQD9GgRgvpRA7YXfBxJiR49/GOVfiO6XnHJuqdsirBkzMwrHXQRjMa/9EgorBgEE
AZdVAQUBAQdAFIqs5uy5H9B5sivpnLN71nr7Kgi0sdEbeVV/mvTf/gsDAQoJAAD/
c5p0uEjgpdAWx00JE2Chx2NHrK6OK6zOWCU+76s+7wgQpMJ4BBgWCAAqBQJjMa/9
CZByDFaXVxoTPhahBO+NvCQYFfAX7iRCPHIMVpdXGhM+AhsMAACyvQD+MP6cS7x8
NgwZn2eLSrKx4mUlHiXZacFoe3QjbF2BUSgA+gLg6Y4rLnanumIKjON63EKToZWP
SPQsuj9ovlCGtTUB
=MAnc
-----END PGP PRIVATE KEY BLOCK-----""".trimIndent()

        const val vaultKeyPassphrase = "GxsqibPklCIOq2ExbaGoOMCznQpKS3yg"
        val vaultKeyContents = """-----BEGIN PGP PRIVATE KEY BLOCK-----
Version: ProtonMail

xYYEYzGv/RYJKwYBBAHaRw8BAQdAAUGskH6Sch6rX3Di95v38iDoK+ARC2b9
bbAzAPaQNW/+CQMIwOWBqjPlsZRgr5yvQ+aYudVEmVpWgxj5kEwEwMixlMm0
L+QFtslpBDGFrfquU8D8qsJ/jKvyupKCLLG8rkglpcp4Kzh1XbyKajgXr2oJ
4M0aVmF1bHRLZXkgPHZhdWx0QHByb3Rvbi5jaD7CjAQTFggAPgUCYzGv/QmQ
q0GRjQ/S+UcWoQSfT1u0CFUSEhw+k6WrQZGND9L5RwIbAwIeAQIZAQMLCQcC
FQgDFgACAiIBAABGrwD/d0RWZ3QIAbNgBnx6tJAEyGymqayXOyNQECTCfSLA
Uo0A+wWb/rnhkF/ssFxNGevEDI1PeEbMPU+HrCUxNxwdlqYFx4sEYzGv/RIK
KwYBBAGXVQEFAQEHQJMANf/Zs/tZjCbtcKpfIv7LcqaTM7AwULqNfVRqd7VN
AwEKCf4JAwiznKInLeswOWAqMmmLYFrTMTT4gCQSuecGQcGNjuhuarrRIuq8
AvIWF+EVNUt4HqqPv57S4KR5rxVBcpMZKxiIEm//PDv1RdjMyHmJXK9PwngE
GBYIACoFAmMxr/0JkKtBkY0P0vlHFqEEn09btAhVEhIcPpOlq0GRjQ/S+UcC
GwwAABrHAQDsIcK84eMaYrbj7xKZNWS0GjBZKbs7EuE6Vf1C7DMG0AD/XzUf
n4WfsBwhASkUyyU8F+z/hihOYcAvqTUk5hhukwY=
=ud4V
-----END PGP PRIVATE KEY BLOCK-----
""".trimIndent()

        const val itemKeyPassphrase = "pxxlgYcOHq45yt8fHaYSIkeJWCUfCDXN"
        val itemKeyContents = """-----BEGIN PGP PRIVATE KEY BLOCK-----
Version: ProtonMail

xYYEYzGv/RYJKwYBBAHaRw8BAQdAtxzcn+TMW5P5bdkVpurAncs8D2H1KuC8
mF73d1TSdIz+CQMIb5Wpg0BzyjRg7XSG529WPaboaW/qwCWGBbpEAW003YVx
VCQ1GomFEYlaPRU5wUpORlplh7jjx2Scj0fwVWQtSbWss8ZDl2dHkg4BtAAs
IM0YSXRlbUtleSA8aXRlbUBwcm90b24uY2g+wowEExYIAD4FAmMxr/0JkFm3
tHq0HBB1FqEE7wke892p8BG6KpVtWbe0erQcEHUCGwMCHgECGQEDCwkHAhUI
AxYAAgIiAQAAnLABAJZpnBWwrrvRgSOxbbZTT5NyXrS/GOhNZAqf/0ZJlcn6
AQD5itpulpHc7qW+Hw1zuPBZYvV03hrsevngVL5qHJWDDseLBGMxr/0SCisG
AQQBl1UBBQEBB0CGklMw5FMqpxvfWkhdyfYjfL/LHCWgB/E46uvuioFjOgMB
Cgn+CQMI/7QoxXBwxx9gmNM1s24YpNoC96cCLDAROPf99mA3Q1jc8AywrcQd
a0giPSFCsLgI+HaZ2IZ+HDJ0VpGvBOL9dTvWuHxaKsu1Byb51h6lLsJ4BBgW
CAAqBQJjMa/9CZBZt7R6tBwQdRahBO8JHvPdqfARuiqVbVm3tHq0HBB1AhsM
AAC2QAD/YpITra4HaZZ3NBDmWhYjD32FTD1flWv9Y+eGMjT20bwA/2oVIexv
5hf3Nr9nfc44aDHUhA3swEqe3e5Uqs1jqxoK
=VhCw
-----END PGP PRIVATE KEY BLOCK-----
""".trimIndent()

        @Suppress("MaxLineLength", "UnderscoresInNumericLiterals")
        val REVISION = ItemRevision(
            itemId = "EHVloycgIIycr6hFVQCEMcV5nUTALVPL38U3GbrRJMRRq90byv35G31JchJFv_9LvMhIF3lYDLSD-KMg4eGWDw==",
            revision = 1,
            contentFormatVersion = 1,
            rotationId = ROTATION_ID,
            content = "wV4Doovy8bj7oowSAQdAw/FIEiFiaoR6xLQUs3bPrajvTco4kvSxTCLfnTlVGkowS3wo2JUJjZfgN206Bsr3VY3Yt293z80pk5dK86STnVqmzzY1KI3TKlfsKVdo2KTB0mcB8fRtRuK1OQSbzZUk7RAYJpJKjdS7QogJT9hrvD0LKhl53m0SiYXVjyX+C2PL1K8l1K6oDz3GKluiVhvSnJ0jTgfQhqnW7eF9b8+tyb8VtOdLk+P+hQrSVdr/beAj8DugK6+Ipq+X",
            userSignature = "wV4Doovy8bj7oowSAQdAw/FIEiFiaoR6xLQUs3bPrajvTco4kvSxTCLfnTlVGkowS3wo2JUJjZfgN206Bsr3VY3Yt293z80pk5dK86STnVqmzzY1KI3TKlfsKVdo2KTB0qgBhRabYOMWtD4Cx36hYBvBtFhZUcl0ChrzOsf01F3cwjALjgjrzUC2ehVkW+TqTd+E5D71jSHt+WInUtk8h6Hc9W8Lk4FTAHnQQtUSFhKZqriGktTKBsUL0J35f8YJunQ/fq1JdPjZDxRdWPIv45pnusdcQhVxEGC1CWuMnGV+x6aeAecjmMvVKkKEp2xZJHkDpmRzpdDUH61Ark7V6IWvTRsDrvHxzdQ=",
            itemKeySignature = "wV4Doovy8bj7oowSAQdAw/FIEiFiaoR6xLQUs3bPrajvTco4kvSxTCLfnTlVGkowS3wo2JUJjZfgN206Bsr3VY3Yt293z80pk5dK86STnVqmzzY1KI3TKlfsKVdo2KTB0qgBcp4cYQJ77OR3/zwchup55UqL1AHxa06BRojLlvvCr/WRp4wTmGf7wFuAQPbwTrm4GUVNp4zBuDo+vqwvJAWJE8FFhdqf78CvDaSVBodX1Sjou3aH95wEOfLMuYnpoqSwhn8y8jvhDr86XaZfIvtqWDW5R9Tl8NSIwYuVhgSn8YCiGOZQwn5c2ryF78r8Y0Vpp9QX6A+rzdzUVF8uWV/cPV6bokyIM0Y=",
            state = 1,
            signatureEmail = "carlostest@proton.black",
            aliasEmail = null,
            labels = emptyList(),
            createTime = 1664195804,
            modifyTime = 1664195804,
            lastUseTime = 1664195804,
            revisionTime = 1664195804
        )
    }

}
