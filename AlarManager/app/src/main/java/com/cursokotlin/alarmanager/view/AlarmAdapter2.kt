package com.cursokotlin.alarmanager.view


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.alarmanager.R
import com.cursokotlin.alarmanager.model.AlarmData

class AlarmAdapter2(private val context: Context, private val alarmList: List<AlarmData>) :
    RecyclerView.Adapter<AlarmAdapter2.AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val currentItem = alarmList[position]
        holder.tvAlarmTime.text = currentItem.alarmHour
        holder.tvAlarmDays.text = currentItem.alarmDays
        //holder.tvAlarmMusic.text = currentItem.alarmMusic

        //ajustar márgenes
        val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
        layoutParams.setMargins(56, 36, 56, 36)
        holder.itemView.layoutParams = layoutParams
    }

    override fun getItemCount() = alarmList.size

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAlarmTime: TextView = itemView.findViewById(R.id.tvAlarmTime)
        val tvAlarmDays: TextView = itemView.findViewById(R.id.tvAlarmDays)
        val tvAlarmMusic: TextView = itemView.findViewById(R.id.tvAlarmMusic)
    }
}
