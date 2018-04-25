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
import android.content.res.ColorStateList
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView

class ColorsPopupAdapter(private val context: Context) : ListAdapter {
    private val colors: List<ColorItem>

    init {
        colors = listOf(ColorItem(null, context.getString(R.string.color_none),
                context.getColor(R.color.color_none),
                context.getColorStateList(android.R.color.primary_text_light))) +
                Color.values().map {
                    ColorItem(
                            it, context.getString(it.string),
                            context.getColor(it.color),
                            context.getColorStateList(
                                    if (it.lightText)
                                        android.R.color.primary_text_dark
                                    else
                                        android.R.color.primary_text_light
                            )
                    )
                }
    }

    override fun isEmpty(): Boolean {
        return colors.isEmpty()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = (getItem(position) as ColorItem)
        val view: View
        val viewHolder: ColorViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.item_color_popup, parent, false)
            viewHolder = ColorViewHolder(view.findViewById(android.R.id.text1))
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ColorViewHolder
        }
        viewHolder.text.text = item.string
        viewHolder.text.setTextColor(item.textColor)
        view.setBackgroundColor(item.color)
        return view
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        // no changes to observe
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getItem(position: Int): Any {
        return colors[position]
    }

    fun getColor(position: Int): Int {
        return (getItem(position) as ColorItem).color
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun isEnabled(position: Int): Boolean {
        return true
    }

    override fun getItemId(position: Int): Long {
        return 0L
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun areAllItemsEnabled(): Boolean {
        return true
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        // see [registerDataSetObserver(DataSetObserver)]
    }

    override fun getCount(): Int {
        return colors.size
    }

    private data class ColorItem(val item: Color?, val string: String, val color: Int,
                                 val textColor: ColorStateList)

    private data class ColorViewHolder(val text: TextView)
}
