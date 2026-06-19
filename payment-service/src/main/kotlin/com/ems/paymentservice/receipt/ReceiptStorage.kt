package com.ems.paymentservice.receipt

data class StoredReceipt(
    val objectKey: String,
    val url: String,
)

interface ReceiptStorage {
    fun store(objectKey: String, content: ByteArray): StoredReceipt
}
