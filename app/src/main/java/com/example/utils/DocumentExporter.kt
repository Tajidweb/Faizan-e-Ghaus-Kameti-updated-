package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.data.db.MemberWithPayment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentExporter {

    // --- QR Code Generator ---
    fun generateUpQrCode(payeeAddress: String, payeeName: String, amount: Double, remarks: String, size: Int = 512): Bitmap? {
        // Standard UPI Pay URL scheme: upi://pay?pa=address&pn=name&am=amount&tn=remarks&cu=INR
        val upiUrl = "upi://pay?pa=$payeeAddress&pn=${payeeName.replace(" ", "%20")}&am=$amount&tn=${remarks.replace(" ", "%20")}&cu=INR"
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(upiUrl, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- HTML Receipt Printing ---
    fun printReceipt(context: Context, member: MemberWithPayment, monthName: String) {
        val dateStr = if (member.paymentDate != null) {
            SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date(member.paymentDate))
        } else {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 20px; color: #333; }
                    .receipt-card { border: 2px solid #2e7d32; padding: 30px; border-radius: 12px; max-width: 500px; margin: auto; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
                    .header { text-align: center; border-bottom: 2px dashed #ccc; padding-bottom: 15px; margin-bottom: 20px; }
                    .header h1 { margin: 0; color: #1b5e20; font-size: 24px; }
                    .header p { margin: 5px 0 0 0; color: #666; font-size: 14px; }
                    .row { display: flex; justify-content: space-between; margin: 10px 0; font-size: 15px; }
                    .label { color: #777; font-weight: 500; }
                    .value { font-weight: bold; color: #111; }
                    .amount-row { background: #e8f5e9; padding: 15px; border-radius: 6px; margin: 20px 0; border-left: 5px solid #2e7d32; display: flex; justify-content: space-between; align-items: center; }
                    .amount-label { font-size: 16px; color: #1b5e20; font-weight: bold; }
                    .amount-val { font-size: 22px; color: #1b5e20; font-weight: 900; }
                    .footer { text-align: center; border-top: 1px dashed #ccc; padding-top: 15px; margin-top: 25px; font-size: 12px; color: #888; }
                    .stamp { border: 2px solid #2e7d32; color: #2e7d32; display: inline-block; padding: 5px 15px; font-weight: bold; border-radius: 4px; text-transform: uppercase; margin-top: 10px; transform: rotate(-5deg); font-size: 14px; letter-spacing: 1px; }
                </style>
            </head>
            <body>
                <div class="receipt-card">
                    <div class="header">
                        <h1>Faizan e Ghaus Kameti</h1>
                        <p>Committee Management System 2026</p>
                    </div>
                    <div class="row" style="display: flex; justify-content: space-between;">
                        <span><b style="color:#777;">Receipt No:</b> ${member.receiptNumber ?: "N/A"}</span>
                        <span><b style="color:#777;">Date:</b> $dateStr</span>
                    </div>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 15px 0;" />
                    <div class="row" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                        <span class="label">Committee Month:</span>
                        <span class="value" style="text-align: right;">$monthName</span>
                    </div>
                    <div class="row" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                        <span class="label">Member Name:</span>
                        <span class="value" style="text-align: right;">${member.name}</span>
                    </div>
                    <div class="row" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                        <span class="label">Mobile Number:</span>
                        <span class="value" style="text-align: right;">${member.mobile}</span>
                    </div>
                    <div class="row" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                        <span class="label">Payment Mode:</span>
                        <span class="value" style="text-align: right;">${member.paymentMode ?: "N/A"}</span>
                    </div>
                    <div class="row" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                        <span class="label">Remarks:</span>
                        <span class="value" style="text-align: right;">${member.remarks?.ifBlank { "None" } ?: "None"}</span>
                    </div>
                    
                    <div class="amount-row">
                        <span class="amount-label">Amount Paid</span>
                        <span class="amount-val">₹${member.monthlyAmount}</span>
                    </div>
                    
                    <div class="footer">
                        <div class="stamp">PAID</div>
                        <p style="margin-top: 15px;">Thank you for your timely contribution!</p>
                        <p style="font-size: 10px; color: #aaa; margin-top: 5px;">This is a computer generated receipt. No signature required.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, htmlContent, "Receipt_${member.name.replace(" ", "_")}_$monthName")
    }

    // --- HTML Report Printing ---
    fun printReport(context: Context, members: List<MemberWithPayment>, monthName: String) {
        val totalMembers = members.size
        val paidMembers = members.filter { it.status == "PAID" }
        val paidCount = paidMembers.size
        val pendingCount = totalMembers - paidCount
        val totalCollection = paidMembers.sumOf { it.monthlyAmount }
        val remainingAmount = members.filter { it.status != "PAID" }.sumOf { it.monthlyAmount }
        val dateGenerated = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date())

        var rowsHtml = ""
        members.forEachIndexed { index, m ->
            val statusStyle = if (m.status == "PAID") "background-color: #e8f5e9; color: #2e7d32;" else "background-color: #ffebee; color: #c62828;"
            val dateStr = if (m.paymentDate != null) SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(m.paymentDate)) else "-"
            rowsHtml += """
                <tr>
                    <td>${m.serialNumber}</td>
                    <td><b>${m.name}</b><br/><small style="color:#666;">${m.mobile}</small></td>
                    <td>₹${m.monthlyAmount}</td>
                    <td><span style="padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 12px; $statusStyle">${m.status ?: "PENDING"}</span></td>
                    <td>${m.paymentMode?.ifBlank { "N/A" } ?: "N/A"}</td>
                    <td>$dateStr</td>
                    <td>${m.remarks?.ifBlank { "-" } ?: "-"}</td>
                </tr>
            """.trimIndent()
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; padding: 15px; color: #333; }
                    .header { text-align: center; margin-bottom: 25px; border-bottom: 3px double #333; padding-bottom: 15px; }
                    .header h1 { margin: 0; color: #1a237e; font-size: 26px; }
                    .header p { margin: 5px 0 0 0; color: #555; font-size: 14px; }
                    .meta-row { display: flex; justify-content: space-between; font-size: 13px; color: #666; margin-bottom: 20px; }
                    .dashboard-grid { display: flex; justify-content: space-between; margin-bottom: 30px; gap: 10px; }
                    .card { flex: 1; padding: 12px; border-radius: 8px; border: 1px solid #ddd; text-align: center; }
                    .card-title { font-size: 12px; color: #666; text-transform: uppercase; margin-bottom: 5px; font-weight: bold; }
                    .card-value { font-size: 18px; font-weight: bold; color: #111; }
                    .card-paid { border-left: 5px solid #2e7d32; background-color: #f1f8e9; }
                    .card-pending { border-left: 5px solid #c62828; background-color: #ffebee; }
                    .card-total { border-left: 5px solid #1a237e; background-color: #e8eaf6; }
                    table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px; }
                    th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                    th { background-color: #1a237e; color: white; font-weight: bold; }
                    tr:nth-child(even) { background-color: #f9f9f9; }
                    .footer { text-align: center; margin-top: 40px; border-top: 1px solid #ccc; padding-top: 15px; font-size: 11px; color: #888; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Faizan e Ghaus Kameti 2026</h1>
                    <p>Monthly Committee Audit & Collection Report</p>
                </div>
                <div class="meta-row">
                    <span><b>Committee Month:</b> $monthName</span>
                    <span><b>Generated On:</b> $dateGenerated</span>
                </div>
                
                <div class="dashboard-grid">
                    <div class="card card-total">
                        <div class="card-title">Total Members</div>
                        <div class="card-value">$totalMembers</div>
                    </div>
                    <div class="card card-paid">
                        <div class="card-title">Paid Members</div>
                        <div class="card-value">$paidCount / $totalMembers</div>
                    </div>
                    <div class="card card-pending">
                        <div class="card-title">Pending Members</div>
                        <div class="card-value">$pendingCount</div>
                    </div>
                    <div class="card card-paid">
                        <div class="card-title">Total Collected</div>
                        <div class="card-value">₹$totalCollection</div>
                    </div>
                    <div class="card card-pending">
                        <div class="card-title">Remaining</div>
                        <div class="card-value">₹$remainingAmount</div>
                    </div>
                </div>

                <h3>Member Contributions Directory</h3>
                <table>
                    <thead>
                        <tr>
                            <th style="width: 5%;">S.No</th>
                            <th style="width: 25%;">Member Name</th>
                            <th style="width: 12%;">Amount</th>
                            <th style="width: 12%;">Status</th>
                            <th style="width: 13%;">Mode</th>
                            <th style="width: 15%;">Date</th>
                            <th style="width: 18%;">Remarks</th>
                        </tr>
                    </thead>
                    <tbody>
                        $rowsHtml
                    </tbody>
                </table>

                <div class="footer">
                    <p>Faizan e Ghaus Kameti © 2026 - All Rights Reserved.</p>
                </div>
            </body>
            </html>
        """.trimIndent()

        printHtml(context, htmlContent, "Report_${monthName.replace(" ", "_")}")
    }

    // Print helper that triggers PrintManager with WebView
    private fun printHtml(context: Context, htmlContent: String, jobName: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val builder = PrintAttributes.Builder()
                printManager.print(jobName, printAdapter, builder.build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    // --- CSV Export & Share ---
    fun exportToCsvAndShare(context: Context, members: List<MemberWithPayment>, monthName: String) {
        if (members.isEmpty()) {
            Toast.makeText(context, "No members to export", Toast.LENGTH_SHORT).show()
            return
        }

        val csvBuilder = StringBuilder()
        // Header
        csvBuilder.append("Serial Number,Member Name,Mobile Number,Monthly Amount,Payment Status,Payment Mode,Payment Date,Remarks\n")
        
        members.forEach { m ->
            val dateStr = if (m.paymentDate != null) {
                SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date(m.paymentDate))
            } else ""
            
            // Clean names and remarks for CSV compatibility (quote strings containing commas)
            val nameClean = if (m.name.contains(",")) "\"${m.name}\"" else m.name
            val remarksClean = if (m.remarks?.contains(",") == true) "\"${m.remarks}\"" else (m.remarks ?: "")

            csvBuilder.append("${m.serialNumber},")
                .append("$nameClean,")
                .append("${m.mobile},")
                .append("${m.monthlyAmount},")
                .append("${m.status ?: "PENDING"},")
                .append("${m.paymentMode ?: "None"},")
                .append("$dateStr,")
                .append("$remarksClean\n")
        }

        val csvText = csvBuilder.toString()
        
        // Use share sheet
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/comma-separated-values"
                putExtra(Intent.EXTRA_SUBJECT, "Committee_Report_$monthName")
                putExtra(Intent.EXTRA_TEXT, csvText)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Committee Report (CSV)"))
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
