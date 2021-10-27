package com.memad.imagenotes.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.memad.imagenotes.R
import com.memad.imagenotes.api.ApiClient.Companion.IMAGE_BASE_URL
import com.memad.imagenotes.databinding.ActivityMainBinding
import com.memad.imagenotes.databinding.ItemBinding
import com.memad.imagenotes.models.ImageNote
import javax.inject.Inject

class ImageNoteAdapter @Inject constructor() :
    RecyclerView.Adapter<ImageNoteAdapter.ImageNoteViewHolder>() {
    private var deleteAction: ((Int) -> Unit)? = null

    var imageNotesList: MutableList<ImageNote>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun setDeleteActionClickListener(listener: (Int) -> Unit) {
        this.deleteAction = listener
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageNoteViewHolder {
        return ImageNoteViewHolder(
            ItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: ImageNoteViewHolder, position: Int) {
        holder.itemBinding.titleText.text = imageNotesList?.get(position)?.title
        holder.itemBinding.descriptionText.text = imageNotesList?.get(position)?.content
        if(imageNotesList?.get(position)?.image == null){
            holder.itemBinding.itemImage.visibility = View.GONE
        }
        if(imageNotesList?.get(position)?.title == null){
            holder.itemBinding.titleText.visibility = View.GONE
        }
        holder.itemBinding.itemImage.load(
            "$IMAGE_BASE_URL${imageNotesList?.get(position)?.image}"
        ) {
            placeholder(R.drawable.ic_image)
        }
    }

    override fun getItemCount(): Int {
        return imageNotesList?.size ?: 0
    }

    /////////////////////////////////////////////////////////////////
    /////////////////////////ViewHolder//////////////////////////////
    /////////////////////////////////////////////////////////////////
    inner class ImageNoteViewHolder(val itemBinding: ItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        init {
            itemBinding.deleteButton.setOnClickListener {
                deleteAction?.let {
                    it(adapterPosition)
                }
            }
        }

    }

}