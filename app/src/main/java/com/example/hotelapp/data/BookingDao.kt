package com.example.hotelapp.data

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingDao(private val db: BookingDatabase) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getAll(
        status: String? = null,
        sortByDate: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): List<Booking> {
        val orderBy = when (sortByDate) {
            "ASC" -> "checkInDate ASC"
            "DESC" -> "checkInDate DESC"
            else -> "id DESC"
        }
        return db.getAll(status, orderBy, limit, offset).map { toDomain(it) }
    }

    fun getTotalCount(status: String? = null): Int {
        return db.getTotalCount(status)
    }

    fun getTotalAmount(status: String? = null): Double {
        return db.getTotalAmount(status)
    }

    fun insert(booking: Booking): Long {
        return db.insert(toDto(booking))
    }

    fun update(booking: Booking) {
        db.update(toDto(booking))
    }

    fun delete(id: Long) {
        db.delete(id)
    }

    fun getById(id: Long): Booking? {
        val dto = db.getById(id)
        return dto?.let { toDomain(it) }
    }

    private fun toDomain(dto: BookingDomain): Booking {
        return Booking(
            id = dto.id,
            guestName = dto.guestName,
            guestCount = dto.guestCount,
            roomNumber = dto.roomNumber,
            checkInDate = parseDate(dto.checkInDate) ?: Date(),
            checkOutDate = parseDate(dto.checkOutDate) ?: Date(),
            totalPrice = dto.totalPrice,
            status = dto.status
        )
    }

    private fun toDto(domain: Booking): BookingDomain {
        return BookingDomain(
            id = domain.id,
            guestName = domain.guestName,
            guestCount = domain.guestCount,
            roomNumber = domain.roomNumber,
            checkInDate = formatDate(domain.checkInDate),
            checkOutDate = formatDate(domain.checkOutDate),
            totalPrice = domain.totalPrice,
            status = domain.status
        )
    }

    private fun formatDate(date: Date?): String {
        return if (date == null) "" else dateFormat.format(date)
    }

    private fun parseDate(dateStr: String?): Date? {
        if (dateStr.isNullOrEmpty()) return null
        return try {
            dateFormat.parse(dateStr)
        } catch (e: ParseException) {
            null
        }
    }
}
