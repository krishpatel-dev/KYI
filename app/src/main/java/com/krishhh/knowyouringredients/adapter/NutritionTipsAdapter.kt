package com.krishhh.knowyouringredients.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.krishhh.knowyouringredients.R

class NutritionTipsAdapter(private val list: List<Pair<String, String>>) :
    RecyclerView.Adapter<NutritionTipsAdapter.TipViewHolder>() {

    class TipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDetail: TextView = view.findViewById(R.id.tvDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nutrition_tip, parent, false)
        return TipViewHolder(view)
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val (title, detail) = list[position]
        holder.tvTitle.text = title
        holder.tvDetail.text = detail
    }

    override fun getItemCount(): Int = list.size
}
