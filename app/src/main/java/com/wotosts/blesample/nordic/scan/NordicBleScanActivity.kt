package com.wotosts.blesample.nordic.scan

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.wotosts.blesample.BaseActivity
import com.wotosts.blesample.R
import com.wotosts.blesample.databinding.ActivityNordicBleScanBinding
import com.wotosts.blesample.nordic.connect.NordicBleConnectionActivity
import com.wotosts.blesample.util.Utils.Companion.KEY_DEVICE
import no.nordicsemi.android.support.v18.scanner.ScanResult

class NordicBleScanActivity : BaseActivity() {

    lateinit var binding: ActivityNordicBleScanBinding
    val viewModel: NordicBleScanViewModel by viewModels()
    lateinit var scanResultAdapter: ScanResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nordic_ble_scan)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        scanResultAdapter =
            ScanResultAdapter(mutableListOf(), object : ScanResultAdapter.ItemClickListener {
                override fun onClicked(result: ScanResult) {
                    connect(result)
                }
            })
        binding.rvResult.adapter = scanResultAdapter

        viewModel.scanResultMap.observe(this, Observer { scanResultAdapter.updateList(it.values.toList()) })
    }

    override fun onStart() {
        super.onStart()
        if (BluetoothAdapter.getDefaultAdapter().isEnabled)
            viewModel.startScan()
        else {
            Toast.makeText(this, "블루투스를 켜주세요.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onPause() {
        viewModel.stopScan()
        super.onPause()
    }

    fun connect(result: ScanResult) {
        val connectIntent = Intent(this, NordicBleConnectionActivity::class.java)
        connectIntent.putExtra(KEY_DEVICE, result.device)
        startActivity(connectIntent)
    }
}
