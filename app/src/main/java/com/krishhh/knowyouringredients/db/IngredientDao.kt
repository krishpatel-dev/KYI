package com.krishhh.knowyouringredients.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface IngredientDao {

    /** Exact, case‑insensitive phrase match. */
    @Query("""
        SELECT * FROM ingredients
        WHERE LOWER(foodProduct) = LOWER(:phrase)
        LIMIT 1
    """)
    suspend fun findExact(phrase: String): IngredientEntity?

    /** Whole‑phrase substring match (case‑insensitive). */
    @Query("""
        SELECT * FROM ingredients
        WHERE LOWER(foodProduct) LIKE '%' || LOWER(:phrase) || '%'
           OR LOWER(mainIngredient) LIKE '%' || LOWER(:phrase) || '%'
        LIMIT 1
    """)
    suspend fun findLike(phrase: String): IngredientEntity?

    /** Fallback: any word contained in either column. */
    @Query("""
        SELECT * FROM ingredients
        WHERE LOWER(foodProduct)    LIKE '%' || LOWER(:word) || '%'
           OR LOWER(mainIngredient) LIKE '%' || LOWER(:word) || '%'
        LIMIT 1
    """)
    suspend fun findContaining(word: String): IngredientEntity?

    /* Bulk CSV import */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun bulkInsert(list: List<IngredientEntity>)
}
