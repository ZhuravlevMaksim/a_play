package com.muzic.aplay

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
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
            for (file in files) {
                try {
                    val fileName: String = file.name
                    val fileContent = file.string
//                    Files.write(Paths.get(fileName), fileContent)
                    println(fileName)
                    println(fileContent)
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