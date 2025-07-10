package com.krishhh.knowyouringredients.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.krishhh.knowyouringredients.model.HistoryEntry

object HistoryManager {

    private fun getPrefsName(): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        return "search_history_$uid"
    }

    fun saveHistory(context: Context, phrase: String) {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        val json = prefs.getString("history_list", "[]")
        val type = object : TypeToken<MutableList<HistoryEntry>>() {}.type
        val list: MutableList<HistoryEntry> = Gson().fromJson(json, type)

        // Remove duplicates
        list.removeAll { it.phrase.equals(phrase, true) }
        list.add(0, HistoryEntry(phrase))

        prefs.edit().putString("history_list", Gson().toJson(list)).apply()
    }

    fun getHistory(context: Context): List<HistoryEntry> {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        val json = prefs.getString("history_list", "[]")
        val type = object : TypeToken<List<HistoryEntry>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveAll(context: Context, entries: List<HistoryEntry>) {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit().putString("history_list", Gson().toJson(entries)).apply()
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
