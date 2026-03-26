package com.example.netarchive.data.local

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Копирует изображение из Uri во внутреннее хранилище
     * @return URI сохранённого файла
     */
    fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open URI: $uri")

        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
        val outputFile = File(context.filesDir, "avatars/$fileName").apply {
            parentFile?.mkdirs()
        }

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return outputFile.toURI().toString()
    }

    /**
     * Удаляет файл по URI
     */
    fun deleteFile(uri: String): Boolean {
        return try {
            val file = File(Uri.parse(uri).path!!)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
}