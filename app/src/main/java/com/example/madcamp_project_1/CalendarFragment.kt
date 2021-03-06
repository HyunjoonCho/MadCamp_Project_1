package com.example.madcamp_project_1

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarviewsample.*
import com.kizitonwose.calendarviewsample.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.calendar_add_dialog.*
import kotlinx.android.synthetic.main.calendar_add_dialog.view.*
import kotlinx.android.synthetic.main.calendar_click_dialog.view.*
import kotlinx.android.synthetic.main.calendar_day_legend.view.*
import kotlinx.android.synthetic.main.calendar_day.view.*
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

private val Context.inputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

data class Event(val id: String, val text: String, val date: LocalDate)

class Example3EventsAdapter(val onClick: (Event) -> Unit) :
    RecyclerView.Adapter<Example3EventsAdapter.Example3EventsViewHolder>() {

    val events = mutableListOf<Event>()
    var longClickListener : OnLongClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Example3EventsViewHolder {
        return Example3EventsViewHolder(parent.inflate(R.layout.event_item_view))
    }

    override fun onBindViewHolder(viewHolder: Example3EventsViewHolder, position: Int) {
        viewHolder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class Example3EventsViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView), LayoutContainer {

        init {
            itemView.setOnClickListener {
                onClick(events[adapterPosition])
            }
            itemView.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    if (longClickListener != null) {
                        longClickListener?.onLongClick(events[adapterPosition])
                        return true
                    }
                    return false
                }

            })
        }

        fun bind(event: Event) {
            val itemText = containerView.findViewById<TextView>(R.id.itemEventText)
            itemText.text = event.text.substringBefore("\n\n")
        }
    }

    interface OnLongClickListener {
        fun onLongClick(event: Event)
    }

    fun setOnLongClickListener(listener: OnLongClickListener) {
        longClickListener = listener
    }

}

class CalendarFragment : BaseFragment(), HasBackButton {

    private val eventsAdapter = Example3EventsAdapter {
        val title = it.text.substringBefore("\n\n")
        val memo = it.text.substringAfter("\n\n")

        val dialogView = layoutInflater.inflate(R.layout.calendar_click_dialog, null)
        val closeBtn = dialogView.close_event
        dialogView.titleEvent.text = title
        dialogView.memoEvent.text = memo
        val memoDialog= AlertDialog.Builder(requireContext()).setView(dialogView).create()

        closeBtn.setOnClickListener {
            memoDialog.dismiss()
        }

        memoDialog.show()
    }


    override val titleRes: Int = R.string.example_3_title

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private lateinit var pref : SharedPreferences
    private lateinit var editor : SharedPreferences.Editor

    private val titleSameYearFormatter = DateTimeFormatter.ofPattern("MMM")
    private val titleFormatter = DateTimeFormatter.ofPattern("yyyy년 MMM")
    private val selectionFormatter = DateTimeFormatter.ofPattern("yyyy년 MMM d일")
    private val events = mutableMapOf<LocalDate, List<Event>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = requireContext().getSharedPreferences("todo", Context.MODE_PRIVATE)
        editor = pref.edit()
        val json: String? = pref.getString("events", "")
        if (!json!!.contentEquals("")) {
            val resobj = JSONObject(json)
            var keys = resobj.keys()

            while (keys.hasNext()) {
                var key = keys.next()
                val d = LocalDate.parse(key)
                var arr = resobj.getJSONArray(key)

                for (i in 0 until arr.length()) {
                    var elm = arr.getJSONObject(i)
                    events[d] = events[d].orEmpty().plus(Event(UUID.randomUUID().toString(), elm.getString("text"), d))
                }
            }

            // Log.i("events", Gson().toJson(events))
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exThreeRv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        eventsAdapter.setOnLongClickListener(object : Example3EventsAdapter.OnLongClickListener {
            override fun onLongClick(event: Event) {
                val builder = AlertDialog.Builder(requireContext())

                builder
                    .setMessage("Are you sure you want to delete this event?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Delete") {_, _ ->
                        deleteEvent(event)
                    }
                builder.show()

            }
        })
        exThreeRv.adapter = eventsAdapter
        exThreeRv.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))

        val daysOfWeek = daysOfWeekFromLocale()
        val currentMonth = YearMonth.now()
        exThreeCalendar.setup(currentMonth.minusMonths(10), currentMonth.plusMonths(10), daysOfWeek.first())
        exThreeCalendar.scrollToMonth(currentMonth)

        if (savedInstanceState == null) {
            exThreeCalendar.post {
                // Show today's events initially.
                selectDate(today)
            }
        }

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay // Will be set when this container is bound.
            val textView = view.exThreeDayText
            val dotView = view.exThreeDotView

            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        selectDate(day.date)
                    }
                }
            }
        }
        exThreeCalendar.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                val dotView = container.dotView

                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()
                    when (day.date) {
                        today -> {
                            textView.setTextColorRes(R.color.calendar_white)
                            textView.setBackgroundResource(R.drawable.calendar_today_bg)
                            dotView.makeInVisible()
                        }
                        selectedDate -> {
                            textView.setTextColorRes(R.color.calendar_magenta)
                            textView.setBackgroundResource(R.drawable.calendar_selected_bg)
                            dotView.makeInVisible()
                        }
                        else -> {
                            textView.setTextColorRes(R.color.calendar_black)
                            textView.background = null
                            dotView.isVisible = events[day.date].orEmpty().isNotEmpty()
                        }
                    }
                } else {
                    textView.makeInVisible()
                    dotView.makeInVisible()
                }
            }
        }

        exThreeCalendar.monthScrollListener = {
            requireActivity().homeToolbar.title = if (it.year == today.year) {
                titleSameYearFormatter.format(it.yearMonth)
            } else {
                titleFormatter.format(it.yearMonth)
            }

            // Select the first day of the month when
            // we scroll to a new month.
            selectDate(it.yearMonth.atDay(1))
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = view.legendLayout
        }
        exThreeCalendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                // Setup each header day text if we have not done that already.
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].name.first().toString()
                        tv.setTextColorRes(R.color.calendar_black)
                    }
                }
            }
        }

        exThreeAddButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.calendar_add_dialog, null)
            val editTextTitle = dialogView.editTitle
            val editTextMemo = dialogView.editMemo
            val closeBtn = dialogView.close_dialog
            val saveBtn = dialogView.save_event

            editTextTitle.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                    if ((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        editTextMemo.requestFocus()
                        editTextMemo.postDelayed(Runnable {
                            requireContext().inputMethodManager.showSoftInput(editTextMemo, 0)
                        }, 30)
                        return true
                    }
                    return false
                }
            })

            val inputDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
                .apply {
                    setOnShowListener {
                        // Show the keyboard
                        editTextTitle.requestFocus()
                        editTextTitle.postDelayed(Runnable {
                            context.inputMethodManager.showSoftInput(editTextTitle, 0)
                        }, 30)
                    }
                    setOnDismissListener {
                        // Hide the keyboard
                        context.inputMethodManager.hideSoftInputFromWindow(editTextMemo.windowToken, 0)
                        context.inputMethodManager.hideSoftInputFromWindow(editTextTitle.windowToken, 0)
                    }
                }
            saveBtn.setOnClickListener {
                saveEvent((editTextTitle.text.toString().plus("\n\n").plus(editTextMemo.text.toString())))

                // Prepare EditText for reuse.
                editTextTitle.setText("")
                editTextMemo.setText("")
                inputDialog.dismiss()
            }
            closeBtn.setOnClickListener {
                inputDialog.dismiss()
            }
            saveBtn.setTextColor((resources.getColorStateList(R.color.selector_button)))
            saveBtn.isEnabled = false

            editTextTitle.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    saveBtn.isEnabled = s.toString().trim().isNotEmpty()
                }
            })

            inputDialog.show()
        }
    }

    private fun selectDate(date: LocalDate) {
        if (selectedDate != date) {
            val oldDate = selectedDate
            selectedDate = date
            oldDate?.let { exThreeCalendar.notifyDateChanged(it) }
            exThreeCalendar.notifyDateChanged(date)
            updateAdapterForDate(date)
        }
    }

    private fun saveEvent(text: String) {
        if (text.isBlank()) {
            Toast.makeText(requireContext(), R.string.example_3_empty_input_text, Toast.LENGTH_LONG).show()
        } else {
            selectedDate?.let {
                events[it] = events[it].orEmpty().plus(Event(UUID.randomUUID().toString(), text, it))
                editor.putString("events", Gson().toJson(events))
                editor.commit()
                updateAdapterForDate(it)
            }
        }
    }

    private fun deleteEvent(event: Event) {
        val date = event.date
        events[date] = events[date].orEmpty().minus(event)
        editor.putString("events", Gson().toJson(events))
        editor.commit()
        updateAdapterForDate(date)
    }

    private fun updateAdapterForDate(date: LocalDate) {
        eventsAdapter.events.clear()
        eventsAdapter.events.addAll(events[date].orEmpty())
        eventsAdapter.notifyDataSetChanged()
        exThreeSelectedDateText.text = selectionFormatter.format(date)
    }
}

