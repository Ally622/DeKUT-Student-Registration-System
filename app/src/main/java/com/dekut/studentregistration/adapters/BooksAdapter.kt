package com.dekut.studentregistration.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dekut.studentregistration.R
import com.dekut.studentregistration.models.Book
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class BooksAdapter(
    private var books: List<Book>,
    private val onBookSelected: (Book) -> Unit
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    private var selectedBookId: String? = null

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardBook: MaterialCardView = itemView as MaterialCardView
        val tvBookTitle: TextView = itemView.findViewById(R.id.tvBookTitle)
        val tvAvailabilityBadge: TextView = itemView.findViewById(R.id.tvAvailabilityBadge)
        val tvAuthor: TextView = itemView.findViewById(R.id.tvAuthor)
        val tvIsbn: TextView = itemView.findViewById(R.id.tvIsbn)
        val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        val btnSelectBook: MaterialButton = itemView.findViewById(R.id.btnSelectBook)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.tvBookTitle.text = book.title
        holder.tvAuthor.text = "Author: ${book.author}"
        holder.tvIsbn.text = "ISBN: ${book.isbn}"
        holder.tvDepartment.text = "Dept: ${book.department}"

        val isSelected = (selectedBookId == book.id)

        if (isSelected) {
            holder.cardBook.strokeColor = Color.parseColor("#156504")
            holder.cardBook.strokeWidth = 6
        } else {
            holder.cardBook.strokeColor = Color.parseColor("#E0E0E0")
            holder.cardBook.strokeWidth = 2
        }

        if (book.availableCopies > 0) {
            holder.tvAvailabilityBadge.text = "AVAILABLE (${book.availableCopies}/${book.totalCopies})"
            holder.tvAvailabilityBadge.setBackgroundColor(Color.parseColor("#E8F5E9"))
            holder.tvAvailabilityBadge.setTextColor(Color.parseColor("#2E7D32"))
            holder.btnSelectBook.isEnabled = true
            holder.btnSelectBook.text = if (isSelected) "✓ SELECTED" else "📖 Select Book to Borrow"
        } else {
            holder.tvAvailabilityBadge.text = "OUT OF STOCK"
            holder.tvAvailabilityBadge.setBackgroundColor(Color.parseColor("#FFEBEE"))
            holder.tvAvailabilityBadge.setTextColor(Color.parseColor("#C62828"))
            holder.btnSelectBook.isEnabled = false
            holder.btnSelectBook.text = "❌ Out of Stock"
        }

        val selectAction = View.OnClickListener {
            if (book.availableCopies > 0) {
                selectedBookId = book.id
                notifyDataSetChanged()
                onBookSelected(book)
            }
        }

        holder.btnSelectBook.setOnClickListener(selectAction)
        holder.itemView.setOnClickListener(selectAction)
    }

    override fun getItemCount(): Int = books.size

    fun setSelectedBookId(id: String?) {
        selectedBookId = id
        notifyDataSetChanged()
    }

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}
