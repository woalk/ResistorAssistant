/*
 * Resistor Assistant - Resistance Calculator
 * Copyright (C) 2018 Alexander Köster (Woalk Software)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.woalk.apps.resistorassistant

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer

class SpeechRecognitionListener(private val listener: ResultListener): RecognitionListener {
    companion object {
        const val ERROR_CANCELED = -1
    }

    var isListening = false

    override fun onReadyForSpeech(params: Bundle?) {
        listener.onStartSpeechRecognition()
        isListening = true
    }

    override fun onRmsChanged(rmsdB: Float) {
        // ignore
    }

    override fun onBufferReceived(buffer: ByteArray?) {
        // ignore
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // ignore
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
        // ignore
    }

    override fun onBeginningOfSpeech() {
        // ignore
    }

    override fun onEndOfSpeech() {
        // ignore
    }

    override fun onError(error: Int) {
        when (error) {
            SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
            ERROR_CANCELED -> {
                listener.onStopSpeechRecognition()
                isListening = false
            }
        }
        logIfDebug("[Voice] onError: $error")
    }

    override fun onResults(results: Bundle?) {
        listener.onStopSpeechRecognition()
        isListening = false
        val resultArray: List<String> = results?.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION) ?: emptyList()
        if (BuildConfig.DEBUG) {
            android.util.Log.d(LOG_TAG, resultArray.joinToString(" | "))
        }
        listener.onResults(resultArray)
    }

    interface ResultListener {
        fun onResults(results: List<String>)
        fun onStartSpeechRecognition()
        fun onStopSpeechRecognition()
    }
}
