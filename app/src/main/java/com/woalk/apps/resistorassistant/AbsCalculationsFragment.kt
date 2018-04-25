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
import android.support.v4.app.Fragment
import android.view.WindowManager

/**
 * A general [Fragment] class for resistance calculations.
 *
 * Activities that contain this fragment must implement the
 * [OnCalculationListener] interface to handle interaction events.
 */
abstract class AbsCalculationsFragment: Fragment() {
    internal var listener: OnCalculationListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCalculationListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnCalculationListener.")
        }
        onLoadWindowSoftInputMode()
    }

    internal open fun onLoadWindowSoftInputMode() {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnCalculationListener {
        fun onCalculation(calculation: Calculation)
    }
}
