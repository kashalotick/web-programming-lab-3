package com.example.hotelapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val guestName: String,
    val guestCount: Int,
    val roomNumber: String,
    val checkInDate: Date,
    val checkOutDate: Date,
    val totalPrice: Double,
    val status: String
)
