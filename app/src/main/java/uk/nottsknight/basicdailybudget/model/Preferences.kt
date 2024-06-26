package uk.nottsknight.basicdailybudget.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val KEY_CURRENT_ACCOUNT = intPreferencesKey("bdb-current-account")

class PreferencesRepository(prefsStore: DataStore<Preferences>) {
    val currentAccountId: Flow<Int> =
        prefsStore.data.map { prefs -> prefs[KEY_CURRENT_ACCOUNT] ?: -1 }
}