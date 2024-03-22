package fr.isen.sayer.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.sayer.androiderestaurant.ui.theme.AndroidERestaurantTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle("DroidRestaurant")
        setContent {
            AndroidERestaurantTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MenuScreen()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Log a message with tag "HomeActivity" when the activity is destroyed
        Log.v("HomeActivity", "HomeActivity est détruite")
    }
}

@Composable
fun MenuScreen() {
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 60.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "Bienvenue" en haut, centré
        Text(
            text = "Bienvenue chez",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6750A4),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 35.dp), // Ajustez cette valeur pour aligner "chez" comme souhaité
        ) {
            Text(
                text = "DroidRestaurant",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF6750A4),
            )
        }

        // Les boutons centrés verticalement dans le reste de l'écran
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CategoryButton(context, "Entrées")
            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(0.8f))
            CategoryButton(context, "Plats")
            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(0.8f))
            CategoryButton(context, "Desserts")
        }
    }
}

@Composable
fun CategoryButton(context: Context, categoryName: String) {
    OutlinedButton(
        onClick = {
            // Créer un intent pour démarrer CategoryActivity avec un extra pour le nom de la catégorie
            val intent = Intent(context, CategoryActivity::class.java)
            intent.putExtra("categoryName", categoryName)
            context.startActivity(intent)
        },
        border = BorderStroke(1.dp, Color.Transparent),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = categoryName, color = Color(0xFF6750A4), fontSize = 26.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    AndroidERestaurantTheme {
        MenuScreen()
    }
}