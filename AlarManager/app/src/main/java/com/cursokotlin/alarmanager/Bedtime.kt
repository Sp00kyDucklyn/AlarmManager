package com.cursokotlin.alarmanager

import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class Bedtime : Fragment(), SensorEventListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var status: TextView
    private lateinit var startStopLayout: LinearLayout
    private lateinit var startStop: FloatingActionButton
    private lateinit var startStopText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var dashboard_header:TextView
    private lateinit var no_sleeps:TextView

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private val sharedPreferenceListener = SharedPreferencesChangeListener()

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("startStop", false)) {
                it.removeExtra("startStop")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_bedtime, container, false)

        status = root.findViewById(R.id.status)
        startStopLayout = root.findViewById(R.id.start_stop_layout)
        startStop = root.findViewById(R.id.start_stop)
        startStopText = root.findViewById(R.id.start_stop_text)
        recyclerView = root.findViewById(R.id.sleeps)
        dashboard_header = root.findViewById(R.id.dashboard_header)
        no_sleeps = root.findViewById(R.id.no_sleeps)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        sharedPreferenceListener.applyTheme(
            PreferenceManager.getDefaultSharedPreferences(
                requireContext()
            )
        )

        val preferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener)
        DataModel.init(requireContext().applicationContext, preferences)

        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(MainViewModel::class.java)

        preferences.liveData("dashboard_duration", "-7").observe(
            requireActivity()
        ) { setDashboardText(it ?: "0") }

        val sleepsAdapter = SleepsAdapter(preferences)
        viewModel.durationSleepsLive.observe(
            requireActivity()
        ) { sleeps ->
            if (sleeps != null) {
                val fragments = childFragmentManager
                val stats = fragments.findFragmentById(R.id.dashboard_body)?.view
                val countStat = stats?.findViewById<TextView>(R.id.fragment_stats_sleeps)
                countStat?.text = DataModel.getSleepCountStat(sleeps)
                val durationStat = stats?.findViewById<TextView>(R.id.fragment_stats_average)
                durationStat?.text = DataModel.getSleepDurationStat(
                    sleeps,
                    DataModel.getCompactView()
                )
                val durationDailyStat = stats?.findViewById<TextView>(R.id.fragment_stats_daily)
                durationDailyStat?.text = DataModel.getSleepDurationDailyStat(
                    sleeps,
                    DataModel.getCompactView(),
                    DataModel.getIgnoreEmptyDays()
                )
                sleepsAdapter.data = sleeps

                if (sleeps.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    no_sleeps.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    no_sleeps.visibility = View.GONE
                }
            }
        }

        val recyclerViewLayout = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = recyclerViewLayout
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = sleepsAdapter

        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            recyclerViewLayout.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        sleepsAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(
                    positionStart: Int,
                    itemCount: Int
                ) {
                    recyclerView.scrollToPosition(positionStart)
                }
            })

        val itemTouchHelper = ItemTouchHelper(
            SleepTouchCallback(
                requireContext().applicationContext,
                requireContext().contentResolver,
                viewModel,
                sleepsAdapter
            )
        )
        itemTouchHelper.attachToRecyclerView(recyclerView)
        val sleepClickCallback = SleepClickCallback(requireContext(), sleepsAdapter, recyclerView)
        sleepsAdapter.clickCallback = sleepClickCallback

        val fabText = startStopText
        val listener = View.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                fabText.visibility = View.GONE
            } else {
                fabText.visibility = View.VISIBLE
            }
        }
        recyclerView.setOnScrollChangeListener(listener)

        handleIntent(requireActivity().intent)

        DataModel.start==null
        detenerSueño()

        updateView()

        return root
    }

    private fun setDashboardText(durationStr: String) {
        var index = resources.getStringArray(R.array.duration_entry_values).indexOf(durationStr)
        val durations = resources.getStringArray(R.array.duration_entries)
        if (index == -1) {
            index = durations.size - 1
        }
        val durationHeaderStr = resources.getStringArray(R.array.duration_entries)[index]
        dashboard_header.text =
            getString(R.string.dashboard, durationHeaderStr)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), MainService::class.java)
        requireActivity().stopService(intent)
        recyclerView.findViewHolderForAdapterPosition(0)?.let {
            recyclerView.adapter?.onBindViewHolder(it, 0)
        }
    }

    private fun iniciarSueño() {
        alarm.run { showTimePicker() }
        DataModel.start = Calendar.getInstance().time
        DataModel.stop = null
        updateView()
        findNavController().navigate(R.id.action_bedtimeFragment_to_start_Sleep)
    }

    private fun detenerSueño() {
        if(DataModel.start!=null){
            DataModel.stop = Calendar.getInstance().time
            viewModel.stopSleep(requireContext().applicationContext, requireContext().contentResolver)
            updateView()
            DataModel.start=null
        }
    }

    private fun createColorStateList(color: Int): ColorStateList {
        return ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private fun updateView() {
        val dndManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        val dndEnabled = preferences.getBoolean("enable_dnd", false)

        startStop.contentDescription = getString(R.string.start_again)
        startStop.setImageResource(R.drawable.ic_start)
        startStopText.text = getString(R.string.start)

        startStop.backgroundTintList = createColorStateList(R.color.colorFabPrimary)
        startStopLayout.backgroundTintList = startStop.backgroundTintList

        DataModel.start?.let { start ->
            preferences.edit()
                .putInt("current_dnd", dndManager.currentInterruptionFilter)
                .apply()
            if (dndEnabled && dndManager.isNotificationPolicyAccessGranted) {
                val filterPri = NotificationManager.INTERRUPTION_FILTER_PRIORITY
                dndManager.setInterruptionFilter(filterPri)
            }
            return
        }

        startStop.backgroundTintList = createColorStateList(R.color.colorFabPrimary)
        startStopLayout.backgroundTintList = startStop.backgroundTintList
    }
    val alarm = Alarm()
    override fun onSensorChanged(event: SensorEvent?) {
        startStopLayout.setOnClickListener {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val lightValue = event.values[0]
                if (lightValue < 100f) {
                    iniciarSueño()
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("¡Alerta!")
                        .setMessage("Hay mucha luz, es posible que no puedas dormir cómodamente. ¿Estás seguro de querer continuar?")
                        .setPositiveButton("Continuar") { dialog, _ ->
                            dialog.dismiss()
                            iniciarSueño()
                        }
                        .setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
