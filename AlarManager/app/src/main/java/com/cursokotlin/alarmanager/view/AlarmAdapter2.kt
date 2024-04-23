package com.cursokotlin.alarmanager.view


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.cursokotlin.alarmanager.Alarm
import com.cursokotlin.alarmanager.R
import com.cursokotlin.alarmanager.model.AlarmData
import com.cursokotlin.alarmanager.model.State
import java.io.File


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
        //obtener el nombre del archivo de la URI del tono de alarma
        val fileName = if (currentItem.alarmTone != null) {
            context.getFileName(currentItem.alarmTone)
        } else {
            null
        }

        //mostrar solo el nombre del archivo en tvAlarmMusic
        holder.tvAlarmMusic.text = fileName.toString()

        if (currentItem.alarmState == State.ON) {
            holder.switchAlarm.isChecked = true
        } else {
            holder.switchAlarm.isChecked = false
        }

        //ajustar márgenes
        val layoutParams = holder.itemView.layoutParams as RecyclerView.LayoutParams
        layoutParams.setMargins(56, 16, 56, 16)
        holder.itemView.layoutParams = layoutParams

        holder.btnOptions.setOnClickListener {
            showPopupMenu(context, it, currentItem)
        }
    }

    fun showPopupMenu(context: Context,view: View, currentItem: AlarmData) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.show_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.editAlarm -> {
                    val editAlarm = Alarm()
                    editAlarm.editAlarm(context, currentItem.alarmId)
                    true
                }
                R.id.deleteAlarm -> {

                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount() = alarmList.size

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAlarmTime: TextView = itemView.findViewById(R.id.tvAlarmTime)
        val tvAlarmDays: TextView = itemView.findViewById(R.id.tvAlarmDays)
        val tvAlarmMusic: TextView = itemView.findViewById(R.id.tvAlarmMusic)
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val switchAlarm: Switch = itemView.findViewById(R.id.switchAlarm)
        val btnOptions: ImageButton = itemView.findViewById(R.id.btnOptions)
    }

    fun Context.getFileName(uri: Uri): String? = when(uri.scheme) {
        ContentResolver.SCHEME_CONTENT -> getContentFileName(uri)
        else -> uri.path?.let(::File)?.name
    }

    private fun Context.getContentFileName(uri: Uri): String? = runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
        }
    }.getOrNull()
}
