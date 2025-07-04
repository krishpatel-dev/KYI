package com.krishhh.knowyouringredients

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.TextView

class SearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = TextView(requireContext()).apply {
        text = "Search Fragment (coming soon)"
        textSize = 20f
        setPadding(32, 32, 32, 32)
    }
}
