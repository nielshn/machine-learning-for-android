package com.dicoding.asclepius.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.data.MainRepository
import com.dicoding.asclepius.helper.Injection

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory (private val mainRepository: MainRepository): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(mainRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class data : ${modelClass.name} please create new ViewModel at factory")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MainViewModelFactory? = null

        fun getInstance(context: Context) : MainViewModelFactory =
            INSTANCE ?: synchronized(this) {
                val historyRepository = Injection.provideRepository(context)
                INSTANCE ?: MainViewModelFactory(historyRepository)
            }.also { INSTANCE = it }
    }
}