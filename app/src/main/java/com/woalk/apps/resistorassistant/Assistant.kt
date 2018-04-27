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
import android.os.Parcel
import android.os.Parcelable
import java.util.*

open class Calculation(val colors: ColorListPair, val resistance: Resistance,
                       val backwards: Boolean): Parcelable {
    open fun getColor1String(context: Context): String {
        val text1 = StringBuilder(colors.first.size * 2)
        for (color in colors.first) {
            text1.append(context.getString(color.string))
            text1.append(' ')
        }
        return text1.toString()
    }

    open fun getColor2String(context: Context): String {
        val text2 = StringBuilder(colors.second.size * 2)
        for (color in colors.second) {
            text2.append(context.getString(color.string))
            text2.append(' ')
        }
        return text2.toString()
    }

    open fun getResistanceString(): String {
        return resistance.humanReadable
    }

    open fun getSpeechOutput(context: Context): String {
        return if (backwards) {
            if (colors.first.isNotEmpty() && colors.second.isEmpty()) {
                context.getString(R.string.voice_answer_color, getColor1String(context))
            } else if (colors.first.isEmpty() && colors.second.isNotEmpty()) {
                context.getString(R.string.voice_answer_color, getColor2String(context))
            } else if (colors.first.isEmpty() && colors.second.isEmpty()) {
                context.getString(R.string.voice_answer_no_color)
            } else {
                context.getString(R.string.voice_answer_colors, getColor1String(context),
                        getColor2String(context))
            }
        } else {
            context.getString(R.string.voice_answer_number, humanReadableSI(resistance.ohm))
        }
    }

    internal fun humanReadableSI(value: Double): String {
        when {
            // TODO i18n for "pico", "nano", ...
            value <= 0 -> return "%.00f ".format(Locale.getDefault(), value)
            value < .000000000000001 -> return "%.00f atto".format(Locale.getDefault(), value * 1000000000000000000f)
            value < .000000000001 -> return "%.00f femto".format(Locale.getDefault(), value * 1000000000000000f)
            value < .000000001 -> return "%.00f pico".format(Locale.getDefault(), value * 1000000000000f)
            value < .000001 -> return "%.00f nano".format(Locale.getDefault(), value * 1000000000f)
            value < .001 -> return "%.00f micro".format(Locale.getDefault(), value * 1000000f)
            value < 1 || value % 1.0 != 0.0 -> return "%.00f milli".format(Locale.getDefault(), value * 1000f)
            value < 1000 || value % 1000.0 != 0.0 -> return "%.00f ".format(Locale.getDefault(), value)
            value < 1000000 || value % 1000000.0 != 0.0 -> return "%.00f kilo".format(Locale.getDefault(), value / 1000f)
            value < 1000000000 || value % 1000000000.0 != 0.0 -> return "%.00f mega".format(Locale.getDefault(), value / 1000000f)
            value < 1000000000000 || value % 1000000000000.0 != 0.0 -> return "%.00f giga".format(Locale.getDefault(), value / 1000000000f)
            value < 1000000000000000 || value % 1000000000000000.0 != 0.0 -> return "%.00f tera".format(Locale.getDefault(), value / 1000000000000f)
            value < 1000000000000000000 || value % 1000000000000000000.0 != 0.0 -> return "%.00f peta".format(Locale.getDefault(), value / 1000000000000000f)
            value > 1000000000000000000 -> return "%.00f E".format(Locale.getDefault(), value / 1000000000000000000f)
            else -> throw IllegalStateException("Unknown number range.")
        }
    }

    //region Parcelable implementation
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(ColorListPair::class.java.classLoader),
            parcel.readParcelable(Resistance::class.java.classLoader),
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(colors, flags)
        parcel.writeParcelable(resistance, flags)
        parcel.writeByte(if (backwards) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Calculation> {
        override fun createFromParcel(parcel: Parcel): Calculation {
            return Calculation(parcel)
        }

        override fun newArray(size: Int): Array<Calculation?> {
            return arrayOfNulls(size)
        }
    }
    //endregion
}

class CalculationCustom(private val question: String, private val speechFormat: String? = null,
                        colors: ColorListPair, resistance: Resistance, backwards: Boolean) :
        Calculation(colors, resistance, backwards) {
    override fun getColor1String(context: Context): String {
        return if (!backwards) question else super.getColor1String(context)
    }

    override fun getColor2String(context: Context): String {
        return if (!backwards) question else super.getColor2String(context)
    }

    override fun getResistanceString(): String {
        return if (backwards) question else super.getResistanceString()
    }

    override fun getSpeechOutput(context: Context): String {
        return if (speechFormat != null) {
            if (!backwards) {
                speechFormat.format(Locale.getDefault(), humanReadableSI(resistance.ohm))
            } else {
                speechFormat.format(Locale.getDefault(), getColor1String(context))
            }
        } else {
            super.getSpeechOutput(context)
        }
    }
}

class CalculationUnknown(private val question: String): Calculation(ColorListPair(),
        Resistance(0.0, 0f, null), false), Parcelable {
    override fun getResistanceString(): String {
        return question
    }

    override fun getSpeechOutput(context: Context): String {
        return context.getString(R.string.voice_answer_unknown)
    }

    //region Parcelable implementation
    constructor(parcel: Parcel) : this(parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(question)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CalculationUnknown> {
        override fun createFromParcel(parcel: Parcel): CalculationUnknown {
            return CalculationUnknown(parcel)
        }

        override fun newArray(size: Int): Array<CalculationUnknown?> {
            return arrayOfNulls(size)
        }
    }
    //endregion
}