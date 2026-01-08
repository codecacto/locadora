package br.com.codecacto.locadora.core.pdf

expect class ReceiptPdfGenerator {
    suspend fun generateReceipt(data: ReceiptData): String
    fun shareReceipt(filePath: String)
}
