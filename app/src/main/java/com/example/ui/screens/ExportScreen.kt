package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavedDocumentEntity
import com.example.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val reportTitle by viewModel.reportTitle.collectAsState()
    val savedDocs by viewModel.savedDocuments.collectAsState()

    var selectedFormat by remember { mutableStateOf("PDF") } // "PDF" or "EXCEL"
    var targetDirectory by remember { mutableStateOf("Download") } // "Download" or "Documents"
    var isEncryptionEnabled by remember { mutableStateOf(false) }
    var encryptionPassword by remember { mutableStateOf("") }
    var customTitleInput by remember { mutableStateOf(reportTitle) }

    var docToDelete by remember { mutableStateOf<SavedDocumentEntity?>(null) }
    var docToPreviewWithPassword by remember { mutableStateOf<SavedDocumentEntity?>(null) }
    var promptPasswordInput by remember { mutableStateOf("") }

    if (docToDelete != null) {
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Hapus Dokumen") },
            text = { Text("Apakah Anda yakin ingin menghapus arsip file \"${docToDelete?.fileName}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        docToDelete?.let { viewModel.deleteSavedDoc(it) }
                        docToDelete = null
                    }
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (docToPreviewWithPassword != null) {
        AlertDialog(
            onDismissRequest = { docToPreviewWithPassword = null },
            title = { Text("Masukkan Password Dokumen") },
            text = {
                Column {
                    Text("Dokumen ini dienkripsi dengan proteksi AES. Masukkan password untuk membuka:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = promptPasswordInput,
                        onValueChange = { promptPasswordInput = it },
                        label = { Text("Password Enkripsi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val doc = docToPreviewWithPassword
                        val pass = promptPasswordInput
                        docToPreviewWithPassword = null
                        promptPasswordInput = ""
                        if (doc != null) {
                            viewModel.loadDocumentForPreview(doc, pass)
                        }
                    }
                ) {
                    Text("Buka Dokumen")
                }
            },
            dismissButton = {
                TextButton(onClick = { docToPreviewWithPassword = null }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pusat Ekspor & Dokumen", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Export Generator Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Buat Laporan Baru",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Judul Laporan
                            OutlinedTextField(
                                value = customTitleInput,
                                onValueChange = { customTitleInput = it },
                                label = { Text("Judul Laporan Dokumen") },
                                placeholder = { Text("DATA WARGA KOS") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_title_input")
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Format Selection (PDF vs Excel)
                            Text(
                                text = "Pilih Format Dokumen:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedFormat == "PDF") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFormat = "PDF" }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = if (selectedFormat == "PDF") MaterialTheme.colorScheme.primary else Color(0xFFDC2626)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "PDF Landscape",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Siap Cetak / TTD",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedFormat == "EXCEL") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFormat = "EXCEL" }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TableChart,
                                            contentDescription = null,
                                            tint = if (selectedFormat == "EXCEL") MaterialTheme.colorScheme.secondary else Color(0xFF16A34A)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Excel Spreadsheet",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Format .xls / xml",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Directory Selection: Download vs Documents
                            Text(
                                text = "Direktori Penyimpanan Internal:",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilterChip(
                                    selected = targetDirectory == "Download",
                                    onClick = { targetDirectory = "Download" },
                                    label = { Text("Download / DataWargaKos") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = targetDirectory == "Documents",
                                    onClick = { targetDirectory = "Documents" },
                                    label = { Text("Documents / DataWargaKos") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Encryption Toggle (AES-256)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Enkripsi File (AES-GCM)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Lindungi dokumen dengan password rahasia",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isEncryptionEnabled,
                                    onCheckedChange = { isEncryptionEnabled = it }
                                )
                            }

                            if (isEncryptionEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = encryptionPassword,
                                    onValueChange = { encryptionPassword = it },
                                    label = { Text("Password Proteksi Enkripsi") },
                                    placeholder = { Text("Minimal 6 karakter") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Main Action Button
                            Button(
                                onClick = {
                                    viewModel.exportDocument(
                                        type = selectedFormat,
                                        customTitle = customTitleInput,
                                        targetDir = targetDirectory,
                                        isEncrypted = isEncryptionEnabled,
                                        password = encryptionPassword
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("export_generate_button")
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buat & Pratinjau Dokumen $selectedFormat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Saved Documents Section
                    Text(
                        text = "Dokumen Tersimpan (${savedDocs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Akses cepat, pratinjau, dan bagikan file ke WhatsApp / medsos lain dengan sekali klik",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (savedDocs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Belum Ada Dokumen Yang Disimpan",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Dokumen PDF dan Excel yang Anda buat akan muncul di sini untuk akses instan.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(savedDocs, key = { it.id }) { doc ->
                    SavedDocumentCard(
                        doc = doc,
                        onPreview = {
                            if (doc.isEncrypted) {
                                docToPreviewWithPassword = doc
                            } else {
                                viewModel.loadDocumentForPreview(doc)
                            }
                        },
                        onShare = { viewModel.shareSavedDoc(doc) },
                        onOpen = { viewModel.openSavedDocExternal(doc) },
                        onDelete = { docToDelete = doc },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun SavedDocumentCard(
    doc: SavedDocumentEntity,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(doc.createdAt))
    val sizeKb = (doc.fileSizeBytes / 1024f)
    val sizeText = if (sizeKb > 1024) String.format(Locale.getDefault(), "%.1f MB", sizeKb / 1024) else String.format(Locale.getDefault(), "%.1f KB", sizeKb)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // File Type Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (doc.fileType == "PDF") Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (doc.fileType == "PDF") Icons.Default.PictureAsPdf else Icons.Default.TableChart,
                        contentDescription = null,
                        tint = if (doc.fileType == "PDF") Color(0xFFDC2626) else Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = doc.fileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        if (doc.isEncrypted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Terenkripsi",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$sizeText • $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons: Pratinjau, Share Sekali Klik, Buka File
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pratinjau", fontSize = 12.sp)
                }

                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share Cepat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onOpen,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Buka File", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
