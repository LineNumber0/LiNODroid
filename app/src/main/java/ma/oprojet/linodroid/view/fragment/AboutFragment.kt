package ma.oprojet.linodroid.view.fragment

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.databinding.FragmentAboutBinding
import ma.oprojet.linodroid.network.WordpressApi.Companion.WEBSITE

class AboutFragment : Fragment(R.layout.fragment_about) {

	private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

	private lateinit var appVersion: TextView
	private lateinit var devWebsite: TextView
    
        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appVersion = view.findViewById(R.id.app_version)
        devWebsite = view.findViewById(R.id.developer_website)

        devWebsite.text = "$WEBSITE/blog"
        setupToolbar()
        setupAppVersion()
    }
    
	    private fun setupToolbar() {
        binding.topAppBarAbout.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAppVersion() {
        appVersion.text = getString(R.string.version_format, getVersionName())
    }

    private fun getVersionName(): String {
        return try {
            activity?.packageManager?.getPackageInfo(requireActivity().packageName, 0)?.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }

}
