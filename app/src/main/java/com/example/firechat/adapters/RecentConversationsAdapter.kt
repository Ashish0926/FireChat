package com.example.firechat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.databinding.ItemContainerRecentBinding
import com.example.firechat.listeners.ConversionListener
import com.example.firechat.models.ChatMessage
import com.example.firechat.models.User

class RecentConversationsAdapter(private val chatMessages: List<ChatMessage>, private val conversionListener: ConversionListener): RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>() {

    inner class ConversionViewHolder(itemContainerRecentBinding: ItemContainerRecentBinding): RecyclerView.ViewHolder(itemContainerRecentBinding.root) {
        private val binding = itemContainerRecentBinding

        fun setData(chatMessage: ChatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage.toString()))
            binding.textName.text = chatMessage.conversionName.toString()
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener {
                val user = User(
                    chatMessage.conversionName.toString(),
                    null,
                    chatMessage.conversionImage.toString(),
                    null,
                    chatMessage.conversionId.toString()
                )
                conversionListener.onConversionClicked(user)
            }
        }
    }

    private fun getConversionImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        val itemContainerRecentBinding = ItemContainerRecentBinding.inflate(LayoutInflater.from(parent.context),
        parent, false)
        return ConversionViewHolder(itemContainerRecentBinding)
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessages[position])
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }
}