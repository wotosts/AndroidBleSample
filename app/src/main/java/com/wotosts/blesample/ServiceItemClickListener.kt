package com.wotosts.blesample

import com.wotosts.blesample.model.ServiceItem
import java.util.*

interface ServiceItemClickListener {
    fun onItemClicked(item: ServiceItem)
    fun onConnectClicked(
        checked: Boolean,
        uuid: UUID
    )

    fun onReadBtnClicked(uuid: UUID)
    fun onWriteBtnClicked(
        uuid: UUID,
        msg: String?
    )

    fun onNotiBtnClicked(isChecked: Boolean, uuid: UUID)
}