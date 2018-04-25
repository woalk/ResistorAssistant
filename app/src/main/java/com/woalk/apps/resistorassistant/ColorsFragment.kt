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
import android.support.v4.app.Fragment
import android.support.v7.widget.ListPopupWindow
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_colors.*

/**
 * The [Fragment] for calculation of resistance values by supplying the color bands.
 *
 * See [AbsCalculationsFragment].
 */
class ColorsFragment : AbsCalculationsFragment() {
    companion object {
        const val KEY_COLOR1 = "1"
        const val KEY_COLOR2 = "2"
        const val KEY_COLOR3 = "3"
        const val KEY_COLOR4 = "4"
        const val KEY_COLOR5 = "5"
        const val KEY_COLOR6 = "6"
        const val KEY_VIEW_COLOR1 = "color1"
        const val KEY_VIEW_COLOR2 = "color2"
        const val KEY_VIEW_COLOR3 = "color3"
        const val KEY_VIEW_COLOR4 = "color4"
        const val KEY_VIEW_COLOR5 = "color5"
        const val KEY_VIEW_COLOR6 = "color6"
    }

    private var selectedColor1: Int = 0
    private var selectedColor2: Int = 0
    private var selectedColor3: Int = 0
    private var selectedColor4: Int = 0
    private var selectedColor5: Int = 0
    private var selectedColor6: Int = 0
    private var viewColor1: Int = 0
    private var viewColor2: Int = 0
    private var viewColor3: Int = 0
    private var viewColor4: Int = 0
    private var viewColor5: Int = 0
    private var viewColor6: Int = 0
    private var lastResult: Resistance? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_colors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            selectedColor1 = savedInstanceState.getInt(KEY_COLOR1)
            selectedColor2 = savedInstanceState.getInt(KEY_COLOR2)
            selectedColor3 = savedInstanceState.getInt(KEY_COLOR3)
            selectedColor4 = savedInstanceState.getInt(KEY_COLOR4)
            selectedColor5 = savedInstanceState.getInt(KEY_COLOR5)
            selectedColor6 = savedInstanceState.getInt(KEY_COLOR6)
            viewColor1 = savedInstanceState.getInt(KEY_VIEW_COLOR1)
            viewColor2 = savedInstanceState.getInt(KEY_VIEW_COLOR2)
            viewColor3 = savedInstanceState.getInt(KEY_VIEW_COLOR3)
            viewColor4 = savedInstanceState.getInt(KEY_VIEW_COLOR4)
            viewColor5 = savedInstanceState.getInt(KEY_VIEW_COLOR5)
            viewColor6 = savedInstanceState.getInt(KEY_VIEW_COLOR6)
            if (viewColor1 != 0) {
                button_color1.setBackgroundColor(viewColor1)
            }
            if (viewColor2 != 0) {
                button_color2.setBackgroundColor(viewColor2)
            }
            if (viewColor3 != 0) {
                button_color3.setBackgroundColor(viewColor3)
            }
            if (viewColor4 != 0) {
                button_color4.setBackgroundColor(viewColor4)
            }
            if (viewColor5 != 0) {
                button_color5.setBackgroundColor(viewColor5)
            }
            if (viewColor6 != 0) {
                button_color6.setBackgroundColor(viewColor6)
            }

            updateResistance()
        }

        button_color1.setOnClickListener { v ->
            colorsPopup(v, { position, color ->
                selectedColor1 = position
                viewColor1 = color
                v.setBackgroundColor(color)
                updateResistance()
            })
        }
        button_color2.setOnClickListener { v ->
            colorsPopup(v, { position, color ->
                selectedColor2 = position
                viewColor2 = color
                v.setBackgroundColor(color)
                updateResistance()
            })
        }
        button_color3.setOnClickListener { v ->
            colorsPopup(v, { position, color ->
                selectedColor3 = position
                viewColor3 = color
                v.setBackgroundColor(color)
                updateResistance()
            })
        }
        button_color4.setOnClickListener { v ->
            colorsPopup(v, { position, color ->
                selectedColor4 = position
                viewColor4 = color
                v.setBackgroundColor(color)
                updateResistance()
            })
        }
        button_color5.setOnClickListener { v ->
            colorsPopup(v, { position, color ->
                selectedColor5 = position
                viewColor5 = color
                v.setBackgroundColor(color)
                updateResistance()
            })
        }
        button_color6.setOnClickListener { v ->
            colorsPopup(v, { position, color ->
                selectedColor6 = position
                viewColor6 = color
                v.setBackgroundColor(color)
                updateResistance()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewColor1 != 0) {
            button_color1.setBackgroundColor(viewColor1)
        }
        if (viewColor2 != 0) {
            button_color2.setBackgroundColor(viewColor2)
        }
        if (viewColor3 != 0) {
            button_color3.setBackgroundColor(viewColor3)
        }
        if (viewColor4 != 0) {
            button_color4.setBackgroundColor(viewColor4)
        }
        if (viewColor5 != 0) {
            button_color5.setBackgroundColor(viewColor5)
        }
        if (viewColor6 != 0) {
            button_color6.setBackgroundColor(viewColor6)
        }
        updateResistance()
    }

    private fun colorsPopup(button: View, update: (position: Int, color: Int) -> Unit) {
        ListPopupWindow(context!!).let {
            it.anchorView = button
            it.isModal = true
            it.width = context!!.resources.getDimensionPixelSize(R.dimen.color_popup_width)
            val resistorHeight = context!!.resources.getDimensionPixelSize(R.dimen.resistor_height)
            it.height = Math.max(view!!.height - resistorHeight, resistorHeight)
            val adapter = ColorsPopupAdapter(context!!)
            it.setAdapter(adapter)
            it.setOnItemClickListener { _, _, position, _ ->
                it.dismiss()
                update(position, adapter.getColor(position))
            }
            it.show()
        }
    }

    private fun updateResistance() {
        val colors = ArrayList<Color>(6)
        if (selectedColor1 > 0) {
            colors.add(Color.values()[selectedColor1 - 1])
        }
        if (selectedColor2 > 0) {
            colors.add(Color.values()[selectedColor2 - 1])
        }
        if (selectedColor3 > 0) {
            colors.add(Color.values()[selectedColor3 - 1])
        }
        if (selectedColor4 > 0) {
            colors.add(Color.values()[selectedColor4 - 1])
        }
        if (selectedColor5 > 0) {
            colors.add(Color.values()[selectedColor5 - 1])
        }
        if (selectedColor6 > 0) {
            colors.add(Color.values()[selectedColor6 - 1])
        }
        try {
            lastResult = Calculator.resistanceFromColors(colors)
            result.text = getString(R.string.resistance_format, lastResult!!.humanReadable)
            tolerance_result.text = getString(R.string.resistance_tolerance_format,
                    lastResult!!.tolerance * 100f)
            norm_result.text = getString(R.string.resistance_norm_format,
                    lastResult!!.minValue, lastResult!!.maxValue)
            if (lastResult!!.temp != null) {
                info_result.text = getString(R.string.resistance_info_format,
                        lastResult!!.temp!!.number, lastResult!!.temp!!.letter)
            } else {
                info_result.setText(R.string.resistance_info_none)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resetResistanceToNone()
        }
    }

    private fun resetResistanceToNone() {
        lastResult = null
        result.setText(R.string.resistance_none)
        tolerance_result.setText(R.string.resistance_tolerance_none)
        norm_result.setText(R.string.resistance_norm_none)
        info_result.setText(R.string.resistance_info_none)
    }

    override fun onDetach() {
        val list = listOf(selectedColor1, selectedColor2, selectedColor3, selectedColor4,
                selectedColor5, selectedColor6).mapNotNull {
            Color.values().getOrNull(it - 1)
        }
        if (lastResult != null) {
            listener?.onCalculation(Calculation(ColorListPair(list, emptyList()), lastResult!!,
                    false))
        }
        super.onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_COLOR1, selectedColor1)
        outState.putInt(KEY_COLOR2, selectedColor2)
        outState.putInt(KEY_COLOR3, selectedColor3)
        outState.putInt(KEY_COLOR4, selectedColor4)
        outState.putInt(KEY_COLOR5, selectedColor5)
        outState.putInt(KEY_COLOR6, selectedColor6)
        outState.putInt(KEY_VIEW_COLOR1, viewColor1)
        outState.putInt(KEY_VIEW_COLOR2, viewColor2)
        outState.putInt(KEY_VIEW_COLOR3, viewColor3)
        outState.putInt(KEY_VIEW_COLOR4, viewColor4)
        outState.putInt(KEY_VIEW_COLOR5, viewColor5)
        outState.putInt(KEY_VIEW_COLOR6, viewColor6)
        super.onSaveInstanceState(outState)
    }
}
