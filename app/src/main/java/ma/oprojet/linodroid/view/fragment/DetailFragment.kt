package ma.oprojet.linodroid.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.facebook.shimmer.Shimmer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.coroutines.flow.collect
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.models.post.Post
import ma.oprojet.linodroid.state.PostState
import ma.oprojet.linodroid.utils.Logger
import ma.oprojet.linodroid.viewmodel.SharedViewModel


class DetailFragment : Fragment(R.layout.fragment_detail) {


    private val viewModel: SharedViewModel by activityViewModels()
    private val navArgs: DetailFragmentArgs by navArgs()

    var posT : Post? = null

    val postId = arguments?.getInt("post_id")

    override fun onStart() {
        super.onStart()
        setUpWebView()
        viewModel.getPostById(navArgs.postId)


        // {B.A} Show icons by default on overflow menu
        val menu = topAppBar.menu
        if (menu.javaClass.simpleName == "MenuBuilder") {
            try {
                val method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    Boolean::class.javaPrimitiveType
                )
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                Logger.e(e.toString(), e.printStackTrace().toString(), e)
            }
        }


        // {B.A}
        topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.menu_settings -> {
                    findNavController().navigate(R.id.SettingsFragment)
                    true
                }

                R.id.menu_item_share -> {
                    posT?.let { post ->

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"

                            val sharedText =
                                "${getString(R.string.readMore)} \"${post.title.rendered}\" " +
                                        "${getString(R.string.wrby)} ${getString(R.string.dev)} ${getString(R.string.postLink)} ${post.link}"

                            putExtra(Intent.EXTRA_TEXT, sharedText)
                        }

                        requireActivity().startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                    }
                    true
                }

                R.id.menu_about -> {
                    findNavController().navigate(R.id.AboutFragment)
                    true
                }

                else -> false
            }
        }



        lifecycleScope.launchWhenStarted {
            viewModel.postState.collect { postState ->
                when (postState) {

                    is PostState.Empty -> {
                        viewModel.getPostById(navArgs.postId)

                    }

                    is PostState.Loading -> {
                        detailShimmerLayout.setShimmer(Shimmer.AlphaHighlightBuilder().setBaseAlpha(0.6f).setHighlightAlpha(0.3f).setDuration(5000).build())
                        shimmerState(isShimmer = true)

                        imgPostError.visibility = View.GONE

                    }

                    is PostState.Success -> {
                        shimmerState(isShimmer = false)

                        showPost(post = postState.post)

                        imgPostError.visibility = View.GONE

                        posT = postState.post

                    }

                    is PostState.Error -> {
                        detailShimmerLayout.isVisible = false
                       /* Toast.makeText(
                            this@DetailFragment.context,
                            postState.message,
                            Toast.LENGTH_LONG
                        ).show()
                        {B.A} The user does not need to see this message, the developer does. */
                        Logger.d("POSTSTATE",postState.message)
                        Snackbar.make(appBarLayout, R.string.posterror, Snackbar.LENGTH_INDEFINITE).setDuration(8000).show()
                        // {B.A} TODO separate the postState error from the network error ("No Internet").
                        imgPostError.visibility = View.VISIBLE
                        imgPostError.alpha = 0f
                        imgPostError.animate().alpha(1f).setDuration(2000).start()

                    }
                }


            }
        }


    }

    override fun onResume() {
        super.onResume()
        topAppBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        buttonShowComments.setOnClickListener {
            findNavController().navigate(
                DetailFragmentDirections.actionDetailFragmentToCommentsFragment(
                    navArgs.postId
                )
            )
        }

    }


    private fun showPost(post: Post) {

        // {B.A} Set Webview to dark for lower SDKs than 29 and have dark mode forced.
        val nightMode:Int = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isdarkInlessQ:Boolean = nightMode ==  Configuration.UI_MODE_NIGHT_YES
        var darkCSS = ""

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            darkCSS = if (isdarkInlessQ) {
                "body {color: white; background: black;}"
            } else {
                ""
            }
        }

        val htmlContent =
            "<!DOCTYPE html> <html> <head> </head><meta name= viewport content= width=device-width initial-scale=1.0 > <style> $darkCSS img{display: block;height: auto;max-width: 100%; margin:1rem auto; -webkit-filter: none !important;filter: none !important;} video{display: inline;width: 100%;} p{height: auto;width: 100%;line-height: 1.6;text-align:justify } iframe, button{display:none} a {color:#24A91A;text-decoration:none} * {overflow-x:scroll} figure {min-width: 100%;margin: auto;} figcaption { text-align: center; } </style> <body> ${post.content.rendered.replace("\"","")} </body></html>"


        // {B.A} Prevent WebView from flashing before loading data.
        postWebView.setBackgroundColor(Color.TRANSPARENT)

        postWebView.loadDataWithBaseURL(
            null,
            htmlContent,
            "text/html; charset=utf-8",
            "UTF-8",
            null
        )



        if (!post._embedded.wp_FeaturedMedia.isNullOrEmpty()){
            Glide.with(this@DetailFragment)
                .load(post._embedded.wp_FeaturedMedia[0].source_url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.kbdtp)
                .into(postImage)
        }




        postTitle.text = Html.fromHtml(Html.fromHtml(post.title.rendered).toString())
        postCategory.text = post._embedded.wp_Term[0][0].name
    }

    private fun shimmerState(isShimmer: Boolean) {

        if (isShimmer) {
            detailShimmerLayout.isVisible = isShimmer

        } else {
            detailShimmerLayout.isVisible = isShimmer
            // {B.A} Leave the visibility control to the onProgressChanged overridden in WebChromeClient object.
            //coordinatorLayout.isVisible = true


        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView() {
        // {B.A} Enable WebView debugging to debug in chrome://inspect in Chrome for PC.
        WebView.setWebContentsDebuggingEnabled(true)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {


                    WebSettingsCompat.setForceDark(
                        postWebView.settings,
                        WebSettingsCompat.FORCE_DARK_ON
                    )
                    Logger.d("WEBVIEW", "Dark mode is forced !")

                }
            }
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {


                    WebSettingsCompat.setForceDark(
                        postWebView.settings,
                        WebSettingsCompat.FORCE_DARK_OFF
                    )
                    Logger.d("WEBVIEW","Dark mode is OFF !")

                }
            }
            else -> {
                // {B.A} Those messages will be printed when the Dark mode is switched and depending on the Webview version.
                Logger.d("WEBVIEW","Do something useful here !")
            }
        }

        postWebView.apply {

            this.fitsSystemWindows = true
            this.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true    //{B.A}

            }
            this.setInitialScale(1)
            

            this.webChromeClient= object : WebChromeClient() {

                private  var mCustomView: View? = null
                private var mCustomViewCallback: CustomViewCallback? = null
                private var mOriginalOrientation = 0
                private var mOriginalSystemUiVisibility = 0

                override fun onHideCustomView() {
                    super.onHideCustomView()
                    (activity!!.window.decorView as FrameLayout).removeView(mCustomView)

                    this.mCustomView = null
                    activity!!.window.decorView.setSystemUiVisibility(this.mOriginalSystemUiVisibility)
                    activity!!.requestedOrientation = this.mOriginalOrientation
                    this.mCustomViewCallback?.onCustomViewHidden()
                    this.mCustomViewCallback=null

                }


                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    super.onShowCustomView(view, callback)


                    if (this.mCustomView != null)
                    {
                        onHideCustomView()
                        return
                    }

                    this.mCustomView = view
                    this.mOriginalSystemUiVisibility = activity?.window?.decorView!!.getSystemUiVisibility()
                    this.mOriginalOrientation = activity!!.requestedOrientation
                    this.mCustomViewCallback = callback

                    (activity!!.window.decorView as FrameLayout).addView(
                        mCustomView,
                        FrameLayout.LayoutParams(-1, -1)
                    )
                    activity!!.window.decorView
                        .setSystemUiVisibility(3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)


                }

                // {B.A} Delay the WebView display by its parent coordinatorLayout, otherwise jumping WebView is a bad UX.
                var pageReady = false

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {

                        if (newProgress >= 100 && !pageReady) {
                            pageReady = true

                            coordinatorLayout?.postDelayed({
                                coordinatorLayout?.isVisible = true
                            }, 120)
                        }
                    }


            }

        }


    }


}