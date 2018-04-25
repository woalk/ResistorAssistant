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

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import java.util.*
import kotlin.math.roundToLong

object Calculator {
    private const val TOO_PRECISE_MESSAGE = "Value too precise for resistor."

    fun resistanceFromColors(colors: List<Color>): Resistance {
        when {
            colors.size == 1 && colors[0] == Color.BLACK -> return Resistance(
                    0.0, 0f, null, 0.0, .05
            )
            colors.size == 4 -> return Resistance(
                    (colors[0].digit!! * 10 + colors[1].digit!!) * colors[2].multiplier,
                    colors[3].tolerance!!,
                    null
            )
            colors.size == 5 -> return Resistance(
                    (colors[0].digit!! * 100 + colors[1].digit!! * 10 + colors[2].digit!!)
                            * colors[3].multiplier,
                    colors[4].tolerance!!,
                    null
            )
            colors.size == 6 -> return Resistance(
                    (colors[0].digit!! * 100 + colors[1].digit!! * 10 + colors[2].digit!!)
                            * colors[3].multiplier,
                    colors[4].tolerance!!,
                    colors[5].temp!!
            )
            else -> throw IllegalArgumentException("Not a valid amount of colors.")
        }
    }

    fun humanReadableSI(value: Double): String {
        when {
            value <= 0 -> return "%.00f ".format(Locale.getDefault(), value)
            value < .000000000000001 -> return "%.00f a".format(Locale.getDefault(), value * 1000000000000000000f)
            value < .000000000001 -> return "%.00f f".format(Locale.getDefault(), value * 1000000000000000f)
            value < .000000001 -> return "%.00f p".format(Locale.getDefault(), value * 1000000000000f)
            value < .000001 -> return "%.00f n".format(Locale.getDefault(), value * 1000000000f)
            value < .001 -> return "%.00f µ".format(Locale.getDefault(), value * 1000000f)
            value < 1 || value % 1.0 != 0.0 -> return "%.00f m".format(Locale.getDefault(), value * 1000f)
            value < 1000 || value % 1000.0 != 0.0 -> return "%.00f ".format(Locale.getDefault(), value)
            value < 1000000 || value % 1000000.0 != 0.0 -> return "%.00f k".format(Locale.getDefault(), value / 1000f)
            value < 1000000000 || value % 1000000000.0 != 0.0 -> return "%.00f M".format(Locale.getDefault(), value / 1000000f)
            value < 1000000000000 || value % 1000000000000.0 != 0.0 -> return "%.00f G".format(Locale.getDefault(), value / 1000000000f)
            value < 1000000000000000 || value % 1000000000000000.0 != 0.0 -> return "%.00f T".format(Locale.getDefault(), value / 1000000000000f)
            value < 1000000000000000000 || value % 1000000000000000000.0 != 0.0 -> return "%.00f P".format(Locale.getDefault(), value / 1000000000000000f)
            value > 1000000000000000000 -> return "%.00f E".format(Locale.getDefault(), value / 1000000000000000000f)
            else -> throw IllegalStateException("Unknown number range.")
        }
    }

    fun colorsFromResistance(ohm: Double): Pair<List<Color>, List<Color>> {
        val ohmBiased = (ohm * 100.0).roundToLong()
        if (ohmBiased.toDouble() / 100.0 != ohm) {
            throw IllegalArgumentException(TOO_PRECISE_MESSAGE)
        } else {
            return Pair(fourColors(ohmBiased), fiveColors(ohmBiased))
        }
    }

    private fun fourColors(ohmBiased: Long): List<Color> {
        try {
            var color1: Color? = null
            var color2: Color? = null
            var color3: Color? = null
            when (ohmBiased) {
                0L -> {
                    return listOf(Color.BLACK)
                }
                in 10..99 -> {
                    val digit1 = (ohmBiased % 10).toInt()
                    val digit2 = ((ohmBiased - digit1) / 10).toInt()
                    color1 = colorForDigit(digit2)
                    color2 = colorForDigit(digit1)
                    color3 = Color.SILVER // x .01
                }
                in 100..990 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 100)
                    color1 = d1; color2 = d2
                    color3 = Color.GOLD // x .1
                }
                in 1000..9900 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 1000)
                    color1 = d1; color2 = d2
                    color3 = Color.BLACK // x 1
                }
                in 10000..99000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 10000)
                    color1 = d1; color2 = d2
                    color3 = Color.BROWN // x 10
                }
                in 100000..990000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 100000)
                    color1 = d1; color2 = d2
                    color3 = Color.RED // x 100
                }
                in 1000000..9900000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 1000000)
                    color1 = d1; color2 = d2
                    color3 = Color.ORANGE // x 1000
                }
                in 10000000..99000000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 10000000)
                    color1 = d1; color2 = d2
                    color3 = Color.YELLOW // x 10000
                }
                in 100000000..990000000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 100000000)
                    color1 = d1; color2 = d2
                    color3 = Color.GREEN // x 100000
                }
                in 1000000000..9900000000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 1000000000)
                    color1 = d1; color2 = d2
                    color3 = Color.BLUE // x 1000000
                }
                in 10000000000..99000000000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 10000000000)
                    color1 = d1; color2 = d2
                    color3 = Color.VIOLET // x 10000000
                }
                in 100000000000..990000000000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 100000000000)
                    color1 = d1; color2 = d2
                    color3 = Color.GRAY // x 100000000
                }
                in 1000000000000..9900000000000 -> {
                    val (d1, d2) = fourColorDigits(ohmBiased, 1000000000000)
                    color1 = d1; color2 = d2
                    color3 = Color.WHITE // x 1000000000
                }
            }
            return listOfNotNull(color1, color2, color3)
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun fourColorDigits(ohmBiased: Long, magnitude: Long): Pair<Color?, Color?> {
        val subMagnitude = magnitude / 10
        val sufPart = ohmBiased % subMagnitude
        if (sufPart != 0L) {
            throw IllegalArgumentException(TOO_PRECISE_MESSAGE)
        }
        val digit1 = ((ohmBiased / subMagnitude) % 10).toInt()
        val digit2 = (ohmBiased / magnitude).toInt()
        return Pair(colorForDigit(digit2), colorForDigit(digit1))
    }

    private fun fiveColors(ohmBiased: Long): List<Color> {
        try {
            var color1: Color? = null
            var color2: Color? = null
            var color3: Color? = null
            var color4: Color? = null
            when (ohmBiased) {
                in 100..999 -> {
                    val digit1 = (ohmBiased % 10).toInt()
                    val digit2 = ((ohmBiased / 10) % 10).toInt()
                    val digit3 = (ohmBiased / 100).toInt()
                    color1 = colorForDigit(digit3)
                    color2 = colorForDigit(digit2)
                    color3 = colorForDigit(digit1)
                    color4 = Color.SILVER // x .01
                }
                in 1000..9990 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 1000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.GOLD // x .1
                }
                in 10000..99900 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 10000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.BLACK // x 1
                }
                in 100000..999000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 100000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.BROWN // x 10
                }
                in 1000000..9990000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 1000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.RED // x 100
                }
                in 10000000..99900000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 10000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.ORANGE // x 1000
                }
                in 100000000..999000000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 100000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.YELLOW // x 10000
                }
                in 1000000000..9990000000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 1000000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.GREEN // x 100000
                }
                in 10000000000..99900000000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 10000000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.BLUE // x 1000000
                }
                in 100000000000..999000000000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 100000000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.VIOLET // x 10000000
                }
                in 1000000000000..9990000000000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 1000000000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.GRAY // x 100000000
                }
                in 10000000000000..99900000000000 -> {
                    val (d1, d2, d3) = fiveColorDigits(ohmBiased, 10000000000000)
                    color1 = d1; color2 = d2; color3 = d3
                    color4 = Color.WHITE // x 1000000000
                }
            }
            return listOfNotNull(color1, color2, color3, color4)
        } catch (_: Exception) {
            return emptyList()
        }
    }

    private fun fiveColorDigits(ohmBiased: Long, magnitude: Long): Triple<Color?, Color?, Color?> {
        val subMagnitude = magnitude / 10
        val subSubMagnitude = magnitude / 100
        val sufPart = ohmBiased % subSubMagnitude
        if (sufPart != 0L) {
            throw IllegalArgumentException(TOO_PRECISE_MESSAGE)
        }
        val digit1 = ((ohmBiased / subSubMagnitude) % 10).toInt()
        val digit2 = ((ohmBiased / subMagnitude) % 10).toInt()
        val digit3 = (ohmBiased / magnitude).toInt()
        return Triple(colorForDigit(digit3), colorForDigit(digit2), colorForDigit(digit1))
    }

    private fun colorForDigit(digit: Int): Color? {
        return Color.values().find { it.digit == digit }
    }
}

data class Resistance(val ohm: Double, val tolerance: Float, val temp: TemperatureCoefficient?,
                      val minValue: Double = ohm - tolerance * ohm,
                      val maxValue: Double = ohm + tolerance * ohm): Parcelable {
    val humanReadable: String get() = Calculator.humanReadableSI(ohm) + "Ω"

    //region Parcelable implementation
    constructor(parcel: Parcel) : this(
            parcel.readDouble(),
            parcel.readFloat(),
            parcel.readParcelable(TemperatureCoefficient::class.java.classLoader),
            parcel.readDouble(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(ohm)
        parcel.writeFloat(tolerance)
        parcel.writeParcelable(temp, flags)
        parcel.writeDouble(minValue)
        parcel.writeDouble(maxValue)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Resistance> {
        override fun createFromParcel(parcel: Parcel): Resistance {
            return Resistance(parcel)
        }

        override fun newArray(size: Int): Array<Resistance?> {
            return arrayOfNulls(size)
        }
    }
    //endregion
}

data class ColorListPair(val first: List<Color>, val second: List<Color>) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.createTypedArrayList(Color),
            parcel.createTypedArrayList(Color))

    constructor() : this(emptyList(), emptyList())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(first)
        parcel.writeTypedList(second)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ColorListPair> {
        override fun createFromParcel(parcel: Parcel): ColorListPair {
            return ColorListPair(parcel)
        }

        override fun newArray(size: Int): Array<ColorListPair?> {
            return arrayOfNulls(size)
        }
    }
}

enum class Color(val digit: Int?, val multiplier: Double, val tolerance: Float?,
                 val temp: TemperatureCoefficient?,  @ColorRes val color: Int,
                 val lightText: Boolean, @StringRes val string: Int) : Parcelable {
    SILVER(null, .01, .1f, null,
            R.color.color_silver, false, R.string.color_silver),
    GOLD  (null, .1, .05f, null,
            R.color.color_gold, false, R.string.color_gold),
    BLACK (0, 1.0, null, TemperatureCoefficient.BLACK,
            R.color.color_black, true, R.string.color_black),
    BROWN (1, 10.0, .01f, TemperatureCoefficient.BROWN,
            R.color.color_brown, true, R.string.color_brown),
    RED   (2, 100.0, .02f, TemperatureCoefficient.RED,
            R.color.color_red, true, R.string.color_red),
    ORANGE(3, 1000.0, null, TemperatureCoefficient.ORANGE,
            R.color.color_orange, false, R.string.color_orange),
    YELLOW(4, 10000.0, null, TemperatureCoefficient.YELLOW,
            R.color.color_yellow, false, R.string.color_yellow),
    GREEN (5, 100000.0, .005f, TemperatureCoefficient.GREEN,
            R.color.color_green, false, R.string.color_green),
    BLUE  (6, 1000000.0, .0025f, TemperatureCoefficient.BLUE,
            R.color.color_blue, false, R.string.color_blue),
    VIOLET(7, 10000000.0, .001f, TemperatureCoefficient.VIOLET,
            R.color.color_violet, true, R.string.color_violet),
    GRAY  (8, 100000000.0, .0005f, TemperatureCoefficient.GRAY,
            R.color.color_gray, false, R.string.color_gray),
    WHITE (9, 1000000000.0, null, TemperatureCoefficient.GRAY,
            R.color.color_white, false, R.string.color_white);

    //region Parcelable implementation
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Color> {
        override fun createFromParcel(parcel: Parcel): Color {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<Color?> {
            return arrayOfNulls(size)
        }
    }
    //endregion
}

enum class TemperatureCoefficient(val number: Int, val letter: Char): Parcelable {
    BLACK (250, 'U'),
    BROWN (100, 'S'),
    RED   ( 50, 'R'),
    ORANGE( 15, 'P'),
    YELLOW( 25, 'Q'),
    GREEN ( 20, 'Z'),
    BLUE  ( 10, 'Z'),
    VIOLET(  5, 'M'),
    GRAY  (  1, 'K');

    //region Parcelable implementation
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TemperatureCoefficient> {
        override fun createFromParcel(parcel: Parcel): TemperatureCoefficient {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<TemperatureCoefficient?> {
            return arrayOfNulls(size)
        }
    }
    //endregion
}
