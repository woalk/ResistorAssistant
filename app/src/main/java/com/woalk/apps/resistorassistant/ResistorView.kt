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

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.view_resistor.view.*

/**
 * A [View] showing a resistor with color patterns.
 */
class ResistorView : FrameLayout {
    //region attributes
    private var _color1: Int = context.getColor(R.color.resistorColor)
    private var _color2: Int = context.getColor(R.color.resistorColor)
    private var _color3: Int = context.getColor(R.color.resistorColor)
    private var _color4: Int = context.getColor(R.color.resistorColor)
    private var _color5: Int = context.getColor(R.color.resistorColor)
    private var _color6: Int = context.getColor(R.color.resistorColor)

    var color1: Int
        get() = _color1
        set(value) {
            _color1 = value
            updateColors()
        }
    var color2: Int
        get() = _color2
        set(value) {
            _color2 = value
            updateColors()
        }
    var color3: Int
        get() = _color3
        set(value) {
            _color3 = value
            updateColors()
        }
    var color4: Int
        get() = _color4
        set(value) {
            _color4 = value
            updateColors()
        }
    var color5: Int
        get() = _color5
        set(value) {
            _color5 = value
            updateColors()
        }
    var color6: Int
        get() = _color6
        set(value) {
            _color6 = value
            updateColors()
        }
    //endregion

    //region contructors
    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }
    //endregion

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_resistor, this)

        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.ResistorView, defStyle, 0)

        _color1 = a.getColor(R.styleable.ResistorView_color1, _color1)
        _color2 = a.getColor(R.styleable.ResistorView_color2, _color2)
        _color3 = a.getColor(R.styleable.ResistorView_color3, _color3)
        _color4 = a.getColor(R.styleable.ResistorView_color4, _color4)
        _color5 = a.getColor(R.styleable.ResistorView_color5, _color5)
        _color6 = a.getColor(R.styleable.ResistorView_color6, _color6)

        a.recycle()

        updateColors()
    }

    fun applyColors(colors: List<Color>) {
        color1 = context.getColor(colors.getOrNull(0)?.color ?: R.color.resistorColor)
        color2 = context.getColor(colors.getOrNull(1)?.color ?: R.color.resistorColor)
        color3 = context.getColor(colors.getOrNull(2)?.color ?: R.color.resistorColor)
        color4 = context.getColor(colors.getOrNull(3)?.color ?: R.color.resistorColor)
        color5 = context.getColor(colors.getOrNull(4)?.color ?: R.color.resistorColor)
        color6 = context.getColor(colors.getOrNull(5)?.color ?: R.color.resistorColor)
    }

    fun resetColors() {
        color1 = context.getColor(R.color.resistorColor)
        color2 = color1
        color3 = color1
        color4 = color1
        color5 = color1
        color6 = color1
    }

    private fun updateColors() {
        view_color1.setBackgroundColor(color1)
        view_color2.setBackgroundColor(color2)
        view_color3.setBackgroundColor(color3)
        view_color4.setBackgroundColor(color4)
        view_color5.setBackgroundColor(color5)
        view_color6.setBackgroundColor(color6)
    }
}
