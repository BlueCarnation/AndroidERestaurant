package fr.isen.sayer.androiderestaurant

import CartItem
import CartManager
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import coil.compose.rememberImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class DishDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dish = intent.getParcelableExtra<MenuItem>("dishDetail")

        setContent {
            dish?.let {
                DishDetailScreen(dish = it)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(dish: MenuItem) {
    var quantity by remember { mutableStateOf(1) }
    val initialPageIndex = if (dish.images.size > 1) 1 else 0
    val pagerState = rememberPagerState(initialPage = initialPageIndex, pageCount = { dish.images.size })

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = screenWidth * 0.10f
    val context = LocalContext.current
    val cartManager = CartManager(context)
    var cartItemCount by remember { mutableStateOf(cartManager.getTotalItemCount()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DroidRestaurant",
                        color = Color.White,
                        modifier = Modifier.clickable {
                            val homeIntent = Intent(context, HomeActivity::class.java)
                            context.startActivity(homeIntent)
                        }
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = {
                            val intent = Intent(context, CartActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Panier", tint = Color.White)
                        }
                        // Condition pour afficher la pastille
                        if (cartItemCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd) // Positionne la pastille en haut à droite de l'IconButton
                                    .background(Color.Red, CircleShape)
                                    .padding(3.dp) // Ajustez le padding pour contrôler la taille de la pastille
                            ) {
                                // Affiche "9+" si le nombre d'articles est supérieur à 9
                                val displayText = if (cartItemCount > 9) "9+" else (cartItemCount).toString()
                                Text(
                                    text = displayText,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4)
                )
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth() // Prend toute la largeur
            ) { page ->
                val imageUrl = dish.images[page]
                Image(
                    painter = rememberImagePainter(data = imageUrl, builder = { crossfade(true) }),
                    contentDescription = "Image $page for ${dish.name_fr}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentScale = ContentScale.FillWidth // Étirer l'image pour prendre toute la largeur
                )
            }

            Text(
                text = dish.name_fr,
                color = Color.Gray,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 16.dp)
            )

            val ingredientsText = dish.ingredients.joinToString(separator = ", ") { it.name_fr }
            Text(
                text = ingredientsText,
                color = Color.Gray,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 24.dp)
            )

            QuantitySelector(
                quantity = quantity,
                onQuantityChange = { newQuantity -> quantity = newQuantity })

            val cartManager = CartManager(LocalContext.current)

            val totalPrice = quantity * (dish.prices.firstOrNull()?.price?.toFloatOrNull() ?: 0f)
            var showDialog by remember { mutableStateOf(false) }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Panier") },
                    text = { Text("Article ajouté au panier.") },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp)) // Espacement entre les éléments

            // Modifier le onClick du bouton pour déclencher l'AlertDialog
            Button(onClick = {
                val cartItem = CartItem(dish.id, dish.name_fr, quantity, totalPrice)
                cartManager.addToCart(cartItem)
                showDialog = true
                // Mettez à jour cartItemCount après l'ajout pour rafraîchir immédiatement la pastille
                cartItemCount = cartManager.getTotalItemCount()
            }) {
                Text("Ajouter au panier : $totalPrice €")
            }
        }
    }
}

@Composable
fun QuantitySelector(quantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Moins (-)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp) // Ajusté pour un meilleur visuel
                .background(Color(0xFF6750A4), shape = CircleShape)
                .clickable { if (quantity > 1) onQuantityChange(quantity - 1) }
        ) {
            Text(
                text = "-",
                color = Color.White,
                fontSize = 24.sp, // Taille du texte
                modifier = Modifier.offset(y = (-2).dp), // Légèrement remonté
                fontWeight = FontWeight.Bold // Texte plus épais
            )
        }

        Spacer(modifier = Modifier.width(20.dp)) // Espacement entre les éléments

        Text(
            text = "Quantité : $quantity",
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(20.dp)) // Espacement entre les éléments

        // Plus (+)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(30.dp) // Ajusté pour un meilleur visuel
                .background(Color(0xFF6750A4), shape = CircleShape)
                .clickable { onQuantityChange(quantity + 1) }
        ) {
            Text(
                text = "+",
                color = Color.White,
                fontSize = 24.sp, // Taille du texte
                modifier = Modifier.offset(y = (-1).dp) // Légèrement remonté
            )
        }
    }
}
