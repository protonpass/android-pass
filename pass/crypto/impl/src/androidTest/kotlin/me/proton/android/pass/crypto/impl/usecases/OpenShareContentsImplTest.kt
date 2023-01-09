package me.proton.android.pass.crypto.impl.usecases

import me.proton.android.pass.crypto.api.usecases.EncryptedShareResponse
import me.proton.android.pass.crypto.impl.TestUtils
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.key.domain.entity.key.PrivateKey
import org.junit.Test
import proton_pass_vault_v1.VaultV1
import kotlin.test.assertEquals

class OpenShareContentsImplTest {

    @Test
    fun testCanOpenShareContents() {
        val instance = OpenShareContentsImpl(TestUtils.cryptoContext)
        val decrypted = instance.openVaultShareContents(
            createVaultResponse.content!!,
            getVaultKey()
        )
        val contents = VaultV1.Vault.parseFrom(decrypted)

        assertEquals(VAULT_NAME, contents.name)
        assertEquals(VAULT_DESCRIPTION, contents.description)
    }

    companion object {
        private const val VAULT_NAME = "SomeName"
        private const val VAULT_DESCRIPTION = "Vault created from the Rust CLI"

        private const val VAULT_KEY_PASSPHRASE = "2umO7tad38zpziZSBuUdfi1qymO2fxNf"
        private val VAULT_KEY = """
            -----BEGIN PGP PRIVATE KEY BLOCK-----
            Comment: https://gopenpgp.org
            Version: GopenPGP 2.2.4

            xYYEYsWLVRYJKwYBBAHaRw8BAQdAn9Jyfk2qtQgnVAfo12K4IRprfEx8hpOFVtQK
            Xlpqnpj+CQMIIt5a0rGfJYBgJk/cWnE1GYbyjwyvTBfw8WDHGqZWlRwZ/ZbL/33B
            zImyt9shJ0ohRV4V6KAUr5P/yQ8VJldM1KQ4t2n5aNI3PFLOwaJ7As0aVmF1bHRL
            ZXkgPHZhdWx0QHByb3Rvbi5jaD7CjAQTFggAPgUCYsWLVQmQjygPhlbjx/EWoQQy
            /kBhGG8Jf/XNndSPKA+GVuPH8QIbAwIeAQIZAQMLCQcCFQgDFgACAiIBAABY8AD+
            Jho1dOGnZYNYu7XGa1jbdV9Vp6mtMzftCHXxdG01vecBAMfoepzTAH0JIsIUizWk
            /TzWLNxnlt0j50pKJ3b3L1sEx4sEYsWLVRIKKwYBBAGXVQEFAQEHQNpUrzl4OeK5
            PatC8kvWvJIjhxKG/eSDI+DI04R8y1BDAwEKCf4JAwhelMK2P6HUEGBLwHpB0zCF
            s+EwQjCEmss1eAS4WYotx2zEGT8zp/xFjcrApfCTa1JL9ms6SkC+4cZDJrKf8HkH
            a6eisTuQfM1TAH53UgfjwngEGBYIACoFAmLFi1UJkI8oD4ZW48fxFqEEMv5AYRhv
            CX/1zZ3UjygPhlbjx/ECGwwAAIHlAQCqh+ZJSVbcTOShAhuP5EoF9hNAPwUlyjt9
            oe8L0NDNQgD9EDo8UvKPMBPd9pCkagzK7dMHvmVwXZ01qDjsu35utgs=
            =GmDm
            -----END PGP PRIVATE KEY BLOCK-----
        """.trimIndent()

        @Suppress("MaxLineLength", "UnderscoresInNumericLiterals")
        private val createVaultResponse = EncryptedShareResponse(
            shareId = "ziWi-ZOb28XR4sCGFCEpqQbd1FITVWYfTfKYUmV_wKKR3GsveN4HZCh9er5dhelYylEp-fhjBbUPDMHGU699fw==",
            vaultId = "l8vWAXHBQmv0u7OVtPbcqMa4iwQaBqowINSQjPrxAr-Da8fVPKUkUcqAq30_BCxj1X0nW70HQRmAa-rIvzmKUA==",
            targetType = 1,
            targetId = "l8vWAXHBQmv0u7OVtPbcqMa4iwQaBqowINSQjPrxAr-Da8fVPKUkUcqAq30_BCxj1X0nW70HQRmAa-rIvzmKUA==",
            permission = 1,
            contentFormatVersion = 1,
            acceptanceSignature = "wsBzBAABCgAnBQJixYtVCZCxxaTHfHJcrBahBAJbjxf5H602QyQMqbHFpMd8clysAABeyAgAsdS602ALcD//wp3QtoSwNe8jwA5xjtQkeSBFrZ2wgOYgIHzKUf2iaatpGvoJDQN6GJLXAj791QapXuzDWtu9JH+/vkABH7hfVCb/33uHYdXfuoRqxJg128he2sBptNqjkF46D7+n0jhfUMTcDZFuoatNkqEuxqGWQasvV8ZrM3uWJ3MPjk+Vhehp8T3PqIXOkLyHUWv3kRzgDLvHrHAAE7ccSopGsGEKXW9EM2yztmXHs6I72XYWwKaUNQcboO6lAZBQxBraiUjAmJydY4JQgJAGXOWGNzBW8yOEr0/eF2hn1Ou0XpHgmGnbPqjzdUjKW/MWTCq5vojMrNQRT94b2g==",
            inviterEmail = "free@protonmail.dev",
            inviterAcceptanceSignature = "wsBzBAABCgAnBQJixYtVCZCxxaTHfHJcrBahBAJbjxf5H602QyQMqbHFpMd8clysAABeyAgAsdS602ALcD//wp3QtoSwNe8jwA5xjtQkeSBFrZ2wgOYgIHzKUf2iaatpGvoJDQN6GJLXAj791QapXuzDWtu9JH+/vkABH7hfVCb/33uHYdXfuoRqxJg128he2sBptNqjkF46D7+n0jhfUMTcDZFuoatNkqEuxqGWQasvV8ZrM3uWJ3MPjk+Vhehp8T3PqIXOkLyHUWv3kRzgDLvHrHAAE7ccSopGsGEKXW9EM2yztmXHs6I72XYWwKaUNQcboO6lAZBQxBraiUjAmJydY4JQgJAGXOWGNzBW8yOEr0/eF2hn1Ou0XpHgmGnbPqjzdUjKW/MWTCq5vojMrNQRT94b2g==",
            signingKey = "-----BEGIN PGP PRIVATE KEY BLOCK-----\nVersion: ProtonMail\n\nxYYEYsWLVRYJKwYBBAHaRw8BAQdAYe0B6w4y0dX0QT5135MNa3DpzGDoZFc3\n0UV6swjJJDP+CQMItepbCMt5q7hgcBTfR2RHWJnybvYf0dFLxbZWD2hE0WZQ\nVA46tmNNebHOhGqCCv8wlQ2LFe4zmTOBgmXMTXA4qIs9xb0nLtmDg6D4MYga\n0c0pVmF1bHRTaWduaW5nS2V5IDx2YXVsdF9zaWduaW5nQHByb3Rvbi5jaD7C\njAQTFggAPgUCYsWLVQmQmoiBOiGGyYsWoQR8DJXWelGgB6mTWhyaiIE6IYbJ\niwIbAwIeAQIZAQMLCQcCFQgDFgACAiIBAABcvwEAtoIbVYqgeZM4OkWhAyQJ\nMFybMhIfevMeQnjRjoxBvh4A/17VxFr3hccqBICaH32jRzsBb2amls9y8GUa\nsVSAWooIx4sEYsWLVRIKKwYBBAGXVQEFAQEHQCl7WRv6krhp0ez1A7BJ6Pii\nV8AXpRkhueyyNKucrxl3AwEKCf4JAwgtbBUu8hWt8GDyzmU5OJAaGRF6CI5S\nzr0kwv1LLaCD+tONkb0DMAL6zSrdE78SNTJvsH7yiUg5yILNe3YbhjpC34cL\nduOeQGgUy5sjY6XiwngEGBYIACoFAmLFi1UJkJqIgTohhsmLFqEEfAyV1npR\noAepk1ocmoiBOiGGyYsCGwwAAND6AQCgzP6kFhe4fBjrxB8+Ed7MIV0kMV/e\nKmeoL2QOON8l8gD7BV9NAbO6F4/MtnrDTuLW/JRpvh96B6C3a1nLusb0ngI=\n=J+30\n-----END PGP PRIVATE KEY BLOCK-----\n",
            signingKeyPassphrase = "wcBMAy/0ZYUV6hn1AQf9Ei+O7J957ajbJ7SEGS2aB+tZJDbqywbqVp1V76Vt0nwJfL3dqGtYIIFq0w0RJ0FwhAujGFvZ8LMqOTf/+s51NVzF9cOC3f1WTg4ZqAyCL2Z3QPL1kFFijh1wl842eXL7UkncN7mtFeqh13D2uc8YG4RJsxXMy+0dPOs+Xbq7yO1DBBcEI2uoqPEx+hKMVs5f3B4FK97jBkN6dzQAUGGuo8pCf2F3+IvZW94qbiXM13ZrcA/I8hLlGRBlOLgHtUyekwhqwnDyGHnWMFah+/RnCslOmQea3Zpzkt8wANp/j90DotJ15YOFSoQFlPfpqkU9WymGjXZyziB3XbVqlRoKc9JRAQqKBQSDLVp+1DskFeSXMeyzldk+/yqQJk1lk82DSHMLx8d0o5ZfMLhCew/zdO4IYhYbBFjh+Wro4sHAcM+AHxEyQvOC0H7XbHhAJfrkrlvn",
            content = "wV4DTVLdXEpi9m0SAQdAtRO2o3I8J4s7zSTSzYGB2h6PLe02f4sF5F9ic9XUN1cwZZayMLup+esKbuJd2I0OERHZTbT+r6GAX03iz3muYOqdtUNKs7gUHh2G5yNzFFNR0sAiARODb+wO6D4YP0oFGNCc0+od6YIUVFcRyd+YvGYc229RSOXgcteidV81CmBbzsT+8KEvxq77w+s6NyWfC/j6iy9AXEUbTlp16Rx4rcHEjoMmeP44Gas1aLp0EnWE9Hm7BO3CJqojmTNofehqWzJgJlatOqoUxElEnKG2wQqTSGnBSChrp6fgHZSFO2bzhi7C6imRFMjeBPNG3+sSXWfPJuZTzqMKEwxzWGlyTlZwcheL61ZCJtU7/bAoe5Ohn/lE3oB78h/9M4eyEKDaBrGI2kiZEWojXz4zZvGfLXcQLQ7AVg==",
            contentRotationId = "1n_k9CcSJMEENfiOEx_Si54IBdw1ogPxSDkIKXZSEwR-3FSZ7d6fy32TMPIrsm_lfmOfTWhmpo3dz0NB_kYPhw==",
            contentEncryptedAddressSignature = "wV4DTVLdXEpi9m0SAQdAXghUtrW54F9pZoqdcwV2WSaojqpNJVB+kWQQbSE/ph8wzg658zCfqur1dppLNoom2pGpaUDhUy9jfIfTcvnWpcAcsDVHl+KSHKUJc+xHmPEg0sEuAff9OqrJIHy6FrHcAaLFw3w+9VNNQkykOsCr4WzYR+i6Sju6oL78PNuqGHf7o/rs1CpIgExWe8QYTwgVnrVOEUhUf/pdNysQ/2XQ2SwiezDeBF3aBPRXNF9pzkW1lq6es+neN6TE3wMGHIjeuPfm7c7jMAQMdJPAiXMlkYdp8mDZ3e/lhT2Al7lsMSSy7zA1rmKBrcmPTl3gRvpHavVlDn+mOp68QJlEk9mfEi4M8s9TsW2ZUUq1hfpr0TjUUg/AqdftQye8NlCDdV5GY2JZ+12HG0mDPm+Rrdf3dJ8qvJHl5vmGQqHbUDP7nLzXRlymP/CKKIa3YgG+yPB5tUb5UL0lUpnl6TV+do55FtTh5F/hqjr59ZxhlMPtyZ6K92QXyv0A7/QXXHtCslYg7s4gaxfyy5GgpbseC33WNAkVKiux85XJIsC52u6zkn1Ff83RjMGR/LpY3duyfmqtmoOEAx0IjoLNrvhinrXiJ9riv2tBsIXnOekuQv1KrlAmbxJi7m2khMM9vGHbWepUISxJzI3fFkCVL7mo8Whacrl4dLK2B+nPEakq/vo9ZSjWbwnmkucenCw0a7r6LIZquJmGyKV4wIP8G+hHZ5mO9epNSkUYaugGheDJxR+bWla+xLlh/VvdLKsAuBpJDheNHkE=",
            contentEncryptedVaultSignature = "wV4DTVLdXEpi9m0SAQdAzr3sVMQzw/jb4EhjKUe08HpYO3Bp5LpImBdiWwgFIiQwqn38vDpCygMl7cqUMBU2HnMfz2tyS3JsLegkuSC5p7XTv9O5AzM4x3P+FPI/OKnc0sBuAUm8zteYmqUuCgEABRXrid4OkSfAkaIJvvtq2qbF5Er6EVmmtvQPN0Pbp4hb9gy9lm8I3qMWdtgX55aLJBm0e4QG8PjJ26zbXGTepCM0wM0RZFhDyA6hOxJGdWrZDRFGYgK9kCVyHEPBI4s1CBjYHaptxwGA+Ok3n0EikX1EC3IwvM/KLqrO8T2XbLXIDfruL+diyUajBY/uZ0yXerUzfY4t2EqTWMNItqHiKAewI/7mAsXhec5GNzT6swg3cY7RFFHMNYWXNElkVNWwIGcTiWaqN5DmZDkBUgD6C+rBsBJaygdb2+LxFaOXufrnoA9l4vUknJNZcvbfn/yXm5W1tmj419ofQrNsPifcz8eKRBb+yOMi4V95q3WosTei4hDoEEAsbNXYvpXRwW9bOpE=",
            contentSignatureEmail = "free@protonmail.dev",
            expirationTime = null,
            createTime = 1657113429
        )

        private fun getVaultKey(): PrivateKey = PrivateKey(
            key = VAULT_KEY,
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = PlainByteArray(
                VAULT_KEY_PASSPHRASE.encodeToByteArray()
            ).encrypt(TestUtils.cryptoContext.keyStoreCrypto)
        )
    }
}
