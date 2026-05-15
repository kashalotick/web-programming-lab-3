package com.example.hotelapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BookingDatabase (context: Context) :
    SQLiteOpenHelper(context, "hotel.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE bookings (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                guestName TEXT NOT NULL,
                guestCount INTEGER NOT NULL,
                roomNumber TEXT NOT NULL,
                checkInDate TEXT NOT NULL,
                checkOutDate TEXT NOT NULL,
                totalPrice REAL NOT NULL,
                status TEXT NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS bookings")
        onCreate(db)
    }

    fun getAll(): List<BookingDomain> {
        val list = mutableListOf<BookingDomain>()
        val cursor = readableDatabase.query("bookings", null, null, null, null, null, "id DESC")
        with(cursor) {
            while (moveToNext()) {
                list.add(BookingDomain(
                    id = getLong(getColumnIndexOrThrow("id")),
                    guestName = getString(getColumnIndexOrThrow("guestName")),
                    guestCount = getInt(getColumnIndexOrThrow("guestCount")),
                    roomNumber = getString(getColumnIndexOrThrow("roomNumber")),
                    checkInDate = getString(getColumnIndexOrThrow("checkInDate")),
                    checkOutDate = getString(getColumnIndexOrThrow("checkOutDate")),
                    totalPrice = getDouble(getColumnIndexOrThrow("totalPrice")),
                    status = getString(getColumnIndexOrThrow("status"))
                ))
            }
            close()
        }
        return list
    }

    fun insert(b: BookingDomain): Long {
        val cv = ContentValues().apply {
            put("guestName", b.guestName)
            put("guestCount", b.guestCount)
            put("roomNumber", b.roomNumber)
            put("checkInDate", b.checkInDate)
            put("checkOutDate", b.checkOutDate)
            put("totalPrice", b.totalPrice)
            put("status", b.status)
        }
        return writableDatabase.insert("bookings", null, cv)
    }

    fun update(b: BookingDomain) {
        val cv = ContentValues().apply {
            put("guestName", b.guestName)
            put("guestCount", b.guestCount)
            put("roomNumber", b.roomNumber)
            put("checkInDate", b.checkInDate)
            put("checkOutDate", b.checkOutDate)
            put("totalPrice", b.totalPrice)
            put("status", b.status)
        }
        writableDatabase.update("bookings", cv, "id=?", arrayOf(b.id.toString()))
    }

    fun delete(id: Long) {
        writableDatabase.delete("bookings", "id=?", arrayOf(id.toString()))
    }

    fun getById(id: Long): BookingDomain? {
        val cursor = readableDatabase.query("bookings", null, "id=?", arrayOf(id.toString()), null, null, null)
        return if (cursor.moveToFirst()) {
            BookingDomain(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                guestName = cursor.getString(cursor.getColumnIndexOrThrow("guestName")),
                guestCount = cursor.getInt(cursor.getColumnIndexOrThrow("guestCount")),
                roomNumber = cursor.getString(cursor.getColumnIndexOrThrow("roomNumber")),
                checkInDate = cursor.getString(cursor.getColumnIndexOrThrow("checkInDate")),
                checkOutDate = cursor.getString(cursor.getColumnIndexOrThrow("checkOutDate")),
                totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("totalPrice")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            ).also { cursor.close() }
        } else null
    }
}
