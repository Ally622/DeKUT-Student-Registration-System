package com.dekut.studentregistration.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dekut.studentregistration.models.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    fun generateStudentSummaryHtml(
        student: Student,
        libraryRequests: List<LibraryRequest>,
        hostelRequest: HostelRequest?,
        payments: List<Payment>
    ): String {
        val totalPaid = payments.filter { it.paymentStatus == "SUCCESS" }.sumOf { it.amount }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 40px;
                        color: #333;
                    }
                    .header {
                        text-align: center;
                        color: #8B0000;
                        border-bottom: 3px solid #8B0000;
                        padding-bottom: 20px;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        margin: 5px 0;
                        font-size: 24px;
                    }
                    .header h2 {
                        margin: 5px 0;
                        font-size: 18px;
                        font-weight: normal;
                    }
                    .section {
                        margin-bottom: 25px;
                    }
                    .section-title {
                        background-color: #8B0000;
                        color: white;
                        padding: 10px;
                        font-size: 16px;
                        font-weight: bold;
                        margin-bottom: 15px;
                    }
                    .info-row {
                        display: flex;
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .info-label {
                        font-weight: bold;
                        width: 200px;
                    }
                    .info-value {
                        flex: 1;
                    }
                    .status-badge {
                        display: inline-block;
                        padding: 4px 12px;
                        border-radius: 4px;
                        font-size: 12px;
                        font-weight: bold;
                    }
                    .status-confirmed {
                        background-color: #28a745;
                        color: white;
                    }
                    .status-pending {
                        background-color: #ffc107;
                        color: #000;
                    }
                    .status-paid {
                        background-color: #17a2b8;
                        color: white;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 10px;
                    }
                    th, td {
                        border: 1px solid #ddd;
                        padding: 10px;
                        text-align: left;
                    }
                    th {
                        background-color: #f4f4f4;
                        font-weight: bold;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        font-size: 12px;
                        color: #666;
                        border-top: 1px solid #ddd;
                        padding-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY</h1>
                    <h2>First Year Student Registration Summary</h2>
                    <p>Generated on ${dateFormat.format(Date())}</p>
                </div>
                
                <div class="section">
                    <div class="section-title">Personal Information</div>
                    <div class="info-row">
                        <div class="info-label">Full Name:</div>
                        <div class="info-value">${student.fullName}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">ID Number:</div>
                        <div class="info-value">${student.idNumber}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Registration Number:</div>
                        <div class="info-value">${student.registrationNumber}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Gender:</div>
                        <div class="info-value">${student.gender}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Date Registered:</div>
                        <div class="info-value">${dateFormat.format(Date(student.dateRegistered))}</div>
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">Academic Information</div>
                    <div class="info-row">
                        <div class="info-label">Course:</div>
                        <div class="info-value">${student.course}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Department:</div>
                        <div class="info-value">${student.department}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Registration Status:</div>
                        <div class="info-value">
                            <span class="status-badge ${if (student.registrationStatus == "CONFIRMED") "status-confirmed" else "status-pending"}">
                                ${student.registrationStatus}
                            </span>
                        </div>
                    </div>
                </div>
                
                <div class="section">
                    <div class="section-title">Payment Information</div>
                    <div class="info-row">
                        <div class="info-label">Payment Status:</div>
                        <div class="info-value">
                            <span class="status-badge ${if (student.paymentStatus == "PAID") "status-paid" else "status-pending"}">
                                ${student.paymentStatus}
                            </span>
                        </div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Total Amount Paid:</div>
                        <div class="info-value">KES ${String.format("%.2f", totalPaid)}</div>
                    </div>
                    ${if (payments.isNotEmpty()) {
                        """
                        <table>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Amount (KES)</th>
                                    <th>Transaction Ref</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${payments.joinToString("") { payment ->
                                    """
                                    <tr>
                                        <td>${dateFormat.format(Date(payment.transactionDate))}</td>
                                        <td>${String.format("%.2f", payment.amount)}</td>
                                        <td>${payment.transactionReference}</td>
                                        <td>${payment.paymentStatus}</td>
                                    </tr>
                                    """
                                }}
                            </tbody>
                        </table>
                        """
                    } else ""}
                </div>
                
                ${if (hostelRequest != null) {
                    """
                    <div class="section">
                        <div class="section-title">Hostel Accommodation</div>
                        <div class="info-row">
                            <div class="info-label">Hostel Location:</div>
                            <div class="info-value">${hostelRequest.hostelLocation}</div>
                        </div>
                        <div class="info-row">
                            <div class="info-label">Room Type:</div>
                            <div class="info-value">${hostelRequest.roomType}</div>
                        </div>
                        <div class="info-row">
                            <div class="info-label">Sharable:</div>
                            <div class="info-value">${hostelRequest.sharable}</div>
                        </div>
                    </div>
                    """
                } else ""}
                
                ${if (libraryRequests.isNotEmpty()) {
                    """
                    <div class="section">
                        <div class="section-title">Library Book Requests</div>
                        <table>
                            <thead>
                                <tr>
                                    <th>ISBN</th>
                                    <th>Title</th>
                                    <th>Author</th>
                                    <th>Borrow Date</th>
                                    <th>Return Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${libraryRequests.joinToString("") { req ->
                                    """
                                    <tr>
                                        <td>${req.bookIsbn}</td>
                                        <td>${req.bookTitle}</td>
                                        <td>${req.author}</td>
                                        <td>${dateFormat.format(Date(req.dateBorrowed))}</td>
                                        <td>${dateFormat.format(Date(req.dateOfReturn))}</td>
                                    </tr>
                                    """
                                }}
                            </tbody>
                        </table>
                    </div>
                    """
                } else ""}
                
                <div class="footer">
                    <p>This is an official registration summary from DeKUT Student Registration System</p>
                    <p>&copy; ${Calendar.getInstance().get(Calendar.YEAR)} Dedan Kimathi University of Technology</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    fun generateAllStudentsSummaryHtml(students: List<Student>): String {
        val confirmedStudents = students.filter { it.registrationStatus == "CONFIRMED" }
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 40px;
                        color: #333;
                    }
                    .header {
                        text-align: center;
                        color: #8B0000;
                        border-bottom: 3px solid #8B0000;
                        padding-bottom: 20px;
                        margin-bottom: 30px;
                    }
                    .header h1 {
                        margin: 5px 0;
                        font-size: 24px;
                    }
                    .header h2 {
                        margin: 5px 0;
                        font-size: 18px;
                        font-weight: normal;
                    }
                    .summary-stats {
                        background-color: #f8f9fa;
                        padding: 20px;
                        margin-bottom: 30px;
                        border-radius: 8px;
                    }
                    .stat-row {
                        padding: 8px 0;
                        font-size: 16px;
                    }
                    .stat-label {
                        font-weight: bold;
                        display: inline-block;
                        width: 250px;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-top: 10px;
                    }
                    th, td {
                        border: 1px solid #ddd;
                        padding: 10px;
                        text-align: left;
                        font-size: 12px;
                    }
                    th {
                        background-color: #8B0000;
                        color: white;
                        font-weight: bold;
                    }
                    tr:nth-child(even) {
                        background-color: #f9f9f9;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        font-size: 12px;
                        color: #666;
                        border-top: 1px solid #ddd;
                        padding-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY</h1>
                    <h2>All Registered Students Summary Report</h2>
                    <p>Generated on ${dateFormat.format(Date())}</p>
                </div>
                
                <div class="summary-stats">
                    <div class="stat-row">
                        <span class="stat-label">Total Registered Students:</span>
                        <span>${students.size}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Confirmed Registrations:</span>
                        <span>${confirmedStudents.size}</span>
                    </div>
                    <div class="stat-row">
                        <span class="stat-label">Pending Registrations:</span>
                        <span>${students.size - confirmedStudents.size}</span>
                    </div>
                </div>
                
                <table>
                    <thead>
                        <tr>
                            <th>No.</th>
                            <th>Reg. Number</th>
                            <th>Full Name</th>
                            <th>ID Number</th>
                            <th>Gender</th>
                            <th>Course</th>
                            <th>Department</th>
                            <th>Payment Status</th>
                            <th>Registration Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${students.mapIndexed { index, student ->
                            """
                            <tr>
                                <td>${index + 1}</td>
                                <td>${student.registrationNumber}</td>
                                <td>${student.fullName}</td>
                                <td>${student.idNumber}</td>
                                <td>${student.gender}</td>
                                <td>${student.course}</td>
                                <td>${student.department}</td>
                                <td>${student.paymentStatus}</td>
                                <td>${student.registrationStatus}</td>
                            </tr>
                            """
                        }.joinToString("")}
                    </tbody>
                </table>
                
                <div class="footer">
                    <p>This is an official report from DeKUT Student Registration System</p>
                    <p>&copy; ${Calendar.getInstance().get(Calendar.YEAR)} Dedan Kimathi University of Technology</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    fun printHtmlContent(html: String, documentName: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                createPrintJob(view, documentName)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
    
    private fun createPrintJob(webView: WebView, documentName: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        
        val printAdapter: PrintDocumentAdapter = webView.createPrintDocumentAdapter(documentName)
        
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        
        printManager.print(documentName, printAdapter, printAttributes)
    }

    // ── Comprehensive individual student report (for Admin) ──────────────────

    fun generateComprehensiveStudentReport(
        student: com.dekut.studentregistration.models.Student,
        hostelRequest: com.dekut.studentregistration.models.HostelRequest?,
        libraryRequests: List<com.dekut.studentregistration.models.LibraryRequest>,
        payments: List<com.dekut.studentregistration.models.Payment>
    ): String {
        val dtFmt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val dFmt  = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val now   = dtFmt.format(java.util.Date())
        val successPayments = payments.filter { it.paymentStatus == "SUCCESS" }
        val totalPaid = successPayments.sumOf { it.amount }
        val feeBalanceHtml = if (student.totalFeesPayable > 0) {
            val balance = student.totalFeesPayable - totalPaid
            """
            <div class="fr"><div class="fl">Total Fees Payable</div><div class="fv">KES ${String.format("%,.2f", student.totalFeesPayable)}</div></div>
            <div class="fr"><div class="fl">Total Verified Payments</div><div class="fv">KES ${String.format("%,.2f", totalPaid)}</div></div>
            <div class="fr"><div class="fl">Outstanding Balance</div><div class="fv"><strong>KES ${String.format("%,.2f", balance)}</strong></div></div>
            """.trimIndent()
        } else {
            "<div class='nd'>Fee balance unavailable (total fees not configured).</div>" +
            if (totalPaid > 0) "<div class='fr'><div class='fl'>Total Verified Payments</div><div class='fv'>KES ${String.format("%,.2f", totalPaid)}</div></div>" else ""
        }

        return """<!DOCTYPE html><html><head><meta charset="UTF-8">
<style>
  body{font-family:Arial,sans-serif;margin:30px;color:#333;font-size:11pt;}
  h1{color:#156504;text-align:center;font-size:20pt;margin-bottom:4px;}
  h2{color:#156504;text-align:center;font-size:14pt;margin-top:0;}
  .meta{text-align:center;color:#555;margin-bottom:24px;font-size:10pt;}
  .sh{background:#156504;color:white;padding:7px 14px;margin:14px -14px;font-weight:bold;font-size:11pt;}
  .fr{display:flex;padding:5px 0;border-bottom:1px solid #eee;}
  .fl{font-weight:bold;width:200px;color:#444;font-size:10pt;}
  .fv{flex:1;font-size:10pt;}
  table{width:100%;border-collapse:collapse;margin-top:8px;font-size:9pt;}
  th{background:#156504;color:white;padding:6px;text-align:left;}
  td{border:1px solid #ddd;padding:5px 7px;}
  tr:nth-child(even){background:#f9f9f9;}
  .ok{color:#2E7D32;font-weight:bold;}.bad{color:#D32F2F;font-weight:bold;}
  .nd{color:#888;font-style:italic;padding:6px 0;}
  .footer{text-align:center;margin-top:40px;font-size:9pt;color:#888;border-top:1px solid #ddd;padding-top:12px;}
  .card{border:1px solid #eee;border-radius:6px;padding:14px;margin-bottom:16px;}
</style></head><body>
<h1>DEDAN KIMATHI UNIVERSITY OF TECHNOLOGY</h1>
<h2>Student Information Report</h2>
<div class="meta">Generated: $now</div>

<div class="card">
<div class="sh">Personal &amp; Academic Information</div>
${row("Full Names", student.fullName)}
${row("National ID Number", student.idNumber)}
${row("Registration Number", student.registrationNumber)}
${row("Gender", student.gender)}
${row("Course", student.course)}
${row("Department", student.department)}
${row("Registration Status", student.registrationStatus)}
${row("Payment Status", student.paymentStatus)}
${row("Date Registered", dFmt.format(java.util.Date(student.dateRegistered)))}
</div>

<div class="card">
<div class="sh">Hostel Information</div>
${if (hostelRequest != null) """
${row("Hostel Location", hostelRequest.hostelLocation)}
${row("Room Type", hostelRequest.roomType)}
${row("Sharable", hostelRequest.sharable)}
${row("Request Date", dFmt.format(java.util.Date(hostelRequest.requestDate)))}
""".trimIndent() else "<div class='nd'>No hostel request.</div>"}
</div>

<div class="card">
<div class="sh">Library Book Requests (${libraryRequests.size})</div>
${if (libraryRequests.isNotEmpty()) """
<table><thead><tr><th>ISBN</th><th>Title</th><th>Author</th><th>Borrowed</th><th>Return By</th></tr></thead><tbody>
${libraryRequests.joinToString("") { lib ->
    "<tr><td>${lib.bookIsbn}</td><td>${lib.bookTitle}</td><td>${lib.author}</td>" +
    "<td>${dFmt.format(java.util.Date(lib.dateBorrowed))}</td><td>${dFmt.format(java.util.Date(lib.dateOfReturn))}</td></tr>"
}}
</tbody></table>""".trimIndent() else "<div class='nd'>No library book requests.</div>"}
</div>

<div class="card">
<div class="sh">Fees &amp; Payment Summary</div>
$feeBalanceHtml
${if (payments.isNotEmpty()) """
<table style="margin-top:12px;"><thead><tr><th>Date</th><th>Amount (KES)</th><th>Transaction Ref</th><th>Status</th></tr></thead><tbody>
${payments.joinToString("") { p ->
    val cls = if (p.paymentStatus=="SUCCESS") "ok" else if (p.paymentStatus=="FAILED") "bad" else ""
    "<tr><td>${dFmt.format(java.util.Date(p.transactionDate))}</td><td>${String.format("%,.2f", p.amount)}</td>" +
    "<td>${p.transactionReference}</td><td><span class='$cls'>${p.paymentStatus}</span></td></tr>"
}}
</tbody></table>""".trimIndent() else ""}
</div>

<div class="footer">
  DeKUT Student Registration System — Confidential — Generated $now<br/>
  &copy; ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Dedan Kimathi University of Technology
</div>
</body></html>"""
    }

    private fun row(label: String, value: String) =
        "<div class='fr'><div class='fl'>$label</div><div class='fv'>$value</div></div>"
}
