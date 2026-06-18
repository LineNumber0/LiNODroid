package ma.oprojet.linodroid.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.utils.ThemeUtils
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var themeutils: ThemeUtils

    override fun onCreate(savedInstanceState: Bundle?) {

        // Initialize the property
        themeutils = ThemeUtils(this)
        // Apply the current theme before any view is created
        themeutils.applyTheme(themeutils.getSavedTheme())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (intent.getBooleanExtra("from_notification", false)) {

            val postId = intent.getIntExtra("postId", -1)

            val bundle = Bundle().apply {
                putInt("postId", postId)
            }

            findNavController(R.id.NevHostFragment)
                .navigate(R.id.detailFragment, bundle)
        }

    }


}