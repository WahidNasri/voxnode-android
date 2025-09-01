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
package org.linphone.ui.main.voxsms.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import org.linphone.R
import org.linphone.core.tools.Log
import org.linphone.databinding.VoxSmsFragmentBinding
import org.linphone.ui.main.fragment.AbstractMainFragment
import org.linphone.ui.main.voxsms.adapter.SmsListAdapter
import org.linphone.ui.main.voxsms.viewmodel.VoxSmsViewModel

@UiThread
class VoxSmsFragment : AbstractMainFragment() {
    companion object {
        private const val TAG = "[VoxSms Fragment]"
    }

    private lateinit var binding: VoxSmsFragmentBinding
    private lateinit var listViewModel: VoxSmsViewModel
    private lateinit var adapter: SmsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("$TAG onCreateView called")
        
        binding = DataBindingUtil.inflate(
            inflater, R.layout.vox_sms_fragment, container, false
        )
        
        listViewModel = ViewModelProvider(this)[VoxSmsViewModel::class.java]
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
        listViewModel.title.value = getString(R.string.bottom_navigation_voxsms_label)
        setViewModel(listViewModel)
        initViews(
            binding.slidingPaneLayout,
            binding.topBar,
            binding.bottomNavBar,
            R.id.voxSmsFragment
        )
        
        setupRecyclerView()
        observeEvents()
    }

    override fun onResume() {
        super.onResume()
        Log.i("$TAG onResume called")
        listViewModel.updateUnreadMessagesCount()
        listViewModel.refreshSmsList()
    }

    private fun setupRecyclerView() {
        adapter = SmsListAdapter { smsModel ->
            Log.i("$TAG SMS clicked: ${smsModel.recipientNumber}")
            // Handle SMS item click if needed
        }
        
        binding.smsList.layoutManager = LinearLayoutManager(requireContext())
        binding.smsList.adapter = adapter
    }

    private fun observeEvents() {
        Log.i("$TAG Setting up observers for VoxSms")
        
        // Set up FAB click listener
        binding.newSmsFab.setOnClickListener {
            Log.i("$TAG New SMS FAB clicked")
            listViewModel.onNewSmsClicked()
        }
        
        // Observe SMS list
        listViewModel.smsList.observe(viewLifecycleOwner) { smsList ->
            Log.i("$TAG SMS list updated with ${smsList.size} items")
            adapter.submitList(smsList)
        }
        
        // Observe navigation events
        listViewModel.navigateToSendSmsEvent.observe(viewLifecycleOwner) { event ->
            event.consume {
                Log.i("$TAG Navigating to send SMS fragment")
                // Use the main navigation controller
                try {
                    findNavController().navigate(R.id.action_voxSmsFragment_to_sendSmsFragment)
                } catch (e: Exception) {
                    Log.e("$TAG Failed to navigate to send SMS: ${e.message}")
                }
            }
        }
    }

    override fun onDefaultAccountChanged() {
        Log.i("$TAG Default account changed, updating VoxSms if needed")
        // Handle account changes here if needed for VoxSms
        listViewModel.refreshSmsList()
    }
}
