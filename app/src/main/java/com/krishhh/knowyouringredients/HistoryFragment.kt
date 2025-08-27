package com.krishhh.knowyouringredients

import android.graphics.*
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
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
        adapter = HistoryAdapter(HistoryManager.getHistory(requireContext())) {
            startActivity(IngredientDetailActivity.intent(requireContext(), it.phrase))
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter

        updateNoHistoryView()
        setupSwipeToDelete()
    }

    override fun onResume() {
        super.onResume()
        reloadHistory()
    }

    private fun reloadHistory() {
        val newList = HistoryManager.getHistory(requireContext())
        adapter.setItems(newList)
        updateNoHistoryView()
    }

    private fun updateNoHistoryView() {
        binding.tvNoHistory.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
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
                updateNoHistoryView()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = Paint().apply {
                    color = ContextCompat.getColor(recyclerView.context, R.color.my_custom_red)
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val cornerRadius = 24f

                val rectF: RectF
                val radii: FloatArray
                val icon = ContextCompat.getDrawable(recyclerView.context, R.drawable.delete_30dp)!!
                val iconMargin = (itemView.height - icon.intrinsicHeight) / 3
                val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom = iconTop + icon.intrinsicHeight

                if (dX > 0) {
                    rectF = RectF(itemView.left.toFloat(), itemView.top.toFloat(), itemView.left + dX, itemView.bottom.toFloat())
                    radii = floatArrayOf(cornerRadius, cornerRadius, 0f, 0f, 0f, 0f, cornerRadius, cornerRadius)
                    icon.setBounds(itemView.left + iconMargin, iconTop, itemView.left + iconMargin + icon.intrinsicWidth, iconBottom)
                } else {
                    rectF = RectF(itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    radii = floatArrayOf(0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0f, 0f)
                    icon.setBounds(itemView.right - iconMargin - icon.intrinsicWidth, iconTop, itemView.right - iconMargin, iconBottom)
                }

                val path = Path().apply { addRoundRect(rectF, radii, Path.Direction.CW) }
                c.drawPath(path, background)
                icon.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvHistory)
    }

    private fun showUndoSnackbar() {
        val snackbar = Snackbar.make(binding.root, "Item deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") {
                recentlyDeletedItem?.let {
                    adapter.setItems(
                        HistoryManager.getHistory(requireContext())
                    )
                    updateNoHistoryView()
                }
                recentlyDeletedItem = null
            }
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    if (event != DISMISS_EVENT_ACTION && recentlyDeletedItem != null) {
                        HistoryManager.saveAll(requireContext(), (0 until adapter.itemCount).map { adapter.getItemAt(it) })
                        recentlyDeletedItem = null
                    }
                }
            })

        snackbar.anchorView = requireActivity().findViewById(R.id.bottomNav)
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
