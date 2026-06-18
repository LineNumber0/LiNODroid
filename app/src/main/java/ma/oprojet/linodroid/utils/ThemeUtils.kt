package ma.oprojet.linodroid.utils
// ThemeUtils.kt  (DeepSeek)

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    companion object {
        const val THEME_FOLLOW_SYSTEM = "follow_system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }

    private val _currentTheme = MutableLiveData<String>()
    val currentTheme: LiveData<String> = _currentTheme

    init {
        _currentTheme.value = getSavedTheme()
    }

    fun getSavedTheme(): String {
        return prefs.getString("Theme.LiNODroid", THEME_FOLLOW_SYSTEM) ?: THEME_FOLLOW_SYSTEM
    }

    fun saveTheme(theme: String) {
        prefs.edit().putString("Theme.LiNODroid", theme).apply()
        _currentTheme.postValue(theme)
    }

    fun applyTheme(theme: String) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun initTheme() {
        val saved = getSavedTheme()
        _currentTheme.value = saved
        applyTheme(saved)
    }

    fun getThemeModeInt(theme: String): Int = when (theme) {
        THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
        else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
}
