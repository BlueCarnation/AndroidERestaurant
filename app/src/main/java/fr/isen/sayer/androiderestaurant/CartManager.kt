import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class CartManager(private val context: Context) {
    private val fileName = "cart.json"
    private val gson = Gson()

    fun addToCart(item: CartItem) {
        val cart = getCartItems().toMutableList()
        cart.add(item)
        saveCartToFile(cart)
    }

    private fun getCartItems(): List<CartItem> {
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
}

data class CartItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val price: Float
)

