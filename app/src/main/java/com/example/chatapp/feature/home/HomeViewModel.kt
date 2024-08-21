package com.example.chatapp.feature.home

import androidx.lifecycle.ViewModel
import com.example.chatapp.model.Channel
import com.google.firebase.Firebase
import com.google.firebase.database.database

class HomeViewModel : ViewModel() {

    private val firebaseDatabase = Firebase.database

    init {
        getChannels()
    }

    private fun getChannels() {
        firebaseDatabase.getReference("channel").get().addOnSuccessListener {
            val list = mutableListOf<Channel>()
            it.children.forEach { data ->
                val channel = Channel(data.key!!, data.value.toString())
                list.add(channel)
            }

        }

    }
}