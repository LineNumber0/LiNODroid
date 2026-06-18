package ma.oprojet.linodroid.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import ma.oprojet.linodroid.models.post.Post
import ma.oprojet.linodroid.network.WordpressApi
import ma.oprojet.linodroid.paging.PostPagingSource
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WordPressRepository @Inject constructor(private val wordpressApi: WordpressApi) {


    fun getPostByCategory(CategoryId: Int) = Pager(
        config = PagingConfig(
            pageSize = 10,
            maxSize = 100,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            PostPagingSource (
                categoryId = CategoryId,
                wordpressApi = wordpressApi
            )
        }
    ).liveData

    suspend fun getCategories() = wordpressApi.getCategories()


    suspend fun getPostById(postId: Int) = wordpressApi.getPostById(postId = postId)

    suspend fun getPostComments(postId: Int) = wordpressApi.getPostComments(postId = postId)

    suspend fun getLatestPost(): Post? {
        return wordpressApi.getLatestPost().firstOrNull()
    }


}