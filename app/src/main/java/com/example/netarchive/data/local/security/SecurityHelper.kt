package com.example.netarchive.data.local.security

import com.example.netarchive.BuildConfig
import java.security.KeyStore
import java.security.MessageDigest

object SecurityHelper {
    private const val KEY_ALIAS = "netarchive_db_key"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"

    /**
     * Stable passphrase for SQLCipher. Android Keystore keys do not expose [javax.crypto.SecretKey.getEncoded],
     * so an older implementation could produce different passphrases across installs/devices.
     */
    fun getDatabasePassword(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$KEY_ALIAS:${BuildConfig.APPLICATION_ID}"
        return digest.digest(input.toByteArray(Charsets.UTF_8))
    }

    /** Removes unused Keystore entry from previous app versions. */
    fun clearLegacyKeystoreEntry() {
        try {
            KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
                .deleteEntry(KEY_ALIAS)
        } catch (_: Exception) {
        }
    }
}
