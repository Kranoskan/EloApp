package com.example.myapplication.util

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {
    fun zip(files: List<File>, zipFile: File, baseDir: File? = null) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
            for (file in files) {
                if (file.exists()) {
                    val entryName = if (baseDir != null && file.absolutePath.startsWith(baseDir.absolutePath)) {
                        file.absolutePath.removePrefix(baseDir.absolutePath).removePrefix("/")
                    } else {
                        file.name
                    }
                    
                    val data = ByteArray(1024)
                    FileInputStream(file).use { fi ->
                        BufferedInputStream(fi).use { origin ->
                            val entry = ZipEntry(entryName)
                            out.putNextEntry(entry)
                            var count: Int
                            while (origin.read(data, 0, 1024).also { count = it } != -1) {
                                out.write(data, 0, count)
                            }
                        }
                    }
                }
            }
        }
    }

    fun unzip(zipFile: InputStream, targetDirectory: File) {
        ZipInputStream(BufferedInputStream(zipFile)).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                val file = File(targetDirectory, entry.name)
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    val parent = file.parentFile
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs()
                    }
                    FileOutputStream(file).use { fos ->
                        val buffer = ByteArray(1024)
                        var count: Int
                        while (zis.read(buffer).also { count = it } != -1) {
                            fos.write(buffer, 0, count)
                        }
                    }
                }
                entry = zis.nextEntry
            }
        }
    }
}