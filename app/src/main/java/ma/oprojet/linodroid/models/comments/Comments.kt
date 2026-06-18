package ma.oprojet.linodroid.models.comments

import com.google.gson.annotations.SerializedName
import ma.oprojet.linodroid.models.post.Content

data class Comments(
    @SerializedName("author_name")
    val author_name: String,
    val content: Content,
    val date: String,
    val id: Int,
    val post: Int,
    @SerializedName("author_avatar_urls")
    val avatar: AvatarUrls
)