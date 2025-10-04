package com.android.blebutton

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ConnectingFragment : Fragment(), LogCalls {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        printLog(TAG, "onCreateView()")
        val view = inflater.inflate(R.layout.fragment_connecting, container, false)

        if (activity is DeviceActivity) {
            val service = (activity as DeviceActivity).serviceConnection.service

            val textAddress: TextView = view.findViewById(R.id.text_address)
            textAddress.text = service?.bleDeviceAddress
        }

        return view
    }

    companion object {
        const val TAG = "ConnectingFragment"
    }
}
