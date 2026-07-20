package com.example.myapplication.util

import android.util.Log
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections

class DriveServiceHelper(private val mDriveService: Drive) {

    fun uploadFile(filePath: java.io.File, fileName: String): String? {
        Log.d("DriveServiceHelper", "Uploading file: $fileName")
        val metadata = File()
            .setName(fileName)
            .setParents(Collections.singletonList("appDataFolder"))

        val mediaContent = FileContent("application/octet-stream", filePath)

        return try {
            val result = mDriveService.files().list()
                .setSpaces("appDataFolder")
                .execute()
            
            val existingFile = result.files?.find { it.name == fileName }
            
            if (existingFile != null) {
                Log.d("DriveServiceHelper", "Updating existing file: ${existingFile.id}")
                mDriveService.files().update(existingFile.id, null, mediaContent).execute()
                existingFile.id
            } else {
                Log.d("DriveServiceHelper", "Creating new file")
                val googleFile = mDriveService.files().create(metadata, mediaContent).execute()
                googleFile.id
            }
        } catch (e: IOException) {
            Log.e("DriveServiceHelper", "Error uploading file", e)
            null
        }
    }

    fun downloadFile(destFile: java.io.File, fileName: String): Boolean {
        Log.d("DriveServiceHelper", "Downloading file: $fileName")
        return try {
            val result = mDriveService.files().list()
                .setSpaces("appDataFolder")
                .execute()

            val googleFile = result.files?.find { it.name == fileName }
            
            if (googleFile == null) {
                Log.d("DriveServiceHelper", "File not found in Drive")
                return false
            }

            Log.d("DriveServiceHelper", "Downloading file id: ${googleFile.id}")
            val outputStream = FileOutputStream(destFile)
            mDriveService.files().get(googleFile.id).executeMediaAndDownloadTo(outputStream)
            outputStream.flush()
            outputStream.close()
            true
        } catch (e: IOException) {
            Log.e("DriveServiceHelper", "Error downloading file", e)
            false
        }
    }
}