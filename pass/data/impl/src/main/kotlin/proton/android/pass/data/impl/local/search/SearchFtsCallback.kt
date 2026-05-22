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

/**
 * The FTS table is FTS5, which Room cannot generate (it only supports @Fts3/@Fts4).
 * We create the external-content virtual table and its sync triggers manually, both on
 * fresh creation and after a destructive migration.
 *
 * Both [onCreate] and [onDestructiveMigration] funnel through the same [createFts] so the
 * two code paths can never drift apart. It is exposed (internal) so instrumented tests can
 * build a [SearchDatabase] backed by the exact same FTS DDL that ships in production.
 */
internal object SearchFtsCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        createFts(db)
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        super.onDestructiveMigration(db)
        createFts(db)
    }

    fun createFts(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TRIGGER IF EXISTS search_items_fts_ai")
        db.execSQL("DROP TRIGGER IF EXISTS search_items_fts_ad")
        db.execSQL("DROP TRIGGER IF EXISTS search_items_fts_au")
        db.execSQL("DROP TABLE IF EXISTS search_items_fts")

        db.execSQL(
            "CREATE VIRTUAL TABLE search_items_fts USING fts5(" +
                "title, subtitle, content='search_items', content_rowid='rowId', " +
                "tokenize='unicode61')"
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
