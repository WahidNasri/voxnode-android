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
import androidx.annotation.UiThread
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.linphone.core.tools.Log
import org.linphone.R

@UiThread
class LanguageSelectionBottomSheet(
    private val currentLanguage: String,
    private val onLanguageSelected: (String) -> Unit
) : BottomSheetDialogFragment() {
    companion object {
        private const val TAG = "[Language Selection Bottom Sheet]"

        fun newInstance(
            currentLanguage: String,
            onLanguageSelected: (String) -> Unit
        ): LanguageSelectionBottomSheet {
            return LanguageSelectionBottomSheet(currentLanguage, onLanguageSelected)
        }
    }

    private lateinit var rootView: View
    private lateinit var englishOption: View
    private lateinit var frenchOption: View
    private lateinit var englishCheck: View
    private lateinit var frenchCheck: View
    private lateinit var closeButton: View

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
        rootView = inflater.inflate(R.layout.language_selection_bottom_sheet, container, false)
        
        // Initialize view references
        englishOption = rootView.findViewById(R.id.english_option)
        frenchOption = rootView.findViewById(R.id.french_option)
        englishCheck = rootView.findViewById(R.id.english_check)
        frenchCheck = rootView.findViewById(R.id.french_check)
        closeButton = rootView.findViewById(R.id.close_button)
        
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("$TAG Language selection bottom sheet opened")
        
        setupLanguageOptions()
    }

    private fun setupLanguageOptions() {
        // Set current language selection
        updateLanguageSelection()
        
        // English option click
        englishOption.setOnClickListener {
            Log.i("$TAG English language selected")
            onLanguageSelected("en")
            dismiss()
        }
        
        // French option click
        frenchOption.setOnClickListener {
            Log.i("$TAG French language selected")
            onLanguageSelected("fr")
            dismiss()
        }
        
        // Close button
        closeButton.setOnClickListener {
            Log.i("$TAG Language selection cancelled")
            dismiss()
        }
    }

    private fun updateLanguageSelection() {
        // Reset all selections
        englishCheck.visibility = View.GONE
        frenchCheck.visibility = View.GONE
        
        // Show check mark for current language
        when (currentLanguage) {
            "en" -> englishCheck.visibility = View.VISIBLE
            "fr" -> frenchCheck.visibility = View.VISIBLE
            else -> englishCheck.visibility = View.VISIBLE // Default to English
        }
        
        Log.i("$TAG Current language: $currentLanguage")
    }
}
