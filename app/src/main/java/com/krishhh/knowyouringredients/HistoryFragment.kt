package com.krishhh.knowyouringredients

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.google.android.material.snackbar.Snackbar
import com.krishhh.knowyouringredients.adapter.HistoryAdapter
import com.krishhh.knowyouringredients.databinding.FragmentHistoryBinding
import com.krishhh.knowyouringredients.model.HistoryEntry
import com.krishhh.knowyouringredients.utils.HistoryManager

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryEntry>()

    private var recentlyDeletedItem: HistoryEntry? = null
    private var recentlyDeletedPosition: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = HistoryAdapter(historyList) {
            startActivity(IngredientDetailActivity.intent(requireContext(), it.phrase))
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        loadHistory()
        setupSwipeToDelete()
    }

    private fun loadHistory() {
        historyList.clear()
        historyList.addAll(HistoryManager.getHistory(requireContext()))
        adapter.notifyDataSetChanged()
        binding.tvNoHistory.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                rv: RecyclerView, vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                recentlyDeletedPosition = holder.adapterPosition
                recentlyDeletedItem = adapter.getItemAt(recentlyDeletedPosition)

                adapter.removeAt(recentlyDeletedPosition)
                showUndoSnackbar()

                if (adapter.itemCount == 0) {
                    binding.tvNoHistory.visibility = View.VISIBLE
                }
            }

            override fun getSwipeDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                return super.getSwipeDirs(recyclerView, viewHolder)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.rvHistory)
    }

    private fun showUndoSnackbar() {
        val snackbar = Snackbar.make(binding.root, "Item deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                recentlyDeletedItem?.let {
                    historyList.add(recentlyDeletedPosition, it)
                    adapter.notifyItemInserted(recentlyDeletedPosition)
                    binding.tvNoHistory.visibility = View.GONE
                }
                recentlyDeletedItem = null
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION && recentlyDeletedItem != null) {
                        saveUpdatedList()
                        recentlyDeletedItem = null
                    }
                }
            })

        // 🧱 Push it above the BottomNavigationView
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNav)
        snackbar.anchorView = bottomNav

        snackbar.show()
    }


    private fun saveUpdatedList() {
        val updated = adapter.let { (0 until it.itemCount).map { pos -> it.getItemAt(pos) } }
        HistoryManager.saveAll(requireContext(), updated)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
