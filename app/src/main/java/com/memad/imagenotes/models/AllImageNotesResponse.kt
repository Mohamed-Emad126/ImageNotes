package com.memad.imagenotes.models

data class AllImageNotesResponse(
    val `data`: List<ImageNote>,
    val success: Boolean
)