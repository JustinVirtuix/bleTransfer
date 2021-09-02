package com.virtuix.bluetoothtransfer

import android.bluetooth.*
import android.content.Context
import android.util.Log
import android.bluetooth.BluetoothGattCharacteristic.*
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import java.util.*
import android.bluetooth.le.AdvertiseData
import android.os.ParcelUuid
import android.bluetooth.le.AdvertiseSettings
import com.welie.blessed.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.bluetooth.BluetoothGattCharacteristic



class BlessedPeripheral(private val context: Context) {
    val junkData = ByteArray(DATA_SIZE)
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val peripheralManagerCallback :BluetoothPeripheralManagerCallback = object : BluetoothPeripheralManagerCallback() {
        override fun onAdvertiseFailure(advertiseError: AdvertiseError) {
            Log.d(TAG, "onAdvertiseFailure ${advertiseError.name}")
            super.onAdvertiseFailure(advertiseError)
        }

        override fun onServiceAdded(status: GattStatus, service: BluetoothGattService) {
            Log.d(TAG, "onServiceAdded $status ${service.uuid}")
            super.onServiceAdded(status, service)
        }

        override fun onCentralConnected(bluetoothCentral: BluetoothCentral) {
            Log.d(TAG, "onCentralConnected ${bluetoothCentral.name} current MTU ${bluetoothCentral.currentMtu}")
            (0 until DATA_SIZE).forEach { value ->
                junkData[value] = 0//value.toByte();
            }
            super.onCentralConnected(bluetoothCentral)
        }

        override fun onCentralDisconnected(bluetoothCentral: BluetoothCentral) {
            Log.d(TAG, "onCentralDisconnected ${bluetoothCentral.name}")
            super.onCentralDisconnected(bluetoothCentral)
        }

        override fun onNotifyingEnabled(bluetoothCentral: BluetoothCentral, characteristic: BluetoothGattCharacteristic) {
            Log.d(TAG, "onNotifyingEnabled ${bluetoothCentral.name}::${characteristic.uuid}")
            if(characteristic.uuid == CHARACTERISTIC_UUID) {
                GlobalScope.launch(Dispatchers.IO) {
                    val startTime = Date().time
                    (0 until DATA_SIZE).forEach { value ->
                        junkData[value] = value.toByte();
                        peripheralManager.notifyCharacteristicChanged(junkData, characteristic)
                    }
//                    repeat(NUMBER_OF_MESSAGES) {
////                        Thread.sleep(20)
//                        peripheralManager.notifyCharacteristicChanged(junkData, characteristic)
//                    }
                    Log.d(TAG, "Time to send ${Date().time - startTime} ms")
                }
            }
            super.onNotifyingEnabled(bluetoothCentral, characteristic)
        }

        override fun onNotifyingDisabled(bluetoothCentral: BluetoothCentral, characteristic: BluetoothGattCharacteristic) {
            Log.d(TAG, "onNotifyingDisabled ${bluetoothCentral.name}::${characteristic.uuid}")
            super.onNotifyingDisabled(bluetoothCentral, characteristic)
        }
    }
    private val peripheralManager = BluetoothPeripheralManager(context, bluetoothManager, peripheralManagerCallback)

    fun setupAndStartAdvertising() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        Log.e(TAG, "AdvertistementSupported: ${bluetoothAdapter.isMultipleAdvertisementSupported}")
        if(bluetoothAdapter.isMultipleAdvertisementSupported) {
            addGiftOfGabService()
            startAdvertising()
        }
    }

    private fun startAdvertising() {
        val advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .build()

        val advertiseData = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        peripheralManager.startAdvertising(advertiseSettings, scanResponse, advertiseData)
    }

    private fun addGiftOfGabService() {
        val service = BluetoothGattService(SERVICE_UUID, SERVICE_TYPE_PRIMARY)
        val measurement = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            PROPERTY_READ or PROPERTY_INDICATE or PROPERTY_NOTIFY,
            PERMISSION_READ
        )
        measurement.value = ByteArray(0)
        val bluetoothGattDescriptor = BluetoothGattDescriptor(CCC_DESCRIPTOR_UUID, PERMISSION_READ or PERMISSION_WRITE)
        measurement.addDescriptor(bluetoothGattDescriptor)
        service.addCharacteristic(measurement)
        peripheralManager.add(service)
    }

    companion object {
        const val DATA_SIZE = 128
        const val NUMBER_OF_MESSAGES = DATA_SIZE //100
        private const val TAG = "BluetoothTransfer::BlessedPeripheral"
        val SERVICE_UUID: UUID = UUID.fromString("0000180D-2345-1000-8000-00805f9b34fb")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A37-2345-1000-8000-00805f9b34fb")
        private val CUD_DESCRIPTOR_UUID = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb")
        private val CCC_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

}