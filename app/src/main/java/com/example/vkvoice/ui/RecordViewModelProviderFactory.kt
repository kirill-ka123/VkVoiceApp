package com.example.vkvoice.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vkvoice.repository.RecordRepository

class RecordViewModelProviderFactory(
    val recordRepository: RecordRepository
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RecordViewModel(recordRepository) as T
    }
}