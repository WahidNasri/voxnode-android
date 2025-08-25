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
package org.linphone.ui.main.voxdialer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.linphone.R
import org.linphone.core.tools.Log
import org.linphone.databinding.VoxDialerFragmentBinding
import org.linphone.ui.main.fragment.AbstractMainFragment
import org.linphone.ui.main.voxdialer.viewmodel.VoxDialerViewModel

@UiThread
class VoxDialerFragment : AbstractMainFragment() {
    companion object {
        private const val TAG = "[VoxDialer Fragment]"
    }

    private lateinit var binding: VoxDialerFragmentBinding
    private lateinit var listViewModel: VoxDialerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("$TAG onCreateView called")
        
        binding = DataBindingUtil.inflate(
            inflater, R.layout.vox_dialer_fragment, container, false
        )
        
        listViewModel = ViewModelProvider(this)[VoxDialerViewModel::class.java]
        binding.viewModel = listViewModel
        
        // Set lifecycle owner for live data
        binding.lifecycleOwner = viewLifecycleOwner
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("$TAG onViewCreated called")
        
        // Observer for any future functionality
        observeToastEvents(listViewModel)
        
        // AbstractMainFragment related - set up the title and initialize main fragment components
        listViewModel.title.value = getString(R.string.bottom_navigation_dialer_label)
        setViewModel(listViewModel)
        initViews(
            binding.slidingPaneLayout,
            binding.topBar,
            binding.bottomNavBar,
            R.id.voxDialerFragment
        )
        
        setupDialerClickListeners()
        observeEvents()
    }

    override fun onResume() {
        super.onResume()
        Log.i("$TAG onResume called")
        listViewModel.updateUnreadMessagesCount()
    }

    private fun setupDialerClickListeners() {
        Log.i("$TAG Setting up dialer click listeners")
        
        // Number digit buttons
        binding.digit1Container.setOnClickListener { listViewModel.appendDigit("1") }
        binding.digit2Container.setOnClickListener { listViewModel.appendDigit("2") }
        binding.digit3Container.setOnClickListener { listViewModel.appendDigit("3") }
        binding.digit4Container.setOnClickListener { listViewModel.appendDigit("4") }
        binding.digit5Container.setOnClickListener { listViewModel.appendDigit("5") }
        binding.digit6Container.setOnClickListener { listViewModel.appendDigit("6") }
        binding.digit7Container.setOnClickListener { listViewModel.appendDigit("7") }
        binding.digit8Container.setOnClickListener { listViewModel.appendDigit("8") }
        binding.digit9Container.setOnClickListener { listViewModel.appendDigit("9") }
        binding.digit0Container.setOnClickListener { listViewModel.appendDigit("0") }
        
        // Special characters
        binding.digitStarContainer.setOnClickListener { listViewModel.appendDigit("*") }
        binding.digitHashContainer.setOnClickListener { listViewModel.appendDigit("#") }
        
        // Long press on 0 for + (international prefix)
        binding.digit0Container.setOnLongClickListener {
            val currentText = listViewModel.enteredUri.value.orEmpty()
            if (currentText.isEmpty()) {
                listViewModel.appendDigit("+")
                true
            } else {
                false // Let normal click handle it
            }
        }
        
        // Delete button
        binding.deleteButton.setOnClickListener { listViewModel.deleteLastDigit() }
        binding.deleteButton.setOnLongClickListener {
            listViewModel.longPressDelete()
            true
        }
        
        // Call button
        binding.callButton.setOnClickListener { listViewModel.makeCall() }
    }

    private fun observeEvents() {
        // Add any specific observers for VoxDialer functionality here
        Log.i("$TAG Setting up observers for VoxDialer")
        
        // Example: observe call button state
        listViewModel.callButtonEnabled.observe(viewLifecycleOwner) { enabled ->
            Log.d("$TAG Call button enabled: $enabled")
        }
    }

    override fun onDefaultAccountChanged() {
        Log.i("$TAG Default account changed, updating VoxDialer if needed")
        // Handle account changes here if needed for VoxDialer
    }
}
