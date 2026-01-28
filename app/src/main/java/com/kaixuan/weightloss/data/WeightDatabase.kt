package com.kaixuan.weightloss.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [WeightRecord::class], version = 2, exportSchema = false)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var INSTANCE: WeightDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建新表
                db.execSQL("""
                    CREATE TABLE weight_records_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date INTEGER NOT NULL,
                        dateKey TEXT NOT NULL,
                        weight REAL NOT NULL
                    )
                """)
                // 迁移数据，每天只保留最新的一条记录
                db.execSQL("""
                    INSERT INTO weight_records_new (id, date, dateKey, weight)
                    SELECT id, date, strftime('%Y-%m-%d', date/1000, 'unixepoch', 'localtime'), weight
                    FROM weight_records
                    WHERE id IN (
                        SELECT id FROM weight_records w1
                        WHERE date = (
                            SELECT MAX(date) FROM weight_records w2
                            WHERE strftime('%Y-%m-%d', w1.date/1000, 'unixepoch', 'localtime') =
                                  strftime('%Y-%m-%d', w2.date/1000, 'unixepoch', 'localtime')
                        )
                    )
                """)
                // 删除旧表
                db.execSQL("DROP TABLE weight_records")
                // 重命名新表
                db.execSQL("ALTER TABLE weight_records_new RENAME TO weight_records")
                // 创建唯一索引
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weight_records_dateKey ON weight_records(dateKey)")
            }
        }

        fun getDatabase(context: Context): WeightDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeightDatabase::class.java,
                    "weight_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
