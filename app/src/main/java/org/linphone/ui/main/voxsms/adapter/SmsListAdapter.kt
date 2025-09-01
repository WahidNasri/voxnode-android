/*
 * Copyright (c) 2010-2023 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.ui.main.voxsms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.linphone.R
import org.linphone.core.tools.Log
import org.linphone.ui.main.voxsms.model.SmsModel
import java.text.SimpleDateFormat
import java.util.Locale

class SmsListAdapter(
    private val onItemClicked: (SmsModel) -> Unit
) : ListAdapter<SmsModel, SmsListAdapter.ViewHolder>(SmsModelDiffCallback()) {
    
    companion object {
        private const val TAG = "[SMS List Adapter]"
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIcon: ImageView = itemView.findViewById(R.id.sms_status_icon)
        val recipientNumber: TextView = itemView.findViewById(R.id.recipient_number)
        val timestamp: TextView = itemView.findViewById(R.id.timestamp)
        val smsContent: TextView = itemView.findViewById(R.id.sms_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sms_list_cell, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sms = getItem(position)
        
        holder.recipientNumber.text = sms.recipientNumber
        holder.smsContent.text = sms.content
        
        // Format timestamp
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.timestamp.text = timeFormat.format(sms.timestamp)
        
        // Set status icon based on SMS status
        when (sms.status) {
            SmsModel.SmsStatus.SENT -> {
                holder.statusIcon.setImageResource(R.drawable.check)
                holder.statusIcon.setColorFilter(
                    holder.itemView.context.getColor(android.R.color.holo_green_dark)
                )
            }
            SmsModel.SmsStatus.FAILED -> {
                holder.statusIcon.setImageResource(R.drawable.warning_circle)
                holder.statusIcon.setColorFilter(
                    holder.itemView.context.getColor(R.color.danger_500)
                )
            }
            SmsModel.SmsStatus.PENDING -> {
                holder.statusIcon.setImageResource(R.drawable.clock)
                holder.statusIcon.setColorFilter(
                    holder.itemView.context.getColor(android.R.color.holo_orange_dark)
                )
            }
        }
        
        holder.itemView.setOnClickListener {
            Log.i("$TAG SMS item clicked: ${sms.recipientNumber}")
            onItemClicked(sms)
        }
    }

    private class SmsModelDiffCallback : DiffUtil.ItemCallback<SmsModel>() {
        override fun areItemsTheSame(oldItem: SmsModel, newItem: SmsModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SmsModel, newItem: SmsModel): Boolean {
            return oldItem == newItem
        }
    }
}
