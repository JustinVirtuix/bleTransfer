package com.virtuix.bluetoothtransfer

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.virtuix.bluetoothtransfer.databinding.FragmentFirstBinding
import android.location.LocationManager
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.os.HandlerThread





/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    fun missingPermissions(permissions: List<String>): List<String> {
        val missingPermissions = mutableListOf<String>()
        permissions.forEach{ permission ->
            val permissionStatus = ContextCompat.checkSelfPermission(requireContext(), permission)
            if(permissionStatus == PackageManager.PERMISSION_DENIED) {
                Log.d(TAG,"Missing $permission")
                missingPermissions.add(permission)
            }
            if(permissionStatus == PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG,"Granted $permission")
            }
        }
        return missingPermissions;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LocationServices are enabled: ${areLocationServicesEnabled()}")
        missingPermissions(listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN,
        ))
        val fineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
        if(fineLocationPermission == PackageManager.PERMISSION_DENIED) {
            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                binding.deviceButton.isEnabled = isGranted
            }
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!areLocationServicesEnabled()) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        binding.peripheralButton.setOnClickListener {
            context?.let { validContext ->
                val giftOfGabPeripheral = BlessedPeripheral(validContext)
                giftOfGabPeripheral.setupAndStartAdvertising()
                disableButtonsUpdateStatusText(statusText = validContext.getString(R.string.status_text_peripheral_mode))
            }
        }
        binding.deviceButton.setOnClickListener {
            context?.let { validContext ->
                val handlerThread = HandlerThread("MyHandlerThread")
                handlerThread.start()
                val handler = Handler(handlerThread.looper)
//                val rawDevice = RawDevice(validContext)
//                rawDevice.scanForPeripheral()

                val listenerDevice = BlessedDevice(validContext, handler)
                listenerDevice.scanForGabPeripheral()
                disableButtonsUpdateStatusText(statusText = validContext.getString(R.string.status_text_device_mode))
            }
        }
    }

    private fun areLocationServicesEnabled(): Boolean {
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return isGpsEnabled || isNetworkEnabled
    }

    private fun disableButtonsUpdateStatusText(statusText: String) {
        binding.statusTextview.text = statusText
        binding.peripheralButton.isEnabled = false
        binding.deviceButton.isEnabled = false
    }

    companion object {
        private const val TAG = "BluetoothTransfer::FirstFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}