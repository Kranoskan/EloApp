package com.example.myapplication.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileUtils {
    fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resuelve un URI guardado de forma que sea portable entre dispositivos.
     * Si la ruta absoluta guardada no existe (por ejemplo, al restaurar en otro dispositivo),
     * intenta buscar el archivo por nombre en el directorio de archivos interno actual.
     */
    fun getPortableUri(context: Context, uriString: String?): Uri? {
        if (uriString == null) return null
        val uri = Uri.parse(uriString)
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (!file.exists()) {
                val fileName = file.name
                val localFile = File(context.filesDir, fileName)
                if (localFile.exists()) {
                    return Uri.fromFile(localFile)
                }
            }
        }
        return uri
    }
}
