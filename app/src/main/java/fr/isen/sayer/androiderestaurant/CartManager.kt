import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class CartManager(private val context: Context) {
    private val fileName = "cart.json"
    private val gson = Gson()

    var onCartChanged: (() -> Unit)? = null

    fun addToCart(item: CartItem) {
        val cart = getCartItems().toMutableList()
        cart.add(item)
        saveCartToFile(cart)
        onCartChanged?.invoke()
    }

    fun getCartItems(): List<CartItem> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            return emptyList()
        }
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(file.readText(), type)
    }

    private fun saveCartToFile(cart: List<CartItem>) {
        val file = File(context.filesDir, fileName)
        file.writeText(gson.toJson(cart))
    }

    fun getTotalItemCount(): Int {
        return getCartItems().sumOf { it.quantity }
    }

    fun removeFromCart(itemId: String) {
        val cart = getCartItems().toMutableList()
        cart.removeAll { it.id == itemId }
        saveCartToFile(cart)
        onCartChanged?.invoke()
    }

    fun clearCart() {
        saveCartToFile(emptyList())
        onCartChanged?.invoke()
    }

    fun getTotalPrice(): Float {
        val cartItems = getCartItems()
        return cartItems.sumOf { (it.price * it.quantity).toInt() }.toFloat()
    }
}

data class CartItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Float
)