package com.memad.imagenotes.api

import com.memad.imagenotes.models.AllImageNotesResponse
import com.memad.imagenotes.models.ImageNote
import com.memad.imagenotes.models.ImageNoteResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Part
import javax.inject.Inject

class MainRepository @Inject constructor(private val apiClient: ApiClient) {

    suspend fun getAllImageNotes(): Response<AllImageNotesResponse> {
        return apiClient.getAllImageNotes()
    }

    suspend fun deleteNote(id: String): Response<ImageNoteResponse> {
        return apiClient.deleteNote(id)
    }

    suspend fun saveNote(
        imageNote: HashMap<String, RequestBody>,
        image: MultipartBody.Part
    ): Response<ImageNoteResponse> {
        return apiClient.saveNoteParts(imageNote, image)
    }
}