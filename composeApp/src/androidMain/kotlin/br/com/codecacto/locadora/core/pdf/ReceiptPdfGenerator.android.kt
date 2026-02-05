package br.com.codecacto.locadora.core.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import br.com.codecacto.locadora.LocadoraApplication
import br.com.codecacto.locadora.core.model.StatusPagamento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import java.io.FileOutputStream

actual class ReceiptPdfGenerator {
    private val context: Context = LocadoraApplication.instance

    actual suspend fun generateReceipt(data: ReceiptData): String = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawReceipt(canvas, data)

        pdfDocument.finishPage(page)

        // Save to cache
        val receiptsDir = File(context.cacheDir, "receipts")
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }

        val fileName = "recibo_${data.locacao.id.take(8)}_${System.currentTimeMillis()}.pdf"
        val file = File(receiptsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()

        file.absolutePath
    }

    private fun drawReceipt(canvas: Canvas, data: ReceiptData) {
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            isFakeBoldText = true
        }

        val normalPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val pageWidth = 595f
        val margin = 40f
        var yPosition = 60f
        val lineHeight = 20f

        // Header - Company Name
        if (data.dadosEmpresa.nomeEmpresa.isNotBlank()) {
            canvas.drawText(data.dadosEmpresa.nomeEmpresa, pageWidth / 2, yPosition, titlePaint)
            yPosition += lineHeight * 1.5f

            // Company Info
            val smallPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }

            val companyInfo = buildString {
                if (data.dadosEmpresa.endereco.isNotBlank()) append(data.dadosEmpresa.endereco)
                if (data.dadosEmpresa.telefone.isNotBlank()) {
                    if (isNotBlank()) append(" | ")
                    append("Tel: ${formatPhone(data.dadosEmpresa.telefone)}")
                }
            }
            if (companyInfo.isNotBlank()) {
                canvas.drawText(companyInfo, pageWidth / 2, yPosition, smallPaint)
                yPosition += lineHeight
            }

            val companyInfo2 = buildString {
                if (data.dadosEmpresa.documento.isNotBlank()) {
                    val formattedDoc = if (data.dadosEmpresa.tipoPessoa == "FISICA") {
                        "CPF: ${formatCpf(data.dadosEmpresa.documento)}"
                    } else {
                        "CNPJ: ${formatCnpj(data.dadosEmpresa.documento)}"
                    }
                    append(formattedDoc)
                }
                if (data.dadosEmpresa.email.isNotBlank()) {
                    if (isNotBlank()) append(" | ")
                    append(data.dadosEmpresa.email)
                }
            }
            if (companyInfo2.isNotBlank()) {
                canvas.drawText(companyInfo2, pageWidth / 2, yPosition, smallPaint)
                yPosition += lineHeight
            }
        } else {
            canvas.drawText("Locacao de Equipamentos", pageWidth / 2, yPosition, titlePaint)
            yPosition += lineHeight * 1.5f
        }

        yPosition += 10f

        // Separator line
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += lineHeight * 1.5f

        // Receipt Title
        val receiptTitlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("RECIBO DE LOCACAO", pageWidth / 2, yPosition, receiptTitlePaint)
        yPosition += lineHeight

        // Date and Receipt Number
        val datePaint = Paint().apply {
            color = Color.GRAY
            textSize = 11f
            textAlign = Paint.Align.CENTER
        }
        val currentDate = formatDate(System.currentTimeMillis())
        canvas.drawText("Data: $currentDate    |    No: ${data.locacao.id.take(8).uppercase()}", pageWidth / 2, yPosition, datePaint)
        yPosition += lineHeight * 1.5f

        // Separator line
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += lineHeight * 1.5f

        // Client Section
        canvas.drawText("CLIENTE", margin, yPosition, headerPaint)
        yPosition += lineHeight

        canvas.drawText("Nome: ${data.cliente.nomeRazao}", margin, yPosition, normalPaint)
        yPosition += lineHeight

        if (!data.cliente.cpfCnpj.isNullOrBlank()) {
            val doc = if (data.cliente.cpfCnpj.length == 11) formatCpf(data.cliente.cpfCnpj) else formatCnpj(data.cliente.cpfCnpj)
            canvas.drawText("CPF/CNPJ: $doc", margin, yPosition, normalPaint)
            yPosition += lineHeight
        }

        canvas.drawText("Telefone: ${formatPhone(data.cliente.telefoneWhatsapp)}", margin, yPosition, normalPaint)
        yPosition += lineHeight * 1.5f

        // Separator line
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += lineHeight * 1.5f

        // Equipment Section
        val equipmentLabel = if (data.equipamentos.size > 1) "EQUIPAMENTOS" else "EQUIPAMENTO"
        canvas.drawText(equipmentLabel, margin, yPosition, headerPaint)
        yPosition += lineHeight

        data.equipamentos.forEachIndexed { index, equipamento ->
            if (data.equipamentos.size > 1) {
                canvas.drawText("${index + 1}. ${equipamento.nome}", margin, yPosition, normalPaint)
            } else {
                canvas.drawText(equipamento.nome, margin, yPosition, normalPaint)
            }
            yPosition += lineHeight

            canvas.drawText("   Categoria: ${equipamento.categoria}", margin, yPosition, normalPaint)
            yPosition += lineHeight

            if (!equipamento.identificacao.isNullOrBlank()) {
                canvas.drawText("   Identificacao: ${equipamento.identificacao}", margin, yPosition, normalPaint)
                yPosition += lineHeight
            }

            if (index < data.equipamentos.size - 1) {
                yPosition += lineHeight * 0.3f
            }
        }

        yPosition += lineHeight * 0.5f

        // Separator line
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += lineHeight * 1.5f

        // Rental Details Section
        canvas.drawText("DETALHES DA LOCACAO", margin, yPosition, headerPaint)
        yPosition += lineHeight

        canvas.drawText("Periodo: ${formatDate(data.locacao.dataInicio)} a ${formatDate(data.locacao.dataFimPrevista)}", margin, yPosition, normalPaint)
        yPosition += lineHeight

        val valorPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText("Valor: ${formatCurrency(data.locacao.valorLocacao)}", margin, yPosition, valorPaint)
        yPosition += lineHeight

        val statusPagamento = if (data.locacao.statusPagamento == StatusPagamento.PAGO) "Pago" else "Pendente"
        canvas.drawText("Status Pagamento: $statusPagamento", margin, yPosition, normalPaint)
        yPosition += lineHeight * 2f

        // Separator line
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, linePaint)
        yPosition += lineHeight * 3f

        // Signature Section
        canvas.drawText("Assinatura: ___________________________________________", margin, yPosition, normalPaint)
        yPosition += lineHeight * 4f

        // Footer
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Documento gerado em ${formatDate(System.currentTimeMillis())}", pageWidth / 2, yPosition, footerPaint)
    }

    actual suspend fun generateRecebimentoReceipt(data: RecebimentoReceiptData): String = withContext(Dispatchers.IO) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawRecebimentoReceipt(canvas, data)

        pdfDocument.finishPage(page)

        // Save to cache
        val receiptsDir = File(context.cacheDir, "receipts")
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }

        val fileName = "recibo_recebimento_${data.recebimento.id.take(8)}_${System.currentTimeMillis()}.pdf"
        val file = File(receiptsDir, fileName)

        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()

        file.absolutePath
    }

    private fun drawRecebimentoReceipt(canvas: Canvas, data: RecebimentoReceiptData) {
        val pageWidth = 595f
        val margin = 50f
        val contentWidth = pageWidth - (margin * 2)
        var yPosition = 80f
        val lineHeight = 20f

        // Title
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isUnderlineText = true
        }
        canvas.drawText("RECIBO DE LOCAÇÃO DE EQUIPAMENTO", pageWidth / 2, yPosition, titlePaint)
        yPosition += lineHeight * 4f

        // Body text paint
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
        }

        // Build the receipt text
        val clienteNome = data.cliente.nomeRazao
        val clienteDoc = data.cliente.cpfCnpj?.let { doc ->
            if (doc.length == 11) "CPF" else "CNPJ"
        } ?: "CNPJ"
        val clienteDocFormatted = data.cliente.cpfCnpj?.let { doc ->
            if (doc.length == 11) formatCpf(doc) else formatCnpj(doc)
        } ?: ""
        val clienteEndereco = data.cliente.endereco ?: ""
        val valor = formatCurrency(data.recebimento.valor)
        val valorExtenso = valorPorExtenso(data.recebimento.valor)
        val equipamentosNomes = data.equipamentos.joinToString(", ") { it.nome }

        // Build text parts
        val text1 = "Recebi da Empresa $clienteNome, portador do $clienteDoc nº"
        val text2 = "$clienteDocFormatted"
        val text3 = if (clienteEndereco.isNotBlank()) " localizada na $clienteEndereco" else ""
        val text4 = " o valor de $valor"
        val text5 = "($valorExtenso), como forma de pagamento referente a locação de"
        val text6 = "$equipamentosNomes."

        // Draw text with word wrap
        val fullText = "$text1 $text2$text3$text4 $text5 $text6"
        yPosition = drawWrappedText(canvas, fullText, margin, yPosition, contentWidth, bodyPaint)

        // Date
        yPosition += lineHeight * 3f
        val datePaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            textAlign = Paint.Align.RIGHT
            isFakeBoldText = true
        }
        val dataPagamento = data.recebimento.dataPagamento ?: System.currentTimeMillis()
        val dataFormatted = formatDateExtended(dataPagamento, data.dadosEmpresa.cidade, data.dadosEmpresa.estado)
        canvas.drawText("$dataFormatted.", pageWidth - margin, yPosition, datePaint)

        // Signature line
        yPosition += lineHeight * 8f
        val signaturePaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("_____________________________________", pageWidth / 2, yPosition, signaturePaint)
        yPosition += lineHeight * 1.5f

        // Company name
        val companyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(data.dadosEmpresa.nomeEmpresa, pageWidth / 2, yPosition, companyPaint)
        yPosition += lineHeight

        // Company document
        val docLabel = if (data.dadosEmpresa.tipoPessoa == "FISICA") "CPF" else "CNPJ"
        val docFormatted = if (data.dadosEmpresa.tipoPessoa == "FISICA") {
            formatCpf(data.dadosEmpresa.documento)
        } else {
            formatCnpj(data.dadosEmpresa.documento)
        }
        canvas.drawText("$docLabel: $docFormatted", pageWidth / 2, yPosition, companyPaint)
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, startY: Float, maxWidth: Float, paint: Paint): Float {
        var y = startY
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            val testWidth = paint.measureText(testLine)

            if (testWidth > maxWidth && currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, y, paint)
                y += 22f
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint)
            y += 22f
        }

        return y
    }

    private fun formatDateExtended(timestamp: Long, cidade: String, estado: String): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val meses = listOf(
            "janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
        )

        val localidade = if (cidade.isNotBlank() && estado.isNotBlank()) {
            "$cidade-$estado"
        } else if (cidade.isNotBlank()) {
            cidade
        } else {
            ""
        }
        val dia = localDateTime.dayOfMonth.toString().padStart(2, '0')
        val mes = meses[localDateTime.monthNumber - 1]
        val ano = localDateTime.year

        return if (localidade.isNotBlank()) {
            "$localidade, $dia de $mes de $ano"
        } else {
            "$dia de $mes de $ano"
        }
    }

    private fun valorPorExtenso(valor: Double): String {
        val intPart = valor.toLong()
        val decPart = ((valor - intPart) * 100).toInt()

        val unidades = listOf("", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove")
        val especiais = listOf("dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove")
        val dezenas = listOf("", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa")
        val centenas = listOf("", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos")

        fun extenso0a99(n: Int): String {
            return when {
                n == 0 -> ""
                n < 10 -> unidades[n]
                n < 20 -> especiais[n - 10]
                else -> {
                    val dezena = dezenas[n / 10]
                    val unidade = unidades[n % 10]
                    if (unidade.isEmpty()) dezena else "$dezena e $unidade"
                }
            }
        }

        fun extenso0a999(n: Int): String {
            return when {
                n == 0 -> ""
                n == 100 -> "cem"
                n < 100 -> extenso0a99(n)
                else -> {
                    val centena = centenas[n / 100]
                    val resto = extenso0a99(n % 100)
                    if (resto.isEmpty()) centena else "$centena e $resto"
                }
            }
        }

        fun extensoMilhares(n: Long): String {
            return when {
                n == 0L -> "zero"
                n < 1000 -> extenso0a999(n.toInt())
                n < 1000000 -> {
                    val milhares = (n / 1000).toInt()
                    val resto = (n % 1000).toInt()
                    val milharesTxt = if (milhares == 1) "mil" else "${extenso0a999(milhares)} mil"
                    val restoTxt = extenso0a999(resto)
                    when {
                        resto == 0 -> milharesTxt
                        resto < 100 -> "$milharesTxt e $restoTxt"
                        resto % 100 == 0 -> "$milharesTxt e $restoTxt"
                        else -> "$milharesTxt e $restoTxt"
                    }
                }
                else -> n.toString() // For very large numbers, just return the number
            }
        }

        val reaisTxt = extensoMilhares(intPart)
        val reaisLabel = if (intPart == 1L) "real" else "reais"

        return if (decPart > 0) {
            val centavosTxt = extenso0a99(decPart)
            val centavosLabel = if (decPart == 1) "centavo" else "centavos"
            "${reaisTxt.replaceFirstChar { it.uppercase() }} $reaisLabel e $centavosTxt $centavosLabel"
        } else {
            "${reaisTxt.replaceFirstChar { it.uppercase() }} $reaisLabel"
        }
    }

    actual fun shareReceipt(filePath: String) {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Compartilhar Recibo").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }

    private fun formatDate(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/" +
                "${localDateTime.monthNumber.toString().padStart(2, '0')}/" +
                "${localDateTime.year}"
    }

    private fun formatCurrency(value: Double): String {
        val intPart = value.toLong()
        val decPart = ((value - intPart) * 100).toInt()

        // Formatar parte inteira com separador de milhares
        val intPartFormatted = intPart.toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        return "R$ $intPartFormatted,${decPart.toString().padStart(2, '0')}"
    }

    private fun formatPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) return phone
        return if (digits.length == 11) {
            "(${digits.take(2)}) ${digits.drop(2).take(5)}-${digits.drop(7)}"
        } else {
            "(${digits.take(2)}) ${digits.drop(2).take(4)}-${digits.drop(6)}"
        }
    }

    private fun formatCpf(cpf: String): String {
        if (cpf.length != 11) return cpf
        return "${cpf.take(3)}.${cpf.drop(3).take(3)}.${cpf.drop(6).take(3)}-${cpf.drop(9)}"
    }

    private fun formatCnpj(cnpj: String): String {
        if (cnpj.length != 14) return cnpj
        return "${cnpj.take(2)}.${cnpj.drop(2).take(3)}.${cnpj.drop(5).take(3)}/${cnpj.drop(8).take(4)}-${cnpj.drop(12)}"
    }
}
