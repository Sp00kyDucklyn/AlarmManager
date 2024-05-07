package com.cursokotlin.alarmanager

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.alarmanager.model.AlarmData
import com.cursokotlin.alarmanager.view.AlarmAdapter2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        

              bottomNav.setupWithNavController(navController)
             
                     // Configurar el listener para manejar la navegación cuando se hace clic en un elemento del menú
                     bottomNav.setOnNavigationItemSelectedListener { menuItem ->
                         when (menuItem.itemId) {
                             R.id.menu_Alarm -> {
                                 // Navegar al fragmento de alarma
                                 navController.navigate(R.id.alarmFragment)
                                 true
                             }
                             R.id.menu_Bedtime -> {
                                 // Navegar al fragmento de hora de dormir
                                 navController.navigate(R.id.bedtimeFragment)
                                 true
                             }
                             R.id.menu_Graphs -> {
                                 //startActivity(Intent(this, GraphsActivity::class.java))
                                 navController.navigate(R.id.graphsFragment)
                                 true
                             }
                             else -> false

                         }
                     }
       // requestNotificationPermissions()
    }
    fun goToBedtimeFragment() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.bedtimeFragment)
    }
    private fun requestNotificationPermissions() {
        println("pip")
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            println("po")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_NOTIFICATION_POLICY)) {
                // Si el usuario ha rechazado el permiso previamente, muestra un diálogo explicativo
                println("poop")
                showPermissionExplanationDialog()
            } else {
                // Si es la primera vez que se solicita el permiso o si el usuario marcó "No volver a preguntar", solicita el permiso directamente
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY), 1)
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        println("tilin")
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("This app requires notification permission to function properly.")
            .setPositiveButton("OK") { _, _ ->
                // Cuando el usuario hace clic en Aceptar, solicita el permiso
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY), 1)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Cuando el usuario hace clic en Cancelar, cierra el diálogo
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
