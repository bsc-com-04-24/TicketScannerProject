package com.example.ticket_scanner.eventcreation.localdb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TicketDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ticket_scanner.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                venue TEXT,
                start_millis INTEGER NOT NULL,
                end_millis INTEGER NOT NULL,
                created_at INTEGER NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE tickets (
                code TEXT PRIMARY KEY,
                event_id INTEGER NOT NULL,
                event_name TEXT NOT NULL,
                venue TEXT,
                start_millis INTEGER NOT NULL,
                ticket_type TEXT NOT NULL,
                ticket_number INTEGER NOT NULL,
                total_tickets INTEGER NOT NULL,
                qr_payload TEXT NOT NULL,
                purchased_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                checked_in_at INTEGER,
                gate TEXT,
                FOREIGN KEY(event_id) REFERENCES events(id)
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tickets")
        db.execSQL("DROP TABLE IF EXISTS events")
        onCreate(db)
    }
}
