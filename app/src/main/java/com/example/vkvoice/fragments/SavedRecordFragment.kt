package com.example.vkvoice.fragments

import android.app.Activity
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.vkvoice.MainActivity
import com.example.vkvoice.R
import com.example.vkvoice.adapters.SavedRecordAdapter
import com.example.vkvoice.ui.RecordViewModel
import com.example.vkvoice.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.item_saved_record.*
import kotlinx.android.synthetic.main.saved_record_fragment.*


class SavedRecordFragment : Fragment(R.layout.saved_record_fragment) {
    private lateinit var viewModel: RecordViewModel
    private lateinit var savedRecordAdapter: SavedRecordAdapter
    private lateinit var mainHandler: Handler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).recordViewModel
        setupRecyclerView(view)
        mainHandler = Handler(Looper.getMainLooper())
        viewModel.initMediaPlayer()

        viewModel.getRecords().observe(viewLifecycleOwner, Observer { records ->
            if (records.isEmpty()) {
                previewLayout.visibility = View.VISIBLE
                title_saved_record_fragment.visibility = View.INVISIBLE
            } else {
                previewLayout.visibility = View.INVISIBLE
                title_saved_record_fragment.visibility = View.VISIBLE
            }
            savedRecordAdapter.notifyItemChanged(viewModel.currentPosition)
            records.forEach { record ->
                record.playStopButtonStatus = false
                record.clickListener = { position ->
                    viewModel.playRecord(record.filePath, position)
                    record.playStopButtonStatus = true
                    enableSeek(true)
                    savedRecordAdapter.notifyItemChanged(position)
                }
            }
            savedRecordAdapter.differ.submitList(records)
        })

        viewModel.progress.observe(viewLifecycleOwner, Observer { progress ->
            savedRecordAdapter.setBarMaxProcess(viewModel.recordDurationPlay)
            savedRecordAdapter.setBarProcess(progress)
            savedRecordAdapter.setCurrentDuration(progress)
            if (progress == 0) {
                savedRecordAdapter.setState("stop")
            } else savedRecordAdapter.setState("playing")
            savedRecordAdapter.notifyItemChanged(viewModel.currentPosition)
        })

        viewModel.playingState.observe(viewLifecycleOwner, Observer { playingState ->
            when (playingState) {
                is Resource.Playing -> {
                    Log.d("qwert", "play")
                    val records = savedRecordAdapter.differ.currentList
                    if (records.isNotEmpty()) {
                        records[viewModel.currentPosition].clickListener = { position ->
                            viewModel.pauseRecord()
                            records[viewModel.currentPosition].playStopButtonStatus = false
                            enableSeek(false)
                            savedRecordAdapter.notifyItemChanged(position)
                        }
                    }
                }
                is Resource.Pause -> {
                    Log.d("qwert", "Pause")
                    val records = savedRecordAdapter.differ.currentList
                    if (records.isNotEmpty()) {
                        records[viewModel.currentPosition].clickListener = { position ->
                            viewModel.resumeRecord()
                            records[viewModel.currentPosition].playStopButtonStatus = true
                            enableSeek(true)
                            savedRecordAdapter.notifyItemChanged(position)
                        }
                    }
                }
                is Resource.Stop -> {
                    Log.d("qwert", "Stop")
                    val records = savedRecordAdapter.differ.currentList
                    if (viewModel.currentPosition != -1 && records.isNotEmpty()) {
                        records[viewModel.currentPosition].playStopButtonStatus = false
                        enableSeek(false)
                        savedRecordAdapter.notifyItemChanged(viewModel.currentPosition)
                        records[viewModel.currentPosition].clickListener = { position ->
                            viewModel.playRecord(records[position].filePath, position)
                            records[position].playStopButtonStatus = true
                            enableSeek(true)
                            savedRecordAdapter.notifyItemChanged(position)
                        }
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), playingState.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })


        viewModel.recordingState.observe(viewLifecycleOwner, Observer { recordingState ->
            when (recordingState) {
                is Resource.Recording -> {
                    Log.d("qwert", "Recording")
                    clearFocusOnEditText()
                    inputNameLayout.visibility = View.INVISIBLE
                    recordButton.setImageResource(R.drawable.ic_stop)
                }
                is Resource.Done -> {
                    Log.d("qwert", "Done")
                    recordButton.setImageResource(R.drawable.ic_mic)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), recordingState.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })

        ok.setOnClickListener {
            viewModel.stopPlaying()
            viewModel.getTitlesRecords(inputName.text.toString())
        }

        cancel.setOnClickListener {
            clearFocusOnEditText()
            inputNameLayout.visibility = View.INVISIBLE
        }

        recordButton.setOnClickListener {

            if (viewModel.recordingButtonStatus == "mic") {
                inputName.text.clear()
                inputNameLayout.visibility = View.VISIBLE
                focusOnEditText()
            } else if (viewModel.recordingButtonStatus == "stop") {
                viewModel.recordingButtonStatus = "mic"
                viewModel.stopRecording()
            }
        }

        viewModel.allowedNameStatus.observe(viewLifecycleOwner, Observer { status ->
            if (!status) {
                Toast.makeText(
                    requireContext(),
                    "Запись с таким именем уже существует",
                    Toast.LENGTH_SHORT
                )
                    .show()
                viewModel.allowedNameStatus.postValue(true)
            }
        })
    }

    private fun enableSeek(bool: Boolean) {
        val runner = object : Runnable {
            override fun run() {
                viewModel.getLiveProgress()
                mainHandler.postDelayed(this, 5)
            }
        }
        if (bool) {
            mainHandler.post(runner)
        } else {
            if (viewModel.playingState.value is Resource.Stop) {
                viewModel.setProgress(0)
            }
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun setupRecyclerView(view: View) {
        (rvSavedRecords.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        savedRecordAdapter = SavedRecordAdapter()
        rvSavedRecords.apply {
            adapter = savedRecordAdapter
            layoutManager = LinearLayoutManager(activity)
        }
        ItemTouchHelper(getItemTouchHelper(savedRecordAdapter, view)).attachToRecyclerView(
            rvSavedRecords
        )
    }

    private fun focusOnEditText() {
        inputName.isFocusableInTouchMode = true
        inputName.isFocusable = true
        inputName.requestFocus()
        val inputMethodManager =
            context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(inputName, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun clearFocusOnEditText() {
        val inputMethodManager =
            context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getItemTouchHelper(
        savedRecordAdapter: SavedRecordAdapter,
        view: View
    ): ItemTouchHelper.SimpleCallback {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.stopPlaying()
                enableSeek(false)
                val position = viewHolder.adapterPosition
                val record = savedRecordAdapter.differ.currentList[position]
                viewModel.deleteRecord(record)
                Snackbar.make(view, "Запись успешно удалена", Snackbar.LENGTH_LONG).apply {
                    setAction("Отмена") {
                        viewModel.saveRecord(record)
                    }
                    show()
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (viewHolder != null) {
                    val foregroundView: View =
                        (viewHolder as SavedRecordAdapter.SavedRecordViewsHolder).viewForeground
                    getDefaultUIUtil().onSelected(foregroundView)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val foregroundView: View =
                    (viewHolder as SavedRecordAdapter.SavedRecordViewsHolder).viewForeground
                getDefaultUIUtil().onDraw(
                    c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive
                )
            }

            override fun onChildDrawOver(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder?,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val foregroundView: View =
                    (viewHolder as SavedRecordAdapter.SavedRecordViewsHolder).viewForeground
                getDefaultUIUtil().onDrawOver(
                    c, recyclerView, foregroundView, dX, dY,
                    actionState, isCurrentlyActive
                )
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)

                val foregroundView: View =
                    (viewHolder as SavedRecordAdapter.SavedRecordViewsHolder).viewForeground
                getDefaultUIUtil().clearView(foregroundView)
            }
        }
        return itemTouchHelperCallback
    }

    override fun onStop() {
        super.onStop()
        Log.d("qwert", "onStop")
        viewModel.stopPlaying()
        enableSeek(false)
    }
}