package com.cursokotlin.alarmanager

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
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.mikepenz.iconics.Iconics
import java.util.Calendar

class Start_Sleep : Fragment() {

    private lateinit var startStop: FloatingActionButton
    private lateinit var startStopText: TextView
    private lateinit var stopSleep: TextView
    private lateinit var bedtime: Bedtime
    private lateinit var stopLayout: LinearLayout
    private lateinit var viewModel: MainViewModel

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            if (it.getBooleanExtra("startStop", false)) {
                //onClick(stopLayout)
                it.removeExtra("startStop")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_start__sleep, container, false)

        startStopText = root.findViewById(R.id.start_stop_text)
        stopSleep = root.findViewById(R.id.stop_sleep)
        startStop = root.findViewById(R.id.start_stop)
        stopLayout = root.findViewById(R.id.stop_layout)

        bedtime = Bedtime()

        val dndManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(requireContext().applicationContext)
        val dndEnabled = preferences.getBoolean("enable_dnd", false)



        startStop.contentDescription = getString(R.string.stop)
        startStop.setImageResource(R.drawable.ic_stop)
        startStopText.text = getString(R.string.stop)

        // Back to default, ~red.
        startStop.backgroundTintList = createColorStateList(R.color.colorFabAccent)
        stopLayout.backgroundTintList = startStop.backgroundTintList

        stopLayout.setOnClickListener {
            // Llama al método para detener el sueño en el Fragmento Bedtime
            //(requireActivity() as Bedtime).onStop()
            // Regresa al Fragmento Bedtime
            //onClick()

            if (DataModel.start != null && DataModel.stop != null) {
                // When user stops tracking sleep
                if (dndEnabled && dndManager.isNotificationPolicyAccessGranted) {
                    // Restore Do Not Disturb status when user started tracking
                    val filterAll = NotificationManager.INTERRUPTION_FILTER_ALL
                    dndManager.setInterruptionFilter(preferences.getInt("current_dnd", filterAll))
                }

                //return
            }
            //onClick(view?.findViewById(R.id.stop_layout))
            findNavController().navigate(R.id.action_start_Sleep_to_bedtimeFragment)
        }

        //handleIntent(requireActivity().intent)

        return root
    }


    private fun createColorStateList(color: Int): ColorStateList {
        return ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    /**
    override fun onStart() {
        super.onStart()
        val intent = Intent(requireContext(), MainService::class.java)
        requireActivity().stopService(intent)
        // Obtén una instancia del FragmentoB
        val fragmentManager = requireActivity().supportFragmentManager
        val recyclerView = fragmentManager.findFragmentById(R.id.sleeps) as? Bedtime

        recyclerView?.onStart()
        //val recyclerView = findViewById<RecyclerView>(R.id.sleeps)
        //recyclerView.findViewHolderForAdapterPosition(0)?.let {
            // Since the adapter unconditionally gets assigned in `onCreate()`
            // it shouldn't be necessary to consider fixing it here.
            // If it is null at this point a lot more must have gone wrong as well.
          //  recyclerView.adapter?.onBindViewHolder(it, 0)
        }




    override fun onClick(v: View?) {
        when (view?.id) {
            R.id.stop_layout -> {
                if (DataModel.start != null && DataModel.stop == null) {
                    DataModel.stop = Calendar.getInstance().time
                    viewModel.stopSleep(requireContext().applicationContext, requireContext().contentResolver)
                }
            }
        }
    }
    **/
}