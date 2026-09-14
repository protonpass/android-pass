/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.local.search

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

internal object SearchFtsCallback : RoomDatabase.Callback() {

    private const val TRIGGER_COUNT = 3

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        if (!ftsIsCurrentVersion(db) || !triggersExist(db)) {
            createFts(db)
        }
    }

    private fun ftsIsCurrentVersion(db: SupportSQLiteDatabase): Boolean {
        val cursor = db.query(
            "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'search_items_fts'"
        )
        return cursor.use { c ->
            c.moveToFirst() && c.getString(0).contains("trigram", ignoreCase = true)
        }
    }

    private fun triggersExist(db: SupportSQLiteDatabase): Boolean {
        val cursor = db.query(
            "SELECT COUNT(*) FROM sqlite_master WHERE type = 'trigger' AND name IN " +
                "('search_items_fts_ai', 'search_items_fts_ad', 'search_items_fts_au')"
        )
        return cursor.use { c -> c.moveToFirst() && c.getInt(0) == TRIGGER_COUNT }
    }

    fun createFts(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TRIGGER IF EXISTS search_items_fts_ai")
        db.execSQL("DROP TRIGGER IF EXISTS search_items_fts_ad")
        db.execSQL("DROP TRIGGER IF EXISTS search_items_fts_au")
        db.execSQL("DROP TABLE IF EXISTS search_items_fts")

        db.execSQL(
            "CREATE VIRTUAL TABLE search_items_fts USING fts5(" +
                "title, subtitle, content='search_items', content_rowid='rowId', " +
                "tokenize='trigram')"
        )

        db.execSQL(
            "INSERT INTO search_items_fts(rowid, title, subtitle) " +
                "SELECT rowId, title, subtitle FROM search_items"
        )

        db.execSQL(
            "CREATE TRIGGER search_items_fts_ai AFTER INSERT ON search_items BEGIN " +
                "INSERT INTO search_items_fts(rowid, title, subtitle) " +
                "VALUES (new.rowId, new.title, new.subtitle); END"
        )
        db.execSQL(
            "CREATE TRIGGER search_items_fts_ad AFTER DELETE ON search_items BEGIN " +
                "INSERT INTO search_items_fts(search_items_fts, rowid, title, subtitle) " +
                "VALUES ('delete', old.rowId, old.title, old.subtitle); END"
        )
        db.execSQL(
            "CREATE TRIGGER search_items_fts_au AFTER UPDATE ON search_items BEGIN " +
                "INSERT INTO search_items_fts(search_items_fts, rowid, title, subtitle) " +
                "VALUES ('delete', old.rowId, old.title, old.subtitle); " +
                "INSERT INTO search_items_fts(rowid, title, subtitle) " +
                "VALUES (new.rowId, new.title, new.subtitle); END"
        )
    }
}
