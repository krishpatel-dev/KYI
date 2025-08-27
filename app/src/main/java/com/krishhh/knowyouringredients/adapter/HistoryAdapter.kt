package com.krishhh.knowyouringredients.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.krishhh.knowyouringredients.databinding.ItemHistoryBinding
import com.krishhh.knowyouringredients.model.HistoryEntry
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    items: List<HistoryEntry>,
    private val onItemClick: (HistoryEntry) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val items = items.toMutableList()

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvPhrase.text = item.phrase
        holder.binding.tvTimestamp.text = formatTimestamp(item.timestamp)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    fun removeAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItemAt(position: Int): HistoryEntry = items[position]

    fun setItems(newItems: List<HistoryEntry>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    private fun formatTimestamp(time: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(time))
    }
}
