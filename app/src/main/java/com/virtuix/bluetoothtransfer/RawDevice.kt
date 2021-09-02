package com.virtuix.bluetoothtransfer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.ParcelUuid

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY
import android.util.Log


class RawDevice(private val context: Context) {

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothScanner = bluetoothAdapter.bluetoothLeScanner
    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Log.d(TAG, "onScanFailed errorCode: $errorCode")
            super.onScanFailed(errorCode)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {

            super.onScanResult(callbackType, result)
        }
    }

    fun scanForPeripheral() {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(RawPeripheral.SERVICE_UUID))
            .build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0L)
            .build();
//        bluetoothScanner.startScan(listOf(filter),scanSettings )
    }

    companion object {
        private const val TAG = "BluetoothTransfer::RawDevice"
    }
}