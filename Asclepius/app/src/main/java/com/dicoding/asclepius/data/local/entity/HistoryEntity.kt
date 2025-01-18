package com.dicoding.asclepius.data.local.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "history_entity")
@Parcelize
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @field:ColumnInfo(name = "imageUri")
    val imageUri: String,

    @field:ColumnInfo(name = "label")
    val label: String,

    @field:ColumnInfo(name = "confidenceScore")
    val confidenceScore: Float,

    @field:ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
): Parcelable