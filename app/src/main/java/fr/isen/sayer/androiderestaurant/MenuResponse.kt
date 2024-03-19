package fr.isen.sayer.androiderestaurant

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
data class MenuResponse(val data: List<Category>)

data class Category(
    val name_fr: String,
    val items: List<MenuItem>
)

@Parcelize
data class MenuItem(
    val id: String,
    val name_fr: String,
    val images: List<String>,
    val ingredients: List<Ingredient>,
    val prices: List<Price>
) : Parcelable

@Parcelize
data class Ingredient(
    val id: String,
    val name_fr: String
) : Parcelable

@Parcelize
data class Price(
    val id: String,
    val price: String,
    val size: String
) : Parcelable

