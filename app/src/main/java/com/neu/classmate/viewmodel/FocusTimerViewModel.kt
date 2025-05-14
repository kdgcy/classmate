import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neu.classmate.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

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

    fun startTimer(context: Context) {
        if (_isRunning.value) return
        _isRunning.value = true

        timerJob = viewModelScope.launch {
            while (true) {
                if (_isRunning.value && _timeLeft.value > 0) {
                    delay(1000)
                    _timeLeft.value -= 1
                }

                if (_timeLeft.value == 0 && _isRunning.value) {
                    playAlarm(context)

                    if (_isBreak.value) {
                        if (_cycleCount.value < 4) {
                            _cycleCount.value += 1
                            _timeLeft.value = 25 * 60
                            _isBreak.value = false
                        } else {
                            _isRunning.value = false
                            break
                        }
                    } else {
                        _timeLeft.value = 5 * 60
                        _isBreak.value = true
                    }
                }

                if (!_isRunning.value) break
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

    private fun playAlarm(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }
}
