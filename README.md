# Data Warga Kos - Android Native App

Aplikasi Android native untuk pengelolaan data warga kos, dilengkapi dengan sinkronisasi cloud real-time, database lokal Room (offline-first), autentikasi aman berbasis enkripsi token & OAuth 2.0, ekspor dokumen PDF/Excel berformat resmi dengan tanda tangan, pratinjau dokumen (*document preview*), dan fitur *share* multi-platform.

---

## Fitur Utama

- **Database Offline-First (Room)**: Pencatatan identitas warga kos (Pemilik, Nama, Kamar, Jumlah Orang, Alamat, Status Pernikahan, Catatan) secara lokal tanpa koneksi internet.
- **Autentikasi Aman**:
  - Form login dengan token akses terenkripsi **AES-256 GCM**.
  - Autentikasi **OAuth 2.0 Google**.
- **Ekspor Dokumen Resmi**:
  - **PDF Landscape**: Format tabel rapi dilengkapi kop laporan dan tanda tangan resmi (Ketua RW 01, Ketua RT 01, Ketua RT 02).
  - **Excel Spreadsheet (.xls)**: Format sel tabel XML siap buka di Excel, Google Sheets, dan WPS Office.
  - Tersimpan di direktori internal publik (`Download/DataWargaKos` atau `Documents/DataWargaKos`).
  - Opsi enkripsi file dengan password.
- **Pratinjau Dokumen & Share 1-Klik**: Pratinjau visual sebelum dikirimkan ke WhatsApp, Telegram, Gmail, atau media sosial lainnya melalui `FileProvider`.
- **Mode Gelap (Dark Mode)**: Mendukung tema Ikuti Sistem, Mode Terang, dan Mode Gelap.
- **Sinkronisasi Cloud Real-Time**: Sinkronisasi dua arah otomatis dengan antrean offline dan penanganan konflik.

---

## Cara Download File APK Lewat GitHub Actions

Setiap kali Anda melakukan `git push` ke GitHub, GitHub Actions akan otomatis mengompilasi dan menghasilkan file `.apk`.

1. **Push kode ke GitHub**:
   ```bash
   git add .
   git commit -m "Update aplikasi Data Warga Kos"
   git push origin main
   ```
2. Buka repositori Anda di browser GitHub.
3. Klik tab **Actions** di bilah atas.
4. Klik riwayat alur kerja terbaru (**Build & Package APK**).
5. Di bagian paling bawah halaman ringkasan (*Summary*), lihat bagian **Artifacts**.
6. Klik **`Data-Warga-Kos-APK`** untuk mengunduh file `.zip` yang berisi berkas **`Data-Warga-Kos.apk`** yang siap diinstal di HP Android.
