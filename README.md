# bleTransfer

Start application on a phone and select Start Peripheral

Start application on a second phone attached to logcat and select start device

After some time you will see a message like this in logcat

BluetoothTransfer::ListenerDevice: Time: 11479

The time is in MS

You can change the size and currently number of packets sent by changing

const val DATA_SIZE = 128

In BlessedPeripheral companion object.