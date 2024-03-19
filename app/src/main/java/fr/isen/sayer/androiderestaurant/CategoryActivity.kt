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
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.google.gson.Gson
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import fr.isen.sayer.androiderestaurant.ui.theme.AndroidERestaurantTheme
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.volley.Cache
import androidx.compose.runtime.*

class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("categoryName") ?: return

        // Initialement, les plats sont vides jusqu'à ce que la réponse soit reçue
        val dishes = mutableListOf<MenuItem>()

        fetchMenuData(this, categoryName) { items ->
            dishes.clear()
            dishes.addAll(items)
            // Force Compose à recomposer avec les nouvelles données
            runOnUiThread {
                setContent {
                    AndroidERestaurantTheme {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                            CategoryScreen(categoryName, dishes, this@CategoryActivity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(categoryName: String, dishes: List<MenuItem>, context: Context = LocalContext.current) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = categoryName,
            color = Color(0xFFFFA500),
            fontSize = 26.sp,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(dishes) { dish ->
                DishItem(dish, context)
            }
        }
    }
}

@Composable
fun DishItem(dish: MenuItem, context: Context) {
    // Choix de l'URL de l'image : essaie la 2ème image si disponible, sinon prend la première.
    val imageUrl = if (dish.images.size > 1) dish.images[1] else dish.images.firstOrNull() ?: ""

    Row(
        modifier = Modifier
            .clickable {
                val intent = Intent(context, DishDetailActivity::class.java).apply {
                    // En passant l'objet complet 'dish' à DishDetailActivity.
                    // Assurez-vous que MenuItem implémente Parcelable ou Serializable.
                    putExtra("dishDetail", dish)
                }
                context.startActivity(intent)
            }
            .padding(8.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Log.v("DishItem", "Loading image: $imageUrl")
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .error(R.drawable.error_image) // Image d'erreur
                .placeholder(R.drawable.placeholder) // Image de chargement
                .build(),
            contentDescription = "Image of ${dish.name_fr}",
            modifier = Modifier.size(88.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = dish.name_fr,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Prix: ${dish.prices.firstOrNull()?.price ?: "N/A"} €",
                style = MaterialTheme.typography.bodySmall
            )
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


