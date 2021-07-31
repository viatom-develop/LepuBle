package com.lepu.lepuble.views

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lepu.lepuble.R
import com.lepu.lepuble.ble.BleLogs
import com.lepu.lepuble.objs.BleLogItem
import com.lepu.lepuble.utils.bytesToHex

class LogAdapter(mContext: Context, private val logs: ArrayList<BleLogItem>) :
    RecyclerView.Adapter<LogAdapter.ViewHolder>() {

    private var context: Context = mContext

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val type: TextView
        val content: TextView

        init {
            type = view.findViewById(R.id.log_type)
            content = view.findViewById(R.id.log_content)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_ble_log, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (logs[position].type == BleLogItem.SEND) {
            holder.type.text = BleLogItem.SEND
            holder.type.setTextColor(context.getColor(R.color.text_green))
        } else {
            holder.type.text = BleLogItem.RECEIVE
            holder.type.setTextColor(context.getColor(R.color.text_yellow))
        }

        holder.content.text = bytesToHex(logs[position].content)
    }

    override fun getItemCount(): Int {
        return logs.size
    }
}