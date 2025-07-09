package com.krishhh.knowyouringredients.db

import android.content.ContentValues
import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.opencsv.CSVReaderBuilder
import java.io.InputStreamReader

@Database(entities = [IngredientEntity::class], version = 4, exportSchema = false)
abstract class IngredientDatabase : RoomDatabase() {
    abstract fun dao(): IngredientDao

    companion object {
        @Volatile private var INSTANCE: IngredientDatabase? = null

        fun get(ctx: Context): IngredientDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(ctx).also { INSTANCE = it }
            }

        private fun build(ctx: Context) =
            Room.databaseBuilder(ctx, IngredientDatabase::class.java, "ingredients.db")
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        preloadCsvSynchronously(ctx, db)
                    }
                })
                .build()

        /** Insert every CSV row synchronously → data ready before first query. */
        private fun preloadCsvSynchronously(ctx: Context, db: SupportSQLiteDatabase) {
            db.beginTransaction()
            try {
                ctx.assets.open("ingredients.csv").use { input ->
                    CSVReaderBuilder(InputStreamReader(input))
                        .withSkipLines(1)          // skip header
                        .build()
                        .forEach { cols ->
                            if (cols.size >= 7) {
                                val cv = ContentValues().apply {
                                    put("foodProduct",   cols[0].trim())
                                    put("mainIngredient",cols[1].trim())
                                    put("sweetener",     cols[2].trim())
                                    put("fatOil",        cols[3].trim())
                                    put("seasoning",     cols[4].trim())
                                    put("allergens",     cols[5].trim())
                                    put("prediction",    cols[6].trim())
                                }
                                db.insert("ingredients", OnConflictStrategy.IGNORE, cv)
                            }
                        }
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
