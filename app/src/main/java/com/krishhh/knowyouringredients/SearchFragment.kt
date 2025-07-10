package com.krishhh.knowyouringredients.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.krishhh.knowyouringredients.IngredientDetailActivity
import com.krishhh.knowyouringredients.databinding.FragmentSearchBinding
import com.krishhh.knowyouringredients.db.IngredientDatabase
import com.krishhh.knowyouringredients.utils.HistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        binding.listSuggestions.adapter = adapter

        // Live search
        binding.etSearch.doAfterTextChanged { text ->
            val query = text.toString().trim()
            if (query.length >= 2) {
                searchSuggestions(query)
            } else {
                adapter.clear()
            }
        }

        // Handle enter key
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                launchDetails(query)
            }
            true
        }

        // Handle item click
        binding.listSuggestions.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener
            launchDetails(selected)
        }
    }

    private fun searchSuggestions(query: String) {
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                IngredientDatabase.get(requireContext()).dao().searchSuggestions(query)
            }
            adapter.clear()
            adapter.addAll(results)
        }
    }

    private fun launchDetails(query: String) {
        HistoryManager.saveHistory(requireContext(), query)
        startActivity(IngredientDetailActivity.intent(requireContext(), query))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
