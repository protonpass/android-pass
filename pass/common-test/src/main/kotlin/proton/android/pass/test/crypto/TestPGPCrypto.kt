package proton.android.pass.test.crypto

import me.proton.core.crypto.common.pgp.Armored
import me.proton.core.crypto.common.pgp.DataPacket
import me.proton.core.crypto.common.pgp.DecryptedData
import me.proton.core.crypto.common.pgp.DecryptedFile
import me.proton.core.crypto.common.pgp.DecryptedMimeMessage
import me.proton.core.crypto.common.pgp.DecryptedText
import me.proton.core.crypto.common.pgp.EncryptedFile
import me.proton.core.crypto.common.pgp.EncryptedMessage
import me.proton.core.crypto.common.pgp.EncryptedPacket
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.crypto.common.pgp.HashKey
import me.proton.core.crypto.common.pgp.KeyPacket
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.PGPHeader
import me.proton.core.crypto.common.pgp.SessionKey
import me.proton.core.crypto.common.pgp.Signature
import me.proton.core.crypto.common.pgp.SignatureContext
import me.proton.core.crypto.common.pgp.Unarmored
import me.proton.core.crypto.common.pgp.UnlockedKey
import me.proton.core.crypto.common.pgp.VerificationContext
import me.proton.core.crypto.common.pgp.VerificationTime
import java.io.File

object TestPGPCrypto : PGPCrypto {
    override fun decryptAndVerifyData(
        data: DataPacket,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime
    ): DecryptedData {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptAndVerifyData(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime
    ): DecryptedData {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptAndVerifyFile(
        source: EncryptedFile,
        destination: File,
        sessionKey: SessionKey,
        publicKeys: List<Armored>,
        time: VerificationTime
    ): DecryptedFile {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptAndVerifyText(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime
    ): DecryptedText {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptAndVerifyMimeMessage(
        message: EncryptedMessage,
        publicKeys: List<Armored>,
        unlockedKeys: List<Unarmored>,
        time: VerificationTime
    ): DecryptedMimeMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptData(data: DataPacket, sessionKey: SessionKey): ByteArray {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptData(message: EncryptedMessage, unlockedKey: Unarmored): ByteArray {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptFile(
        source: EncryptedFile,
        destination: File,
        sessionKey: SessionKey
    ): DecryptedFile {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptSessionKey(keyPacket: KeyPacket, unlockedKey: Unarmored): SessionKey {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptSessionKeyWithPassword(
        keyPacket: KeyPacket,
        password: ByteArray
    ): SessionKey {
        throw IllegalStateException("This method should not be called")
    }

    override fun signText(
        plainText: String,
        unlockedKey: Unarmored,
        trimTrailingSpaces: Boolean,
        signatureContext: SignatureContext?
    ): Signature {
        throw IllegalStateException("This method should not be called")
    }

    override fun signData(
        data: ByteArray,
        unlockedKey: Unarmored,
        signatureContext: SignatureContext?
    ): Signature {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptText(message: EncryptedMessage, unlockedKey: Unarmored): String {
        throw IllegalStateException("This method should not be called")
    }

    override fun decryptMimeMessage(
        message: EncryptedMessage,
        unlockedKeys: List<Unarmored>
    ): DecryptedMimeMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptAndSignData(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptAndSignData(
        data: ByteArray,
        sessionKey: SessionKey,
        unlockedKey: Unarmored
    ): DataPacket {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptAndSignDataWithCompression(
        data: ByteArray,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptAndSignFile(
        source: File,
        destination: File,
        sessionKey: SessionKey,
        unlockedKey: Unarmored
    ): EncryptedFile {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptAndSignText(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptAndSignTextWithCompression(
        plainText: String,
        publicKey: Armored,
        unlockedKey: Unarmored
    ): EncryptedMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptData(data: ByteArray, publicKey: Armored): EncryptedMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptData(data: ByteArray, sessionKey: SessionKey): DataPacket {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptFile(
        source: File,
        destination: File,
        sessionKey: SessionKey
    ): EncryptedFile {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptSessionKey(sessionKey: SessionKey, publicKey: Armored): KeyPacket {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptSessionKeyWithPassword(
        sessionKey: SessionKey,
        password: ByteArray
    ): KeyPacket {
        throw IllegalStateException("This method should not be called")
    }

    override fun encryptText(plainText: String, publicKey: Armored): EncryptedMessage {
        throw IllegalStateException("This method should not be called")
    }

    override fun generateNewHashKey(): HashKey {
        throw IllegalStateException("This method should not be called")
    }

    override fun generateNewKeySalt(): String {
        throw IllegalStateException("This method should not be called")
    }

    override fun generateNewPrivateKey(
        username: String,
        domain: String,
        passphrase: ByteArray
    ): Armored {
        throw IllegalStateException("This method should not be called")
    }

    override fun generateNewSessionKey(): SessionKey {
        throw IllegalStateException("This method should not be called")
    }

    override fun generateNewToken(size: Long): ByteArray {
        throw IllegalStateException("This method should not be called")
    }

    override fun generateRandomBytes(size: Long): ByteArray {
        throw IllegalStateException("This method should not be called")
    }

    override fun getArmored(data: Unarmored, header: PGPHeader): Armored {
        throw IllegalStateException("This method should not be called")
    }

    override fun getBase64Decoded(string: String): ByteArray {
        throw IllegalStateException("This method should not be called")
    }

    override fun getBase64Encoded(array: ByteArray): String {
        throw IllegalStateException("This method should not be called")
    }

    override fun getEncryptedPackets(message: EncryptedMessage): List<EncryptedPacket> {
        throw IllegalStateException("This method should not be called")
    }

    override fun getFingerprint(key: Armored): String {
        throw IllegalStateException("This method should not be called")
    }

    override fun getJsonSHA256Fingerprints(key: Armored): String {
        throw IllegalStateException("This method should not be called")
    }

    override fun getPassphrase(password: ByteArray, encodedSalt: String): ByteArray {
        throw IllegalStateException("This method should not be called")
    }

    override fun getPublicKey(privateKey: Armored): Armored {
        throw IllegalStateException("This method should not be called")
    }

    override fun getUnarmored(data: Armored): Unarmored {
        throw IllegalStateException("This method should not be called")
    }

    override fun isPrivateKey(key: Armored): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun isPublicKey(key: Armored): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun isValidKey(key: Armored): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun lock(unlockedKey: Unarmored, passphrase: ByteArray): Armored {
        throw IllegalStateException("This method should not be called")
    }

    override fun signFile(file: File, unlockedKey: Unarmored): Signature {
        throw IllegalStateException("This method should not be called")
    }

    override fun signTextEncrypted(
        plainText: String,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        trimTrailingSpaces: Boolean,
        signatureContext: SignatureContext?
    ): EncryptedSignature {
        throw IllegalStateException("This method should not be called")
    }

    override fun signDataEncrypted(
        data: ByteArray,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>,
        signatureContext: SignatureContext?
    ): EncryptedSignature {
        throw IllegalStateException("This method should not be called")
    }

    override fun signFileEncrypted(
        file: File,
        unlockedKey: Unarmored,
        encryptionKeys: List<Armored>
    ): EncryptedSignature {
        throw IllegalStateException("This method should not be called")
    }

    override fun verifyText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun verifyData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun verifyFile(
        file: DecryptedFile,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun getVerifiedTimestampOfText(
        plainText: String,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Long? {
        throw IllegalStateException("This method should not be called")
    }

    override fun getVerifiedTimestampOfData(
        data: ByteArray,
        signature: Armored,
        publicKey: Armored,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Long? {
        throw IllegalStateException("This method should not be called")
    }

    override fun verifyTextEncrypted(
        plainText: String,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        trimTrailingSpaces: Boolean,
        verificationContext: VerificationContext?
    ): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun verifyDataEncrypted(
        data: ByteArray,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun verifyFileEncrypted(
        file: File,
        encryptedSignature: EncryptedSignature,
        privateKey: Unarmored,
        publicKeys: List<Armored>,
        time: VerificationTime,
        verificationContext: VerificationContext?
    ): Boolean {
        throw IllegalStateException("This method should not be called")
    }

    override fun unlock(privateKey: Armored, passphrase: ByteArray): UnlockedKey {
        throw IllegalStateException("This method should not be called")
    }

    override fun updatePrivateKeyPassphrase(
        privateKey: Armored,
        passphrase: ByteArray,
        newPassphrase: ByteArray
    ): Armored {
        throw IllegalStateException("This method should not be called")
    }

    override fun updateTime(epochSeconds: Long) {
        throw IllegalStateException("This method should not be called")
    }

    override suspend fun getCurrentTime(): Long {
        throw IllegalStateException("This method should not be called")
    }
}
