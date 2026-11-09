package com.example.util

import android.content.Context
import com.example.data.model.WargaKosEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    fun generateExcel(
        context: Context,
        title: String,
        data: List<WargaKosEntity>,
        isEncrypted: Boolean = false,
        password: String = "",
        targetDir: String = "Download"
    ): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val printTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val baseFileName = "Laporan_Warga_Kos_$timeStamp"
        val tempExcelFile = File(context.cacheDir, "$baseFileName.xls")

        try {
            val xmlContent = buildSpreadsheetXml(title, printTime, data)
            FileOutputStream(tempExcelFile).use { fos ->
                fos.write(xmlContent.toByteArray(Charsets.UTF_8))
            }

            // Handle encryption if requested
            val fileToSave: File = if (isEncrypted && password.isNotBlank()) {
                val encFile = File(context.cacheDir, "$baseFileName.enc.xls")
                val success = EncryptionHelper.encryptFile(tempExcelFile, encFile, password)
                if (success) encFile else tempExcelFile
            } else {
                tempExcelFile
            }

            // Save to public storage (Download/Document)
            PdfExporter.saveFileToPublicStorage(
                context = context,
                sourceFile = fileToSave,
                displayName = fileToSave.name,
                mimeType = if (isEncrypted) "application/octet-stream" else "application/vnd.ms-excel",
                targetFolder = targetDir
            )

            return fileToSave
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildSpreadsheetXml(title: String, printTime: String, data: List<WargaKosEntity>): String {
        val totalRows = maxOf(40, data.size)
        val sb = StringBuilder()

        sb.append("""<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Center"/>
   <Borders/>
   <Font ss:FontName="Arial" x:Family="Swiss" ss:Size="10" ss:Color="#000000"/>
  </Style>
  <Style ss:ID="TitleStyle">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Arial" x:Family="Swiss" ss:Size="15" ss:Bold="1" ss:Color="#1E3A8A"/>
  </Style>
  <Style ss:ID="DateStyle">
   <Alignment ss:Horizontal="Right" ss:Vertical="Center"/>
   <Font ss:FontName="Arial" x:Family="Swiss" ss:Size="9" ss:Italic="1" ss:Color="#555555"/>
  </Style>
  <Style ss:ID="HeaderStyle">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1"/>
   </Borders>
   <Font ss:FontName="Arial" x:Family="Swiss" ss:Size="10" ss:Bold="1" ss:Color="#000000"/>
   <Interior ss:Color="#E2E8F0" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="CellCenter">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
   </Borders>
   <Font ss:FontName="Arial" ss:Size="9.5"/>
  </Style>
  <Style ss:ID="CellLeft">
   <Alignment ss:Horizontal="Left" ss:Vertical="Center"/>
   <Borders>
    <Border ss:Position="Bottom" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
    <Border ss:Position="Left" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
    <Border ss:Position="Right" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
    <Border ss:Position="Top" ss:LineStyle="Continuous" ss:Weight="1" ss:Color="#CCCCCC"/>
   </Borders>
   <Font ss:FontName="Arial" ss:Size="9.5"/>
  </Style>
  <Style ss:ID="TtdJabatan">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center" ss:WrapText="1"/>
   <Font ss:FontName="Arial" ss:Size="10" ss:Bold="1"/>
  </Style>
  <Style ss:ID="TtdNama">
   <Alignment ss:Horizontal="Center" ss:Vertical="Center"/>
   <Font ss:FontName="Arial" ss:Size="10"/>
  </Style>
 </Styles>
 <Worksheet ss:Name="Data Warga Kos">
  <Table ss:DefaultRowHeight="20">
   <Column ss:Width="40"/>
   <Column ss:Width="160"/>
   <Column ss:Width="160"/>
   <Column ss:Width="85"/>
   <Column ss:Width="100"/>
   <Column ss:Width="200"/>
   <Column ss:Width="85"/>
   <Column ss:Width="105"/>
   <Column ss:Width="150"/>
   
   <!-- Row 1: Title -->
   <Row ss:Height="28">
    <Cell ss:MergeAcross="8" ss:StyleID="TitleStyle"><Data ss:Type="String">${escapeXml(title)}</Data></Cell>
   </Row>
   
   <!-- Row 2: Print Date -->
   <Row ss:Height="18">
    <Cell ss:MergeAcross="8" ss:StyleID="DateStyle"><Data ss:Type="String">Dicetak pada: $printTime</Data></Cell>
   </Row>
   
   <!-- Row 3: Spacer -->
   <Row ss:Height="8"/>
   
   <!-- Row 4: Header Row 1 -->
   <Row ss:Height="24">
    <Cell ss:MergeDown="1" ss:StyleID="HeaderStyle"><Data ss:Type="String">No</Data></Cell>
    <Cell ss:MergeDown="1" ss:StyleID="HeaderStyle"><Data ss:Type="String">Nama Pemilik Kos</Data></Cell>
    <Cell ss:MergeAcross="2" ss:StyleID="HeaderStyle"><Data ss:Type="String">Nama Warga Kos</Data></Cell>
    <Cell ss:MergeDown="1" ss:StyleID="HeaderStyle"><Data ss:Type="String">Alamat Lengkap</Data></Cell>
    <Cell ss:MergeAcross="1" ss:StyleID="HeaderStyle"><Data ss:Type="String">Status</Data></Cell>
    <Cell ss:MergeDown="1" ss:StyleID="HeaderStyle"><Data ss:Type="String">Keterangan</Data></Cell>
   </Row>
   
   <!-- Row 5: Header Row 2 -->
   <Row ss:Height="22">
    <Cell ss:Index="3" ss:StyleID="HeaderStyle"><Data ss:Type="String">Nama</Data></Cell>
    <Cell ss:StyleID="HeaderStyle"><Data ss:Type="String">No Kamar</Data></Cell>
    <Cell ss:StyleID="HeaderStyle"><Data ss:Type="String">Jumlah Orang</Data></Cell>
    <Cell ss:Index="7" ss:StyleID="HeaderStyle"><Data ss:Type="String">Nikah</Data></Cell>
    <Cell ss:StyleID="HeaderStyle"><Data ss:Type="String">Belum Nikah</Data></Cell>
   </Row>
""")

        // Data Rows
        for (i in 0 until totalRows) {
            val item = if (i < data.size) data[i] else null
            val no = "${i + 1}"
            val pemilik = item?.pemilik ?: ""
            val nama = item?.nama ?: ""
            val noKamar = item?.noKamar ?: ""
            val jumlahOrang = item?.jumlahOrang ?: ""
            val alamat = item?.alamat ?: ""
            val statusNikah = item?.statusNikah ?: ""
            val statusBelumNikah = item?.statusBelumNikah ?: ""
            val keterangan = item?.keterangan ?: ""

            sb.append("""   <Row ss:Height="21">
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">$no</Data></Cell>
    <Cell ss:StyleID="CellLeft"><Data ss:Type="String">${escapeXml(pemilik)}</Data></Cell>
    <Cell ss:StyleID="CellLeft"><Data ss:Type="String">${escapeXml(nama)}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${escapeXml(noKamar)}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${escapeXml(jumlahOrang)}</Data></Cell>
    <Cell ss:StyleID="CellLeft"><Data ss:Type="String">${escapeXml(alamat)}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${escapeXml(statusNikah)}</Data></Cell>
    <Cell ss:StyleID="CellCenter"><Data ss:Type="String">${escapeXml(statusBelumNikah)}</Data></Cell>
    <Cell ss:StyleID="CellLeft"><Data ss:Type="String">${escapeXml(keterangan)}</Data></Cell>
   </Row>
""")
        }

        // Signature Rows
        sb.append("""   <!-- Signature spacer -->
   <Row ss:Height="16"/>
   <Row ss:Height="30">
    <Cell ss:MergeAcross="2" ss:StyleID="TtdJabatan"><Data ss:Type="String">Mengetahui,&#10;Ketua RW. 01</Data></Cell>
    <Cell ss:MergeAcross="2" ss:StyleID="TtdJabatan"><Data ss:Type="String">Ketua RT. 01</Data></Cell>
    <Cell ss:MergeAcross="2" ss:StyleID="TtdJabatan"><Data ss:Type="String">Ketua RT. 02</Data></Cell>
   </Row>
   <Row ss:Height="36"/>
   <Row ss:Height="24">
    <Cell ss:MergeAcross="2" ss:StyleID="TtdNama"><Data ss:Type="String">( ____________________ )</Data></Cell>
    <Cell ss:MergeAcross="2" ss:StyleID="TtdNama"><Data ss:Type="String">( ____________________ )</Data></Cell>
    <Cell ss:MergeAcross="2" ss:StyleID="TtdNama"><Data ss:Type="String">( ____________________ )</Data></Cell>
   </Row>
  </Table>
  <WorksheetOptions xmlns="urn:schemas-microsoft-com:office:excel">
   <PageSetup>
    <Layout x:Orientation="Landscape"/>
    <PageMargins x:Bottom="0.35" x:Left="0.25" x:Right="0.25" x:Top="0.35"/>
   </PageSetup>
   <FitToPage/>
   <Print>
    <FitWidth>1</FitWidth>
    <FitHeight>1</FitHeight>
    <ValidPrinterInfo/>
   </Print>
  </WorksheetOptions>
 </Worksheet>
</Workbook>""")

        return sb.toString()
    }
}
