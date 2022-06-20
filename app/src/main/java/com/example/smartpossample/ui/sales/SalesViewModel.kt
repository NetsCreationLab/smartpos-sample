package com.example.smartpossample.ui.sales

import android.app.Application
import androidx.lifecycle.*
import com.example.smartpossample.persistence.TransactionDatabase
import eu.nets.lab.smartpos.sdk.payload.PaymentResult
import eu.nets.lab.smartpos.sdk.room.entity.PaymentDataEntity
import eu.nets.lab.smartpos.sdk.room.entity.PaymentResultEntity
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
            database.paymentDataDao.insert(PaymentDataEntity(result.data))
            database.paymentResultDao.insert(PaymentResultEntity(result))
        }
    }
}