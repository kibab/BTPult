package com.kibab.btpult

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_robot_manager.*
import kotlinx.coroutines.*
import java.io.IOException

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

    override fun onDestroy() {
        if (this.btSocket != null && this.btSocket!!.isConnected) {
            this.btSocket!!.close()
        }
        super.onDestroy()
        cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_robot_manager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceAddr.text = args.deviceAddress

        connectDevice.setOnClickListener { connectToDevice() }
        speedSelector.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                Log.i("RobotManager", "Set speed to: $i")
                launch {
                    withContext(Dispatchers.IO) {
                        Mgr?.SetSpeed(i)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        pingButton.setOnClickListener {
            try {
                Mgr?.SendPing()
            } catch (e: IOException) {
                Snackbar.make(view, "ping failed: " + e.localizedMessage, 3).show()
            }
        }

        bt_fwd.setOnClickListener {
            SendMovement('F')
        }
        bt_rev.setOnClickListener {
            SendMovement('B')
        }
        bt_left.setOnClickListener {
            SendMovement('L')
        }
        bt_right.setOnClickListener {
            SendMovement('R')
        }

        refreshButtons()
    }

    private fun refreshButtons() {
        val enable = Mgr?.IsConnected() ?: false
        bt_fwd.isEnabled = enable
        bt_rev.isEnabled = enable
        bt_left.isEnabled = enable
        bt_right.isEnabled = enable
        pingButton.isEnabled = enable
    }

    private fun SendMovement(move: Char) {
        launch {
            withContext(Dispatchers.IO) {
                when (move) {
                    'F' -> Mgr?.MoveForward()
                    'B' -> Mgr?.MoveBackWard()
                    'L' -> Mgr?.TurnLeft()
                    'R' -> Mgr?.TurnRight()
                    else -> throw IllegalArgumentException("Unexpected move '$move'")
                }
            }
        }
    }

    private suspend fun readDataWrapper() {
        withContext(Dispatchers.IO) {
            readData()
        }
    }

    private suspend fun readData() {
        while (Mgr!!.IsConnected()) {
            val logString = Mgr?.GetDeviceLog()
            withContext(Dispatchers.Main) {
                deviceLog.append(logString)
            }
        }
    }

    private fun connectToDevice() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(args.deviceAddress)

        if (Mgr != null)
            return
        Mgr = KibabRobotManager(bluetoothDevice)
        Log.i("RobotManager", "Connecting")
        launch {
            try {
                withContext(Dispatchers.IO) {
                    Mgr?.Connect()
                }
            } catch (e: IOException) {
                Snackbar.make(
                    view!!,
                    "Connection failed: " + e.localizedMessage,
                    Snackbar.LENGTH_LONG
                ).show()
            }
            Log.i("RobotManager", "Connection successful")
            refreshButtons()
            if (Mgr!!.IsConnected())
                readDataWrapper()
        }
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
