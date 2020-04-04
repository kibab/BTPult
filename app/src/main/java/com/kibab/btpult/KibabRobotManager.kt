package com.kibab.btpult

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.*

var Mgr: KibabRobotManager? = null

class KibabRobotManager(dev: BluetoothDevice) {
    // Well-known SDP address: https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createRfcommSocketToServiceRecord(java.util.UUID)
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private var btSocket: BluetoothSocket = dev.createRfcommSocketToServiceRecord(uuid)
    private var input: Scanner? = null

    fun Connect() {
        Log.i("KibabRobotManager", "Connect() called")
        if (!btSocket.isConnected)
            btSocket.connect()
        // Use space as alternate separator since `cu` swallows "\n"
        // when connected to the Bluetooth module via serial port.
        input = Scanner(btSocket.inputStream).useDelimiter("[ \n]")
    }

    fun IsConnected(): Boolean {
        return btSocket.isConnected
    }

    fun GetDeviceLog(): String {
        Log.i("KibabRobotManager", "GetDeviceLog() called")
        if (!this.btSocket.isConnected)
            throw IOException("Socket is not yet connected")
        if (input!!.hasNext()) {
            val data = input!!.next()
            Log.i("KibabRobotManager", "Read $data")
            return data + "\n"
        }
        return ""
    }

    fun SendPing() {
        if (!this.btSocket.isConnected)
            throw IOException("Socket is not yet connected")
        btSocket.outputStream.write("ping\n".toByteArray())
    }

    fun SetSpeed(speed: Int) {
        if (!this.btSocket.isConnected)
            throw IOException("Socket is not yet connected")
        btSocket.outputStream.write("S$speed\n".toByteArray())
    }

    fun MoveForward() {
        Move('F')
    }

    fun MoveBackWard() {
        Move('B')
    }

    fun TurnLeft() {
        Move('L')
    }

    fun TurnRight() {
        Move('R')
    }

    private fun Move(d: Char) {
        if (!this.btSocket.isConnected)
            throw IOException("Socket is not yet connected")
        btSocket.outputStream.write("M$d\n".toByteArray())
    }
}