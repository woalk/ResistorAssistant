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
import java.util.*

class Recognizer(private val context: Context) {
    fun recognize(text: String): Calculation? {
        if (text.equals(context.getString(R.string.voice_ea_rainbow), true)) {
            // ea (c:)soph d
            return getRainbow()
        }
        try {
            val textSplit = text.split(' ')
            if (textSplit.size in 2..3 && text.endsWith("Ohm", true)) {
                val firstWord = textSplit.first()
                val number = firstWord.toDouble()

                val multiplier: Double
                val secondWord = textSplit.component2()
                val thirdWord = if (textSplit.size == 2) secondWord else textSplit.component3()
                if (textSplit.size == 2 && thirdWord.equals("Ohm", true)) {
                    multiplier = 1.0
                } else if (thirdWord == "mOhm" || thirdWord == "mohm" ||
                        thirdWord.equals("milliOhm", true) ||
                        secondWord.equals("milli", true)) {
                    multiplier = .001
                } else if (thirdWord == "kOhm" || thirdWord == "kohm" ||
                        thirdWord.equals("kiloOhm", true) ||
                        secondWord.equals("kilo", true)) {
                    multiplier = 1000.0
                } else if (thirdWord == "MOhm" || thirdWord == "Mohm" ||
                        thirdWord.equals("megaOhm", true) ||
                        secondWord.equals("Mega", true)) {
                    multiplier = 1000000.0
                } else if (thirdWord == "GOhm" || thirdWord == "Gohm" ||
                        thirdWord.equals("gigaOhm", true) ||
                        secondWord.equals("Giga", true)) {
                    multiplier = 1000000000.0
                } else {
                    throw IllegalArgumentException("Ohm input partially invalid.")
                }
                val ohm = number * multiplier

                val (colorList1, colorList2) = Calculator.colorsFromResistance(ohm)
                return Calculation(ColorListPair(colorList1, colorList2), Resistance(ohm,
                        0f, null), true)
            } else {
                val isFirstColor: Boolean = matchesColor(textSplit[0]) != null
                if (textSplit.isNotEmpty() && isFirstColor) {
                    val colorWithNulls = textSplit.map { matchesColor(it) }
                    val colors = LinkedList<Color>()
                    colorWithNulls.filterNotNullTo(colors)
                    if (colors.size == colorWithNulls.size) {
                        val resistance = Calculator.resistanceFromColors(colors)
                        return Calculation(ColorListPair(colors, emptyList()), resistance,
                                false)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun matchesColor(text: String): Color? {
        for (color in Color.values()) {
            if (text.equals(context.getString(color.string), true)) {
                return color
            }
        }
        return null
    }

    private fun getRainbow(): Calculation {
        val resistance = 23400000.0
        val colors = listOf(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE,
                Color.VIOLET)
        return CalculationCustom(context.getString(R.string.voice_ea_rainbow),
                context.getString(R.string.voice_ea_rainbow_f),
                ColorListPair(colors, emptyList()),
                Resistance(resistance, Color.BLUE.tolerance!!, Color.VIOLET.temp),
                false)
    }
}
