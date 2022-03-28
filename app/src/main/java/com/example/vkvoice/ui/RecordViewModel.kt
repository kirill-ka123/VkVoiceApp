package com.example.vkvoice.ui

import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vkvoice.models.Record
import com.example.vkvoice.repository.RecordRepository
import com.example.vkvoice.util.Resource
import kotlinx.android.synthetic.main.saved_record_fragment.*
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class RecordViewModel(private val recordRepository: RecordRepository) : ViewModel() {
    private val _recordingState: MutableLiveData<Resource<Record>> = MutableLiveData()
    val recordingState: LiveData<Resource<Record>>
        get() = _recordingState
    var recordingButtonStatus = "mic"

    private val _playingState: MutableLiveData<Resource<Record>> = MutableLiveData(Resource.Stop())
    val playingState: LiveData<Resource<Record>>
        get() = _playingState
    var playRecordingButtonStatus = "stop"

    private val _progress: MutableLiveData<Int> = MutableLiveData()
    val progress: LiveData<Int>
        get() = _progress

    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var filePath: String
    private lateinit var mediaPlayer: MediaPlayer

    private var startTime = 0L

    var currentPosition: Int = -1

    var allowedNameStatus: MutableLiveData<Boolean> = MutableLiveData()
    var recordName: String = "no_name"

    var recordDuration = 0L
    var recordDurationPlay = 0

    var loginState = false

    private val list: LiveData<List<Record>> = recordRepository.getRecordsLive()


    fun startRecording() {
        viewModelScope.launch {
            _recordingState.value = Resource.Recording()
            record()
        }
    }

    private fun record() {
        filePath =
            Environment.getExternalStorageDirectory().absolutePath + "/" + recordName + ".3gp"

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.setOutputFile(filePath)
        try {
            mediaRecorder.prepare()
            startTime = System.currentTimeMillis()
            mediaRecorder.start()
        } catch (e: IOException) {
            _recordingState.value = Resource.Error("IOException on recording")
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            _recordingState.value = Resource.Error("IllegalStateException on recording")
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            val record = stopRecord()
            _recordingState.value = Resource.Done(record)
        }
    }

    private fun stopRecord(): Record {
        try {
            recordDuration = System.currentTimeMillis() - startTime
            mediaRecorder.stop()
        } catch (e: IOException) {
            _recordingState.value = Resource.Error("IOException on stop recording")
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            _recordingState.value = Resource.Error("IllegalStateException on stop recording")
            e.printStackTrace()
        }

        val record =
            Record(
                title = recordName,
                time = generateDate(),
                filePath = filePath,
                clickListener = {},
                duration = recordDuration
            )
        saveRecord(record)
        return record
    }

    fun saveRecord(record: Record) = viewModelScope.launch {
        recordRepository.upsert(record)
    }

    fun getRecords() = recordRepository.getRecordsLive()

    fun getTitlesRecords(title: String) = viewModelScope.launch {
        recordRepository.getTitlesRecords().forEach {
            if (it == title) {
                allowedNameStatus.postValue(false)
                return@launch
            }
        }
        recordingButtonStatus = "stop"
        recordName = title
        startRecording()
    }

    fun deleteRecord(record: Record) = viewModelScope.launch {
        recordRepository.deleteRecord(record)
    }

    fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            _playingState.value = Resource.Stop()
        }
    }

    fun playRecord(filePath: String, position: Int) {
        if (mediaPlayer.isPlaying || _playingState.value is Resource.Pause) {
            mediaPlayer.stop()
            _playingState.value = Resource.Stop()
        }
        try {
            initMediaPlayer()
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            currentPosition = position
            recordDurationPlay = mediaPlayer.duration
            _playingState.value = Resource.Playing()
        } catch (e: java.lang.IllegalStateException) {
            _playingState.value = Resource.Error("IllegalStateException on play record")
            e.printStackTrace()
        } catch (e: IOException) {
            _playingState.value = Resource.Error("IOException on play record")
            e.printStackTrace()
        }
    }

    fun stopPlaying() {
        if (mediaPlayer.isPlaying || _playingState.value is Resource.Pause) {
            mediaPlayer.stop()
            _playingState.value = Resource.Stop()
        }
    }

    fun getLiveProgress() {
        _progress.value = mediaPlayer.currentPosition
    }

    fun setProgress(process: Int) {
        _progress.value = process
    }

    fun resumeRecord() {
        // risky !! used here
        // null pointer
        mediaPlayer.seekTo(progress.value!!)
        mediaPlayer.start()
        if (mediaPlayer.isPlaying) {
            _playingState.value = Resource.Playing()
        }
    }

    fun pauseRecord() {
        if (mediaPlayer.isPlaying) {
            getLiveProgress()
            mediaPlayer.pause()
        }
        _playingState.value = Resource.Pause()
    }

    private fun generateDate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")
            current.format(formatter)
        } else {
            val date = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy в HH:mm")
            formatter.format(date)
        }
    }
}