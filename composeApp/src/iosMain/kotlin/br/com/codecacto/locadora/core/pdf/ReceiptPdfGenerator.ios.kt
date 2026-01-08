@file:OptIn(ExperimentalForeignApi::class)

package br.com.codecacto.locadora.core.pdf

import br.com.codecacto.locadora.core.model.StatusPagamento
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginPDFContextToData
import platform.UIKit.UIGraphicsBeginPDFPageWithInfo
import platform.UIKit.UIGraphicsEndPDFContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIWindow
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.Foundation.NSMutableData
import platform.Foundation.NSString
import platform.Foundation.drawAtPoint
import platform.Foundation.drawInRect
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextSetStrokeColorWithColor
import platform.CoreGraphics.CGContextMoveToPoint
import platform.CoreGraphics.CGContextAddLineToPoint
import platform.CoreGraphics.CGContextStrokePath
import platform.CoreGraphics.CGContextSetLineWidth

actual class ReceiptPdfGenerator {

    actual suspend fun generateReceipt(data: ReceiptData): String = withContext(Dispatchers.Main) {
        val pdfData = NSMutableData()
        val pageWidth = 595.0
        val pageHeight = 842.0
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)

        UIGraphicsBeginPDFContextToData(pdfData, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        drawReceiptContent(data, pageWidth, pageHeight)

        UIGraphicsEndPDFContext()

        // Save to cache directory
        val cachePaths = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory,
            NSUserDomainMask,
            true
        )
        val cacheDir = cachePaths.firstOrNull() as? String ?: throw Exception("Cache directory not found")

        val receiptsDir = "$cacheDir/receipts"
        NSFileManager.defaultManager.createDirectoryAtPath(
            receiptsDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )

        val timestamp = platform.Foundation.NSDate().timeIntervalSince1970.toLong()
        val fileName = "recibo_${data.locacao.id.take(8)}_$timestamp.pdf"
        val filePath = "$receiptsDir/$fileName"

        pdfData.writeToFile(filePath, atomically = true)

        filePath
    }

    private fun drawReceiptContent(data: ReceiptData, pageWidth: Double, pageHeight: Double) {
        val margin = 40.0
        var yPosition = 60.0
        val lineHeight = 20.0
        val contentWidth = pageWidth - (margin * 2)

        val titleFont = UIFont.boldSystemFontOfSize(24.0)
        val headerFont = UIFont.boldSystemFontOfSize(14.0)
        val normalFont = UIFont.systemFontOfSize(12.0)
        val smallFont = UIFont.systemFontOfSize(10.0)
        val valueFont = UIFont.boldSystemFontOfSize(16.0)

        val blackColor = UIColor.blackColor
        val grayColor = UIColor.grayColor
        val darkGrayColor = UIColor.darkGrayColor
        val lightGrayColor = UIColor.lightGrayColor

        // Header - Company Name
        if (data.dadosEmpresa.nomeEmpresa.isNotBlank()) {
            drawCenteredText(
                data.dadosEmpresa.nomeEmpresa,
                yPosition,
                pageWidth,
                titleFont,
                blackColor
            )
            yPosition += lineHeight * 1.5

            // Company Info
            val companyInfo = buildString {
                if (data.dadosEmpresa.endereco.isNotBlank()) append(data.dadosEmpresa.endereco)
                if (data.dadosEmpresa.telefone.isNotBlank()) {
                    if (isNotBlank()) append(" | ")
                    append("Tel: ${formatPhone(data.dadosEmpresa.telefone)}")
                }
            }
            if (companyInfo.isNotBlank()) {
                drawCenteredText(companyInfo, yPosition, pageWidth, smallFont, grayColor)
                yPosition += lineHeight
            }

            val companyInfo2 = buildString {
                if (data.dadosEmpresa.cnpj.isNotBlank()) append("CNPJ: ${formatCnpj(data.dadosEmpresa.cnpj)}")
                if (data.dadosEmpresa.email.isNotBlank()) {
                    if (isNotBlank()) append(" | ")
                    append(data.dadosEmpresa.email)
                }
            }
            if (companyInfo2.isNotBlank()) {
                drawCenteredText(companyInfo2, yPosition, pageWidth, smallFont, grayColor)
                yPosition += lineHeight
            }
        } else {
            drawCenteredText(
                "Locacao de Equipamentos",
                yPosition,
                pageWidth,
                titleFont,
                blackColor
            )
            yPosition += lineHeight * 1.5
        }

        yPosition += 10.0

        // Separator line
        drawLine(margin, yPosition, pageWidth - margin, yPosition, lightGrayColor)
        yPosition += lineHeight * 1.5

        // Receipt Title
        val receiptTitleFont = UIFont.boldSystemFontOfSize(18.0)
        drawCenteredText("RECIBO DE LOCACAO", yPosition, pageWidth, receiptTitleFont, blackColor)
        yPosition += lineHeight

        // Date and Receipt Number
        val currentDate = formatDate(platform.Foundation.NSDate().timeIntervalSince1970.toLong() * 1000)
        drawCenteredText(
            "Data: $currentDate    |    No: ${data.locacao.id.take(8).uppercase()}",
            yPosition,
            pageWidth,
            smallFont,
            grayColor
        )
        yPosition += lineHeight * 1.5

        // Separator line
        drawLine(margin, yPosition, pageWidth - margin, yPosition, lightGrayColor)
        yPosition += lineHeight * 1.5

        // Client Section
        drawText("CLIENTE", margin, yPosition, headerFont, blackColor)
        yPosition += lineHeight

        drawText("Nome: ${data.cliente.nomeRazao}", margin, yPosition, normalFont, darkGrayColor)
        yPosition += lineHeight

        if (!data.cliente.cpfCnpj.isNullOrBlank()) {
            val doc = if (data.cliente.cpfCnpj.length == 11) formatCpf(data.cliente.cpfCnpj) else formatCnpj(data.cliente.cpfCnpj)
            drawText("CPF/CNPJ: $doc", margin, yPosition, normalFont, darkGrayColor)
            yPosition += lineHeight
        }

        drawText("Telefone: ${formatPhone(data.cliente.telefoneWhatsapp)}", margin, yPosition, normalFont, darkGrayColor)
        yPosition += lineHeight * 1.5

        // Separator line
        drawLine(margin, yPosition, pageWidth - margin, yPosition, lightGrayColor)
        yPosition += lineHeight * 1.5

        // Equipment Section
        drawText("EQUIPAMENTO", margin, yPosition, headerFont, blackColor)
        yPosition += lineHeight

        drawText(data.equipamento.nome, margin, yPosition, normalFont, darkGrayColor)
        yPosition += lineHeight

        drawText("Categoria: ${data.equipamento.categoria}", margin, yPosition, normalFont, darkGrayColor)
        yPosition += lineHeight

        if (!data.equipamento.identificacao.isNullOrBlank()) {
            drawText("Identificacao: ${data.equipamento.identificacao}", margin, yPosition, normalFont, darkGrayColor)
            yPosition += lineHeight
        }

        yPosition += lineHeight * 0.5

        // Separator line
        drawLine(margin, yPosition, pageWidth - margin, yPosition, lightGrayColor)
        yPosition += lineHeight * 1.5

        // Rental Details Section
        drawText("DETALHES DA LOCACAO", margin, yPosition, headerFont, blackColor)
        yPosition += lineHeight

        drawText(
            "Periodo: ${formatDate(data.locacao.dataInicio)} a ${formatDate(data.locacao.dataFimPrevista)}",
            margin,
            yPosition,
            normalFont,
            darkGrayColor
        )
        yPosition += lineHeight

        drawText("Valor: ${formatCurrency(data.locacao.valorLocacao)}", margin, yPosition, valueFont, blackColor)
        yPosition += lineHeight

        val statusPagamento = if (data.locacao.statusPagamento == StatusPagamento.PAGO) "Pago" else "Pendente"
        drawText("Status Pagamento: $statusPagamento", margin, yPosition, normalFont, darkGrayColor)
        yPosition += lineHeight * 2

        // Separator line
        drawLine(margin, yPosition, pageWidth - margin, yPosition, lightGrayColor)
        yPosition += lineHeight * 3

        // Signature Section
        drawText("Assinatura: ___________________________________________", margin, yPosition, normalFont, darkGrayColor)
        yPosition += lineHeight * 4

        // Footer
        drawCenteredText(
            "Documento gerado em ${formatDate(platform.Foundation.NSDate().timeIntervalSince1970.toLong() * 1000)}",
            yPosition,
            pageWidth,
            smallFont,
            grayColor
        )
    }

    private fun drawText(text: String, x: Double, y: Double, font: UIFont, color: UIColor) {
        val nsString = NSString.create(string = text)
        val attributes = mapOf<Any?, Any?>(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color
        )
        nsString.drawAtPoint(CGPointMake(x, y), withAttributes = attributes)
    }

    private fun drawCenteredText(text: String, y: Double, pageWidth: Double, font: UIFont, color: UIColor) {
        val nsString = NSString.create(string = text)
        val attributes = mapOf<Any?, Any?>(
            NSFontAttributeName to font,
            NSForegroundColorAttributeName to color
        )
        val textSize = nsString.sizeWithAttributes(attributes)
        val x = (pageWidth - textSize.useContents { width }) / 2
        nsString.drawAtPoint(CGPointMake(x, y), withAttributes = attributes)
    }

    private fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double, color: UIColor) {
        val context = UIGraphicsGetCurrentContext() ?: return
        CGContextSetStrokeColorWithColor(context, color.CGColor)
        CGContextSetLineWidth(context, 1.0)
        CGContextMoveToPoint(context, x1, y1)
        CGContextAddLineToPoint(context, x2, y2)
        CGContextStrokePath(context)
    }

    actual fun shareReceipt(filePath: String) {
        val fileUrl = NSURL.fileURLWithPath(filePath)

        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null
        )

        // Configure for iPad
        activityViewController.popoverPresentationController?.let { popover ->
            val keyWindow = UIApplication.sharedApplication.windows.firstOrNull { window ->
                (window as? UIWindow)?.isKeyWindow() == true
            } as? UIWindow

            keyWindow?.rootViewController?.view?.let { view ->
                popover.sourceView = view
                popover.sourceRect = view.bounds
            }
        }

        // Present the controller
        val keyWindow = UIApplication.sharedApplication.windows.firstOrNull { window ->
            (window as? UIWindow)?.isKeyWindow() == true
        } as? UIWindow

        keyWindow?.rootViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
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
        return "R$ $intPart,${decPart.toString().padStart(2, '0')}"
    }

    private fun formatPhone(phone: String): String {
        if (phone.length < 10) return phone
        return "(${phone.take(2)}) ${phone.drop(2).take(5)}-${phone.drop(7)}"
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
