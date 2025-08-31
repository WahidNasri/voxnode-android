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
package org.linphone.ui.main.voxsettings.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.linphone.R
import org.linphone.core.tools.Log
import org.voxnode.voxnode.models.CallerId

@UiThread
class CallerIdSelectionBottomSheet(
    private val callerIds: List<CallerId>,
    private val currentCallerId: CallerId?,
    private val onCallerIdSelected: (CallerId) -> Unit
) : BottomSheetDialogFragment() {
    companion object {
        private const val TAG = "[Caller ID Selection Bottom Sheet]"

        fun newInstance(
            callerIds: List<CallerId>,
            currentCallerId: CallerId?,
            onCallerIdSelected: (CallerId) -> Unit
        ): CallerIdSelectionBottomSheet {
            return CallerIdSelectionBottomSheet(callerIds, currentCallerId, onCallerIdSelected)
        }
    }

    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var closeButton: View
    private lateinit var adapter: CallerIdAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.caller_id_selection_bottom_sheet, container, false)
        
        // Initialize view references
        recyclerView = rootView.findViewById(R.id.caller_ids_list)
        closeButton = rootView.findViewById(R.id.close_button)
        
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("$TAG Caller ID selection bottom sheet opened with ${callerIds.size} caller IDs")
        
        setupRecyclerView()
        setupCloseButton()
    }

    private fun setupRecyclerView() {
        adapter = CallerIdAdapter(callerIds, currentCallerId) { selectedCallerId ->
            Log.i("$TAG Caller ID selected: ${selectedCallerId.callerID}")
            onCallerIdSelected(selectedCallerId)
            dismiss()
        }
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupCloseButton() {
        closeButton.setOnClickListener {
            Log.i("$TAG Caller ID selection cancelled")
            dismiss()
        }
    }

    private class CallerIdAdapter(
        private val callerIds: List<CallerId>,
        private val currentCallerId: CallerId?,
        private val onCallerIdSelected: (CallerId) -> Unit
    ) : RecyclerView.Adapter<CallerIdAdapter.CallerIdViewHolder>() {

        class CallerIdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val callerIdText: TextView = itemView.findViewById(R.id.caller_id_text)
            val checkMark: View = itemView.findViewById(R.id.check_mark)
            val statusText: TextView = itemView.findViewById(R.id.status_text)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallerIdViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.caller_id_item, parent, false)
            return CallerIdViewHolder(view)
        }

        override fun onBindViewHolder(holder: CallerIdViewHolder, position: Int) {
            val callerId = callerIds[position]
            
            holder.callerIdText.text = callerId.callerID ?: "Unknown"
            
            // Show check mark for current caller ID
            val isCurrent = currentCallerId?.callerIDId == callerId.callerIDId
            holder.checkMark.visibility = if (isCurrent) View.VISIBLE else View.GONE
            
            // Show status text
            val statusText = when {
                callerId.isAuthorized() -> "Authorized"
                else -> "Pending verification"
            }
            holder.statusText.text = statusText
            
            // Set text color based on authorization status
            val textColor = if (callerId.isAuthorized()) {
                ContextCompat.getColor(holder.itemView.context, android.R.color.black)
            } else {
                ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
            }
            holder.callerIdText.setTextColor(textColor)
            holder.statusText.setTextColor(textColor)
            
            // Only allow selection of authorized caller IDs
            holder.itemView.isEnabled = callerId.isAuthorized()
            holder.itemView.alpha = if (callerId.isAuthorized()) 1.0f else 0.5f
            
            holder.itemView.setOnClickListener {
                if (callerId.isAuthorized()) {
                    onCallerIdSelected(callerId)
                }
            }
        }

        override fun getItemCount(): Int = callerIds.size
    }
}
