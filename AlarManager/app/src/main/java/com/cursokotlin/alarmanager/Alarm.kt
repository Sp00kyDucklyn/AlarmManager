package com.cursokotlin.alarmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.alarmanager.model.AlarmData
import com.cursokotlin.alarmanager.view.AlarmAdapter2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat


class Alarm : Fragment() {

    private lateinit var addsBtn: FloatingActionButton
    private lateinit var recv: RecyclerView
    private lateinit var userList:ArrayList<AlarmData>
    private lateinit var userAdapter: AlarmAdapter2
    private lateinit var picker:MaterialTimePicker


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_alarm, container, false)

        val btnAddAlarm:ImageButton =root.findViewById<ImageButton>(R.id.addAlarmButton)
        btnAddAlarm.setOnClickListener(){
            showTimePicker()
        }

        /**set List*/

        userList = ArrayList()
        /**set find Id*/
        addsBtn = root.findViewById(R.id.addingBtn);
        recv = root.findViewById(R.id.mRecycler);
        /**set Adapter*/
        userAdapter = AlarmAdapter2(requireContext(),userList)
        /**setRecycler view Adapter*/
        recv.layoutManager = LinearLayoutManager(requireContext())
        recv.adapter = userAdapter
        /**set Dialog*/
        addsBtn.setOnClickListener { addInfo() }


        return root
    }

    private fun showTimePicker() {

        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Alarm Time")
            .build()

        picker.show(childFragmentManager,"1")

        picker.addOnPositiveButtonClickListener {

            if(picker.hour >12){


            }

        }


    }


    private fun addInfo() {
        val inflter = LayoutInflater.from(requireContext())
        val v = inflter.inflate(R.layout.add_alarma2,null)
        /**set view*/
        val userName = v.findViewById<EditText>(R.id.userName)
        val userNo = v.findViewById<EditText>(R.id.userNo)

        val addDialog = AlertDialog.Builder(requireContext())

        addDialog.setView(v)
        addDialog.setPositiveButton("Ok"){
                dialog,_->
            val names = userName.text.toString()
            val number = userNo.text.toString()
            userList.add(AlarmData("Name: $names","Mobile No. : $number"))
            userAdapter.notifyDataSetChanged()
            Toast.makeText(requireContext(),"Adding User Information Success", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        addDialog.setNegativeButton("Cancel"){
                dialog,_->
            dialog.dismiss()
            Toast.makeText(requireContext(),"Cancel", Toast.LENGTH_SHORT).show()

        }
        addDialog.create()
        addDialog.show()
    }


}