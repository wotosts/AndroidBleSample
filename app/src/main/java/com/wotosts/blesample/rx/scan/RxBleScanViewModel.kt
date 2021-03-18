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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RxBleScanViewModel(application: Application) : AndroidViewModel(application) {
    var bleClient: RxBleClient = BleApplication.getBleClient(application)

    val isScanning = MutableLiveData<Boolean>()
    val scanResultMap = MutableLiveData<MutableMap<String, ScanResult>>().apply { value = mutableMapOf() }

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
        scanResultMap.value!![scanResult.bleDevice.macAddress] = scanResult
        scanResultMap.value = scanResultMap.value
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}