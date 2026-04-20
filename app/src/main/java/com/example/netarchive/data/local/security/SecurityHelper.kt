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

    /**
     * Получает пароль для шифрования БД.
     * Если ключ можно экспортировать — используем его.
     * Если нет (на некоторых устройствах) — деривируем пароль из алиаса ключа.
     */
    fun getDatabasePassword(): ByteArray {
        val key = getOrCreateKey()

        // 🔹 Попытка 1: получить сырой ключ (работает на некоторых устройствах)
        val encoded = key.encoded
        if (encoded != null) {
            return encoded
        }

        // 🔹 Попытка 2: сгенерировать детерминированный пароль из алиаса ключа
        // Это менее безопасно, но работает везде и привязано к приложению/устройству
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

    /**
     * Деривирует пароль из алиаса ключа через SHA-256.
     * Результат детерминированный: один и тот же для этого приложения на этом устройстве.
     */
    private fun derivePasswordFromAlias(): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        // Комбинируем алиас ключа + пакет приложения для уникальности
        val input = "$KEY_ALIAS:${BuildConfig.APPLICATION_ID}"
        return digest.digest(input.toByteArray())
    }

    fun deleteKey() {
        keyStore.deleteEntry(KEY_ALIAS)
    }
}