package com.dekut.studentregistration.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dekut.studentregistration.R
import com.dekut.studentregistration.models.LibraryRequest
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AdminLibraryRequestsAdapter(
    private var requests: List<LibraryRequest>,
    private val onApprove: (LibraryRequest) -> Unit,
    private val onReject: (LibraryRequest) -> Unit,
    private val onReturn: (LibraryRequest) -> Unit
) : RecyclerView.Adapter<AdminLibraryRequestsAdapter.RequestViewHolder>() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvReqBookTitle: TextView = itemView.findViewById(R.id.tvReqBookTitle)
        val tvReqStatus: TextView = itemView.findViewById(R.id.tvReqStatus)
        val tvReqStudentId: TextView = itemView.findViewById(R.id.tvReqStudentId)
        val tvReqDates: TextView = itemView.findViewById(R.id.tvReqDates)
        val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
        val btnReturn: MaterialButton = itemView.findViewById(R.id.btnReturn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_library_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val req = requests[position]
        holder.tvReqBookTitle.text = req.bookTitle.ifEmpty { "Book ISBN: ${req.bookIsbn}" }
        holder.tvReqStudentId.text = "Student Reg: ${req.studentId}"
        holder.tvReqDates.text = "Borrowed: ${sdf.format(Date(req.dateBorrowed))} | Return: ${sdf.format(Date(req.dateOfReturn))}"

        holder.tvReqStatus.text = req.status
        when (req.status) {
            "APPROVED" -> {
                holder.tvReqStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
                holder.tvReqStatus.setTextColor(Color.parseColor("#2E7D32"))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
                holder.btnReturn.visibility = View.VISIBLE
            }
            "RETURNED" -> {
                holder.tvReqStatus.setBackgroundColor(Color.parseColor("#E3F2FD"))
                holder.tvReqStatus.setTextColor(Color.parseColor("#1565C0"))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
                holder.btnReturn.visibility = View.GONE
            }
            "REJECTED" -> {
                holder.tvReqStatus.setBackgroundColor(Color.parseColor("#FFEBEE"))
                holder.tvReqStatus.setTextColor(Color.parseColor("#C62828"))
                holder.btnApprove.visibility = View.GONE
                holder.btnReject.visibility = View.GONE
                holder.btnReturn.visibility = View.GONE
            }
            else -> { // PENDING
                holder.tvReqStatus.setBackgroundColor(Color.parseColor("#FFF3E0"))
                holder.tvReqStatus.setTextColor(Color.parseColor("#E65100"))
                holder.btnApprove.visibility = View.VISIBLE
                holder.btnReject.visibility = View.VISIBLE
                holder.btnReturn.visibility = View.GONE
            }
        }

        holder.btnApprove.setOnClickListener { onApprove(req) }
        holder.btnReject.setOnClickListener { onReject(req) }
        holder.btnReturn.setOnClickListener { onReturn(req) }
    }

    override fun getItemCount(): Int = requests.size

    fun updateRequests(newRequests: List<LibraryRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
