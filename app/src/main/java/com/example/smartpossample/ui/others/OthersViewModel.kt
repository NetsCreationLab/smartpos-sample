package com.example.smartpossample.ui.others

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartpossample.persistence.TransactionDatabase
import eu.nets.lab.smartpos.sdk.payload.ResultPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OthersViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TransactionDatabase.getDatabase(application)

    fun newest(): LiveData<ResultPayload?> {
        val result = MutableLiveData<ResultPayload?>()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(database.newest())
        }
        return result
    }

    private val _text = MutableLiveData<String>().apply {
        value = "Other functionality"
    }
    val text: LiveData<String> = _text

    fun setText(text: String) {
        _text.postValue(text)
    }
}