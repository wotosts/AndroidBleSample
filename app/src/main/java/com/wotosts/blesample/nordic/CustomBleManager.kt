package com.wotosts.blesample.nordic

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.ble.data.Data
import java.util.*

/** Sample for specific device
 *  nordic-ble 사용
 *  연결을 원하고자 하는 디바이스의 프로토콜을 알고 있는 경우에 UUID를 고정하여 사용 가능
 * */
class CustomBleManager(context: Context) : BleManager<BleManagerCallbacks>(context) {

    companion object {
        val UUID_SERVICE_DATA = UUID.fromString("ab705010-0a3a-11e8-ba89-0ed5f89f718b")
        val UUID_SERVICE_CONTROL = UUID.fromString("ab705110-0a3a-11e8-ba89-0ed5f89f718b")
        val UUID_CHARACTERISTIC_DATA = UUID.fromString("ab705012-0a3a-11e8-ba89-0ed5f89f718b")
        val UUID_CHARACTERISTIC_LED = UUID.fromString("ab705113-0a3a-11e8-ba89-0ed5f89f718b")
    }

    var dataCharacteristic: BluetoothGattCharacteristic? = null
    var ledCharacteristic: BluetoothGattCharacteristic? = null

    override fun getGattCallback(): BleManagerGattCallback = callback

    private val callback = object : BleManagerGattCallback() {
        override fun initialize() {
            setNotificationCallback(dataCharacteristic).with { device, data ->  Log.d("Noti", data.toString())}
            enableNotifications(dataCharacteristic).enqueue()
        }

        override fun onDeviceDisconnected() {
            dataCharacteristic = null
            ledCharacteristic = null
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val dataServices = gatt.getService(UUID_SERVICE_DATA)
            val controlServices = gatt.getService(UUID_SERVICE_CONTROL)

            dataCharacteristic = dataServices?.getCharacteristic(
                UUID_CHARACTERISTIC_DATA
            )
            ledCharacteristic = controlServices?.getCharacteristic(
                UUID_CHARACTERISTIC_LED
            )

            var notiRequest = false
            var writeRequest = false
            if(dataCharacteristic != null) {
                notiRequest =
                    ((dataCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
            }
            if(ledCharacteristic != null) {
                writeRequest =
                    ((ledCharacteristic!!.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
            }

            return notiRequest && writeRequest
        }
    }

    fun send(mode: Int) {
        when(mode) {
            0 -> writeCharacteristic(ledCharacteristic, Data.opCode(0x000000.toByte())).enqueue()
            1 -> writeCharacteristic(ledCharacteristic, Data.opCode(0x000101.toByte())).enqueue()
        }
    }
}