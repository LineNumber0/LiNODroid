package ma.oprojet.linodroid.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import ma.oprojet.linodroid.models.category.Categories
import ma.oprojet.linodroid.models.comments.Comments
import ma.oprojet.linodroid.repository.WordPressRepository
import ma.oprojet.linodroid.state.PostState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ma.oprojet.linodroid.models.post.Post
import ma.oprojet.linodroid.utils.Logger
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(private val wordPressRepository: WordPressRepository) : ViewModel() {

    var tabLayoutPosition = 0
    private var localPostId = 0


    private val currentCategoryPosition = MutableLiveData(DEFAULT_CATEGORY_POSITION)

    // GPT : Cache for Paging streams
    private val pagerCache = mutableMapOf<Int, LiveData<PagingData<Post>>>()

    // {B.A} Modify the old code to fix a bug when onResume() from notification and optimize cache.
    val posts = currentCategoryPosition.switchMap { categoryPosition ->

        val list = categories.value

        val categoryId =
            if (list.isNotEmpty() && categoryPosition < list.size) {
                list[categoryPosition].id
            } else {
                DEFAULT_CATEGORY_POSITION
            }

        pagerCache.getOrPut(categoryId) {

            wordPressRepository
                .getPostByCategory(categoryId)
                .cachedIn(viewModelScope)

        }
    }


    private val _categoryList = MutableStateFlow<List<Categories>>(emptyList())
    val categories: StateFlow<List<Categories>> = _categoryList



    private val _postState = MutableStateFlow<PostState>(PostState.Empty)
    val postState: StateFlow<PostState> = _postState


    private val _commentEventChannel = Channel<List<Comments>>()
    val comments = _commentEventChannel.receiveAsFlow()


    init {

        viewModelScope.launch {

            try {
                // {B.A} filter out categories with zero post.
                // Combine with "?hide_empty=true" in WordPressRepository.
                // GPT : "Kotlin filter acts as a safety fallback."
                val result = wordPressRepository.getCategories()

                _categoryList.value = result.filter { it.count > 0 }

            } catch (exception: IOException) {
                Logger.d("CATEGORIES_IO", exception.toString())

            } catch (exception: HttpException) {
                Logger.d("CATEGORIES_HTTP", exception.toString())
            }


        }
    }

    fun getPostById(postId: Int) {
        if (postId != localPostId) {
            viewModelScope.launch {
                _postState.value = PostState.Loading


                try {
                    _postState.value =
                        PostState.Success(wordPressRepository.getPostById(postId = postId))
                        localPostId = postId

                } catch (exception: IOException) {
                    _postState.value = PostState.Error(exception.message.toString())
                    Logger.d("POSTSTATE_IO", _postState.value.toString())
                } catch (exception: HttpException) {
                    _postState.value = PostState.Error(exception.message.toString())
                    Logger.d("POSTSTATE_HTTP", _postState.value.toString())

                }

            }

        }

    }


    fun getPostByCategory(categoryPosition: Int) {

        currentCategoryPosition.value = categoryPosition

    }

    fun getPostComments(postId: Int) {

        viewModelScope.launch {

            try {

                _commentEventChannel.send(wordPressRepository.getPostComments(postId))
            } catch (e: Exception) {
                Logger.d("COMMENTEVENTCHANNEL", e.toString())
            }
        }


    }


    fun saveTabLayoutPosition(position: Int) {
        tabLayoutPosition = position
        Logger.d("tabPosition", "the tab position is $tabLayoutPosition")
    }

    companion object {
        private const val DEFAULT_CATEGORY_POSITION = 0
    }


}