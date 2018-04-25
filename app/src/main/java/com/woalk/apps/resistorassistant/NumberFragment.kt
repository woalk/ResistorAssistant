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
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.fragment_number.*

class NumberFragment : AbsCalculationsFragment() {
    private var lastResult1: List<Color>? = null
    private var lastResult2: List<Color>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_number, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetResistors()
        resistance_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.isEmpty() != false) {
                    resetResistors()
                } else {
                    try {
                        val ohm = s.toString().toDouble()
                        resistance_value.text = getString(R.string.colors_resistance_format,
                                Calculator.humanReadableSI(ohm))
                        val (fourColor, fiveColor) = Calculator.colorsFromResistance(ohm)
                        if (fourColor.isNotEmpty()) {
                            resistor1.applyColors(fourColor)
                            resistor1.alpha = 1f
                            lastResult1 = fourColor
                        } else {
                            resistor1.resetColors()
                            resistor1.alpha = .3f
                            lastResult1 = null
                        }
                        if (fiveColor.isNotEmpty()) {
                            resistor2.applyColors(fiveColor)
                            resistor2.alpha = 1f
                            lastResult2 = fiveColor
                        } else {
                            resistor2.resetColors()
                            resistor2.alpha = .3f
                            lastResult2 = null
                        }
                        result_or.isEnabled = fourColor.isNotEmpty() && fiveColor.isNotEmpty()
                    } catch (_: NumberFormatException) {
                        // ignore, can only enter valid characters, this will be called when '.'/','
                        resetResistors()
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        resetResistors()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        resistance_input.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE &&
                    (lastResult1 != null || lastResult2 != null)) {
                listener?.onCalculation(Calculation(
                        ColorListPair(lastResult1 ?: emptyList(), lastResult2 ?: emptyList()),
                        Resistance(v.text.toString().toDouble(), 0f, null), true))
            }
            false
        }
    }

    private fun resetResistors() {
        lastResult1 = null
        lastResult2 = null
        resistor1.resetColors()
        resistor2.resetColors()
        resistor1.alpha = .3f
        resistor2.alpha = .3f
        result_or.isEnabled = false
        resistance_value.setText(R.string.colors_resistance_none)
    }
}
