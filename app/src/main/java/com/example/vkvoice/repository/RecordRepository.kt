package com.example.vkvoice.repository

import com.example.vkvoice.db.RecordDatabase
import com.example.vkvoice.models.Record

class RecordRepository(val db: RecordDatabase) {

    suspend fun upsert(record: Record) =
        db.getRecordDao().upsert(record)

    fun getRecordsLive() =
        db.getRecordDao().getRecordsLive()

    suspend fun deleteRecord(record: Record) =
        db.getRecordDao().deleteRecord(record)

    suspend fun getTitlesRecords() =
        db.getRecordDao().getTitlesRecords()
}