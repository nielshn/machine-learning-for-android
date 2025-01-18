package com.dicoding.asclepius.ui.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.BuildConfig
import com.dicoding.asclepius.data.MainRepository
import com.dicoding.asclepius.data.api.response.ArticlesItem
import com.dicoding.asclepius.data.api.retrofit.ApiConfig
import com.dicoding.asclepius.data.local.entity.HistoryEntity
import kotlinx.coroutines.launch

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _listNews = MutableLiveData<List<ArticlesItem>>()
    val listNews: LiveData<List<ArticlesItem>> = _listNews

    private val _croppedImageUri = MutableLiveData<Uri?>()
    val croppedImageUri: LiveData<Uri?> = _croppedImageUri

    private val _listHistory = MutableLiveData<List<HistoryEntity>>()
    val listHistory: LiveData<List<HistoryEntity>> = _listHistory

    init {
        fetchNews()
        getAllImageDetectionHistory()
    }

    fun clearHistoryOnAppLaunch(){
        viewModelScope.launch {
            mainRepository.deleteAllHistory()
            getAllImageDetectionHistory()
        }
    }
    private fun fetchNews() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.API_KEY
                val response = ApiConfig.getApiService().getNews(apiKey)
                val filteredArticles = response.articles.filter { article ->
                    article.title!= "[Removed]" && article.description!= "[Removed]"
                }
                _listNews.value = filteredArticles
            }catch (e: Exception) {
                Log.d(TAG, "fetchNews: ${e.message}")
            }finally {
                _isLoading.value = false
            }
        }
    }

    fun setCroppedImageUri(uri: Uri?) {
        _croppedImageUri.value = uri
    }

    fun saveImageDetectionHistory(history: HistoryEntity) {
        viewModelScope.launch {
            mainRepository.insertHistory(history)
            getAllImageDetectionHistory()
        }
    }

    fun getAllImageDetectionHistory() {
        viewModelScope.launch {
            mainRepository.getAllHistory().observeForever { histories ->
                _listHistory.value = histories
            }
        }
    }

    companion object{
        const val TAG = "MainViewModel"
    }
}