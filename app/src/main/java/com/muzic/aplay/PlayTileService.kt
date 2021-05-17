package com.muzic.aplay

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

//todo: add foreground
class PlayTileService : TileService() {

    private var fileServer: FileServer? = null

    override fun onDestroy() {
        fileServer?.stop()
        fileServer = null
    }

    override fun onStartListening() {
        fileServer = fileServer ?: FileServer(applicationContext)
    }

    override fun onStopListening() {
        fileServer?.stop()
    }

    override fun onClick() {
        with(qsTile) {
            state = when (state) {
                Tile.STATE_ACTIVE -> Tile.STATE_INACTIVE
                Tile.STATE_INACTIVE -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            }
            if (state == Tile.STATE_ACTIVE) {
                fileServer = fileServer ?: FileServer(applicationContext)
                if (fileServer?.isAlive == false) {
                    println(fileServer?.startServer())
                }
            } else {
                fileServer?.stop()
            }
            updateTile()
        }
    }
}