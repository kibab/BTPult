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
    private var isConnected: Boolean = false

    fun Connect() {
        Log.i("KibabRobotManager", "Connect() called")
        if (!isConnected)
            btSocket.connect() // This blocks until connection is successful, or throws an IOException
        isConnected = true
        input = Scanner(btSocket.inputStream).useDelimiter("\n")
    }

    fun IsConnected(): Boolean {
        return isConnected
    }

    fun GetDeviceLog(): String {
        Log.i("KibabRobotManager", "GetDeviceLog() called")
        if (!isConnected)
            throw IOException("Socket is not connected")
        if (input!!.hasNext()) {
            val data = input!!.next()
            Log.i("KibabRobotManager", "Read $data")
            return data + "\n"
        }
        /* If we got here it means that there is no more data in the stream
        * and there won't be any - likely socket is closed.
        * */
        isConnected = false
        return ""
    }

    fun SendPing() {
        if (!isConnected)
            throw IOException("Socket is not yet connected")
        try {
            btSocket.outputStream.write("P_\n".toByteArray())
        } catch (e: IOException) {
            isConnected = false
            throw e /* Propagate to caller */
        }
    }

    fun SetSpeed(speed: Int) {
        if (!isConnected)
            throw IOException("Socket is not yet connected")
        try {
            btSocket.outputStream.write("S$speed\n".toByteArray())
        } catch (e: IOException) {
            isConnected = false
            throw e /* Propagate to caller */
        }
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
        if (!isConnected)
            throw IOException("Socket is not yet connected")
        try {
            btSocket.outputStream.write("M$d\n".toByteArray())
        } catch (e: IOException) {
            isConnected = false
            throw e /* Propagate to caller */
        }
    }
}