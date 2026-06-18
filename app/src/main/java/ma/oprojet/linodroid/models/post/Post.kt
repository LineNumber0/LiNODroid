package ma.oprojet.linodroid.models.post

data class Post(
    val _embedded: Embedded,
    val content: Content,
    val date: String,
    val id: Int,
    val title: Title,
    val excerpt: Excerpt,
    val link: String
)