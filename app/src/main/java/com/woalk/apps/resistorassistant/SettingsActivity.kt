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

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.text.TextUtils
import android.view.MenuItem
import android.support.v4.app.NavUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.woalk.apps.resistorassistant.android.AppCompatPreferenceActivity
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActionBar()
        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, GeneralPreferenceFragment())
                .commit()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this)
            }
            return true
        }
        return super.onMenuItemSelected(featureId, item)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return GeneralPreferenceFragment::class.java.name == fragmentName
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    class GeneralPreferenceFragment : PreferenceFragment() {
        private val buildClickCounterMax = 3
        private var buildClickCounter = 0

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            findPreference("info_version").summary = getString(
                    R.string.pref_info_version_message, BuildConfig.VERSION_NAME)
            findPreference("info_license").onPreferenceClickListener =
                    Preference.OnPreferenceClickListener { _ ->
                        val browserIntent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.gnu.org/licenses/gpl-3.0"))
                        startActivity(browserIntent)
                        true
                    }
            findPreference("info_repo").onPreferenceClickListener =
                    Preference.OnPreferenceClickListener { preference ->
                        val browserIntent = Intent(Intent.ACTION_VIEW,
                                Uri.parse(preference.summary.toString()))
                        startActivity(browserIntent)
                        true
                    }
            val prefBuildDate = findPreference("info_build_date")
            prefBuildDate.summary = getString(R.string.pref_info_build_date_message,
                    DateUtils.formatDateTime(
                            activity,
                            SimpleDateFormat(
                                    "yyyy-MM-dd-HH-mmZ", Locale.US
                            ).parse(BuildConfig.DATE).time,
                            DateUtils.FORMAT_SHOW_DATE or
                                    DateUtils.FORMAT_SHOW_TIME or
                                    DateUtils.FORMAT_SHOW_YEAR
                    )
            )
            prefBuildDate.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener { preference ->
                        when {
                            buildClickCounter < buildClickCounterMax -> {
                                buildClickCounter++
                                true
                            }
                            buildClickCounter == buildClickCounterMax -> {
                                preference.summary = getString(
                                        R.string.pref_info_build_date_long_message,
                                        DateUtils.formatDateTime(
                                                activity,
                                                SimpleDateFormat(
                                                        "yyyy-MM-dd-HH-mmZ", Locale.US
                                                ).parse(BuildConfig.DATE).time,
                                                DateUtils.FORMAT_SHOW_DATE or
                                                        DateUtils.FORMAT_SHOW_TIME or
                                                        DateUtils.FORMAT_SHOW_YEAR
                                        ),
                                        BuildConfig.HOST
                                )
                                true
                            }
                            else -> false
                        }
                    }
            findPreference("info_commit").summary = getString(
                    R.string.pref_info_commit_message,
                    BuildConfig.GIT_COMMIT, BuildConfig.GIT_BRANCH
            )
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener =
                Preference.OnPreferenceChangeListener { preference, value ->
                    val stringValue = value.toString()

                    if (preference is ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        val index = preference.findIndexOfValue(stringValue)

                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                if (index >= 0)
                                    preference.entries[index]
                                else
                                    null)

                    } else if (preference is RingtonePreference) {
                        // For ringtone preferences, look up the correct display value
                        // using RingtoneManager.
                        if (TextUtils.isEmpty(stringValue)) {
                            // Empty values correspond to 'silent' (no ringtone).
                            preference.setSummary(null)

                        } else {
                            val ringtone = RingtoneManager.getRingtone(
                                    preference.getContext(), Uri.parse(stringValue))

                            if (ringtone == null) {
                                // Clear the summary if there was a lookup error.
                                preference.setSummary(null)
                            } else {
                                // Set the summary to reflect the new ringtone display
                                // name.
                                val name = ringtone.getTitle(preference.getContext())
                                preference.setSummary(name)
                            }
                        }

                    } else {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.summary = stringValue
                    }
                    true
                }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }
}
