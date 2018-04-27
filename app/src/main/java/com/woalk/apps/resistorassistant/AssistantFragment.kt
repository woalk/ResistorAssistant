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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_assistant.*
import android.speech.RecognizerIntent
import android.content.Intent
import android.os.Handler
import android.preference.PreferenceManager
import android.speech.tts.TextToSpeech
import java.util.*


class AssistantFragment : AbsCalculationsFragment(), SpeechRecognitionListener.ResultListener,
        TextToSpeech.OnInitListener {
    companion object {
        private const val PERMISSION_REQUEST_RECORD_AUDIO = 0
        private const val TTS_REQUEST_CHECK_CODE = 1
        private const val PREF_ASSISTANT_TTS = "assistant_tts"
    }

    private var reqListener: RequestCalculationListener? = null
    private val recognizer by lazy { Recognizer(context!!) }
    private var speechRecognizer: SpeechRecognizer? = null
    private val speechListener = SpeechRecognitionListener(this)
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var ttsId = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assistant, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is RequestCalculationListener) {
            reqListener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement RequestCalculationListener.")
        }

        prepareSpeechRecognizer()
        prepareTTS()
    }

    override fun onLoadWindowSoftInputMode() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context).also {
                it.stackFromEnd = true
            }
            adapter = RecyclerAdapter(reqListener!!)
            adapter.notifyDataSetChanged()
        }
        message_input.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND && v.text.isNotBlank()) {
                val text = v.text.toString()
                listener?.onCalculation(
                        recognizer.recognize(text) ?: CalculationUnknown(text))
                recycler.adapter.notifyItemRangeInserted(
                        recycler.adapter.itemCount - 2, 2)
                recycler.smoothScrollToPosition(recycler.adapter.itemCount - 1)
                v.text = null
            }
            false
        }
        button_voice.setOnClickListener { _ ->
            if (speechListener.isListening) {
                speechRecognizer!!.cancel()
                speechListener.onError(SpeechRecognitionListener.ERROR_CANCELED)
            } else {
                val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                speechRecognizer?.startListening(i)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        recycler.scrollToPosition(recycler.adapter.itemCount - 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        shutdownSpeechRecognizer()
        shutdownTTS()
    }

    private fun prepareSpeechRecognizer() {
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestAudioPermission()
            Handler().post {
                button_voice.isEnabled = false
            }
        } else {
            startupSpeechRecognizer()
        }
    }

    private fun requestAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!,
                        Manifest.permission.RECORD_AUDIO)) {
            AlertDialog.Builder(context!!)
                    .setTitle(R.string.alert_audio_permission_title)
                    .setMessage(R.string.alert_audio_permission_message)
                    .setPositiveButton(android.R.string.ok, { _, _ ->
                        forceRequestAudioPermission()
                    })
                    .setNegativeButton(R.string.alert_audio_permission_later, null)
                    .show()
        } else {
            forceRequestAudioPermission()
        }
    }

    private fun forceRequestAudioPermission() {
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_RECORD_AUDIO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    button_voice.isEnabled = true
                    startupSpeechRecognizer()
                } else {
                    button_voice.isEnabled = false
                }
            } else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun startupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context!!)) {
            AlertDialog.Builder(context!!)
                    .setTitle(R.string.alert_speech_unavailable_title)
                    .setMessage(R.string.alert_speech_unavailable_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            Handler().post {
                button_voice.isEnabled = false
            }
        } else { // recognition is available
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context!!)
            speechRecognizer!!.setRecognitionListener(speechListener)
        }
    }

    private fun shutdownSpeechRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    //region SpeechRecognitionListener.ResultListener
    override fun onResults(results: List<String>) {
        for (result in results) {
            val calc = recognizer.recognize(result)
            if (calc != null) {
                listener?.onCalculation(calc)
                recycler.adapter.notifyItemRangeInserted(
                        recycler.adapter.itemCount - 2, 2)
                recycler.smoothScrollToPosition(recycler.adapter.itemCount - 1)
                speakResult(calc)
                return // nothing below will be executed
            } // else: continue
        }
        // not returned in loop, did not find result
        val calc = CalculationUnknown(
                if (results.isNotEmpty()) {
                    results.first()
                } else {
                    getString(R.string.voice_recognition_no_answer)
                }
        )
        listener?.onCalculation(calc)
        recycler.adapter.notifyItemRangeInserted(
                recycler.adapter.itemCount - 2, 2)
        recycler.smoothScrollToPosition(recycler.adapter.itemCount - 1)
        speakResult(calc)
    }

    override fun onStartSpeechRecognition() {
        message_input.visibility = View.GONE
        button_voice.setImageResource(R.drawable.ic_mic_off_black_24dp)
        label_voice_listening.visibility = View.VISIBLE
    }

    override fun onStopSpeechRecognition() {
        label_voice_listening.visibility = View.GONE
        button_voice.setImageResource(R.drawable.ic_keyboard_voice_black_24dp)
        message_input.visibility = View.VISIBLE
    }
    //endregion

    private fun prepareTTS() {
        // check for TTS availability
        val checkIntent = Intent()
        checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        startActivityForResult(checkIntent, TTS_REQUEST_CHECK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TTS_REQUEST_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // TTS available
                tts = TextToSpeech(context, this)
            } else {
                // missing data, install it
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.ERROR) {
            ttsReady = false
            try {
                tts?.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            tts = null
            return
        } else if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
        }
    }

    private fun speakResult(calc: Calculation) {
        if (ttsReady && PreferenceManager.getDefaultSharedPreferences(context)
                        .getBoolean(PREF_ASSISTANT_TTS, true)) {
            tts!!.language = Locale.forLanguageTag(getString(R.string.voice_locale))
            tts!!.speak(calc.getSpeechOutput(context!!), TextToSpeech.QUEUE_FLUSH, null,
                    ttsId++.toString())
        }
    }

    private fun shutdownTTS() {
        ttsReady = false
        tts?.shutdown()
        tts = null
    }

    private class RecyclerAdapter(private val listener: RequestCalculationListener) :
            RecyclerView.Adapter<ViewHolder>() {
        companion object {
            private const val TYPE_QUESTION_COLORS = 0
            private const val TYPE_QUESTION_NUMBER = 1
            private const val TYPE_ANSWER_COLORS = 2
            private const val TYPE_ANSWER_NUMBER = 3
            private const val TYPE_QUESTION_UNKNOWN = 4
            private const val TYPE_ANSWER_UNKNOWN = 5
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                TYPE_QUESTION_COLORS -> {
                    val view = layoutInflater.inflate(R.layout.item_question_colors, parent, false)
                    ViewHolderColors(view)
                }
                TYPE_QUESTION_NUMBER -> {
                    val view = layoutInflater.inflate(R.layout.item_question_number, parent, false)
                    ViewHolderNumber(view)
                }
                TYPE_ANSWER_COLORS  -> {
                    val view = layoutInflater.inflate(R.layout.item_answer_colors, parent, false)
                    ViewHolderColors2(view)
                }
                TYPE_ANSWER_NUMBER -> {
                    val view = layoutInflater.inflate(R.layout.item_answer_number, parent, false)
                    ViewHolderNumber(view)
                }
                TYPE_QUESTION_UNKNOWN -> {
                    val view = layoutInflater.inflate(R.layout.item_question_number, parent, false)
                    ViewHolderNumber(view)
                }
                TYPE_ANSWER_UNKNOWN -> {
                    val view = layoutInflater.inflate(R.layout.item_answer_number, parent, false)
                    ViewHolderNumber(view)
                }
                else -> ViewHolder(null)
            }
        }

        override fun getItemViewType(position: Int): Int {
            val (type, _) = getItemForView(position)
            return type
        }

        private fun getItemForView(position: Int): Pair<Int, Calculation> {
            val item = listener.onCalculationRequest()[position / 2]
            return Pair(if (position % 2 == 0) {
                if (item is CalculationUnknown) {
                    TYPE_QUESTION_UNKNOWN
                } else {
                    if (item.backwards) {
                        TYPE_QUESTION_NUMBER
                    } else {
                        TYPE_QUESTION_COLORS
                    }
                }
            } else {
                if (item is CalculationUnknown) {
                    TYPE_ANSWER_UNKNOWN
                } else {
                    if (item.backwards) {
                        TYPE_ANSWER_COLORS
                    } else {
                        TYPE_ANSWER_NUMBER
                    }
                }
            }, item)
        }

        override fun getItemCount(): Int {
            return listener.onCalculationRequest().size * 2
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val (type, item) = getItemForView(position)
            when (type) {
                TYPE_QUESTION_COLORS -> {
                    val h = holder as ViewHolderColors
                    h.resistor.applyColors(item.colors.first)
                    h.text.text = item.getColor1String(h.itemView.context)
                }
                TYPE_ANSWER_NUMBER -> {
                    val h = holder as ViewHolderNumber
                    h.text.text = item.getResistanceString()
                }
                TYPE_QUESTION_NUMBER -> {
                    val h = holder as ViewHolderNumber
                    h.text.text = item.getResistanceString()
                }
                TYPE_ANSWER_COLORS -> {
                    val h = holder as ViewHolderColors2
                    if (item.colors.first.isNotEmpty()) {
                        h.resistor1.applyColors(item.colors.first)
                        h.text1.text = item.getColor1String(h.itemView.context)
                        h.layout1.visibility = View.VISIBLE
                    } else {
                        h.layout1.visibility = View.GONE
                    }
                    if (item.colors.second.isNotEmpty()) {
                        h.resistor2.applyColors(item.colors.second)
                        h.text2.text = item.getColor2String(h.itemView.context)
                        h.layout2.visibility = View.VISIBLE
                    } else {
                        h.layout2.visibility = View.GONE
                    }
                    if (h.layout1.visibility == View.GONE && h.layout2.visibility == View.GONE) {
                        h.textNoColors.visibility = View.VISIBLE
                    } else {
                        h.textNoColors.visibility = View.GONE
                    }
                }
                TYPE_QUESTION_UNKNOWN -> {
                    val h = holder as ViewHolderNumber
                    h.text.text = item.getResistanceString()
                }
                TYPE_ANSWER_UNKNOWN -> {
                    val h = holder as ViewHolderNumber
                    h.text.text = holder.itemView.context.getString(R.string.message_unknown)
                }
            }
        }

    }

    interface RequestCalculationListener {
        fun onCalculationRequest(): List<Calculation>
    }

    private open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView)
    private class ViewHolderColors(itemView: View) : ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text)
        val resistor: ResistorView = itemView.findViewById(R.id.resistor1)
    }
    private class ViewHolderColors2(itemView: View) : ViewHolder(itemView) {
        val text1: TextView = itemView.findViewById(R.id.text1)
        val text2: TextView = itemView.findViewById(R.id.text2)
        val layout1: View = itemView.findViewById(R.id.layout_resistor1)
        val layout2: View = itemView.findViewById(R.id.layout_resistor2)
        val resistor1: ResistorView = itemView.findViewById(R.id.resistor1)
        val resistor2: ResistorView = itemView.findViewById(R.id.resistor2)
        val textNoColors: TextView = itemView.findViewById(R.id.text_no_colors)
    }
    private class ViewHolderNumber(itemView: View) : ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.text)
    }
}
