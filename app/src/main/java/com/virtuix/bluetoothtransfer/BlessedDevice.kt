package com.virtuix.bluetoothtransfer

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.welie.blessed.*
import java.util.*

class BlessedDevice(context: Context, handler: Handler) {

    private val bluetoothPeripheralCallback : BluetoothPeripheralCallback = object : BluetoothPeripheralCallback() {
        var startStartTime = 0L
        var numberMessage = 0
        override fun onCharacteristicUpdate(peripheral: BluetoothPeripheral, value: ByteArray, characteristic: BluetoothGattCharacteristic, status: GattStatus) {
            numberMessage++
            // Log.d(TAG, "$numberMessage ${value.joinToString(",") { it.toString() }}")
            if(startStartTime == 0L) {
                startStartTime = Date().time
            }
            if(numberMessage == BlessedPeripheral.NUMBER_OF_MESSAGES) {
                Log.d(TAG,"Time: ${Date().time - startStartTime}")
                numberMessage = 0
                startStartTime = 0
            }
            super.onCharacteristicUpdate(peripheral, value, characteristic, status)
        }

        override fun onServicesDiscovered(peripheral: BluetoothPeripheral) {
            val characteristic = peripheral.getCharacteristic(BlessedPeripheral.SERVICE_UUID, BlessedPeripheral.CHARACTERISTIC_UUID)
            Log.d(TAG, "Found Characteristic ${characteristic != null}")
            if(characteristic != null) {
                peripheral.setNotify(characteristic, true)
            }
            super.onServicesDiscovered(peripheral)
        }

        override fun onConnectionUpdated(peripheral: BluetoothPeripheral, interval: Int, latency: Int, timeout: Int, status: GattStatus) {
            logPeripheral("onConnectionUpdated", peripheral, "Interval: $interval, Latency: $latency, timeout $timeout")
            super.onConnectionUpdated(peripheral, interval, latency, timeout, status)
        }

        override fun onNotificationStateUpdate(peripheral: BluetoothPeripheral, characteristic: BluetoothGattCharacteristic, status: GattStatus) {
            if (status == GattStatus.SUCCESS) {
                if(peripheral.isNotifying(characteristic)) {
                    logPeripheral("onNotificationStateUpdate", peripheral, "Notifying")
                } else {
                    logPeripheral("onNotificationStateUpdate", peripheral, "Not Notifying no error")
                }
            } else {
                logPeripheral("onNotificationStateUpdate", peripheral, "Error:: Changing state failed $status")
            }
            super.onNotificationStateUpdate(peripheral, characteristic, status)
        }

        override fun onMtuChanged(peripheral: BluetoothPeripheral, mtu: Int, status: GattStatus) {
            logPeripheral("onMtuChanged!!!!", peripheral, "Mtu size $mtu")
            super.onMtuChanged(peripheral, mtu, status)
        }
    }

    private val bluetoothCentralManagerCallback : BluetoothCentralManagerCallback = object : BluetoothCentralManagerCallback() {
        override fun onConnectedPeripheral(peripheral: BluetoothPeripheral) {
            logPeripheral("onConnectedPeripheral", peripheral, "Current MTU ${peripheral.currentMtu}")
            peripheral.requestMtu(MTU)
            Log.d(TAG, "Requested $MTU MTU")
            super.onConnectedPeripheral(peripheral)
        }
        override fun onDisconnectedPeripheral(peripheral: BluetoothPeripheral, status: HciStatus) {
            logPeripheral("onDisconnectedPeripheral", peripheral)
            super.onDisconnectedPeripheral(peripheral, status)
        }

        override fun onDiscoveredPeripheral(peripheral: BluetoothPeripheral, scanResult: ScanResult) {
            logPeripheral("onDiscoveredPeripheral", peripheral)
            bluetoothCentralManager.stopScan()
            bluetoothCentralManager.connectPeripheral(peripheral, bluetoothPeripheralCallback)
            super.onDiscoveredPeripheral(peripheral, scanResult)
        }
    }

    val bluetoothCentralManager = BluetoothCentralManager(context, bluetoothCentralManagerCallback, handler)

    fun scanForGabPeripheral() {
        Log.d(TAG, "scanForGabPeripheral started")
        bluetoothCentralManager.scanForPeripheralsWithServices(arrayOf(BlessedPeripheral.SERVICE_UUID))
    }

    companion object {
        private const val TAG = "BluetoothTransfer::ListenerDevice"
        const val MTU = BlessedPeripheral.DATA_SIZE + 3
        fun logPeripheral(method: String, peripheral: BluetoothPeripheral, additional: String = "") {
            Log.d(TAG, "$method ${peripheral.name}::${peripheral.address} $additional")
        }
    }
}
