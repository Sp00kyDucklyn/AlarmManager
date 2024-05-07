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
//import com.mikepenz.iconics.Iconics.applicationContext
import java.util.Calendar

class Bedtime : Fragment(), SensorEventListener {

    //private lateinit var textView: TextView
    //private lateinit var sleepButton: Button
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

    // SharedPreferences keeps listeners in a WeakHashMap, so keep this as a member.
    private val sharedPreferenceListener = SharedPreferencesChangeListener()

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("startStop", false)) {
                //onClick(startStopLayout)
                it.removeExtra("startStop")
            }
        }
    }
    public fun gle(viewModel: MainViewModel){

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("PASO POR AQUI")
        val root = inflater.inflate(R.layout.fragment_bedtime, container, false)

        //textView = root.findViewById(R.id.start_stop)
        //sleepButton = root.findViewById(R.id.start_stop_text)
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

        //setContentView(R.layout.activity_main)
        //val startStop = findViewById<LinearLayout>(R.id.start_stop_layout)
        //startStopLayout.setOnClickListener(this)

        preferences.liveData("dashboard_duration", "-7").observe(
            requireActivity()
        ) { setDashboardText(it ?: "0") }

        // Hide plain avg and avg(daily sum) if requested:
        preferences.liveDataBoolean("show_average_sleep_durations", false).observe(
            requireActivity()
        ) {
            it?.let {
                val fragments = childFragmentManager
                val stats = fragments.findFragmentById(R.id.dashboard_body)?.view
                //val layout = stats?.findViewById<LinearLayout>(R.id.fragment_stats_average_layout)
                //if (it) {
                //layout?.visibility = View.VISIBLE
                //} else {
                //  layout?.visibility = View.GONE
                //}
            }
        }
        preferences.liveDataBoolean("show_average_daily_sums", true).observe(
            requireActivity()
        ) {
            it?.let {
                val fragments = childFragmentManager
                val stats = fragments.findFragmentById(R.id.dashboard_body)?.view
                //val layout = stats?.findViewById<LinearLayout>(R.id.fragment_stats_daily_layout)
                //if (it) {
                //  layout?.visibility = View.VISIBLE
                //} else {
                // layout?.visibility = View.GONE
                // }
            }
        }

        val sleepsAdapter = SleepsAdapter(preferences)
        //val recyclerView = findViewById<RecyclerView>(R.id.sleeps)
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

                // Set up placeholder text if there are no sleeps.
                //val noSleepsView = findViewById<TextView>(R.id.no_sleeps)
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

        // Enable separators between sleep items.
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

        // Otherwise swipe on a card view deletes it.
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

        // Hide label of FAB on scroll.
        val fabText = startStopText
        val listener = View.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                fabText.visibility = View.GONE
            } else {
                fabText.visibility = View.VISIBLE
            }
        }
        recyclerView.setOnScrollChangeListener(listener)

        // See if the activity is triggered from the widget. If so, toggle the start/stop state.
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
            index = durations.size - 1 // indexOf may return -1, which will out of bounds.
        }
        val durationHeaderStr = resources.getStringArray(R.array.duration_entries)[index]
        dashboard_header.text =
            getString(R.string.dashboard, durationHeaderStr)
    }



    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), MainService::class.java)
        requireActivity().stopService(intent)
        //val recyclerView = findViewById<RecyclerView>(R.id.sleeps)
        recyclerView.findViewHolderForAdapterPosition(0)?.let {
            // Since the adapter unconditionally gets assigned in `onCreate()`
            // it shouldn't be necessary to consider fixing it here.
            // If it is null at this point a lot more must have gone wrong as well.
            recyclerView.adapter?.onBindViewHolder(it, 0)
        }
    }


    /**
    override fun onClick(view: View?) {
    when (view?.id) {
    R.id.start_stop_layout -> {
    /**
    if (DataModel.start != null && DataModel.stop == null) {
    DataModel.stop = Calendar.getInstance().time
    viewModel.stopSleep(applicationContext, contentResolver)
    **/
    DataModel.start = Calendar.getInstance().time
    DataModel.stop = null

    updateView()
    }
    }
    }**/



    // Método para iniciar el sueño en el fragmento Bedtime
    private fun iniciarSueño() {
        // Actualizar el modelo de datos (DataModel) según sea necesario
        DataModel.start = Calendar.getInstance().time
        DataModel.stop = null
        // Llamar al método para actualizar la vista en el fragmento Bedtime
        updateView()

        // Navegar al fragmento StartSleep
        findNavController().navigate(R.id.action_bedtimeFragment_to_start_Sleep)
    }

    // Método para detener el sueño una vez que se regrese al fragmento Bedtime desde StartSleep
    private fun detenerSueño() {
        // Actualizar el modelo de datos (DataModel) según sea necesario
        println("1"+DataModel.start)
        if(DataModel.start!=null){
            DataModel.stop = Calendar.getInstance().time
            // Llamar al método para detener el sueño en el ViewModel
            viewModel.stopSleep(requireContext().applicationContext, requireContext().contentResolver)
            // Llamar al método para actualizar la vista en el fragmento Bedtime
            updateView()
            DataModel.start=null
            println("2"+DataModel.start)
        }

    }

    private fun createColorStateList(color: Int): ColorStateList {
        return ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private fun updateView() {
        val dndManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val preferences = PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        val dndEnabled = preferences.getBoolean("enable_dnd", false)



        /**
        if (DataModel.start != null && DataModel.stop != null) {
        // When user stops tracking sleep
        if (dndEnabled && dndManager.isNotificationPolicyAccessGranted) {
        // Restore Do Not Disturb status when user started tracking
        val filterAll = NotificationManager.INTERRUPTION_FILTER_ALL
        dndManager.setInterruptionFilter(preferences.getInt("current_dnd", filterAll))
        }

        status.text = getString(R.string.tracking_stopped)
        startStop.contentDescription = getString(R.string.start_again)
        startStop.setImageResource(R.drawable.ic_start)
        startStopText.text = getString(R.string.start)

        // Set to custom, ~blue.
        startStop.backgroundTintList = createColorStateList(R.color.colorFabPrimary)
        startStopLayout.backgroundTintList = startStop.backgroundTintList

        return
        }
         **/
        // bedtime.status.text = getString(R.string.tracking_stopped)
        startStop.contentDescription = getString(R.string.start_again)
        startStop.setImageResource(R.drawable.ic_start)
        startStopText.text = getString(R.string.start)

        // Set to custom, ~blue.
        startStop.backgroundTintList = createColorStateList(R.color.colorFabPrimary)
        startStopLayout.backgroundTintList = startStop.backgroundTintList



        DataModel.start?.let { start ->
            // When user starts tracking sleep
            preferences.edit()
                .putInt("current_dnd", dndManager.currentInterruptionFilter)
                .apply() // Saves current Do Not Disturb status
            if (dndEnabled && dndManager.isNotificationPolicyAccessGranted) {
                val filterPri = NotificationManager.INTERRUPTION_FILTER_PRIORITY
                dndManager.setInterruptionFilter(filterPri)
            }
            //status.text = String.format(
            // getString(R.string.sleeping_since),
            DataModel.formatTimestamp(start, DataModel.getCompactView())
            //)


            /**
            startStop.contentDescription = getString(R.string.stop)
            startStop.setImageResource(R.drawable.ic_stop)
            startStopText.text = getString(R.string.stop)

            // Back to default, ~red.
            startStop.backgroundTintList = createColorStateList(R.color.colorFabAccent)
            startStopLayout.backgroundTintList = startStop.backgroundTintList
             **/
            return
        }

        // Set to custom, ~blue.
        startStop.backgroundTintList = createColorStateList(R.color.colorFabPrimary)
        startStopLayout.backgroundTintList = startStop.backgroundTintList
    }

    override fun onSensorChanged(event: SensorEvent?) {


        startStopLayout.setOnClickListener {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val lightValue = event.values[0]
                if (lightValue < 100f) {

                    iniciarSueño()
                    //onClick(view?.findViewById(R.id.start_stop_layout))
                    //findNavController().navigate(R.id.action_bedtimeFragment_to_start_Sleep)
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("¡Alerta!")
                        .setMessage("Hay mucha luz, es posible que no puedas dormir cómodamente. ¿Estás seguro de querer continuar?")
                        .setPositiveButton("Continuar") { dialog, _ ->
                            // Si el usuario elige continuar, avanza al siguiente fragmento
                            dialog.dismiss()
                            iniciarSueño()
                            //onClick(view?.findViewById(R.id.start_stop_layout))
                            //findNavController().navigate(R.id.action_bedtimeFragment_to_start_Sleep)
                        }
                        .setNegativeButton("Cancelar") { dialog, _ ->
                            // Si el usuario elige cancelar, se queda en la misma pantalla
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

}