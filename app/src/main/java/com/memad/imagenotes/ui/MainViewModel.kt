package com.memad.imagenotes.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memad.imagenotes.api.MainRepository
import com.memad.imagenotes.models.ImageNote
import com.memad.imagenotes.models.ImageNoteResponse
import com.memad.imagenotes.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject
constructor(private val mainRepository: MainRepository) : ViewModel() {

    private val _imageNotes =
        MutableStateFlow<Resource<List<ImageNote>>>(Resource.Loading(null))
    val imageNotes: StateFlow<Resource<List<ImageNote>>> = _imageNotes

    private val _addDeleteStates =
        MutableLiveData<Resource<ImageNoteResponse>?>()
    val addDeleteStates: LiveData<Resource<ImageNoteResponse>?> = _addDeleteStates


    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
        _imageNotes.value = Resource.Error(e)
    }
    private val coroutineAddDeleteExceptionHandler = CoroutineExceptionHandler { _, e ->
        Log.e("TAG", "coroutineAddDeleteExceptionHandler: $e", )
        _addDeleteStates.value = Resource.Error(e)
    }


    init {
        getAllImageNotes()
    }

    fun getAllImageNotes() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val response = mainRepository.getAllImageNotes()
            if (response.isSuccessful) {
                _imageNotes.value = Resource.Success(response.body()!!.data.reversed())
            } else {
                _imageNotes.value = Resource.Error(Throwable(response.message()))
            }
        }
    }

    fun addImageNote(imageNote: HashMap<String, RequestBody>,
                     image: MultipartBody.Part) {
        _addDeleteStates.value = Resource.Loading(null)
        viewModelScope.launch(Dispatchers.IO + coroutineAddDeleteExceptionHandler) {
            val addResponse = mainRepository.saveNote(imageNote, image)
            withContext(Dispatchers.Main){
                dealWithResponse(addResponse)
            }
        }
    }

    fun deleteImageNote(id: String) {
        _addDeleteStates.value = Resource.Loading(null)
        viewModelScope.launch(Dispatchers.IO + coroutineAddDeleteExceptionHandler) {
            val deleteResponse = mainRepository.deleteNote(id)
            withContext(Dispatchers.Main){
                dealWithResponse(deleteResponse)
            }
        }
    }

    private fun dealWithResponse(addResponse: Response<ImageNoteResponse>) {
        if (addResponse.isSuccessful && addResponse.body()!!.success) {
            _addDeleteStates.value = Resource.Success(addResponse.body()!!)
        } else {
            _addDeleteStates.value = Resource.Error(Throwable(addResponse.message()))
        }
    }
}
