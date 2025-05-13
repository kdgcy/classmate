package com.neu.classmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FocusTimerViewModel : ViewModel() {
    private val _timeLeft = MutableStateFlow(25 * 60)
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _isBreak = MutableStateFlow(false)
    val isBreak: StateFlow<Boolean> = _isBreak

    private val _cycleCount = MutableStateFlow(1)
    val cycleCount: StateFlow<Int> = _cycleCount

    private var timerJob: Job? = null

    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true

        timerJob = viewModelScope.launch {
            while (_isRunning.value && _timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
            }

            if (_timeLeft.value == 0 && _isRunning.value) {
                _isRunning.value = false  // Stop current session

                if (_isBreak.value) {
                    if (_cycleCount.value < 4) {
                        _cycleCount.value += 1
                        _timeLeft.value = 25 * 60
                        _isBreak.value = false
                    } else {
                        // All cycles complete
                        return@launch
                    }
                } else {
                    _timeLeft.value = 5 * 60
                    _isBreak.value = true
                }

                // Restart for the next session
                startTimer()
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isRunning.value = false
        timerJob?.cancel()
        _isBreak.value = false
        _cycleCount.value = 1
        _timeLeft.value = 25 * 60
    }
}
