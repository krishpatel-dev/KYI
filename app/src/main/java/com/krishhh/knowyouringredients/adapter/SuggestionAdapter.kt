package com.krishhh.knowyouringredients.adapter

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    private val items = mutableListOf<String>()

    fun submitList(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class SuggestionViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvSuggestion: TextView = v.findViewById(R.id.text1)
        init {
            v.setOnClickListener {
                val item = items[adapterPosition]
                if (item != "No such product") {
                    onClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.simple_list_item_1, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.tvSuggestion.text = items[position]
    }
}