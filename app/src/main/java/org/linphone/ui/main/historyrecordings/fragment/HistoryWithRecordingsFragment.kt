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
package org.linphone.ui.main.historyrecordings.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import org.linphone.R
import org.linphone.core.tools.Log
import org.linphone.databinding.HistoryWithRecordingsFragmentBinding
import org.linphone.ui.main.fragment.AbstractMainFragment
import org.linphone.ui.main.historyrecordings.adapter.HistoryRecordingsPagerAdapter
import org.linphone.ui.main.historyrecordings.viewmodel.HistoryWithRecordingsViewModel
import org.linphone.utils.Event
import org.voxnode.voxnode.storage.VoxNodeDataManager

@UiThread
class HistoryWithRecordingsFragment : AbstractMainFragment() {
    companion object {
        private const val TAG = "[HistoryWithRecordings Fragment]"
    }

    private lateinit var binding: HistoryWithRecordingsFragmentBinding
    private lateinit var listViewModel: HistoryWithRecordingsViewModel
    private lateinit var pagerAdapter: HistoryRecordingsPagerAdapter

    override fun onDefaultAccountChanged() {
        Log.i("$TAG Default account changed, notifying child fragments if needed")
        // Child fragments will handle account changes themselves
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("$TAG onCreateView called")
        
        binding = HistoryWithRecordingsFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("$TAG onViewCreated called")
        
        listViewModel = ViewModelProvider(this)[HistoryWithRecordingsViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = listViewModel
        observeToastEvents(listViewModel)

        setupTabs()
        setupObservers()

        // AbstractMainFragment related
        listViewModel.title.value = getString(R.string.bottom_navigation_history_recordings_label)
        setViewModel(listViewModel)
        initViews(
            binding.slidingPaneLayout,
            binding.topBar,
            binding.bottomNavBar,
            R.id.historyWithRecordingsFragment
        )
    }

    override fun onResume() {
        super.onResume()
        Log.i("$TAG onResume called")
        
        // Reset missed calls count when viewing history
        sharedViewModel.resetMissedCallsCountEvent.value = Event(true)
        sharedViewModel.refreshDrawerMenuAccountsListEvent.value = Event(false)
    }

    private fun setupTabs() {
        Log.i("$TAG Setting up tabs")
        
        // Check if recordings are enabled from login result
        val isRecordingEnabled = checkIfRecordingEnabled()
        Log.i("$TAG Recording enabled: $isRecordingEnabled")
        
        pagerAdapter = HistoryRecordingsPagerAdapter(
            childFragmentManager,
            viewLifecycleOwner.lifecycle,
            isRecordingEnabled
        )
        binding.viewPager.adapter = pagerAdapter

        if (isRecordingEnabled) {
            // Show tabs when recordings are enabled
            binding.tabLayout.visibility = View.VISIBLE
            
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    HistoryRecordingsPagerAdapter.CALLS_TAB_INDEX -> 
                        getString(R.string.history_recordings_tab_calls)
                    HistoryRecordingsPagerAdapter.RECORDINGS_TAB_INDEX -> 
                        getString(R.string.history_recordings_tab_recordings)
                    else -> "Unknown"
                }
            }.attach()
        } else {
            // Hide tabs when only calls tab is available
            binding.tabLayout.visibility = View.GONE
            Log.i("$TAG Hiding tab layout since recordings are disabled")
        }
    }

    private fun setupObservers() {
        Log.i("$TAG Setting up observers")
        
        listViewModel.currentTabIndex.observe(viewLifecycleOwner) { index ->
            if (index != binding.viewPager.currentItem) {
                binding.viewPager.setCurrentItem(index, true)
            }
        }

        // Only set up tab listener if recordings are enabled
        if (checkIfRecordingEnabled()) {
            // Listen for tab changes from TabLayout
            binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                    tab?.let {
                        Log.i("$TAG Tab selected: ${it.position}")
                        listViewModel.setCurrentTab(it.position)
                    }
                }

                override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}

                override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            })
        }
    }

    private fun checkIfRecordingEnabled(): Boolean {
        return try {
            val loginResult = VoxNodeDataManager.getLoginResult()
            val isEnabled = loginResult?.clientRecordingEnabled == 1
            Log.i("$TAG clientRecordingEnabled from login result: ${loginResult?.clientRecordingEnabled}, isEnabled: $isEnabled")
            isEnabled
        } catch (e: Exception) {
            Log.e("$TAG Error checking recording enabled status: ${e.message}")
            false
        }
    }
}
