package com.wotosts.blesample.nordic.scan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import no.nordicsemi.android.support.v18.scanner.*

class NordicBleScanViewModel: ViewModel {
    val isScanning = MutableLiveData<Boolean>()
    val scanResults = MutableLiveData<List<ScanResult>>()

    private var scanJob: Job? = null

    constructor() {
        isScanning.value = false
    }

    /**
     * 적절한 scan 시간 정하기
     */
    fun startScan() {
        if (isScanning.value!!)
            return

        val setting = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(500)
            .setUseHardwareBatchingIfSupported(false)
            .build()
        val filter = ScanFilter.Builder()
            .build()
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.startScan(mutableListOf(filter), setting, scanCallback)
        isScanning.value = true

        scanJob = viewModelScope.launch {
            delay(3000L)       // 3 seconds
            stopScan()
        }
    }

    fun stopScan() {
        isScanning.value = false
        BluetoothLeScannerCompat.getScanner().stopScan(scanCallback)

        scanJob?.cancel()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            stopScan()
        }

        /**
         * 리스트 아이템은 스스로 정리해서 사용하기
         */
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val list = scanResults.value as MutableList<ScanResult>
            list.add(result)

            scanResults.value = list
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            if (results.isEmpty())
                return

            scanResults.value = results
        }
    }
}