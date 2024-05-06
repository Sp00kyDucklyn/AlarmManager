package com.cursokotlin.alarmanager

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
                             else -> false
                         }
                     }
    }
    fun goToBedtimeFragment() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.bedtimeFragment)
    }
}
