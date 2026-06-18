package ma.oprojet.linodroid.viewmodel
// ThemeViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.models.ThemeItem
import ma.oprojet.linodroid.utils.ThemeUtils
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    themeUtils: ThemeUtils
) : ViewModel() {

    private val _themes = MutableStateFlow<List<ThemeItem>>(emptyList())
    val themes: StateFlow<List<ThemeItem>> = _themes.asStateFlow()

    private val _selectedTheme = MutableStateFlow(themeUtils.getSavedTheme())
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    init {
        loadThemes()
    }

    private fun loadThemes() {
        viewModelScope.launch {
            _themes.value = listOf(
                ThemeItem(
                    id = ThemeUtils.THEME_FOLLOW_SYSTEM,
                    nameResId = R.string.theme_follow_system,
                    iconResId = R.drawable.ic_theme_auto,
                    descriptionResId = R.string.theme_follow_system_desc
                ),
                ThemeItem(
                    id = ThemeUtils.THEME_LIGHT,
                    nameResId = R.string.theme_light,
                    iconResId = R.drawable.ic_theme_light,
                    descriptionResId = R.string.theme_light_desc
                ),
                ThemeItem(
                    id = ThemeUtils.THEME_DARK,
                    nameResId = R.string.theme_dark,
                    iconResId = R.drawable.ic_theme_dark,
                    descriptionResId = R.string.theme_dark_desc
                )
            )
        }
    }

    fun setSelectedTheme(theme: String) {
        _selectedTheme.value = theme
    }

    fun findThemePosition(themeId: String): Int {
        return _themes.value.indexOfFirst { it.id == themeId }
    }
}
