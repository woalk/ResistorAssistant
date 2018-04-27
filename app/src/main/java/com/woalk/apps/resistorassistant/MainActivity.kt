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
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.view.MenuItem
import android.text.method.LinkMovementMethod
import android.widget.TextView

class MainActivity : AppCompatActivity(), AbsCalculationsFragment.OnCalculationListener,
        AssistantFragment.RequestCalculationListener {
    companion object {
        private const val MAXIMUM_HISTORY = 100
        private const val SAVED_INSTANCE_HISTORY = "hist"

        private const val PREFERENCE_FILE = "main"
        private const val PREF_NAV_ITEM = "nav"
        private const val NAV_ITEM_ASSISTANT = 0
        private const val NAV_ITEM_COLORS = 1
        private const val NAV_ITEM_NUMBER = 2
    }

    private val calculationHistory = LinkedList<Calculation>()
    private val assistantFragment = AssistantFragment()
    private val colorsFragment = ColorsFragment()
    private val numberFragment = NumberFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            when (getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
                    .getInt(PREF_NAV_ITEM, NAV_ITEM_COLORS)) {
                NAV_ITEM_ASSISTANT -> {
                    navigateAssistant()
                    navigation.selectedItemId = R.id.navigation_assistant
                }
                NAV_ITEM_COLORS -> {
                    navigateColors()
                    navigation.selectedItemId = R.id.navigation_colors
                }
                NAV_ITEM_NUMBER -> {
                    navigateNumber()
                    navigation.selectedItemId = R.id.navigation_number
                }
            }
        } else /* savedInstanceState != null */ {
            calculationHistory.addAll(
                    savedInstanceState.getParcelableArrayList(SAVED_INSTANCE_HISTORY)
                            ?: emptyList())
        }

        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_assistant -> {
                    navigateAssistant()
                    getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
                            .edit()
                            .putInt(PREF_NAV_ITEM, NAV_ITEM_ASSISTANT)
                            .apply()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_colors -> {
                    navigateColors()
                    getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
                            .edit()
                            .putInt(PREF_NAV_ITEM, NAV_ITEM_COLORS)
                            .apply()
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_number -> {
                    getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
                            .edit()
                            .putInt(PREF_NAV_ITEM, NAV_ITEM_NUMBER)
                            .apply()
                    navigateNumber()
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        val array = ArrayList(calculationHistory)
        outState!!.putParcelableArrayList(SAVED_INSTANCE_HISTORY, array)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateAssistant() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, assistantFragment)
                .commit()
    }

    private fun navigateColors() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, colorsFragment)
                .commit()
    }

    private fun navigateNumber() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, numberFragment)
                .commit()
    }

    override fun onCalculation(calculation: Calculation) {
        if (calculationHistory.isEmpty() || calculation != calculationHistory.last) {
            if (calculationHistory.size > MAXIMUM_HISTORY) {
                calculationHistory.removeFirst()
            }
            calculationHistory.add(calculation)
        }
    }

    override fun onCalculationRequest(): List<Calculation> {
        return calculationHistory
    }
}
