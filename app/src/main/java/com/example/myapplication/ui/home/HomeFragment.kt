package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.game.Match
import com.example.myapplication.ui.game.MatchWithGame
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class HomeFragment : Fragment() {
    private val viewModel: MatchViewModel by activityViewModels()
    
    private lateinit var rvMatches: RecyclerView
    private lateinit var llCalendarContainer: LinearLayout
    private lateinit var calendarView: CalendarView
    private lateinit var cgMatchDates: ChipGroup
    private lateinit var tvSelectedDateHeader: TextView
    private lateinit var tvNoMatchesCalendar: TextView
    private lateinit var rvCalendarMatches: RecyclerView
    private lateinit var ibToggleView: ImageButton
    private lateinit var fabAddMatch: FloatingActionButton

    private lateinit var matchAdapter: MatchAdapter
    private lateinit var calendarMatchAdapter: MatchAdapter

    private var allMatches: List<MatchWithGame> = emptyList()
    private var isCalendarMode = false

    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        rvMatches = view.findViewById(R.id.rvMatches)
        llCalendarContainer = view.findViewById(R.id.llCalendarContainer)
        calendarView = view.findViewById(R.id.calendarView)
        cgMatchDates = view.findViewById(R.id.cgMatchDates)
        tvSelectedDateHeader = view.findViewById(R.id.tvSelectedDateHeader)
        tvNoMatchesCalendar = view.findViewById(R.id.tvNoMatchesCalendar)
        rvCalendarMatches = view.findViewById(R.id.rvCalendarMatches)
        ibToggleView = view.findViewById(R.id.ibToggleView)
        fabAddMatch = view.findViewById(R.id.fabAddMatch)
        
        matchAdapter = MatchAdapter { match -> navigateToDetail(match) }
        calendarMatchAdapter = MatchAdapter { match -> navigateToDetail(match) }
        
        rvMatches.layoutManager = LinearLayoutManager(context)
        rvMatches.adapter = matchAdapter

        rvCalendarMatches.layoutManager = LinearLayoutManager(context)
        rvCalendarMatches.adapter = calendarMatchAdapter

        ibToggleView.setOnClickListener {
            toggleViewMode()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedYear = year
            selectedMonth = month
            selectedDay = dayOfMonth
            updateCalendarMatches()
            updateChipSelection()
        }

        viewModel.matches.observe(viewLifecycleOwner) { matches ->
            allMatches = matches
            matchAdapter.updateList(matches)
            if (isCalendarMode) {
                updateCalendarMatches()
                updateMatchDateChips()
            }
        }

        fabAddMatch.setOnClickListener {
            navigateToAddMatch()
        }
        
        return view
    }

    private fun toggleViewMode() {
        isCalendarMode = !isCalendarMode
        if (isCalendarMode) {
            ibToggleView.setImageResource(R.drawable.ic_list)
            rvMatches.visibility = View.GONE
            llCalendarContainer.visibility = View.VISIBLE
            updateMatchDateChips()
            updateCalendarMatches()
        } else {
            ibToggleView.setImageResource(R.drawable.ic_calendar)
            llCalendarContainer.visibility = View.GONE
            rvMatches.visibility = View.VISIBLE
        }
    }

    private fun updateMatchDateChips() {
        cgMatchDates.removeAllViews()
        val distinctDays = allMatches.map { matchWithGame ->
            val cal = Calendar.getInstance().apply { timeInMillis = matchWithGame.match.date }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }.distinct().sortedWith(compareBy({ it.first }, { it.second }, { it.third }))

        distinctDays.forEach { (year, month, dayOfMonth) ->
            val chip = Chip(requireContext()).apply {
                text = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                isClickable = true
                isCheckable = true
                isChecked = (year == selectedYear && month == selectedMonth && dayOfMonth == selectedDay)
                setOnClickListener {
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = dayOfMonth
                    val cal = Calendar.getInstance().apply {
                        set(year, month, dayOfMonth)
                    }
                    calendarView.date = cal.timeInMillis
                    updateCalendarMatches()
                    updateChipSelection()
                }
            }
            cgMatchDates.addView(chip)
        }
    }

    private fun updateChipSelection() {
        for (i in 0 until cgMatchDates.childCount) {
            val chip = cgMatchDates.getChildAt(i) as? Chip
            chip?.let {
                val parts = it.text.toString().split("/")
                if (parts.size == 3) {
                    val d = parts[0].toIntOrNull() ?: 0
                    val m = (parts[1].toIntOrNull() ?: 1) - 1
                    val y = parts[2].toIntOrNull() ?: 0
                    it.isChecked = (y == selectedYear && m == selectedMonth && d == selectedDay)
                }
            }
        }
    }

    private fun updateCalendarMatches() {
        val filtered = allMatches.filter { matchWithGame ->
            isSameDay(matchWithGame.match.date, selectedYear, selectedMonth, selectedDay)
        }
        
        val dateStr = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
        tvSelectedDateHeader.text = "Partidas del $dateStr"

        calendarMatchAdapter.updateList(filtered)

        if (filtered.isEmpty()) {
            tvNoMatchesCalendar.visibility = View.VISIBLE
            rvCalendarMatches.visibility = View.GONE
        } else {
            tvNoMatchesCalendar.visibility = View.GONE
            rvCalendarMatches.visibility = View.VISIBLE
        }
    }

    private fun isSameDay(timestamp: Long, year: Int, month: Int, dayOfMonth: Int): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.YEAR) == year &&
               cal.get(Calendar.MONTH) == month &&
               cal.get(Calendar.DAY_OF_MONTH) == dayOfMonth
    }

    private fun navigateToAddMatch() {
        val fragment = AddMatchFragment()
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToDetail(match: Match) {
        val fragment = MatchDetailFragment.newInstance(match.id)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
