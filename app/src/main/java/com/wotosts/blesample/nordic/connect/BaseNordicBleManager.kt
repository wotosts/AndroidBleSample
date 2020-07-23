package com.wotosts.blesample.nordic.connect

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import com.wotosts.blesample.util.Utils
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BleManagerCallbacks
import no.nordicsemi.android.log.LogContract
import no.nordicsemi.android.log.LogSession
import no.nordicsemi.android.log.Logger
import java.util.*
import kotlin.collections.HashMap

open class BaseNordicBleManager(context: Context) :
    BleManager<BaseNordicBleManager.ConnectedLECallback>(context) {

    var serviceList: MutableList<BluetoothGattService> = mutableListOf()
    var characteristicMap: HashMap<UUID, BluetoothGattCharacteristic> = hashMapOf()

    var logSession: LogSession? = null

    protected var callback: BleManagerGattCallback = object : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()
        }

        override fun onDeviceDisconnected() {
        }

        /**
         * BleManagerCallback - onServiceDiscovered 보다 먼저 호출됨
         */
        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            serviceList.clear()
            characteristicMap.clear()

            serviceList = gatt.services

            serviceList.forEach { bluetoothGattService ->
                bluetoothGattService.characteristics.forEach { characteristic ->
                    run {
                        characteristicMap.put(
                            characteristic.uuid,
                            characteristic
                        )
                    }
                }
            }

            mCallbacks.updateServices(serviceList)

            return true
        }
    }

    fun notifyCharacteristic(isChecked: Boolean, uuid: UUID) {
        val characteristic = characteristicMap.get(uuid)

        setNotificationCallback(characteristic).with { device, data ->
            mCallbacks.onNotified(
                uuid,
                data.value
            )
        }
        if (isChecked) {
            enableNotifications(characteristic)
                .done { device ->  mCallbacks.onNotified(uuid, Utils.hexToBytes("set up"))}
                .fail { device, status ->  mCallbacks.onNotified(uuid,  Utils.hexToBytes("failed"))}
                .enqueue()
        } else {
            disableNotifications(characteristic)
                .done { device ->  mCallbacks.onNotified(uuid,  Utils.hexToBytes("end"))}
                .fail { device, status ->  mCallbacks.onNotified(uuid,  Utils.hexToBytes("failed"))}
                .enqueue()
        }
    }

    fun readCharacteristic(uuid: UUID) {
        readCharacteristic(characteristicMap.get(uuid))
            .fail { device, status -> mCallbacks.onRead(uuid, null) }
            .with { device, data -> mCallbacks.onRead(uuid, data.value) }
            .enqueue()
    }

    fun writeCharacteristic(uuid: UUID, data: String) {
        val characteristic = characteristicMap.get(uuid)

        writeCharacteristic(characteristic, data.toByteArray())
            .done { device -> mCallbacks.onWrite(uuid, true) }
            .fail { device, status -> mCallbacks.onWrite(uuid, false) }
            .enqueue()
    }

    override fun log(priority: Int, message: String) {
        Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message)
    }

    override fun getGattCallback(): BleManagerGattCallback = callback

    interface ConnectedLECallback : BleManagerCallbacks {
        fun updateServices(serviceList: MutableList<BluetoothGattService>)
        fun onRead(uuid: UUID, bytes: ByteArray?)
        fun onWrite(uuid: UUID, isSuccess: Boolean)
        fun onNotified(uuid: UUID, bytes: ByteArray?)
    }
}