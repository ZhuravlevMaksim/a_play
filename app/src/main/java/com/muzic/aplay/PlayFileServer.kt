package com.muzic.aplay

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.SimpleWebServer
import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.Authority
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission
import org.apache.ftpserver.usermanager.impl.TransferRatePermission
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.File
import java.util.*


class PlayFileServer {

    private val port = 1224
    private val host = "localhost"
    private val path = File(".")

    private var ftpServer: FtpServer?  = null
    private var httpServer: NanoHTTPD? = null

    fun startHttp(): ServerInfo {
        httpServer = SimpleWebServer(host, port, path, true)
        httpServer?.start()
        return ServerInfo("http://$host:$port/")
    }

    fun startFtp(): ServerInfo {

        val factory = ListenerFactory()
        factory.port = port

        val serverFactory = FtpServerFactory()
        serverFactory.addListener("default", factory.createListener())
        
        val connectionConfigFactory = ConnectionConfigFactory()
        connectionConfigFactory.isAnonymousLoginEnabled = true
        connectionConfigFactory.maxLoginFailures = 5
        connectionConfigFactory.loginFailureDelay = 2000
        serverFactory.connectionConfig = connectionConfigFactory.createConnectionConfig()

        val user = BaseUser()
        user.name = "a play"
        user.password = null
        user.homeDirectory = File(".").path

        val list: MutableList<Authority> = ArrayList()
        list.add(WritePermission())
        list.add(TransferRatePermission(0, 0))
        list.add(ConcurrentLoginPermission(10, 10))
        user.authorities = list

        serverFactory.userManager.save(user)

        ftpServer = serverFactory.createServer()
        ftpServer?.start()

        return ServerInfo("ftp://localhost:$port/", user.name, user.password)
    }

    data class ServerInfo(
        val path: String,
        val user: String? = null,
        val password: String? = null
    )

}