package com.memad.imagenotes.api

import com.memad.imagenotes.models.AllImageNotesResponse
import com.memad.imagenotes.models.ImageNote
import com.memad.imagenotes.models.ImageNoteResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {
    @GET("/notes")
    suspend fun getAllImageNotes(): Response<AllImageNotesResponse>

    @Multipart
    @POST("/note")
    suspend fun saveNoteParts(
        @PartMap ImageNote: HashMap<String, RequestBody>,
        @Part image: MultipartBody.Part
    ): Response<ImageNoteResponse>

    @DELETE("/note/{id}")
    suspend fun deleteNote(@Path("id") id: String): Response<ImageNoteResponse>

    companion object {
        const val BASE_URL = "http://bdee-156-211-140-138.ngrok.io"
        const val IMAGE_BASE_URL = "$BASE_URL/images/l/"
    }
}
