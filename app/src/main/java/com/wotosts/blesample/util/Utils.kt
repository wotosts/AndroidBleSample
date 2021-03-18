package com.wotosts.blesample.util

class Utils {
    companion object {
        val KEY_DEVICE = "device"
        val KEY_MAC = "mac"


        private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

        fun bytesToHex(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v: Int = (bytes[j].toInt() and 0xFF)
                hexChars[j * 2] = HEX_ARRAY.get(v ushr 4)
                hexChars[j * 2 + 1] = HEX_ARRAY.get(v and 0x0F)
            }
            return String(hexChars)
        }

        fun hexToBytes(hexRepresentation: String): ByteArray {
            require(hexRepresentation.length % 2 != 1) { "hexToBytes requires an even-length String parameter" }
            val len = hexRepresentation.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(hexRepresentation[i], 16) shl 4)
                        + Character.digit(
                    hexRepresentation[i + 1],
                    16
                )).toByte()
                i += 2
            }
            return data
        }
    }
}