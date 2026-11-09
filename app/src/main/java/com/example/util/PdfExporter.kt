package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.WargaKosEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    private const val PAGE_WIDTH = 1191 // A3/A4 landscape width in points
    private const val PAGE_HEIGHT = 842  // A3/A4 landscape height in points
    private const val MARGIN_LEFT = 40f
    private const val MARGIN_TOP = 40f
    private const val MARGIN_RIGHT = 40f
    private const val MARGIN_BOTTOM = 40f

    fun generatePdf(
        context: Context,
        title: String,
        data: List<WargaKosEntity>,
        isEncrypted: Boolean = false,
        password: String = "",
        targetDir: String = "Download" // "Download" or "Documents"
    ): File? {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintText = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        val paintBold = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            isFakeBoldText = true
        }

        val paintBorder = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val paintHeaderBg = Paint().apply {
            color = Color.rgb(235, 240, 250)
            style = Paint.Style.FILL
        }

        val paintAltBg = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val printTime = dateFormat.format(Date())

        // 1. Draw Title
        paintBold.textSize = 20f
        paintBold.textAlign = Paint.Align.CENTER
        canvas.drawText(title.uppercase(), PAGE_WIDTH / 2f, MARGIN_TOP + 10f, paintBold)

        // 2. Draw Printed Date
        paintText.textSize = 9f
        paintText.textAlign = Paint.Align.RIGHT
        val printDateStr = "Dicetak pada: $printTime"
        canvas.drawText(printDateStr, PAGE_WIDTH - MARGIN_RIGHT, MARGIN_TOP + 28f, paintText)

        // Column widths definition (Total = 1111 pt)
        // 1: No (45)
        // 2: Pemilik (170)
        // 3: Nama (170)
        // 4: No Kamar (90)
        // 5: Jml Orang (95)
        // 6: Alamat (210)
        // 7: Nikah (95)
        // 8: Belum Nikah (105)
        // 9: Keterangan (131)
        val colWidths = floatArrayOf(45f, 170f, 170f, 90f, 95f, 210f, 95f, 105f, 131f)
        val startX = MARGIN_LEFT
        var startY = MARGIN_TOP + 40f

        val headerRow1Height = 22f
        val headerRow2Height = 20f
        val rowHeight = 17f

        // Draw Header Row 1 & 2
        var currentX = startX

        // A. No (Merged row 1 & 2)
        drawCell(canvas, "No", currentX, startY, colWidths[0], headerRow1Height + headerRow2Height, paintHeaderBg, paintBorder, paintBold, 10f, Paint.Align.CENTER)
        currentX += colWidths[0]

        // B. Nama Pemilik Kos (Merged row 1 & 2)
        drawCell(canvas, "Nama Pemilik Kos", currentX, startY, colWidths[1], headerRow1Height + headerRow2Height, paintHeaderBg, paintBorder, paintBold, 10f, Paint.Align.CENTER)
        currentX += colWidths[1]

        // C. Nama Warga Kos (Merged 3 cols in row 1, separate in row 2)
        val wargaGroupWidth = colWidths[2] + colWidths[3] + colWidths[4]
        drawCell(canvas, "Nama Warga Kos", currentX, startY, wargaGroupWidth, headerRow1Height, paintHeaderBg, paintBorder, paintBold, 10f, Paint.Align.CENTER)
        drawCell(canvas, "Nama", currentX, startY + headerRow1Height, colWidths[2], headerRow2Height, paintHeaderBg, paintBorder, paintBold, 9f, Paint.Align.CENTER)
        drawCell(canvas, "No Kamar", currentX + colWidths[2], startY + headerRow1Height, colWidths[3], headerRow2Height, paintHeaderBg, paintBorder, paintBold, 9f, Paint.Align.CENTER)
        drawCell(canvas, "Jumlah Orang", currentX + colWidths[2] + colWidths[3], startY + headerRow1Height, colWidths[4], headerRow2Height, paintHeaderBg, paintBorder, paintBold, 9f, Paint.Align.CENTER)
        currentX += wargaGroupWidth

        // D. Alamat Lengkap (Merged row 1 & 2)
        drawCell(canvas, "Alamat Lengkap", currentX, startY, colWidths[5], headerRow1Height + headerRow2Height, paintHeaderBg, paintBorder, paintBold, 10f, Paint.Align.CENTER)
        currentX += colWidths[5]

        // E. Status (Merged 2 cols in row 1, separate in row 2)
        val statusGroupWidth = colWidths[6] + colWidths[7]
        drawCell(canvas, "Status", currentX, startY, statusGroupWidth, headerRow1Height, paintHeaderBg, paintBorder, paintBold, 10f, Paint.Align.CENTER)
        drawCell(canvas, "Nikah", currentX, startY + headerRow1Height, colWidths[6], headerRow2Height, paintHeaderBg, paintBorder, paintBold, 9f, Paint.Align.CENTER)
        drawCell(canvas, "Belum Nikah", currentX + colWidths[6], startY + headerRow1Height, colWidths[7], headerRow2Height, paintHeaderBg, paintBorder, paintBold, 9f, Paint.Align.CENTER)
        currentX += statusGroupWidth

        // F. Keterangan (Merged row 1 & 2)
        drawCell(canvas, "Keterangan", currentX, startY, colWidths[8], headerRow1Height + headerRow2Height, paintHeaderBg, paintBorder, paintBold, 10f, Paint.Align.CENTER)

        startY += (headerRow1Height + headerRow2Height)

        // Draw Data Rows (at least 25 rows as in template/script)
        val rowCount = maxOf(25, data.size)
        paintText.textSize = 8.5f

        for (i in 0 until rowCount) {
            val item = if (i < data.size) data[i] else null
            val isAlt = i % 2 == 1
            val bgPaint = if (isAlt) paintAltBg else null

            var cellX = startX
            val noStr = "${i + 1}"
            val pemilikStr = item?.pemilik ?: ""
            val namaStr = item?.nama ?: ""
            val noKamarStr = item?.noKamar ?: ""
            val jmlStr = item?.jumlahOrang ?: ""
            val alamatStr = item?.alamat ?: ""
            val nikahStr = item?.statusNikah ?: ""
            val belumNikahStr = item?.statusBelumNikah ?: ""
            val ketStr = item?.keterangan ?: ""

            drawCell(canvas, noStr, cellX, startY, colWidths[0], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.CENTER)
            cellX += colWidths[0]

            drawCell(canvas, pemilikStr, cellX, startY, colWidths[1], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.LEFT, padX = 6f)
            cellX += colWidths[1]

            drawCell(canvas, namaStr, cellX, startY, colWidths[2], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.LEFT, padX = 6f)
            cellX += colWidths[2]

            drawCell(canvas, noKamarStr, cellX, startY, colWidths[3], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.CENTER)
            cellX += colWidths[3]

            drawCell(canvas, jmlStr, cellX, startY, colWidths[4], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.CENTER)
            cellX += colWidths[4]

            drawCell(canvas, alamatStr, cellX, startY, colWidths[5], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.LEFT, padX = 6f)
            cellX += colWidths[5]

            drawCell(canvas, nikahStr, cellX, startY, colWidths[6], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.CENTER)
            cellX += colWidths[6]

            drawCell(canvas, belumNikahStr, cellX, startY, colWidths[7], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.CENTER)
            cellX += colWidths[7]

            drawCell(canvas, ketStr, cellX, startY, colWidths[8], rowHeight, bgPaint, paintBorder, paintText, 8.5f, Paint.Align.LEFT, padX = 6f)

            startY += rowHeight
        }

        // 3. Draw Signature block (Ketua RW. 01, Ketua RT. 01, Ketua RT. 02)
        val ttdY = maxOf(startY + 20f, PAGE_HEIGHT - 120f)
        val totalTableWidth = colWidths.sum()
        val ttdColWidth = totalTableWidth / 3f

        val ttdTitles = arrayOf(
            "Mengetahui,\nKetua RW. 01",
            "Ketua RT. 01",
            "Ketua RT. 02"
        )

        for (idx in 0..2) {
            val ttdX = startX + (idx * ttdColWidth) + (ttdColWidth / 2f)
            paintBold.textSize = 10f
            paintBold.textAlign = Paint.Align.CENTER

            val lines = ttdTitles[idx].split("\n")
            var textY = ttdY
            for (line in lines) {
                canvas.drawText(line, ttdX, textY, paintBold)
                textY += 14f
            }

            // Signature line
            paintText.textSize = 10f
            paintText.textAlign = Paint.Align.CENTER
            canvas.drawText("( ________________________ )", ttdX, ttdY + 65f, paintText)
        }

        document.finishPage(page)

        // Save PDF to internal app cache first
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val baseFileName = "Laporan_Warga_Kos_$timeStamp"
        val tempPdfFile = File(context.cacheDir, "$baseFileName.pdf")

        try {
            FileOutputStream(tempPdfFile).use { out ->
                document.writeTo(out)
            }
            document.close()

            // Handle Encryption if requested
            val fileToSave: File = if (isEncrypted && password.isNotBlank()) {
                val encFile = File(context.cacheDir, "$baseFileName.enc.pdf")
                val encSuccess = EncryptionHelper.encryptFile(tempPdfFile, encFile, password)
                if (encSuccess) encFile else tempPdfFile
            } else {
                tempPdfFile
            }

            // Save to public storage (Downloads or Documents)
            saveFileToPublicStorage(
                context = context,
                sourceFile = fileToSave,
                displayName = fileToSave.name,
                mimeType = if (isEncrypted) "application/octet-stream" else "application/pdf",
                targetFolder = targetDir
            )

            return fileToSave
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        }
    }

    private fun drawCell(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        bgPaint: Paint?,
        borderPaint: Paint,
        textPaint: Paint,
        textSize: Float,
        align: Paint.Align,
        padX: Float = 4f
    ) {
        // Draw background
        bgPaint?.let {
            canvas.drawRect(x, y, x + width, y + height, it)
        }

        // Draw border
        canvas.drawRect(x, y, x + width, y + height, borderPaint)

        // Draw text
        textPaint.textSize = textSize
        textPaint.textAlign = align

        val bounds = Rect()
        textPaint.getTextBounds(if (text.isNotEmpty()) text else "A", 0, if (text.isNotEmpty()) text.length else 1, bounds)
        val textY = y + (height / 2f) + (bounds.height() / 2f) - 2f

        val drawX = when (align) {
            Paint.Align.CENTER -> x + (width / 2f)
            Paint.Align.LEFT -> x + padX
            Paint.Align.RIGHT -> x + width - padX
        }

        // Truncate text if needed
        val availableWidth = width - (padX * 2)
        val truncated = textPaint.breakText(text, true, availableWidth, null)
        val finalText = if (truncated < text.length && truncated > 3) {
            text.substring(0, truncated - 3) + "..."
        } else {
            text
        }

        canvas.drawText(finalText, drawX, textY, textPaint)
    }

    fun saveFileToPublicStorage(
        context: Context,
        sourceFile: File,
        displayName: String,
        mimeType: String,
        targetFolder: String
    ): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val directory = if (targetFolder.equals("Documents", ignoreCase = true)) {
                    Environment.DIRECTORY_DOCUMENTS
                } else {
                    Environment.DIRECTORY_DOWNLOADS
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "$directory/DataWargaKos")
                }

                val collectionUri = if (targetFolder.equals("Documents", ignoreCase = true)) {
                    MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                }

                val uri = context.contentResolver.insert(collectionUri, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        FileInputStream(sourceFile).use { `is` ->
                            `is`.copyTo(os)
                        }
                    }
                    true
                } else {
                    false
                }
            } else {
                val baseDir = if (targetFolder.equals("Documents", ignoreCase = true)) {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                } else {
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                }
                val destDir = File(baseDir, "DataWargaKos")
                if (!destDir.exists()) destDir.mkdirs()
                val destFile = File(destDir, displayName)
                sourceFile.copyTo(destFile, overwrite = true)
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
