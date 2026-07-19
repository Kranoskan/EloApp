package com.example.myapplication.util

import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections

class DriveServiceHelper(private val mDriveService: Drive) {

    fun uploadFile(filePath: java.io.File, fileName: String): String? {
        val metadata = File()
            .setName(fileName)
            .setParents(Collections.singletonList("appDataFolder"))

        val mediaContent = FileContent("application/octet-stream", filePath)

        return try {
            // First, try to find if the file already exists to overwrite it
            val result = mDriveService.files().list()
                .setSpaces("appDataFolder")
                .execute()
            
            val existingFile = result.files.find { it.name == fileName }
            
            if (existingFile != null) {
                mDriveService.files().update(existingFile.id, null, mediaContent).execute()
                existingFile.id
            } else {
                val googleFile = mDriveService.files().create(metadata, mediaContent).execute()
                googleFile.id
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun downloadFile(destFile: java.io.File, fileName: String): Boolean {
        return try {
            val result = mDriveService.files().list()
                .setSpaces("appDataFolder")
                .execute()

            val googleFile = result.files.find { it.name == fileName } ?: return false

            val outputStream = FileOutputStream(destFile)
            mDriveService.files().get(googleFile.id).executeMediaAndDownloadTo(outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}