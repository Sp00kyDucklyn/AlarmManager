package com.cursokotlin.alarmanager

import androidx.fragment.app.Fragment
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.util.Calendar
import java.util.Date
import kotlin.math.sqrt

class Graphs : Fragment() {

    private lateinit var preferences: SharedPreferences
    private lateinit var viewModel: MainViewModel
    private lateinit var chart: LineChart
    private lateinit var chartAxisLeftLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_graphs, container, false)

        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity().application)

        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(MainViewModel::class.java)

        // The chart is reused for all different graphs. Below are settings which are the same
        // across all possible graphs.
        chart = root.findViewById(R.id.line_chart)
        chart.setTouchEnabled(false)
        chart.description = null


        chart.xAxis.valueFormatter = DateAxisFormatter()
        chart.xAxis.labelRotationAngle = -30f
        chart.xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.textColor)
        chart.xAxis.textSize = 10f
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.setDrawGridLines(false)

        chart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.textColor)
        chart.axisLeft.textSize = 12f
        chart.axisRight.isEnabled = false

        chart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.textColor)
        chart.legend.textSize = 12f
        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT

        chartAxisLeftLabel = root.findViewById(R.id.chart_axis_left_label)

        // Default to deficit/surplus.
        renderDeficitChart()

        return root
    }

    private fun renderDeficitChart(): Boolean {
        requireActivity().title = getString(R.string.graph_deficit)

        viewModel.durationSleepsLive.observe(
            requireActivity()
        ) { sleeps ->
            if (sleeps != null && sleeps.isNotEmpty()) {
                val deficitByDay = sleeps.lengthPerDay()
                    .map { (day, length) -> day to length - getIdealSleep() }
                val deficitSum = deficitByDay.cumulativeSum()

                val deficitDataSet = deficitByDay.toDataSet(
                    R.string.graph_deficit,
                    ContextCompat.getColor(requireContext(), R.color.dash_daily)
                )
                val deficitSumDataSet = deficitSum.toDataSet(
                    R.string.graph_total,
                    ContextCompat.getColor(requireContext(), R.color.dash_average)
                )
                deficitSumDataSet.setDrawFilled(true)
                deficitSumDataSet.fillColor =
                    ContextCompat.getColor(requireContext(), R.color.dash_average)
                deficitSumDataSet.fillAlpha = 65

                renderChart {
                    chart.axisLeft.valueFormatter = FloatAxisFormatter()
                    chart.axisLeft.granularity = 0.2f
                    setChartAxisLeftLabel(getString(R.string.graph_hours))
                    chart.data = LineData(deficitDataSet, deficitSumDataSet)
                }
            }
        }
        return true
    }

    /** Obtain ideal sleep float from preferences. */
    private fun getIdealSleep(): Float {
        val idealSleepStr = preferences.getString("ideal_sleep_length", "8.0") ?: "8.0"
        return idealSleepStr.toFloatOrNull() ?: 8f
    }

    /** Populates the left Y axis label, or hides it if not necessary (null). */
    private fun setChartAxisLeftLabel(text: String?) {
        if (text != null) {
            chartAxisLeftLabel.text = text
        } else {
            chartAxisLeftLabel.text = "      " // hack so TextView stays populated
        }
    }

    /**
     * Helper function to reset chart state before and refresh chart after setting data, axises,
     * etc.
     */
    private fun renderChart(block: () -> Unit) {
        chart.clear()
        chart.axisLeft.resetAxisMinimum()
        chart.axisLeft.resetAxisMaximum()
        chart.axisLeft.isGranularityEnabled = false
        chart.axisLeft.removeAllLimitLines()
        block() // set your data, etc here
        chart.invalidate()
    }

    /** Converts raw day-value points into a [LineDataSet] with default values. */
    private fun List<Pair<Number, Number>>.toDataSet(label_key: Int, color: Int): LineDataSet {
        return LineDataSet(
            map { (day, value) -> Entry(day.toFloat(), value.toFloat()) },
            getString(label_key)
        ).apply {
            lineWidth = if (size > 250) 2f else 3f
            this.color = color
            setDrawCircles(false)
            setDrawCircleHole(false)
            setDrawValues(false)
        }
    }

    /** Given a list of sleeps, returns list of day-rating pairs, sorted by day. */
    private fun List<Sleep>.ratingByDay(): List<Pair<Long, Float>> {
        return groupSleepsByDay()
            .map { (day, daySleeps) -> day to daySleeps.avgRating() }
            .sortedBy { it.first }
    }

    /** Given a list of sleeps, returns the cumulative variance of the dataset, sorted by day. */
    private fun List<Sleep>.cumulativeVariance(): List<Pair<Long, Float>> {
        val days = groupSleepsByDay()

        return days.entries
            .sortedBy { it.key }
            .asSequence()
            .map { it.key to it.value.sumLength() }
            .cumulativeVariance()
            // drop the first item as it's variance is always 0
            .drop(1)
            .toList()
    }

    /** Given a list of sleeps, returns list of day-start of sleep pairs, sorted by day. */
    private fun List<Sleep>.startOfSleepByDay(): List<Pair<Long, Long>> {
        return groupSleepsByDay()
            .map { (day, daySleeps) -> day to daySleeps.earliestSleep().start - day }
            .sortedBy { it.first }
    }

    /** Given a list of sleeps, returns list of day -> stpp of sleep pairs, sorted by day. */
    private fun List<Sleep>.stopOfSleepByDay(): List<Pair<Long, Long>> {
        return groupSleepsByDay()
            .map { (day, daySleeps) -> day to daySleeps.latestSleep().stop - day }
            .sortedBy { it.first }
    }

    /** Given a list of sleeps, returns list of day-sleep length pairs, sorted by day. */
    private fun List<Sleep>.lengthPerDay(): List<Pair<Long, Float>> {
        return groupSleepsByDay()
            .map { (day, daySleeps) -> day to daySleeps.sumLength() }
            .sortedBy { it.first }
    }

    /** Given a list of sleeps, returns the average rating. */
    private fun List<Sleep>.avgRating(): Float {
        return sumOf { it.rating }.toFloat() / size
    }

    /** Given a list of sleeps, returns the start time of the earliest sleep. */
    private fun List<Sleep>.earliestSleep(): Sleep {
        // Run only on output of groupBy (groupSleepsByDay) so should not be empty.
        return minByOrNull { it.start } ?: Sleep()
    }

    /** Given a list of sleeps, returns the stop time of the latest sleep. */
    private fun List<Sleep>.latestSleep(): Sleep {
        return maxByOrNull { it.stop } ?: Sleep()
    }

    /** Given a list of sleeps, returns the sum of length slept. */
    private fun List<Sleep>.sumLength(): Float {
        return map { it.lengthHours }.sum()
    }

    /** Given a list of sleeps, groups sleeps by date of stop time. */
    private fun List<Sleep>.groupSleepsByDay(): Map<Long, List<Sleep>> {
        return groupBy { it.stop.stripTime() }
    }
}

/** Generates a cumulative moving average list of the provided day-value points. */
fun List<Pair<Number, Number>>.cumulativeAverage(): List<Pair<Number, Number>> {
    return mutableListOf(first()).also {
        subList(1, size).forEach { (day, value) ->
            val oldAvg = it.last().second.toFloat()
            val newAvg = ((oldAvg * it.size) + value.toFloat()) / (it.size + 1)
            it.add(day to newAvg)
        }
    }
}

/** Given a list of sleeps, returns the cumulative variance. */
fun <K : Number> Sequence<Pair<K, Number>>.cumulativeVariance(): Sequence<Pair<K, Float>> {
    var sumSquared = 0f
    var sum = 0f

    return mapIndexed { index, (key, sleepNum) ->
        val sleep = sleepNum.toFloat()
        sumSquared += sleep * sleep
        sum += sleep
        val itemNum = index + 1

        // E[X^2] - E[X]^2
        val mean1 = sumSquared / itemNum
        val mean2 = (sum / itemNum).let { it * it }
        val variance = (mean1 - mean2)

        key to variance
    }
}

fun List<Pair<Number, Number>>.varianceToDeviation(): List<Pair<Number, Number>> =
    map { (date, num) -> date to sqrt(num.toFloat()) }

/** Generates a cumulative sum list of the provided day-value points. */
fun List<Pair<Number, Number>>.cumulativeSum(): List<Pair<Number, Number>> {
    return mutableListOf(first()).also {
        subList(1, size).forEach { (day, value) ->
            it.add(day to value.toFloat() + it.last().second.toFloat())
        }
    }
}

/** Strips the time from a UNIX epoch timestamp (ms). */
fun Long.stripTime(): Long = Date(this).stripTime().time

/** Strips the time from a [Date], leaving only the date. */
fun Date.stripTime(): Date {
    return Calendar.getInstance().apply {
        time = this@stripTime
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time
}

/* vim:set shiftwidth=4 softtabstop=4 expandtab: */


