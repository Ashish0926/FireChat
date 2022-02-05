package com.example.firechat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.databinding.ItemContainerUserBinding
import com.example.firechat.listeners.UserListener
import com.example.firechat.models.User

class UsersAdapter(private val users: List<User>, private val userListener: UserListener): RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemContainerUserBinding.inflate(LayoutInflater.from(parent.context),
        parent, false)
        return UserViewHolder(itemContainerUserBinding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(itemContainerUserBinding: ItemContainerUserBinding): RecyclerView.ViewHolder(itemContainerUserBinding.root) {
        private val binding = itemContainerUserBinding

        fun setUserData(user: User){
            binding.textName.text = user.name
            binding.textEmail.text = user.email
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener {
                userListener.onUserClicked(user)
            }
        }
    }

    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }



}