package com.kibab.btpult

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_robot_manager.*
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RobotManager.newInstance] factory method to
 * create an instance of this fragment.
 */
class RobotManager : Fragment(), CoroutineScope by MainScope() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val args: RobotManagerArgs by navArgs()
    private var btSocket: BluetoothSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_robot_manager, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceAddr.text = args.deviceAddress

        pingButton.setEnabled(this.btSocket != null)
        readData.setEnabled(this.btSocket != null)
        connectDevice.setEnabled(this.btSocket == null)

        connectDevice.setOnClickListener{connectToDevice()}
        pingButton.setOnClickListener{
            if (this.btSocket == null)
                return@setOnClickListener
            try {
                this.btSocket!!.outputStream.write("ping\n".toByteArray())
            } catch (e:IOException) {
                Snackbar.make(view!!, "ping failed: " + e.localizedMessage, 3).show()
            }
        }

        readData.setOnClickListener {
            launch {
                readDataWrapper()
            }
        }

    }

    @ExperimentalStdlibApi
    suspend private fun readDataWrapper() {
        while (this.btSocket != null) {
            withContext(Dispatchers.IO) {
                readData()
            }
        }
    }

    @ExperimentalStdlibApi
    suspend private fun readData() {
        if (this.btSocket == null)
            return
        try {
            var data:ByteArray = ByteArray(512)
            do {
                Log.i("RobotManager.readData", "About to call InputStream.read()")
                val bytesRead = this.btSocket!!.inputStream.read(data)
                withContext(Dispatchers.Main) {
                    deviceLog.append(data.decodeToString(0, bytesRead))
                }
                data.fill(0)
            } while (bytesRead > 0 && this.btSocket!!.inputStream.available() > 0)
            Log.i("RobotManager.readData", "Done reading out")
        } catch (e:IOException) {
            withContext(Dispatchers.Main) {
                Snackbar.make(view!!, "Stream read error: " + e.localizedMessage, 3).show()
            }
            return
        }
    }

    private fun connectToDevice() {
        if (this.btSocket != null)
            return
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(args.deviceAddress)

        // Well-known SDP address: https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createRfcommSocketToServiceRecord(java.util.UUID)
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        this.btSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid) ?: return
        try {
            Log.i("RobotManager", "Connecting")
            this.btSocket!!.connect()
        } catch (e: IOException) {
            Snackbar.make(view!!, "Connection failed: " + e.localizedMessage, 3).show()
            return
        }
        Log.i("RobotManager", "Connection successful")
        pingButton.setEnabled(true)
        readData.setEnabled(true)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment robotManager.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RobotManager().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
