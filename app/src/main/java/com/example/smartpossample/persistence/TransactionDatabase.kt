package com.example.smartpossample.persistence

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.room.*
import com.example.smartpossample.persistence.entity.PaymentResultEntity
import com.example.smartpossample.persistence.entity.RefundResultEntity
import eu.nets.lab.smartpos.sdk.client.NetsConverters
import eu.nets.lab.smartpos.sdk.payload.ResultPayload
import eu.nets.lab.smartpos.sdk.payload.paymentResult
import eu.nets.lab.smartpos.sdk.payload.refundResult

@Dao
@Suppress("unused")
interface PaymentResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: PaymentResultEntity)

    @get:Query("select * from payment_results order by epochTimestamp desc limit 1")
    val newest: LiveData<PaymentResultEntity?>
}

@Dao
@Suppress("unused")
interface RefundResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: RefundResultEntity)

    @get:Query("select * from refund_result order by epochTimestamp desc limit 1")
    val newest: LiveData<RefundResultEntity?>
}

@Database(entities = [PaymentResultEntity::class, RefundResultEntity::class], version = 1, exportSchema = false)
@TypeConverters(NetsConverters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract val paymentResultDao: PaymentResultDao
    abstract val refundResultDao: RefundResultDao

    val newest: LiveData<ResultPayload> = MediatorLiveData<ResultPayload>().apply {
        addSource(paymentResultDao.newest) { v -> if (v != null) this.value = paymentResult {
            uuid = v.uuid
            status = v.status
            method = v.method
            epochTimestamp = v.epochTimestamp
            aux copyFrom v.aux
            data = v.data
        } }
        addSource(refundResultDao.newest) { v -> if (v != null) this.value = refundResult {
            uuid = v.uuid
            status = v.status
            method = v.method
            epochTimestamp = v.epochTimestamp
            aux copyFrom v.aux
            data = v.data
        } }
    }

    companion object {
        @Volatile private var instance: TransactionDatabase? = null
        fun getDatabase(context: Context): TransactionDatabase {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val new = Room.databaseBuilder(
                        context.applicationContext,
                        TransactionDatabase::class.java,
                        "trx_db",
                    ).fallbackToDestructiveMigration().build()
                    instance = new
                    new
                }
            }
        }
    }
}