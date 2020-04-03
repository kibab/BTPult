package com.kibab.btpult

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_first.*

/**
 * BT Device selector as the default destination in the navigation.
 */
class DeviceSelector : Fragment() {

    private lateinit var pairedDevices: Set<BluetoothDevice>
    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.SecondFragment)
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "NO BLUETOOTH?!", Toast.LENGTH_SHORT).show()
        } else {
            view.findViewById<Button>(R.id.bt_rescan).setOnClickListener{pairedDeviceList()}
        }

    }

    private fun pairedDeviceList() {
        val list: ArrayList<BluetoothDevice> = ArrayList()

        pairedDevices = bluetoothAdapter!!.bondedDevices
        if (pairedDevices.isNotEmpty()) {
            for (device: BluetoothDevice in pairedDevices) {
                list.add(device)
                Log.i("device", "" + device + ": " + device.name)
            }
        } else {
            Toast.makeText(activity, "No paired devices?!", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = BTDeviceListArrayAdapter(context, android.R.layout.simple_list_item_1, list)
        device_list.adapter = adapter
        device_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
            val address: String = device.address

            Toast.makeText(activity, address, Toast.LENGTH_LONG).show()
            val action = DeviceSelectorDirections.actionManageRobot(address)
            findNavController().navigate(action)
        }
    }

    }
