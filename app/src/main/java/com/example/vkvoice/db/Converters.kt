package com.example.vkvoice.db

import android.util.Log
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromListener(listener: (Int) -> Unit): String {
        return ""
    }

    @TypeConverter
    fun toListener(str: String): ((Int) -> Unit) {
        return {
            Log.d("qwert", "ничего")
        }
    }
}