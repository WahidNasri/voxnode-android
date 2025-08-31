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
package org.linphone.ui.main.historyrecordings.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.linphone.core.tools.Log
import org.linphone.ui.main.history.fragment.HistoryListFragment
import org.linphone.ui.main.recordings.fragment.RecordingsListFragment

class HistoryRecordingsPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val recordingEnabled: Boolean = true
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    companion object {
        private const val TAG = "[HistoryRecordingsPagerAdapter]"
        const val CALLS_TAB_INDEX = 0
        const val RECORDINGS_TAB_INDEX = 1
        const val TAB_COUNT_WITH_RECORDINGS = 2
        const val TAB_COUNT_WITHOUT_RECORDINGS = 1
    }

    override fun getItemCount(): Int {
        val count = if (recordingEnabled) TAB_COUNT_WITH_RECORDINGS else TAB_COUNT_WITHOUT_RECORDINGS
        Log.i("$TAG Item count: $count (recordingEnabled: $recordingEnabled)")
        return count
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            CALLS_TAB_INDEX -> {
                Log.i("$TAG Creating HistoryListFragment for position $position")
                HistoryListFragment()
            }
            RECORDINGS_TAB_INDEX -> {
                if (recordingEnabled) {
                    Log.i("$TAG Creating RecordingsListFragment for position $position")
                    RecordingsListFragment()
                } else {
                    Log.e("$TAG Attempted to create RecordingsListFragment when recordings are disabled")
                    throw IllegalArgumentException("Recordings tab not available when recordings are disabled")
                }
            }
            else -> {
                Log.e("$TAG Invalid tab position: $position")
                throw IllegalArgumentException("Invalid tab position: $position")
            }
        }
    }
}
