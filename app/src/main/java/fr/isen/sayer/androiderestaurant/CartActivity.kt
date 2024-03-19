package fr.isen.sayer.androiderestaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartScreen()
        }
    }
}

@Composable
fun CartScreen() {
    // Ici, vous afficherez les articles du panier
    Text(text = "Panier", style = MaterialTheme.typography.headlineMedium)
}
