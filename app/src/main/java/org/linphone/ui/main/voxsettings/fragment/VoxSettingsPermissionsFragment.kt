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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.databinding.DataBindingUtil
import org.linphone.R
import org.linphone.compatibility.Compatibility
import org.linphone.core.tools.Log
import org.linphone.databinding.AssistantPermissionsFragmentBinding
import org.linphone.ui.GenericFragment

@UiThread
class VoxSettingsPermissionsFragment : GenericFragment() {
    companion object {
        private const val TAG = "[VoxSettings Permissions Fragment]"
    }

    private lateinit var binding: AssistantPermissionsFragmentBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                Log.i("$TAG Permission [$permissionName] is now granted")
            } else {
                Log.i("$TAG Permission [$permissionName] has been denied")
                allGranted = false
            }
        }

        if (allGranted) {
            Log.i("$TAG All permissions have been granted")
        } else {
            Log.w("$TAG Not all permissions were granted")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.assistant_permissions_fragment, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.setBackClickListener {
            Log.i("$TAG Back button clicked")
            requireActivity().onBackPressed()
        }

        binding.setSkipClickListener {
            Log.i("$TAG Skip button clicked")
            requireActivity().onBackPressed()
        }

        binding.setGrantAllClickListener {
            Log.i("$TAG Grant all permissions clicked")
            requestPermissionLauncher.launch(
                Compatibility.getAllRequiredPermissionsArray()
            )
        }
    }
}
