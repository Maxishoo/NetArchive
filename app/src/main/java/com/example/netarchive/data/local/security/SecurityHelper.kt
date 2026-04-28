package com.example.netarchive.data.local.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.example.netarchive.BuildConfig
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


object SecurityHelper {
    private const val KEY_ALIAS = "netarchive_db_key"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }

    fun getDatabasePassword(): ByteArray {
        val key = getOrCreateKey()

        val encoded = key.encoded
        if (encoded != null) {
            return encoded
        }
        return derivePasswordFromAlias()
    }

    private fun getOrCreateKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) {
            return existingKey.secretKey
        }

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEY_STORE
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
        }.generateKey()
    }
    private fun derivePasswordFromAlias(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$KEY_ALIAS:${BuildConfig.APPLICATION_ID}"
        return digest.digest(input.toByteArray())
    }

    fun deleteKey() {
        keyStore.deleteEntry(KEY_ALIAS)
    }
}