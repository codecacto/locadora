package br.com.codecacto.locadora.core.pdf

expect class ReceiptPdfGenerator {
    suspend fun generateReceipt(data: ReceiptData): String
    suspend fun generateRecebimentoReceipt(data: RecebimentoReceiptData): String
    fun shareReceipt(filePath: String)
}
