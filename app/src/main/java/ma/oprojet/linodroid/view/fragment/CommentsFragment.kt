package ma.oprojet.linodroid.view.fragment

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_comments.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.adapter.CommentsListAdapter
import ma.oprojet.linodroid.utils.Logger
import ma.oprojet.linodroid.viewmodel.SharedViewModel


class CommentsFragment : Fragment(R.layout.fragment_comments) {

    private val viewModel: SharedViewModel by activityViewModels()

    private val navArgs:CommentsFragmentArgs by navArgs()

    private val commentAdapter = CommentsListAdapter()


    override fun onStart() {
        super.onStart()
        setUpRecyclerView()

        viewModel.getPostComments(navArgs.postId)

        NoComments.isVisible = false
        ErrorComments.isVisible = false

    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.comments.collect { Comments ->

                when {
                    Comments.isNotEmpty() -> {

                        NoComments.isVisible = false
                        CommentsList.isVisible = true
                        commload.isVisible = false

                        commentAdapter.submitList(Comments)

                    }
                    Comments.isEmpty() -> {

                        NoComments.isVisible = true
                        CommentsList.isVisible = false
                        commload.isVisible = false

                    }
                    else -> {

                        commload.isVisible = false
                        NoComments.isVisible = false
                        ErrorComments.isVisible = true
                    }
                }

            }
        }


        commentTopAppBar.setNavigationOnClickListener {

            findNavController().popBackStack()

        }

        // {B.A} Hide share in comments.
        val shareItem = commentTopAppBar.menu.findItem(R.id.menu_item_share)
        shareItem?.isVisible = false


        // {B.A} Show icons by default on overflow menu
        val menu = commentTopAppBar.menu
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
        commentTopAppBar.setOnMenuItemClickListener {
            when (it.itemId) {

                R.id.menu_settings -> {
                    findNavController().navigate(R.id.SettingsFragment)
                    true
                }

                R.id.menu_about -> {
                findNavController().navigate(R.id.AboutFragment)
                true
                }

                else -> false
            }
        }
    }



    private fun setUpRecyclerView() {

        CommentsList.apply {

            this.adapter = commentAdapter

        }
    }

}