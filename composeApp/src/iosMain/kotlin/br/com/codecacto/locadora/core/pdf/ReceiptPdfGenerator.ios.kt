package br.com.codecacto.locadora.core.pdf

actual class ReceiptPdfGenerator {

    actual suspend fun generateReceipt(data: ReceiptData): String {
        // TODO: Implement iOS PDF generation
        throw UnsupportedOperationException("PDF generation not yet supported on iOS")
    }

    actual suspend fun generateRecebimentoReceipt(data: RecebimentoReceiptData): String {
        // TODO: Implement iOS PDF generation
        throw UnsupportedOperationException("PDF generation not yet supported on iOS")
    }

    actual fun shareReceipt(filePath: String) {
        // TODO: Implement iOS sharing
        throw UnsupportedOperationException("PDF sharing not yet supported on iOS")
    }
}
