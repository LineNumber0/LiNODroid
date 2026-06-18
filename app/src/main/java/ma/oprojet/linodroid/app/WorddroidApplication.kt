package ma.oprojet.linodroid.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ma.oprojet.linodroid.utils.NotificationScheduler
import ma.oprojet.linodroid.utils.ThemeUtils
import javax.inject.Inject


@HiltAndroidApp
class LiNODroidApplication: Application()  {

    @Inject
    lateinit var themeutils: ThemeUtils
    override fun onCreate() {
        super.onCreate()
        // Initialize the property
        themeutils = ThemeUtils(this)
        // Initialize theme from preferences
        themeutils.initTheme()


        /* GPT : Restore state on app start (Important)
        If notifications are enabled by default, schedule the worker once at app start (MainActivity or Application class):
        */
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val enabled = prefs.getBoolean("pref_new_post_notifications", true)

        if (enabled) {
            NotificationScheduler.schedule(this)
        }

    }
}