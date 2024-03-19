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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.google.gson.Gson
import fr.isen.sayer.androiderestaurant.ui.theme.AndroidERestaurantTheme

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
            setContent {
                AndroidERestaurantTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        CategoryScreen(categoryName, dishes.map { it.name_fr })
                    }
                }
            }
        }
        setContent {
            AndroidERestaurantTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CategoryScreen(categoryName, dishes.map { it.name_fr })
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(categoryName: String, dishes: List<String>, context: Context = LocalContext.current) {
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

        // Composable LazyColumn pour afficher une liste
        LazyColumn {
            items(dishes) { dish ->
                DishItem(dish, onClick = {
                    // Intent pour démarrer DishDetailActivity avec le nom du plat en extra
                    val intent = Intent(context, DishDetailActivity::class.java)
                    intent.putExtra("dishName", dish)
                    context.startActivity(intent)
                })
            }
        }
    }
}

@Composable
fun DishItem(dishName: String, onClick: () -> Unit) {
    Text(
        text = dishName,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

fun fetchMenuData(context: Context, categoryName: String, updateUI: (List<MenuItem>) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val url = "http://test.api.catering.bluecodegames.com/menu"

    val postData = JSONObject()
    try {
        postData.put("id_shop", "1")
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val stringRequest = object : StringRequest(Request.Method.POST, url,
        Response.Listener<String> { response ->
            val gson = Gson()
            val menuResponse = gson.fromJson(response, MenuResponse::class.java)
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