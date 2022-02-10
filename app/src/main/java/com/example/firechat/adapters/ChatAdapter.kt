package com.example.firechat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.databinding.ItemContainerReceivedMessageBinding
import com.example.firechat.databinding.ItemContainerSentMessageBinding
import com.example.firechat.models.ChatMessage

class ChatAdapter(private val chatMessages: List<ChatMessage>, private val senderId: String, private val receiverProfileImage: Bitmap):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_SENT = 1
        const val VIEW_TYPE_RECEIVED = 2
    }


    inner class SentMessageViewHolder(itemContainerSentMessageBinding: ItemContainerSentMessageBinding): RecyclerView.ViewHolder(itemContainerSentMessageBinding.root) {
        private val binding = itemContainerSentMessageBinding

        fun setData(chatMessage: ChatMessage) {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            //Log.w("date", chatMessage.dateTime.toString())
        }
    }

    inner class ReceiverMessageViewHolder(itemContainerReceivedMessageBinding: ItemContainerReceivedMessageBinding): RecyclerView.ViewHolder(itemContainerReceivedMessageBinding.root) {
        private val binding = itemContainerReceivedMessageBinding

        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap){
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dateTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_SENT){
            return SentMessageViewHolder(
                ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }else {
            return ReceiverMessageViewHolder(
                ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            (holder as SentMessageViewHolder).setData(chatMessages[position])
        } else {
            (holder as ReceiverMessageViewHolder).setData(chatMessages[position], receiverProfileImage)
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        if(chatMessages[position].senderId == senderId){
            return VIEW_TYPE_SENT
        }else{
            return VIEW_TYPE_RECEIVED
        }
    }
}