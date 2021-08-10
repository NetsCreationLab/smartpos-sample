package com.example.smartpossample.persistence.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import eu.nets.lab.smartpos.sdk.payload.*
import java.util.*

@Entity(tableName = "payment_results", indices = [Index(value = ["uuid"], unique = false)])
data class PaymentResultEntity(
    val uuid: UUID,
    val status: PaymentStatus,
    val method: TargetMethod,
    val epochTimestamp: Long,
    val aux: Map<String, AuxValue>,
    @Embedded(prefix = "data_") val data: PaymentData,
    @PrimaryKey val id: UUID,
) {
    companion object {
        operator fun invoke(
            p: PaymentResult
        ): PaymentResultEntity = PaymentResultEntity(
            p.uuid,
            p.status,
            p.method,
            p.epochTimestamp,
            p.aux,
            p.data,
            UUID.randomUUID(),
        )
    }
}

@Entity(tableName = "refund_result", indices = [Index(value = ["uuid"], unique = false)])
data class RefundResultEntity(
    val uuid: UUID,
    val status: RefundStatus,
    val method: TargetMethod,
    val epochTimestamp: Long,
    val aux: Map<String, AuxValue>,
    @Embedded(prefix = "data_") val data: RefundData,
    @PrimaryKey val id: UUID,
) {
    companion object {
        operator fun invoke(
            r: RefundResult
        ): RefundResultEntity = RefundResultEntity(
            r.uuid,
            r.status,
            r.method,
            r.epochTimestamp,
            r.aux,
            r.data,
            UUID.randomUUID(),
        )
    }
}
