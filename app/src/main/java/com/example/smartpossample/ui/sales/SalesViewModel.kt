package com.example.smartpossample.ui.sales

import android.app.Application
import androidx.lifecycle.*
import com.example.smartpossample.persistence.TransactionDatabase
import com.example.smartpossample.persistence.entity.PaymentResultEntity
import eu.nets.lab.smartpos.sdk.payload.PaymentResult
import kotlinx.coroutines.launch

class SalesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TransactionDatabase.getDatabase(application)

    private val _text = MutableLiveData<String>().apply {
        value = "Sales"
    }
    val text: LiveData<String> = _text

    fun setText(text: String) {
        _text.postValue(text)
    }

    fun persistResult(result: PaymentResult) {
        viewModelScope.launch {
            database.paymentResultDao.insert(PaymentResultEntity(result))
        }
    }
}