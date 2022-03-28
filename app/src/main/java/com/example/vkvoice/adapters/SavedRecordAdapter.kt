package com.example.vkvoice.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.vkvoice.R
import com.example.vkvoice.models.Record
import kotlinx.android.synthetic.main.item_saved_record.view.*

class SavedRecordAdapter : RecyclerView.Adapter<SavedRecordAdapter.SavedRecordViewsHolder>() {

    inner class SavedRecordViewsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewBackground = itemView.view_background
        val viewForeground = itemView.view_foreground
    }

    private val differCallback = object : DiffUtil.ItemCallback<Record>() {
        override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedRecordViewsHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_saved_record, parent, false)
        return SavedRecordViewsHolder(itemView)
    }

    override fun onBindViewHolder(holder: SavedRecordViewsHolder, position: Int) {
        val record = differ.currentList[position]
        holder.itemView.apply {
            nameOfRecord.text = record.title
            dateOfRecord.text = record.time

            if (state == "playing" || state == "pause") {
                recordDuration.text =
                    generateDuration(currentDuration) + " / " + generateDuration(barMaxProcess.toLong())
            } else {
                recordDuration.text = generateDuration(record.duration)
            }

            if (barProcess == 0) {
                progressBar.visibility = View.INVISIBLE
            } else {
                progressBar.visibility = View.VISIBLE
            }

            progressBar.max = barMaxProcess
            progressBar.progress = barProcess

            playStopButton.setOnClickListener {
                record.clickListener(position)
            }

            if (record.playStopButtonStatus) {
                playStopButton.supportBackgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.grey500)
                playStopButton.setImageResource(R.drawable.ic_pause)
            } else {
                playStopButton.supportBackgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.primary)
                playStopButton.setImageResource(R.drawable.ic_play)
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private var barProcess = 0
    fun setBarProcess(t: Int) {
        barProcess = t
    }

    private var barMaxProcess = 2000
    fun setBarMaxProcess(t: Int) {
        barMaxProcess = t
    }

    private var currentDuration = 0L
    fun setCurrentDuration(t: Int) {
        currentDuration = t.toLong()
    }

    private var state = ""
    fun setState(t: String) {
        state = t
    }

    fun generateDuration(recordDuration: Long): String {
        if (recordDuration >= 3600000L) {
            return (recordDuration / 3600000L).toString() + ":" + (recordDuration % 3600000L / 60000L).toString() + ":" + (recordDuration % 3600000L % 60000L / 1000L).toString()
        } else {
            return (recordDuration / 60000L).toString() + ":" + (recordDuration % 60000L / 1000L).toString()
        }
    }
}