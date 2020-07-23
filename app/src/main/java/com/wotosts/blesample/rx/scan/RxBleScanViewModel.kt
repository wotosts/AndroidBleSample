package com.wotosts.blesample.rx.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import com.wotosts.blesample.BleApplication
import com.wotosts.blesample.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RxBleScanViewModel(application: Application) : AndroidViewModel(application) {
    var bleClient: RxBleClient = BleApplication.getBleClient(application)

    val isScanning = MutableLiveData<Boolean>()
    val resultUpdateEvent = SingleLiveEvent<Void>()
    val scanResults = mutableListOf<ScanResult>()

    var scanDisposable: Disposable? = null
    var scanJob: Job? = null

    fun startScan() {
        val scanFilter = ScanFilter.Builder().build()
        val scanSetting =
            ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build()

        scanDisposable = bleClient.scanBleDevices(scanSetting, scanFilter)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { rxBleScanResult: ScanResult ->
                    addScanResult(rxBleScanResult)
                },
                { throwable: Throwable? ->
                    stopScan()
                }
            )

        isScanning.value = true

        scanJob = viewModelScope.launch {
            delay(3000L)       // 3 seconds
            stopScan()
        }
    }

    fun stopScan() {
        if (scanDisposable != null && !scanDisposable!!.isDisposed)
            scanDisposable!!.dispose()
        scanDisposable = null
        isScanning.value = false

        scanJob?.cancel()
    }

    fun addScanResult(scanResult: ScanResult) {
        for(i in scanResults.indices) {
            val it = scanResults[i]
            if (it.bleDevice.macAddress.equals(scanResult.bleDevice.macAddress)) {
                scanResults[i] = scanResult
                return
            }
        }

        scanResults.add(scanResult)
        resultUpdateEvent.call()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}