package ma.oprojet.linodroid.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ma.oprojet.linodroid.network.WordpressApi
import ma.oprojet.linodroid.network.WordpressApi.Companion.WEBSITE
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

class NewPostWorker (
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {

            val retrofit = Retrofit.Builder()
                .baseUrl("$WEBSITE/wp-json/wp/v2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(WordpressApi::class.java)

            val posts = api.getLatestPost()
            val latestPost = posts.firstOrNull()

            val notificationHelper = NotificationHelper(applicationContext)

            val prefs = applicationContext
                .getSharedPreferences("posts", Context.MODE_PRIVATE)

            val savedPostId = prefs.getInt("last_post_id", -1)


            if (latestPost != null && latestPost.id != savedPostId) {

                val imageUrl = latestPost._embedded
                    ?.wp_FeaturedMedia
                    ?.firstOrNull()
                    ?.source_url

                val bitmap = downloadBitmap(imageUrl)

                notificationHelper.createChannel()

                notificationHelper.showNotification(
                    latestPost.id,
                    latestPost.title.rendered,
                    latestPost.excerpt.rendered,
                    bitmap
                )

                prefs.edit().putInt("last_post_id", latestPost.id).apply()
            }


            Logger.d("WORKMANAGER_DEBUG", "Default Worker for new posts has been started !")
            Result.success()

        } catch (e: Exception) {
            Result.retry()
            Logger.e(e.toString(), e.printStackTrace().toString(), e)
            return Result.failure()
        }
    }

    private suspend fun downloadBitmap(url: String?): Bitmap? {
        return try {
            if (url == null) return null

            withContext(Dispatchers.IO) {
                val connection = URL(url).openConnection()
                connection.connect()
                val input = connection.getInputStream()
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            null
        }
    }
}
