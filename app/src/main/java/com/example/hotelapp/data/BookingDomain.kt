package com.example.hotelapp.data

data class BookingDomain(
    val id: Long = 0,
    val guestName: String,
    val guestCount: Int,
    val roomNumber: String,
    val checkInDate: String,
    val checkOutDate: String,
    val totalPrice: Double,
    val status: String
)

