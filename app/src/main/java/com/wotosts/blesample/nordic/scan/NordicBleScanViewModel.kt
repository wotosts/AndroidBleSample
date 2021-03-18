package com.wotosts.blesample.nordic.scan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import no.nordicsemi.android.support.v18.scanner.*

class NordicBleScanViewModel: ViewModel() {
    val isScanning = MutableLiveData<Boolean>(false)
    val scanResultMap = MutableLiveData<MutableMap<String, ScanResult>>().apply { value = mutableMapOf() }

    private var scanJob: Job? = null

    /**
     * 적절한 scan 시간 정하기
     */
    fun startScan() {
        if (isScanning.value!!)
            return

        val setting = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(500)
            .setUseHardwareBatchingIfSupported(true)
            .build()
        val filter = ScanFilter.Builder()
            .build()
        val scanner = BluetoothLeScannerCompat.getScanner()
        scanner.startScan(mutableListOf(filter), setting, scanCallback)
        isScanning.value = true

        scanJob = viewModelScope.launch {
            delay(5000L)       // 3 seconds
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
            // not used
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            if (results.isEmpty())
                return

            val map = scanResultMap.value!!
            for(result in results) {
                map[result.device.address] = result
            }
            scanResultMap.value = map
        }
    }
}