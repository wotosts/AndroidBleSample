package com.wotosts.blesample.nordic.scan

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.wotosts.blesample.BaseActivity
import com.wotosts.blesample.R
import com.wotosts.blesample.databinding.ActivityNordicBleScanBinding
import com.wotosts.blesample.nordic.connect.NordicBleConnectionActivity
import com.wotosts.blesample.util.Utils.Companion.KEY_DEVICE
import no.nordicsemi.android.support.v18.scanner.ScanResult

class NordicBleScanActivity : BaseActivity() {

    lateinit var binding: ActivityNordicBleScanBinding
    lateinit var viewModel: NordicBleScanViewModel
    lateinit var scanResultAdapter: ScanResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_nordic_ble_scan)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this)
            .get(NordicBleScanViewModel::class.java)
        binding.viewModel = viewModel

        scanResultAdapter =
            ScanResultAdapter(mutableListOf(), object : ScanResultAdapter.ItemClickListener {
                override fun onClicked(result: ScanResult) {
                    connect(result)
                }
            })
        binding.rvResult.adapter = scanResultAdapter

        viewModel.scanResults.observe(this, Observer { results -> scanResultAdapter.updateList(results) })
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