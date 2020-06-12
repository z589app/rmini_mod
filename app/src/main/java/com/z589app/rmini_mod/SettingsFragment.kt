package com.z589app.rmini_mod

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import java.io.File

const val TAG_PREF = "rmini_mod.pref"
const val SHARED_PREF_DIR = "/sdcard/z589.rmini_mod/"
const val SHARED_PREF_FILE = "pref.xml"


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onResume() {
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        super.onResume()
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        p0?.edit()?.commit()

        val fromDir = File(context?.filesDir, "../shared_prefs")
        val fromFile = fromDir.listFiles()[0]
        Log.d(TAG_PREF, fromFile.toString())
        // val toDir = context?.getExternalFilesDir("../shared_prefs")
        val toDir = File(SHARED_PREF_DIR)
        if(!toDir?.exists()!!) toDir?.mkdirs()
        val toFile = File(toDir, SHARED_PREF_FILE)
        Log.d(TAG_PREF, toFile.toString())

        fromFile.copyTo(toFile, true)

        Log.d(TAG_PREF, "RET: " + toFile.setReadable(true, false))
        Log.d(TAG_PREF, "RET: " + fromFile.setReadable(true, false))
        Log.d(TAG_PREF, "RET: " + toFile.canRead())
        Log.d(TAG_PREF, "RET: " + fromFile.canRead())
        return
    }

}