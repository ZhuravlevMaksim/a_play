package com.muzic.aplay

import android.content.Intent
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class PlayTileService : TileService() {

    override fun onDestroy() {
        Log.i("tile-service", "onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("tile-service", "onBind")
        return super.onBind(intent)
    }

    override fun onTileAdded() {
        Log.i("tile-service", "onTileAdded")
        super.onTileAdded()
    }

    override fun onTileRemoved() {
        Log.i("tile-service", "onTileRemoved")
        super.onTileRemoved()
    }

    override fun onStartListening() {
        Log.i("tile-service", "onStartListening")
        super.onStartListening()
    }

    override fun onStopListening() {
        Log.i("tile-service", "onStopListening")
        super.onStopListening()
    }

    override fun onClick() {
        Log.i("tile-service", "onClick")
        super.onClick()

        with(qsTile) {
            state = when (state) {
                Tile.STATE_ACTIVE -> Tile.STATE_INACTIVE
                Tile.STATE_INACTIVE -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            }
            if (state == Tile.STATE_ACTIVE) {
                val info = FileServer(applicationContext).startFtp()
                println(info)
            }
            updateTile()
        }
    }

}