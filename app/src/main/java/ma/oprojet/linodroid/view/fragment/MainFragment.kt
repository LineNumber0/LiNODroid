package ma.oprojet.linodroid.view.fragment

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.facebook.shimmer.Shimmer
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.adapter.PostListAdapter
import ma.oprojet.linodroid.adapter.PostLoadStateAdapter
import ma.oprojet.linodroid.models.category.Categories
import ma.oprojet.linodroid.utils.Logger
import ma.oprojet.linodroid.viewmodel.SharedViewModel


class MainFragment : Fragment(R.layout.fragment_main) {

    private val viewModel: SharedViewModel by activityViewModels()
    private val postAdapter = PostListAdapter { postId ->

        findNavController().navigate(MainFragmentDirections.actionGlobalDetailFragment(postId))

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        // {A.B} set a slower Shimmer effect with dark highlight color.
        val shimmerSpeed = Shimmer.AlphaHighlightBuilder().setBaseAlpha(0.6f).setHighlightAlpha(0.3f).setDuration(5000).build()
        tablayout_shimmer_view_container.setShimmer(shimmerSpeed)
        list_shimmer_view_container.setShimmer(shimmerSpeed)


        tablayout_shimmer_view_container.startShimmer()

        lifecycleScope.launch {


            postAdapter.loadStateFlow.map {
                it.refresh

            }
                .distinctUntilChanged()
                .collect { LoadState ->

                    when (LoadState) {
                        is LoadState.Loading -> {

                            HomePostList.isVisible = false
                            list_shimmer_view_container.isVisible = true

                            list_shimmer_view_container.startShimmer()
                            imgPostErrorMain.visibility = View.GONE

                        }
                        is LoadState.NotLoading -> {

                            list_shimmer_view_container.stopShimmer()
                            list_shimmer_view_container.isVisible = false
                            HomePostList.isVisible = true

                            imgPostErrorMain.visibility = View.GONE

                        }
                        is LoadState.Error -> {
                            Logger.d("LOADSTATE", "Error in LoadState !?")
                            Logger.d("LOADSTATE", LoadState.error.toString())
                            list_shimmer_view_container.isVisible = false
                            tablayout_shimmer_view_container.isVisible = false

                            Snackbar.make(ContraintLayoutMain, R.string.posterror, Snackbar.LENGTH_INDEFINITE).setDuration(8000).show()
                            // {B.A} TODO separate the LoadState error from the network error ("No Internet").
                            imgPostErrorMain.visibility = View.VISIBLE
                            imgPostErrorMain.alpha = 0f
                            imgPostErrorMain.animate().alpha(1f).setDuration(2000).start()
                        }
                    }

                }
        }


        viewModel.posts.observe(viewLifecycleOwner) { pagingData ->

            postAdapter.submitData(lifecycle = lifecycle, pagingData = pagingData)

        }


        lifecycleScope.launchWhenStarted {

            viewModel.categories.collect { categories ->

                if (categories.isNotEmpty()) {
                    tablayout_shimmer_view_container.stopShimmer()

                    tablayout_shimmer_view_container.isVisible = false
                    tabLayout.isVisible = true

                    setUpCategoriesTabLayout(categories)
                }

            }
        }


        // GPT : control loading state. Shimmer only appears on first load.
        postAdapter.addLoadStateListener { LoadState ->
            tablayout_shimmer_view_container?.isVisible = LoadState.refresh is LoadState.Loading
            list_shimmer_view_container?.isVisible = LoadState.refresh is LoadState.Loading
            HomePostList?.isVisible = LoadState.refresh is LoadState.NotLoading
        }


    }

    override fun onStart() {
        super.onStart()

          tabLayout.setScrollPosition(viewModel.tabLayoutPosition,0f,false)
    }


    override fun onResume() {
        super.onResume()

        tabLayout.getTabAt(viewModel.tabLayoutPosition)?.select()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {

                HomePostList.scrollToPosition(0)

                viewModel.getPostByCategory(tab!!.position)
                viewModel.saveTabLayoutPosition(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })

        // {B.A} From GPT add swipe left/right code.
        // TODO this still has a minor bug when swiping up/down on the selected tab shortly after the swipe left/right.

        val minSwipeDistance = resources.displayMetrics.widthPixels / 3
        val edgeSize = 80 * resources.displayMetrics.density   // 80dp edge zone

        var startX = 0f
        var startY = 0f

        HomePostList.setOnTouchListener { v, event ->

            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    false
                }

                MotionEvent.ACTION_UP -> {

                    val dx = event.x - startX
                    val dy = event.y - startY

                    val startFromEdge =
                        startX < edgeSize || startX > v.width - edgeSize

                    if (startFromEdge &&
                        kotlin.math.abs(dx) > minSwipeDistance &&
                        kotlin.math.abs(dx) > kotlin.math.abs(dy) * 2) {

                        val current = tabLayout.selectedTabPosition
                        val count = tabLayout.tabCount

                        if (dx > 0 && current > 0) {
                            tabLayout.getTabAt(current - 1)?.select()
                        } else if (dx < 0 && current < count - 1) {
                            tabLayout.getTabAt(current + 1)?.select()
                        }

                        true
                    } else {
                        v.performClick()
                        false
                    }
                }

                else -> false
            }
        }





    }


    private fun setUpRecyclerView() {
        HomePostList.apply {
            this.adapter = postAdapter.withLoadStateFooter(PostLoadStateAdapter())
        }
    }

    private fun setUpCategoriesTabLayout(categories: List<Categories>) {

        // {B.A} Clear all tabs before setting them up to fix a bug when onResume() from notification.
        tabLayout.removeAllTabs()

        for (category in categories) {
            val tab = tabLayout.newTab()
            tab.text = category.name
            tabLayout.addTab(tab)
        }


    }


}