package ma.oprojet.linodroid.adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlinx.android.synthetic.main.post_view_holder_layout.view.*
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.models.post.Post
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


//time.text = SimpleDateFormat("yyyy-MM-dd").parse(post.date).toString()
// Use best practice to display Date-Time. {B.A}
fun formatPostDate(postDate: String): String {
    val parser = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss",
        Locale.US
    )
    parser.timeZone = TimeZone.getTimeZone("UTC")

    val date = parser.parse(postDate) ?: return ""

    val formatter = DateFormat.getDateTimeInstance(
        DateFormat.FULL,
        DateFormat.SHORT
    )

    return formatter.format(date)
}

class PostListAdapter (val postClick:(postId:Int)->Unit):
    PagingDataAdapter<Post, PostListAdapter.PostViewHolder>(POST_COMPARATOR) {

    inner class PostViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView) {

        private val image = itemView.PostImage
        private val title = itemView.PostTitle
        private val category = itemView.postCategory
        private val time = itemView.PostTime

        init {

            image.setOnClickListener {
                getItem(adapterPosition)?.let { it1 -> postClick(it1.id) }
            }
        }


        fun bind(post: Post) {

            if(!post._embedded.wp_FeaturedMedia.isNullOrEmpty()) {

                Glide.with(context)
                    .load(post._embedded.wp_FeaturedMedia[0].source_url)
                    //.placeholder(ColorDrawable(Color.parseColor("#e2e2e2")))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.kbdtp)
                    .into(image)

            }

            title.text = Html.fromHtml(Html.fromHtml(post.title.rendered).toString())

            category.text = post._embedded.wp_Term[0][0].name

            time.text = formatPostDate(post.date)

        }

    }


    companion object{
        private val POST_COMPARATOR= object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {

                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentItem = getItem(position)

        if (currentItem != null) {
            holder.bind(currentItem)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_view_holder_layout, parent, false)
        return PostViewHolder(view,parent.context)    }


}