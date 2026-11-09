package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object FileShareHelper {

    fun shareFile(context: Context, file: File, title: String = "Bagikan Dokumen Warga Kos") {
        if (!file.exists()) {
            Toast.makeText(context, "File tidak ditemukan untuk dibagikan", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val mimeType = when {
                file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                file.name.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
                file.name.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                file.name.endsWith(".enc.pdf", ignoreCase = true) || file.name.endsWith(".enc.xls", ignoreCase = true) -> "application/octet-stream"
                else -> "*/*"
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Berikut adalah lampiran dokumen: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun openFile(context: Context, file: File) {
        if (!file.exists()) {
            Toast.makeText(context, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val mimeType = when {
                file.name.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                file.name.endsWith(".xls", ignoreCase = true) -> "application/vnd.ms-excel"
                file.name.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                else -> "*/*"
            }

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(viewIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Tidak ada aplikasi untuk membuka file ini: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
