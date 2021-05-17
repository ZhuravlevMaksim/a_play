package com.muzic.aplay

import android.content.ContentValues
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.text.format.Formatter
import fi.iki.elonen.NanoFileUpload
import fi.iki.elonen.NanoHTTPD
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory


class FileServer(val context: Context) : NanoHTTPD(12284) {

    private val host by lazy {
        val wm = context.getSystemService(WIFI_SERVICE) as WifiManager
        Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
    }

    fun startServer(): String {
        this.start(SOCKET_READ_TIMEOUT, false)
        return "http://$host:${this.listeningPort}/"
    }

    override fun serve(session: IHTTPSession?): Response {
        return try {
            val files: List<FileItem> = NanoFileUpload(DiskFileItemFactory()).parseRequest(session)
            var uploadedCount = 0

            val downloads = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val path = session?.headers?.get("path") ?: ""

            for (file in files) {
                try {
                    val fileName: String = file.name
                    val fileContent = file.get()

                    val details = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.DownloadColumns.RELATIVE_PATH, "Download/${path}")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val contentUri = context.contentResolver.insert(downloads, details)

                    contentUri?.let {
                        context.contentResolver.openFileDescriptor(it, "w").use { descriptor ->
                            ParcelFileDescriptor.AutoCloseOutputStream(descriptor).write(fileContent)
                        }
                        details.clear()
                        details.put(MediaStore.Downloads.IS_PENDING, 0)
                        context.contentResolver.update(it, details, null, null)
                    }

                    uploadedCount++
                } catch (exception: Exception) {
                    // handle
                }
            }
            newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "Uploaded files " + uploadedCount + " out of " + files.size)
        } catch (e: java.lang.Exception) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Error when uploading")
        }
    }

}