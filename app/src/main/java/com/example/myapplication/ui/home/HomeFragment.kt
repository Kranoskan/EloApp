package com.example.myapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.ui.game.Match
import com.example.myapplication.ui.game.MatchWithGame
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {
    private val viewModel: MatchViewModel by activityViewModels()
    
    private lateinit var rvMatches: RecyclerView
    private lateinit var llCalendarContainer: LinearLayout
    private lateinit var rvCalendarGrid: RecyclerView
    private lateinit var calendarMonthAdapter: CalendarMonthAdapter
    private lateinit var tvMonthYearTitle: TextView
    private lateinit var ibPrevMonth: ImageButton
    private lateinit var ibNextMonth: ImageButton
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

    private var currentCalendarYear = selectedYear
    private var currentCalendarMonth = selectedMonth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        rvMatches = view.findViewById(R.id.rvMatches)
        llCalendarContainer = view.findViewById(R.id.llCalendarContainer)
        rvCalendarGrid = view.findViewById(R.id.rvCalendarGrid)
        tvMonthYearTitle = view.findViewById(R.id.tvMonthYearTitle)
        ibPrevMonth = view.findViewById(R.id.ibPrevMonth)
        ibNextMonth = view.findViewById(R.id.ibNextMonth)
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

        calendarMonthAdapter = CalendarMonthAdapter { day ->
            if (day.dayOfMonth > 0) {
                selectedYear = day.year
                selectedMonth = day.month
                selectedDay = day.dayOfMonth
                currentCalendarYear = day.year
                currentCalendarMonth = day.month
                refreshCalendarMonth()
                updateCalendarMatches()
            }
        }

        rvCalendarGrid.layoutManager = GridLayoutManager(context, 7)
        rvCalendarGrid.adapter = calendarMonthAdapter

        ibToggleView.setOnClickListener {
            toggleViewMode()
        }

        ibPrevMonth.setOnClickListener {
            currentCalendarMonth--
            if (currentCalendarMonth < 0) {
                currentCalendarMonth = 11
                currentCalendarYear--
            }
            refreshCalendarMonth()
        }

        ibNextMonth.setOnClickListener {
            currentCalendarMonth++
            if (currentCalendarMonth > 11) {
                currentCalendarMonth = 0
                currentCalendarYear++
            }
            refreshCalendarMonth()
        }

        viewModel.matches.observe(viewLifecycleOwner) { matches ->
            allMatches = matches
            matchAdapter.updateList(matches)
            if (isCalendarMode) {
                refreshCalendarMonth()
                updateCalendarMatches()
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
            currentCalendarYear = selectedYear
            currentCalendarMonth = selectedMonth
            refreshCalendarMonth()
            updateCalendarMatches()
        } else {
            ibToggleView.setImageResource(R.drawable.ic_calendar)
            llCalendarContainer.visibility = View.GONE
            rvMatches.visibility = View.VISIBLE
        }
    }

    private fun refreshCalendarMonth() {
        val cal = Calendar.getInstance().apply {
            set(currentCalendarYear, currentCalendarMonth, 1)
        }

        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es"))
        tvMonthYearTitle.text = monthFormat.format(cal.time).replaceFirstChar { it.uppercase() }

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7

        val dayItems = mutableListOf<CalendarDayItem>()

        repeat(firstDayOfWeek) {
            dayItems.add(CalendarDayItem(currentCalendarYear, currentCalendarMonth, 0, false, false))
        }

        for (day in 1..daysInMonth) {
            val hasMatches = allMatches.any { matchWithGame ->
                isSameDay(matchWithGame.match.date, currentCalendarYear, currentCalendarMonth, day)
            }
            val isSelected = (currentCalendarYear == selectedYear && currentCalendarMonth == selectedMonth && day == selectedDay)
            dayItems.add(CalendarDayItem(currentCalendarYear, currentCalendarMonth, day, hasMatches, isSelected))
        }

        calendarMonthAdapter.submitList(dayItems)
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
