package com.krishhh.knowyouringredients.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    /** “Food Product” column (unique). */
    @PrimaryKey @ColumnInfo(name = "foodProduct")
    val foodProduct: String,

    @ColumnInfo(name = "mainIngredient") val mainIngredient: String?,
    @ColumnInfo(name = "sweetener")      val sweetener: String?,
    @ColumnInfo(name = "fatOil")         val fatOil: String?,
    @ColumnInfo(name = "seasoning")      val seasoning: String?,
    @ColumnInfo(name = "allergens")      val allergens: String?,
    @ColumnInfo(name = "prediction")     val prediction: String?
)
