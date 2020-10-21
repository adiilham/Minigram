package com.minigram.data

import android.content.Context

class SharedPreferenceData {
    companion object {

        private const val PREF_FILE = "minigram_data"

        private fun generateKey(keyCode: Int): String? {
            return when (keyCode) {
                // Login: String
                1 -> "user_id"
                2 -> "user_name"

                else -> null
            }
        }

        fun setString(context: Context?, key: Int, value: String) {
            val settings = context?.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            val editor = settings?.edit()
            editor?.putString(generateKey(key), value)
            editor?.apply()
        }
        fun getString(context: Context?, key: Int, defValue: String): String {
            val settings = context?.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            return settings?.getString(generateKey(key), defValue) ?: defValue
        }

    }
}