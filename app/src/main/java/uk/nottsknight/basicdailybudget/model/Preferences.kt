package uk.nottsknight.basicdailybudget.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val KEY_CURRENT_ACCOUNT = intPreferencesKey("bdb-current-account")

class PreferencesRepository(private val prefsStore: DataStore<Preferences>) {
    val currentAccountId: Flow<Int> =
        prefsStore.data.map { prefs -> prefs[KEY_CURRENT_ACCOUNT] ?: -1 }

    suspend fun setCurrentAccountId(id: Int) = withContext(Dispatchers.IO) {
        prefsStore.edit { prefs -> prefs[KEY_CURRENT_ACCOUNT] = id }
    }
}