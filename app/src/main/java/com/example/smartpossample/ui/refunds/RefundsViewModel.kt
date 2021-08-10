package com.example.smartpossample.ui.refunds

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartpossample.persistence.TransactionDatabase
import com.example.smartpossample.persistence.entity.RefundResultEntity
import eu.nets.lab.smartpos.sdk.payload.RefundResult
import kotlinx.coroutines.launch

class RefundsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TransactionDatabase.getDatabase(application)

    private val _text = MutableLiveData<String>().apply {
        value = "Refunds"
    }
    val text: LiveData<String> = _text

    fun setText(text: String) {
        _text.postValue(text)
    }

    fun persistResult(result: RefundResult) {
        viewModelScope.launch {
            database.refundResultDao.insert(RefundResultEntity(result))
        }
    }
}