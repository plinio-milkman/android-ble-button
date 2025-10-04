package com.android.blebutton

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.regex.Pattern

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onStart() {
        appCompatDelegate.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, MyPreferenceFragment()).commit()
        super.onStart()
    }

    class MyPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preferences)

            setOnPreferenceChangeListenerAndFire(MyPreference.DEVICE_ADDRESS.name.lowercase()) { preference, value ->
                when (Pattern.compile("""([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})""").matcher(value.toString()).matches()) {
                    true -> {
                        preference.summary = value.toString()
                        true
                    }

                    else -> false
                }
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//            TODO("Not yet implemented")
        }

        private fun setOnPreferenceChangeListenerAndFire(key: String, listener: Preference.OnPreferenceChangeListener?) {
            val preference = preferenceManager.findPreference(key) ?: Preference(requireContext())

            preference.onPreferenceChangeListener = listener
            listener?.onPreferenceChange(preference, preferenceManager.sharedPreferences?.getString(key, ""))

        }
    }
}
