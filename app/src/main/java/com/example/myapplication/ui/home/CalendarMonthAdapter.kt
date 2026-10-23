package com.example.myapplication.ui.home

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.google.android.material.R as MaterialR

data class CalendarDayItem(
    val year: Int,
    val month: Int,
    val dayOfMonth: Int,
    val hasMatches: Boolean,
    val isSelected: Boolean
)

class CalendarMonthAdapter(
    private var days: List<CalendarDayItem> = emptyList(),
    private val onDaySelected: (CalendarDayItem) -> Unit
) : RecyclerView.Adapter<CalendarMonthAdapter.ViewHolder>() {

    fun submitList(newDays: List<CalendarDayItem>) {
        days = newDays
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(days[position], onDaySelected)
    }

    override fun getItemCount() = days.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvDayNumber: TextView = view.findViewById(R.id.tvDayNumber)
        private val vMatchDot: View = view.findViewById(R.id.vMatchDot)

        fun bind(day: CalendarDayItem, onDaySelected: (CalendarDayItem) -> Unit) {
            if (day.dayOfMonth <= 0) {
                tvDayNumber.text = ""
                vMatchDot.visibility = View.GONE
                itemView.isClickable = false
                itemView.setOnClickListener(null)
                tvDayNumber.background = null
            } else {
                tvDayNumber.text = day.dayOfMonth.toString()
                itemView.isClickable = true

                val context = itemView.context

                if (day.isSelected) {
                    tvDayNumber.setBackgroundResource(R.drawable.bg_calendar_selected)
                    val colorOnPrimary = resolveThemeColor(context, MaterialR.attr.colorOnPrimary, Color.WHITE)
                    tvDayNumber.setTextColor(colorOnPrimary)
                } else if (day.hasMatches) {
                    tvDayNumber.setBackgroundResource(R.drawable.bg_board_game_token)
                    val colorPrimary = resolveThemeColor(context, android.R.attr.colorPrimary, Color.BLUE)
                    tvDayNumber.setTextColor(colorPrimary)
                } else {
                    tvDayNumber.background = null
                    val colorTextMain = resolveThemeColor(context, android.R.attr.textColorPrimary, Color.BLACK)
                    tvDayNumber.setTextColor(colorTextMain)
                }

                vMatchDot.visibility = if (day.hasMatches) View.VISIBLE else View.GONE

                itemView.setOnClickListener {
                    onDaySelected(day)
                }
            }
        }

        private fun resolveThemeColor(context: Context, attrRes: Int, defaultColor: Int): Int {
            val typedValue = TypedValue()
            return if (context.theme.resolveAttribute(attrRes, typedValue, true)) {
                typedValue.data
            } else {
                defaultColor
            }
        }
    }
}
