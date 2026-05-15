package com.example.hotelapp.data

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface BookingDao {
    @RawQuery
    fun getAllRaw(query: SupportSQLiteQuery): List<Booking>

    @Query("SELECT COUNT(*) FROM bookings WHERE (:status IS NULL OR :status = 'Всі' OR status = :status)")
    fun getTotalCount(status: String?): Int

    @Query("SELECT SUM(totalPrice) FROM bookings WHERE status != 'Скасовано' AND (:status IS NULL OR :status = 'Всі' OR status = :status)")
    fun getTotalAmount(status: String?): Double?

    @Insert
    fun insert(booking: Booking): Long

    @Update
    fun update(booking: Booking)

    @Query("DELETE FROM bookings WHERE id = :id")
    fun deleteById(id: Long)

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getById(id: Long): Booking?

    fun getAll(
        status: String? = null,
        sortByDate: String? = null,
        limit: Int? = null,
        offset: Int? = null
    ): List<Booking> {
        val queryStr = StringBuilder("SELECT * FROM bookings")
        val args = mutableListOf<Any>()
        var hasWhere = false

        if (status != null && status != "Всі") {
            queryStr.append(" WHERE status = ?")
            args.add(status)
            hasWhere = true
        }

        if (sortByDate != null) {
            queryStr.append(" ORDER BY checkInDate $sortByDate")
        } else {
            queryStr.append(" ORDER BY id DESC")
        }

        if (limit != null) {
            queryStr.append(" LIMIT ?")
            args.add(limit)
            if (offset != null) {
                queryStr.append(" OFFSET ?")
                args.add(offset)
            }
        }

        val query = SimpleSQLiteQuery(queryStr.toString(), args.toTypedArray())
        return getAllRaw(query)
    }
}
