package fr.isen.sayer.androiderestaurant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.foundation.Image
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.google.gson.Gson
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import fr.isen.sayer.androiderestaurant.ui.theme.AndroidERestaurantTheme
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.android.volley.Cache

class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("categoryName") ?: return
        val dishes = mutableListOf<MenuItem>()

        fetchMenuData(this, categoryName) { items ->
            dishes.clear()
            dishes.addAll(items)
            runOnUiThread {
                setContent {
                    AndroidERestaurantTheme {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            CategoryScreen(categoryName, dishes, this@CategoryActivity) {
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(categoryName: String, dishes: List<MenuItem>, context: Context = LocalContext.current, navigateBack: () -> Unit) {
    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(getImageForCategory(categoryName))
                    .build(),
                contentDescription = "Category Image",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            IconButton(onClick = { navigateBack() }, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(
                    Icons.Filled.ArrowBack,
                    "Retour",
                    modifier = Modifier.size(30.dp), // Augmentez la taille ici
                    tint = Color.White
                )
            }
            Text(
                text = categoryName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reste du contenu comme avant
        LazyColumn {
            itemsIndexed(dishes) { index, dish ->
                DishItem(dish, context, index == dishes.lastIndex)
            }
        }
    }
}

fun getImageForCategory(categoryName: String): Int {
    // Remplacez ces URL par les chemins de vos images réelles
    return when(categoryName) {
        "Entrées" -> R.drawable.entree
        "Plats" -> R.drawable.plats
        "Desserts" -> R.drawable.dessert
        else -> R.drawable.error_image
    }
}

@Composable
fun DishItem(dish: MenuItem, context: Context, isLastItem: Boolean) {
    val imageUrl = dish.images.getOrNull(1) ?: dish.images.firstOrNull() ?: ""

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { // Rendre toute la colonne cliquable
            val intent = Intent(context, DishDetailActivity::class.java).apply {
                putExtra("dishDetail", dish)
            }
            context.startActivity(intent)
        }
        .padding(start = 16.dp, end = 8.dp, top = 8.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .error(R.drawable.error_image)
                    .placeholder(R.drawable.placeholder)
                    .build(),
                contentDescription = "Image of ${dish.name_fr}",
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(10.dp)), // Arrondit les bords de l'image
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.padding(end = 16.dp)) { // Ajout de padding à droite pour l'alignement
                Text(
                    text = dish.name_fr,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Prix: ${dish.prices.firstOrNull()?.price ?: "N/A"} €",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Conditionnellement ajouter un Divider avec un espacement avant et après
        if (!isLastItem) {
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp)) // Ajouter de l'espace après le Divider
        }
    }
}

fun fetchMenuData(context: Context, categoryName: String, updateUI: (List<MenuItem>) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val url = "http://test.api.catering.bluecodegames.com/menu"

    val cacheKey = "menu_$categoryName"
    val cacheEntry = queue.cache[cacheKey]

    if (cacheEntry != null) {
        try {
            val jsonString = String(cacheEntry.data, Charsets.UTF_8)
            val menuResponse = Gson().fromJson(jsonString, MenuResponse::class.java)
            val categoryItems = menuResponse.data.find { it.name_fr == categoryName }?.items ?: listOf()
            updateUI(categoryItems)
            return
        } catch (e: Exception) {
            Log.e("CacheError", "Error parsing cache response", e)
        }
    }

    val postData = JSONObject().apply {
        put("id_shop", "1")
    }

    val stringRequest = object : StringRequest(Method.POST, url,
        Response.Listener<String> { response ->
            queue.cache.put(cacheKey, Cache.Entry().apply {
                data = response.toByteArray(Charsets.UTF_8)
            })

            val menuResponse = Gson().fromJson(response, MenuResponse::class.java)
            val categoryItems = menuResponse.data.find { it.name_fr == categoryName }?.items ?: listOf()
            updateUI(categoryItems)
        },
        Response.ErrorListener { error ->
            Log.d("Error.Response", error.toString())
        }
    ) {
        override fun getBodyContentType(): String {
            return "application/json; charset=utf-8"
        }

        override fun getBody(): ByteArray {
            return postData.toString().toByteArray(Charsets.UTF_8)
        }
    }

    queue.add(stringRequest)
}


