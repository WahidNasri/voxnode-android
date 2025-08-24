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
package org.linphone.ui.main.historyrecordings.viewmodel

import androidx.annotation.UiThread
import androidx.lifecycle.MutableLiveData
import org.linphone.core.tools.Log
import org.linphone.ui.main.viewmodel.AbstractMainViewModel

@UiThread
class HistoryWithRecordingsViewModel : AbstractMainViewModel() {
    companion object {
        private const val TAG = "[HistoryWithRecordings ViewModel]"
    }

    val currentTabIndex = MutableLiveData<Int>()

    init {
        title.value = "History"
        currentTabIndex.value = 0 // Default to Calls tab
        Log.i("$TAG HistoryWithRecordings ViewModel initialized")
    }

    @UiThread
    override fun filter() {
        // No filtering needed for this container fragment
        // Child fragments handle their own filtering
    }

    @UiThread
    fun setCurrentTab(index: Int) {
        Log.i("$TAG Setting current tab to index [$index]")
        currentTabIndex.value = index
    }
}
