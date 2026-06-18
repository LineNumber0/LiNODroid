package ma.oprojet.linodroid.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ma.oprojet.linodroid.models.post.Post
import ma.oprojet.linodroid.network.WordpressApi
import retrofit2.HttpException
import java.io.IOException


private const val WORDPRESS_STARTING_PAGE_INDEX = 1

class PostPagingSource( private val categoryId:Int,
                        private val wordpressApi: WordpressApi
                        ): PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {

        return try {
            val position = params.key ?: WORDPRESS_STARTING_PAGE_INDEX

            val posts = wordpressApi.getPostsByCategories(categories = categoryId,page = position, perPage = params.loadSize ,embed = true)

            LoadResult.Page(
                data = posts,
                prevKey = if (position == WORDPRESS_STARTING_PAGE_INDEX) null else position - 1,
                nextKey = if (posts.isEmpty()) null else position + 1
            )

        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {

            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        // GPT : (with only return state.anchorPosition) is not correct for Paging 3 and can cause refresh loop.
        // anchorPosition is not a page number.
        // This prevents: unexpected refresh; jumping list position; extra requests.
        return state.anchorPosition?.let { anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }


}