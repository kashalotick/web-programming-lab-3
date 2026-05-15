package com.example.hotelapp.data

import java.util.Date

data class Booking(
    val id: Long = 0,
    val guestName: String,
    val guestCount: Int,
    val roomNumber: String,
    val checkInDate: Date,
    val checkOutDate: Date,
    val totalPrice: Double,
    val status: String
)