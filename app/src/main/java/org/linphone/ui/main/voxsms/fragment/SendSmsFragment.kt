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
import org.linphone.R
import org.linphone.core.tools.Log
import org.linphone.databinding.SendSmsFragmentBinding
import org.linphone.ui.GenericFragment
import org.linphone.ui.main.voxsms.viewmodel.SendSmsViewModel

@UiThread
class SendSmsFragment : GenericFragment() {
    companion object {
        private const val TAG = "[SendSms Fragment]"
    }

    private lateinit var binding: SendSmsFragmentBinding
    private lateinit var viewModel: SendSmsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("$TAG onCreateView called")
        
        binding = DataBindingUtil.inflate(
            inflater, R.layout.send_sms_fragment, container, false
        )
        
        viewModel = ViewModelProvider(this)[SendSmsViewModel::class.java]
        binding.viewModel = viewModel
        
        // Set lifecycle owner for live data
        binding.lifecycleOwner = viewLifecycleOwner
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.i("$TAG onViewCreated called")
        
        // Observer for any future functionality
        observeToastEvents(viewModel)
        
        setupClickListeners()
        observeEvents()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            Log.i("$TAG Back button clicked")
            findNavController().popBackStack()
        }
        
        binding.sendButton.setOnClickListener {
            Log.i("$TAG Send button clicked")
            viewModel.sendSms()
        }
    }

    private fun observeEvents() {
        Log.i("$TAG Setting up observers for SendSms")
        
        // Observe SMS success event
        viewModel.smsSuccessEvent.observe(viewLifecycleOwner) { event ->
            event.consume {
                Log.i("$TAG SMS sent successfully, navigating back")
                findNavController().popBackStack()
            }
        }
    }
}
