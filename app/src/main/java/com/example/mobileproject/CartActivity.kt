package com.example.mobileproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.ui.theme.AppBackground
import com.example.mobileproject.ui.theme.MobileProjectTheme
import kotlinx.coroutines.launch

class CartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(this)
        val userId = intent.getLongExtra("userId", -1L)

        setContent {
            MobileProjectTheme {
                CartScreen(
                    db = db,
                    userId = userId,
                    onCheckout = { method ->
                        val orderId = System.currentTimeMillis()
                        val intent = Intent(this@CartActivity, CheckoutActivity::class.java).apply {
                            putExtra("orderId", orderId)
                            putExtra("method", method)
                            putExtra("userId", userId)
                        }
                        this@CartActivity.startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(db: AppDatabase, userId: Long, onCheckout: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val cartItems by db.cartDao().getCartItems(userId).collectAsState(initial = emptyList())
    var showMenu by remember { mutableStateOf(false) }

    // حساب الإجمالي بدقة
    val total = cartItems.sumOf { it.price * it.qty }

    AppBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Cart 🛒",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = false,
                        onClick = {
                            val intent = Intent(context, MenuActivity::class.java).putExtra("userId", userId)
                            context.startActivity(intent)
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = false,
                        onClick = {
                            val intent = Intent(context, FavouritesActivity::class.java).putExtra("userId", userId)
                            context.startActivity(intent)
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") },
                        selected = true,
                        onClick = { Toast.makeText(context, "You are alraedy here", Toast.LENGTH_SHORT).show() }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = false,
                        onClick = {
                            val intent = Intent(context, ProfileActivity::class.java).putExtra("userId", userId)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        CartRowItem(
                            item = item,
                            onInc = { scope.launch { db.cartDao().updateQty(item.id, item.qty + 1) } },
                            onDec = {
                                if (item.qty > 1) {
                                    scope.launch { db.cartDao().updateQty(item.id, item.qty - 1) }
                                } else {
                                    scope.launch { db.cartDao().deleteItem(item.id) }
                                }
                            },
                            onRemove = { scope.launch { db.cartDao().deleteItem(item.id) } }
                        )
                    }
                }

                Text(
                    text = String.format("Total: $%.2f", total),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (cartItems.isEmpty()) {
                                Toast.makeText(context, "Your cart is empty", Toast.LENGTH_SHORT).show()
                            } else {val orderId = System.currentTimeMillis()
                                val intent = Intent(context, CheckoutActivity::class.java).apply {
                                    putExtra("orderId", orderId)
                                    putExtra("userId", userId)
                                }
                                context.startActivity(intent)}
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Checkout")
                    }
                }
            }
        }
    }
}

@Composable
fun CartRowItem(
    item: CartItem,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "$${item.price}",
                    color = androidx.compose.ui.graphics.Color.Gray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDec) {
                    Text("➖")
                }

                Text(
                    text = "${item.qty}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = onInc) {
                    Text("➕")
                }
            }

            IconButton(onClick = onRemove) {
                Text("🗑️")
            }
        }
    }
}