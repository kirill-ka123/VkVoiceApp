package com.example.vkvoice.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.vkvoice.models.Record

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: Record): Long

    @Query("SELECT * FROM records")
    fun getRecordsLive(): LiveData<List<Record>>

    @Query("SELECT title FROM records")
    suspend fun getTitlesRecords(): List<String>

    @Delete
    suspend fun deleteRecord(location: Record)
}