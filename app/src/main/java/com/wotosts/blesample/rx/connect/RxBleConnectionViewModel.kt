package com.wotosts.blesample.rx.connect

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jakewharton.rx.ReplayingShare
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleConnection.RxBleConnectionState
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.RxBleDeviceServices
import com.wotosts.blesample.SingleLiveEvent
import com.wotosts.blesample.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit

class RxBleConnectionViewModel : ViewModel() {

    private var bleDevice: RxBleDevice? = null
    private var connection: Observable<RxBleConnection>? = null

    private val disconnectTriggerSubject = PublishSubject.create<Boolean>()
    private var stateDisposable: Disposable? = null

    private val connectedCharacteristic: MutableMap<UUID, BluetoothGattCharacteristic> = hashMapOf()
    private val notiDisposable: MutableMap<UUID, Disposable> = hashMapOf()

    private val compositeDisposable = CompositeDisposable()
    val foundServices = MutableLiveData<RxBleDeviceServices?>()
    var log = MutableLiveData<String>()

    val connectionErrorEvent = SingleLiveEvent<Unit>()

    fun setBleDevice(bleDevice: RxBleDevice) {
        this.bleDevice = bleDevice
        stateDisposable = bleDevice.observeConnectionStateChanges()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{ rxBleConnectionState: RxBleConnectionState ->
                onConnectionStateChange(
                    rxBleConnectionState
                )
            }
        disconnectTriggerSubject.onNext(false)

        // keep connection
        connection = bleDevice.establishConnection(false)
            .takeUntil(disconnectTriggerSubject)
            .doOnError{ throwable: Throwable ->
                onConnectionError(
                    throwable
                )
            }
            .compose(ReplayingShare.instance<RxBleConnection>())

        discoverServices()
    }

    fun getBleDevice(): RxBleDevice? = bleDevice

    private fun discoverServices() {
        val disposable: Disposable = connection!!
            .flatMapSingle{ obj: RxBleConnection -> obj.discoverServices() }
            .retryWhen{ throwableObservable: Observable<Throwable?> ->
                    throwableObservable.flatMap {
                        Observable.timer(
                            150,
                            TimeUnit.MILLISECONDS
                        )
                    }
                }
            //.take(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { rxBleDeviceServices: RxBleDeviceServices ->
                    if (foundServices.value != null) return@subscribe

                    if (rxBleDeviceServices.bluetoothGattServices.size > 0) {
                        foundServices.value = rxBleDeviceServices
                        for (service in foundServices.value!!.bluetoothGattServices) {
                            for (characteristic in service.characteristics) {
                                connectedCharacteristic[characteristic.uuid] = characteristic
                            }
                        }
                        log("${bleDevice!!.macAddress} connected.")
                    }
                }
                ,
                {
                    log("DiscoveryService Failed")
                }
            )
        compositeDisposable.add(disposable)
    }

    fun connectCharacteristic(uuid: UUID) {
        if (connectedCharacteristic[uuid] != null) return

        val disposable: Disposable =
            connection!!
                .flatMapSingle{ connection1: RxBleConnection ->
                        connection1.requestConnectionPriority(
                            BluetoothGatt.CONNECTION_PRIORITY_HIGH,
                            10,
                            TimeUnit.MILLISECONDS
                        )
                        connection1.discoverServices()
                    }
                .retryWhen{ throwableObservable: Observable<Throwable?> ->
                        throwableObservable.flatMap {
                            Observable.timer(
                                150,
                                TimeUnit.MILLISECONDS
                            )
                        }
                    }
                .flatMapSingle{ rxBleDeviceServices: RxBleDeviceServices ->
                        rxBleDeviceServices.getCharacteristic(uuid)
                    }
                .retryWhen{ throwableObservable: Observable<Throwable?> ->
                        throwableObservable.flatMap {
                            Observable.timer(
                                150,
                                TimeUnit.MILLISECONDS
                            )
                        }
                    }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { characteristic: BluetoothGattCharacteristic ->
                        connectedCharacteristic[uuid] = characteristic
                        log("Connected $uuid")
                    },
                    {
                        log(
                            "Connect characteristic Failed"
                        )
                    }
                )
        compositeDisposable.add(disposable)
    }

    fun toggleNotification(uuid: UUID) {
        if (connectedCharacteristic[uuid] == null || !isConnected()) return

        if (notiDisposable[uuid] == null) {
            val disposable: Disposable = connection!!
                .flatMap{ connection1: RxBleConnection ->
                        connection1.setupNotification(
                            uuid
                        )
                    }
                .doOnNext{
                    log(
                        "$uuid/ Notification) Set up\n"
                    )
                }
                .flatMap{ observable: Observable<ByteArray>? -> observable }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ bytes: ByteArray ->
                        onNotificationReceived(
                            uuid,
                            bytes
                        )
                    },
                    {
                        log("$uuid/ Notification) Error\n")
                        notiDisposable[uuid]!!.dispose()
                        notiDisposable.remove(uuid)
                    }
                )
            notiDisposable[uuid] = disposable
            compositeDisposable.add(disposable)
        } else {
            log("$uuid/ Notification) end\n")
            notiDisposable[uuid]!!.dispose()
            notiDisposable.remove(uuid)
        }
    }

    fun readCharacteristic(uuid: UUID) {
        if (connectedCharacteristic[uuid] != null && isConnected()) {
            val disposable: Disposable = connection!!
                .firstOrError()
                .flatMap{ connection1: RxBleConnection ->
                        connection1.readCharacteristic(
                            uuid
                        )
                    }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { bytes: ByteArray ->
                        log(
                            uuid.toString() + "/ Read) " + Utils.bytesToHex(bytes)
                        )
                    }
                    ,
                    { throwable: Throwable? ->
                        if (throwable is NullPointerException) log("$uuid/ Read) String is null")
                    }
                )
            compositeDisposable.add(disposable)
        }
    }

    fun writeCharacteristic(uuid: UUID, msg: String?) {
        if(TextUtils.isEmpty(msg)) return

        if (connectedCharacteristic[uuid] != null && isConnected()) {
            val disposable: Disposable = connection!!
                .firstOrError()
                .flatMap{ connection1: RxBleConnection ->
                        connection1.writeCharacteristic(
                            uuid,
                            Utils.hexToBytes(msg!!)
                        )
                    }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { bytes: ByteArray ->
                        log(
                            uuid.toString() + "/ Write) " + Utils.bytesToHex(bytes)
                        )
                    },
                    {
                        log(
                            "${uuid.toString()} / Write Failure"
                        )
                    }
                )
            compositeDisposable.add(disposable)
        }
    }

    private fun onNotificationReceived(
        uuid: UUID,
        bytes: ByteArray
    ) {
        log(uuid.toString() + "/ Notification) " + Utils.bytesToHex(bytes))
    }

    private fun isConnected(): Boolean {
        return bleDevice!!.connectionState == RxBleConnectionState.CONNECTED
    }

    fun disconnectCharacteristic(uuid: UUID) {
        notiDisposable[uuid]!!.dispose()
        notiDisposable.remove(uuid)

        connectedCharacteristic.remove(uuid)
    }

    fun disconnectDevice() {
        disconnectTriggerSubject.onNext(true)
        stateDisposable?.dispose()
        compositeDisposable.clear()

        connectedCharacteristic.clear()
        notiDisposable.values.stream()
            .forEach{ disposable: Disposable -> disposable.dispose() }
        notiDisposable.clear()
        connection = null
    }

    private fun onConnectionStateChange(newState: RxBleConnectionState) {
        log(newState.toString())
    }

    private fun onConnectionError(throwable: Throwable) {
        log("connection error")

        disconnectDevice()
        connectionErrorEvent.postCall()
    }

    private fun log(msg: String) {
        log.postValue((if (TextUtils.isEmpty(log.value)) "" else log.value) + "\n" + msg)
    }

    override fun onCleared() {
        super.onCleared()
        disconnectDevice()
    }
}