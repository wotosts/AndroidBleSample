package com.wotosts.blesample.nordic.connect

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.wotosts.blesample.util.Utils
import no.nordicsemi.android.log.Logger
import java.util.*

class NordicBleConnectionViewModel(application: Application) : AndroidViewModel(application) {

    val foundService = MutableLiveData<List<BluetoothGattService>>()
    val connectionState = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val mac = MutableLiveData<String>()
    val log = MutableLiveData<String>()

    private val bleManager: BaseNordicBleManager = BaseNordicBleManager(application)
    private val bleManagerCallbacks = object : BaseNordicBleManager.ConnectedLECallback {
        override fun updateServices(serviceList: MutableList<BluetoothGattService>) {
            foundService.value = serviceList
        }

        override fun onRead(uuid: UUID, bytes: ByteArray?, msg: String?) {
            val readData =
                if (bytes != null) Utils.bytesToHex(bytes) else if (!TextUtils.isEmpty(msg)) msg else "data is null"
            log("${uuid.toString()}/ Read) $readData")
        }

        override fun onWrite(uuid: UUID, isSuccess: Boolean) {
            log("${uuid.toString()}/ Write) " + (if (isSuccess) "success" else "failed"))
        }

        override fun onNotified(uuid: UUID, bytes: ByteArray?, msg: String?) {
            val notiData =
                if (bytes != null) Utils.bytesToHex(bytes) else if (!TextUtils.isEmpty(msg)) msg else "data is null"
            log("${uuid.toString()}/ notify) $notiData")
        }

        override fun onDeviceDisconnecting(device: BluetoothDevice) {
            connectionState.postValue("Disconnecting")
        }

        override fun onDeviceDisconnected(device: BluetoothDevice) {
            connectionState.postValue("Disconnected")
            log("${device.address} disconnected.")

            reconnect()
        }

        override fun onDeviceConnected(device: BluetoothDevice) {
            connectionState.postValue("Connected")
            log("${device.address} connected.")
        }

        override fun onDeviceNotSupported(device: BluetoothDevice) {
            // nothing to do
        }

        override fun onBondingFailed(device: BluetoothDevice) {
            // nothing to do
        }

        override fun onServicesDiscovered(device: BluetoothDevice, optionalServicesFound: Boolean) {
            log("service discovered")
        }

        override fun onBondingRequired(device: BluetoothDevice) {
            // nothing to do
        }

        override fun onLinkLossOccurred(device: BluetoothDevice) {
            // nothing to do
        }

        override fun onBonded(device: BluetoothDevice) {
            // nothing to do
        }

        override fun onDeviceReady(device: BluetoothDevice) {
            // nothing to do
        }

        override fun onError(device: BluetoothDevice, message: String, errorCode: Int) {
            log(message)
        }

        override fun onDeviceConnecting(device: BluetoothDevice) {
            connectionState.postValue("Connecting")
        }
    }

    var bleDevice: BluetoothDevice? = null

    init {
        bleManager.setGattCallbacks(bleManagerCallbacks)
    }

    override fun onCleared() {
        super.onCleared()

        if (bleManager.isConnected) {
            bleManager.disconnect().enqueue()
            bleDevice = null
        }
    }

    fun connect(device: BluetoothDevice) {
        bleDevice = device
        bleManager.logSession =
            Logger.newSession(getApplication(), null, device.address, device.name)
        name.value = device.name
        mac.value = device.address

        reconnect()
    }

    fun reconnect() {
        if (bleDevice == null)
            return

        bleManager.connect(bleDevice!!)
            .retry(10, 200)
            .useAutoConnect(false)
            .enqueue()
    }

    fun writeData(uuid: UUID, data: String?) {
        if (data == null) {
            log("data must be not null")
            return
        }

        //if (bleManager.isConnected)
        bleManager.writeCharacteristic(uuid, data)
    }

    fun readData(uuid: UUID) {
        //if (bleManager.isConnected)
        bleManager.readCharacteristic(uuid)
    }

    fun toggleNotification(isChecked: Boolean, uuid: UUID) {
//        if (!bleManager.isConnected)
//            return

        bleManager.notifyCharacteristic(isChecked, uuid)
    }

    private fun log(msg: String) {
        log.postValue((if (TextUtils.isEmpty(log.value)) "" else log.value) + "\n" + msg)
    }
}