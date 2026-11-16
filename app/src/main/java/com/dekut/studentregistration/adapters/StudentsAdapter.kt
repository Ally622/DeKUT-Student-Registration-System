package com.dekut.studentregistration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dekut.studentregistration.R
import com.dekut.studentregistration.models.Student
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class StudentsAdapter(
    private val onStudentClick: (Student) -> Unit,
    private val onPrintClick: (Student) -> Unit
) : ListAdapter<Student, StudentsAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardStudent: MaterialCardView = itemView.findViewById(R.id.cardStudent)
        private val tvStudentName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvRegNumber: TextView = itemView.findViewById(R.id.tvRegNumber)
        private val tvIdNumber: TextView = itemView.findViewById(R.id.tvIdNumber)
        private val tvCourse: TextView = itemView.findViewById(R.id.tvCourse)
        private val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)
        private val btnPrintSummary: MaterialButton = itemView.findViewById(R.id.btnPrintSummary)

        fun bind(student: Student) {
            tvStudentName.text = student.fullName
            tvRegNumber.text = "Reg. No: ${student.registrationNumber}"
            tvIdNumber.text = "ID: ${student.idNumber}"
            tvCourse.text = student.course

            // Status badge
            tvStatus.text = student.registrationStatus
            tvStatus.setBackgroundColor(
                if (student.registrationStatus == "CONFIRMED") {
                    itemView.context.getColor(R.color.status_success)
                } else {
                    itemView.context.getColor(R.color.status_pending)
                }
            )

            // Payment status
            tvPaymentStatus.text = student.paymentStatus
            tvPaymentStatus.setTextColor(
                if (student.paymentStatus == "PAID") {
                    itemView.context.getColor(R.color.status_success)
                } else {
                    itemView.context.getColor(R.color.status_pending)
                }
            )

            cardStudent.setOnClickListener {
                onStudentClick(student)
            }

            btnPrintSummary.setOnClickListener {
                onPrintClick(student)
            }
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
}
