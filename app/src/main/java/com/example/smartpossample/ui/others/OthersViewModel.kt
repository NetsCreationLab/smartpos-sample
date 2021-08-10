package com.example.smartpossample.ui.others

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.smartpossample.persistence.TransactionDatabase
import eu.nets.lab.smartpos.sdk.payload.ResultPayload

class OthersViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TransactionDatabase.getDatabase(application)

    val newest: LiveData<ResultPayload> get() = database.newest

    private val _text = MutableLiveData<String>().apply {
        value = "Other functionality"
    }
    val text: LiveData<String> = _text

    fun setText(text: String) {
        _text.postValue(text)
    }
}