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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.linphone.R
import org.linphone.core.tools.Log
import org.linphone.databinding.VoxSettingsFragmentBinding
import org.linphone.ui.main.fragment.AbstractMainFragment
import org.linphone.ui.main.voxsettings.viewmodel.VoxSettingsViewModel

@UiThread
class VoxSettingsFragment : AbstractMainFragment() {
    companion object {
        private const val TAG = "[VoxSettings Fragment]"
    }

    private lateinit var binding: VoxSettingsFragmentBinding
    private lateinit var listViewModel: VoxSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("$TAG onCreateView called")
        
        binding = DataBindingUtil.inflate(
            inflater, R.layout.vox_settings_fragment, container, false
        )
        
        listViewModel = ViewModelProvider(this)[VoxSettingsViewModel::class.java]
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
        observeEvents()
    }

    override fun onResume() {
        super.onResume()
        Log.i("$TAG onResume called")
        listViewModel.updateUnreadMessagesCount()
    }

    private fun observeEvents() {
        // Add any specific observers for VoxSettings functionality here
        Log.i("$TAG Setting up observers for VoxSettings")
    }

    override fun onDefaultAccountChanged() {
        Log.i("$TAG Default account changed, updating VoxSettings if needed")
        // Handle account changes here if needed for VoxSettings
    }
}
