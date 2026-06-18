package ma.oprojet.linodroid.view.fragment
// SettingsFragment.kt

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.adapter.ThemeAdapter
import ma.oprojet.linodroid.databinding.FragmentSettingsBinding
import ma.oprojet.linodroid.utils.NotificationScheduler
import ma.oprojet.linodroid.utils.ThemeUtils
import ma.oprojet.linodroid.viewmodel.ThemeViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val themeViewModel: ThemeViewModel by viewModels()

    @Inject
    lateinit var themeUtils: ThemeUtils

    private lateinit var themeAdapter: ThemeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupSwitch()
    }

    private fun setupToolbar() {
        binding.topAppBarSettings.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        themeAdapter = ThemeAdapter { themeItem ->
            applyThemeChange(themeItem.id)
        }

        binding.themesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = themeAdapter
            addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            )
        }
    }

    private fun setupObservers() {
        // Observe themes list
        lifecycleScope.launch {
            themeViewModel.themes.collect { themes ->
                themeAdapter.submitList(themes)

                val savedTheme = themeUtils.getSavedTheme()
                val position = themeViewModel.findThemePosition(savedTheme)
                if (position != -1) {
                    themeAdapter.setSelectedPosition(position)
                    binding.themesRecyclerView.scrollToPosition(position)
                }
            }
        }

        // Observe theme changes from ThemeUtils (if updated elsewhere)
        lifecycleScope.launch {
            themeUtils.currentTheme.asFlow().collect { theme ->
                themeViewModel.setSelectedTheme(theme)
                val position = themeViewModel.findThemePosition(theme)
                if (position != -1) {
                    themeAdapter.setSelectedPosition(position)
                }
            }
        }
    }

    private fun applyThemeChange(themeId: String) {
        themeUtils.saveTheme(themeId)
        themeUtils.applyTheme(themeId)

        // Get the activity's AppCompatDelegate
        val activity = requireActivity() as AppCompatActivity
        // Apply to current activity with animation
        val delegate = activity.delegate

        delegate.localNightMode = themeUtils.getThemeModeInt(themeId)
        delegate.applyDayNight()

        // Show confirmation
        showThemeAppliedMessage(themeId)
    }

    private fun showThemeAppliedMessage(themeId: String) {
        val message = when (themeId) {
            ThemeUtils.THEME_LIGHT -> getString(R.string.theme_applied_light)
            ThemeUtils.THEME_DARK -> getString(R.string.theme_applied_dark)
            else -> getString(R.string.theme_applied_system)
        }

        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupSwitch() {
        binding.switchNotifications.isChecked =
            requireContext()
                .getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("pref_new_post_notifications", true)

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->

            val prefs = requireContext()
                .getSharedPreferences("settings", Context.MODE_PRIVATE)

            prefs.edit().putBoolean("pref_new_post_notifications", isChecked).apply()

            if (isChecked) {
                NotificationScheduler.schedule(requireContext())
                Snackbar.make(binding.root, R.string.notifications_enabled, Snackbar.LENGTH_SHORT).show()
            } else {
                NotificationScheduler.cancel(requireContext())
                Snackbar.make(binding.root, R.string.notifications_disabled, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

}
