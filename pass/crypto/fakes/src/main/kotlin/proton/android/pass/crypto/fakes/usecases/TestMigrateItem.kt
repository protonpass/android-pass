package proton.android.pass.crypto.fakes.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.usecases.EncryptedMigrateItemBody
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.pass.domain.key.ShareKey

class TestMigrateItem : MigrateItem {

    private var output: EncryptedMigrateItemBody? = null

    fun setOutput(value: EncryptedMigrateItemBody) {
        output = value
    }

    override fun migrate(
        destinationKey: ShareKey,
        encryptedItemContents: EncryptedByteArray,
        contentFormatVersion: Int
    ): EncryptedMigrateItemBody = output ?: throw IllegalStateException("output not set")
}
