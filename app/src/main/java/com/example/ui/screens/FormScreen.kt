package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(MainViewModel.SCREEN_HOME)
    }

    val editingId by viewModel.editingId.collectAsState()
    val pemilik by viewModel.formPemilik.collectAsState()
    val nama by viewModel.formNama.collectAsState()
    val noKamar by viewModel.formNoKamar.collectAsState()
    val jumlahOrang by viewModel.formJumlahOrang.collectAsState()
    val alamat by viewModel.formAlamat.collectAsState()
    val statusNikah by viewModel.formStatusNikah.collectAsState()
    val statusBelumNikah by viewModel.formStatusBelumNikah.collectAsState()
    val keterangan by viewModel.formKeterangan.collectAsState()

    val isEditing = editingId != null
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Data Warga Kos" else "Tambah Warga Kos Baru",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(MainViewModel.SCREEN_HOME) },
                        modifier = Modifier.testTag("form_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Formulir Identitas Warga",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Nama Warga Kos (Required)
                    OutlinedTextField(
                        value = nama,
                        onValueChange = { viewModel.formNama.value = it },
                        label = { Text("Nama Warga Kos *") },
                        placeholder = { Text("Masukkan nama lengkap warga") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_nama_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Nama Pemilik Kos
                    OutlinedTextField(
                        value = pemilik,
                        onValueChange = { viewModel.formPemilik.value = it },
                        label = { Text("Nama Pemilik Kos") },
                        placeholder = { Text("Contoh: H. Sudirman / Ibu Aminah") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_pemilik_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. No Kamar & Jumlah Orang
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = noKamar,
                            onValueChange = { viewModel.formNoKamar.value = it },
                            label = { Text("No Kamar") },
                            placeholder = { Text("Contoh: 01 / A-12") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.MeetingRoom, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_no_kamar_input")
                        )

                        OutlinedTextField(
                            value = jumlahOrang,
                            onValueChange = { viewModel.formJumlahOrang.value = it },
                            label = { Text("Jumlah Orang") },
                            placeholder = { Text("1") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.People, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_jumlah_orang_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Alamat Lengkap
                    OutlinedTextField(
                        value = alamat,
                        onValueChange = { viewModel.formAlamat.value = it },
                        label = { Text("Alamat Lengkap / Asal") },
                        placeholder = { Text("Alamat domisili asal / KTP warga kos") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Place, contentDescription = null)
                        },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_alamat_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Status Pernikahan (Nikah & Belum Nikah)
                    Text(
                        text = "Status Pernikahan:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = statusBelumNikah == "Ya",
                            onClick = {
                                viewModel.formStatusBelumNikah.value = if (statusBelumNikah == "Ya") "" else "Ya"
                                if (viewModel.formStatusBelumNikah.value == "Ya") {
                                    viewModel.formStatusNikah.value = ""
                                }
                            },
                            label = { Text("Belum Nikah (Lajang)") },
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = statusNikah == "Ya",
                            onClick = {
                                viewModel.formStatusNikah.value = if (statusNikah == "Ya") "" else "Ya"
                                if (viewModel.formStatusNikah.value == "Ya") {
                                    viewModel.formStatusBelumNikah.value = ""
                                }
                            },
                            label = { Text("Nikah (Menikah)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. Keterangan
                    OutlinedTextField(
                        value = keterangan,
                        onValueChange = { viewModel.formKeterangan.value = it },
                        label = { Text("Keterangan") },
                        placeholder = { Text("Catatan khusus, pekerjaan, kendaraan, dll.") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                        },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_keterangan_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearForm() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_clear_button")
                        ) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kosongkan")
                        }

                        Button(
                            onClick = { viewModel.saveWarga() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("form_save_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEditing) "Simpan Perubahan" else "Simpan Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
