package fr.isen.sayer.androiderestaurant

import CartManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import CartItem
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.navigation.compose.rememberNavController

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CartScreen()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen() {
    val context = LocalContext.current
    val cartManager = remember { CartManager(context) }
    var refreshTrigger by remember { mutableStateOf(0) }

    fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        val homeIntent = Intent(context, HomeActivity::class.java)
                        context.startActivity(homeIntent)
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Panier") },
                actions = { /* Add actions if needed */ }
            )
        }
    )
    {
        val cartItems by remember(refreshTrigger) { mutableStateOf(cartManager.getCartItems()) }
        val itemsPerPage = 5
        var currentPage by remember { mutableStateOf(0) }
        val totalPages = (cartItems.size + itemsPerPage - 1) / itemsPerPage

        val startIndex = currentPage * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, cartItems.size)
        val currentPageItems = cartItems.subList(startIndex, endIndex)

        Column(modifier = Modifier.padding(top = 66.dp, start = 16.dp, end = 16.dp)) {
            LazyColumn {
                itemsIndexed(currentPageItems) { index, item ->
                    CartItemView(cartItem = item, onRemoveClicked = { itemId ->
                        cartManager.removeFromCart(itemId)
                        refreshTrigger++
                    })
                    if (index < currentPageItems.size - 1) {
                        Divider()
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(enabled = currentPage > 0, onClick = { currentPage-- }) {
                    Text("< Précédent")
                }

                Button(enabled = currentPage < totalPages - 1, onClick = { currentPage++ }) {
                    Text("Suivant >")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    cartManager.clearCart()
                    refreshTrigger++
                    currentPage = 0 // Reset to the first page
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vider le panier")
            }

            var showDialog by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    // Simulez le passage de la commande
                    showDialog = true // Supposons que showDialog est une variable remember { mutableStateOf(false) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Passer la commande : ${cartManager.getTotalPrice()} €")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmation") },
                    text = { Text("Commande passée avec succès.") },
                    confirmButton = {
                        Button(onClick = { showDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CartItemView(cartItem: CartItem, onRemoveClicked: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("${cartItem.name} x${cartItem.quantity}", modifier = Modifier.weight(1f))
        Text("${cartItem.price} €")
        IconButton(onClick = { onRemoveClicked(cartItem.id) }) {
            Icon(Icons.Default.Delete, contentDescription = "Supprimer")
        }
    }
}

