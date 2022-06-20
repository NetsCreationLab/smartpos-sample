package com.example.smartpossample.persistence

import android.content.Context
import androidx.room.*
import eu.nets.lab.smartpos.sdk.payload.ResultPayload
import eu.nets.lab.smartpos.sdk.room.NetsConverters
import eu.nets.lab.smartpos.sdk.room.entity.*

@Dao
@Suppress("unused")
interface PaymentDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: PaymentDataEntity)

    @Transaction
    @Query("select * from payment_data order by updatedAt desc limit 1")
    suspend fun newest(): PaymentDataWithResults?
}

@Dao
@Suppress("unused")
interface PaymentResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: PaymentResultEntity)
}

@Dao
@Suppress("unused")
interface RefundDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: RefundDataEntity)

    @Transaction
    @Query("select * from refund_data order by updatedAt desc limit 1")
    suspend fun newest(): RefundDataWithResults?
}

@Dao
@Suppress("unused")
interface RefundResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: RefundResultEntity)
}

@Database(
    entities = [PaymentDataEntity::class, RefundDataEntity::class, PaymentResultEntity::class, RefundResultEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(NetsConverters::class)
abstract class TransactionDatabase : RoomDatabase() {
    abstract val paymentDataDao: PaymentDataDao
    abstract val paymentResultDao: PaymentResultDao
    abstract val refundDataDao: RefundDataDao
    abstract val refundResultDao: RefundResultDao

    suspend fun newest(): ResultPayload? {
        val pr = paymentDataDao.newest().toPaymentResult()
        val rr = refundDataDao.newest().toRefundResult()
        return when {
            pr != null && rr != null -> if (pr.epochTimestamp > rr.epochTimestamp) pr else rr
            pr == null && rr != null -> rr
            pr != null && rr == null -> pr
            else -> null
        }
    }
//        MediatorLiveData<ResultPayload>().apply {
//        addSource(paymentDataDao.newest()) { comp ->
//            val currentNewest =
//                (this.value as? PaymentResult)?.epochTimestamp ?:
//                (this.value as? RefundResult)?.epochTimestamp ?:
//                0
//            val v = comp.toPaymentResult()
//            if (v != null && v.epochTimestamp > currentNewest) {
//                this.value = v
//            }
//        }
//        addSource(refundDataDao.newest()) { comp ->
//            val currentNewest =
//                (this.value as? PaymentResult)?.epochTimestamp ?:
//                (this.value as? RefundResult)?.epochTimestamp ?:
//                0
//            val v = comp.toRefundResult()
//            if (v != null && v.epochTimestamp > currentNewest) {
//                this.value = v
//            }
//        }
//    }

    companion object {
        @Volatile
        private var instance: TransactionDatabase? = null
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