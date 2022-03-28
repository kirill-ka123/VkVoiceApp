package com.example.vkvoice.models

import android.widget.ExpandableListView
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.vkvoice.db.Converters

@Entity(tableName = "records")
@TypeConverters(Converters::class)
data class Record(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    var title: String,
    var time: String,
    var filePath: String,
    var duration: Long,
    // true - playing
    var playStopButtonStatus: Boolean = false,
    var clickListener: ((Int) -> Unit)
)