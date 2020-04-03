package com.kibab.btpult

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.bt_device_row.view.*

/*
 * Default ArrayAdapter just calls toString() on its items, which for BluetoothDevice just makes an address.
 * We want to have a device name in the list as well, probably in the future also something else.
 */
class BTDeviceListArrayAdapter(context: Context?, resource: Int, list: ArrayList<BluetoothDevice>) : ArrayAdapter<BluetoothDevice>(context, resource, list) {

    private var deviceList: ArrayList<BluetoothDevice> = list
    private val inflater: LayoutInflater
            = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = inflater.inflate(R.layout.bt_device_row, parent, false)
        val device = deviceList[position]
        rowView.device_name.text = device.name
        rowView.device_address.text = device.address
        rowView.device_class.text = device.bluetoothClass.toString()
        return rowView
    }
}