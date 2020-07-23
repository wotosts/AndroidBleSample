package com.wotosts.blesample.model

import java.util.*

data class ServiceItem constructor(
    val type: Int,
    val description: String,
    val uuid: UUID
) {
    companion object {
        const val SERVICE = 1
        const val CHARACTERISTIC = 2
    }
}