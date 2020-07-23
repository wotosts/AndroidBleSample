package com.wotosts.blesample.rx.connect

import android.os.Bundle
import android.widget.ScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.wotosts.blesample.BaseActivity
import com.wotosts.blesample.BleApplication
import com.wotosts.blesample.R
import com.wotosts.blesample.ServiceItemClickListener
import com.wotosts.blesample.databinding.ActivityRxBleConnectionBinding
import com.wotosts.blesample.model.ServiceItem
import com.wotosts.blesample.util.Utils
import java.util.*

class RxBleConnectionActivity : BaseActivity() {

    lateinit var binding: ActivityRxBleConnectionBinding
    lateinit var viewModel: RxBleConnectionViewModel

    lateinit var serviceAdapter: DiscoveredServiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rx_ble_connection)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this).get(RxBleConnectionViewModel::class.java)
        val mac = intent.getStringExtra(Utils.KEY_MAC)
        if (mac == null) {
            finish()
        }
        connect(mac!!)
        binding.viewModel = viewModel

        serviceAdapter = DiscoveredServiceAdapter(object : ServiceItemClickListener {
            override fun onItemClicked(item: ServiceItem) {
                // nothing to do
            }

            override fun onConnectClicked(checked: Boolean, uuid: UUID) {
                // 사용하지 않아도 됨
                if(checked)
                    viewModel.connectCharacteristic(uuid)
                else
                    viewModel.disconnectCharacteristic(uuid)
            }

            override fun onReadBtnClicked(uuid: UUID) {
                viewModel.readCharacteristic(uuid)
            }

            override fun onWriteBtnClicked(uuid: UUID, msg: String?) {
                viewModel.writeCharacteristic(uuid, msg)
            }

            override fun onNotiBtnClicked(isChecked: Boolean, uuid: UUID) {
                viewModel.toggleNotification(uuid)
            }
        })
        binding.rvService.adapter = serviceAdapter

        viewModel.foundServices.observe(this, Observer { services ->  if(services != null) serviceAdapter.updateScanResult(services) })
        viewModel.connectionErrorEvent.observe(this, Observer { event -> connect(mac!!) })
        viewModel.log.observe(this, Observer { str ->
            binding.scroll.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        })
    }

    override fun onDestroy() {
        viewModel.disconnectDevice()
        super.onDestroy()
    }

    fun connect(mac: String) {
        viewModel.setBleDevice(BleApplication.getBleClient(this).getBleDevice(mac))
    }
}
