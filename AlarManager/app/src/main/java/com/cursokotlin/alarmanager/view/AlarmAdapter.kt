package com.cursokotlin.alarmanager.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.alarmanager.R
import com.cursokotlin.alarmanager.model.AlarmData

class AlarmAdapter(val c:Context,val alarmList:ArrayList<AlarmData>):RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>(){

    inner class AlarmViewHolder(val v: View):RecyclerView.ViewHolder(v){
        val name = v.findViewById<TextView>(R.id.title)
        val mbNum = v.findViewById<TextView>(R.id.subTitle)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.item_alarm,parent,false)
        return AlarmViewHolder(v)
    }

    override fun getItemCount(): Int {
        return alarmList.size
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val newList = alarmList[position]
        holder.name.text = newList.alarmHour
        holder.mbNum.text = newList.alarmDays
    }

}