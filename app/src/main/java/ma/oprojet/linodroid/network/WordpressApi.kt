package ma.oprojet.linodroid.network

import ma.oprojet.linodroid.models.category.Categories
import ma.oprojet.linodroid.models.comments.Comments
import ma.oprojet.linodroid.models.post.Post
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WordpressApi {

    companion object {
        const val BASE_URL = "https://oprojet.ma"
        const val FOLDER = "/ln0wp"
        const val WEBSITE = BASE_URL + FOLDER

    }


    @GET("$FOLDER/wp-json/wp/v2/posts")
    suspend fun getPostsByCategories(
        @Query("categories") categories:Int,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("_embed") embed: Boolean
    ) :List<Post>



    @GET("$FOLDER/wp-json/wp/v2/categories?hide_empty=true&per_page=50")
    suspend fun getCategories():List<Categories>


    @GET("$FOLDER/wp-json/wp/v2/posts/{postId}?&_embed=true")
    suspend fun getPostById(
        @Path("postId") postId:Int
    ): Post


    @GET("$FOLDER/wp-json/wp/v2/comments")
    suspend fun getPostComments(
        @Query("post") postId: Int
    ):List<Comments>

    @GET("$FOLDER/wp-json/wp/v2/posts?_embed&per_page=1")
    suspend fun getLatestPost(): List<Post>

}