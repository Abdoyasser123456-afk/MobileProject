package com.example.mobileproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.ui.theme.AppBackground
import com.example.mobileproject.ui.theme.BgCardColor
import com.example.mobileproject.ui.theme.CategoryTextColor
import com.example.mobileproject.ui.theme.MobileProjectTheme
import kotlinx.coroutines.launch

class FavouritesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = intent.getLongExtra("userId", -1L)
        if (userId == -1L) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = AppDatabase.getDatabase(this)

        setContent {
            MobileProjectTheme {
                FavouritesScreen(db = db, userId = userId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(db: AppDatabase, userId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val favItems by db.favouriteDao().getFavourites(userId).collectAsState(initial = emptyList())
    val allDishes by db.dishDao().getAllDishes().collectAsState(initial = emptyList())

    val favDishes = allDishes.filter { dish -> favItems.any { it.dishId == dish.id } }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Favourites ❤️",
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
                        selected = true,
                        onClick = { Toast.makeText(context, "You are alraedy here", Toast.LENGTH_SHORT).show() }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") },
                        selected = false,
                        onClick = {
                            val intent = Intent(context, CartActivity::class.java).putExtra("userId", userId)
                            context.startActivity(intent)
                        }
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
                    .padding(8.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favDishes, key = { it.id }) { dish ->
                        FavDishRowItem(
                            dish = dish,
                            onShareClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out ${dish.name} (${dish.category}) for only $${dish.price}!"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Dish via"))
                            },
                            onOrderClick = {
                                scope.launch {
                                    db.cartDao().addToCart(
                                        CartItem(userId = userId, dishId = dish.id, name = dish.name, price = dish.price, qty = 1)
                                    )
                                    Toast.makeText(context, "${dish.name} added to cart", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onRemoveFavClick = {
                                scope.launch {
                                    db.favouriteDao().removeFavourite(dishId = dish.id, userId = userId)
                                    Toast.makeText(context, "Removed from favourites", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavDishRowItem(
    dish: Dish,
    onShareClick: () -> Unit,
    onOrderClick: () -> Unit,
    onRemoveFavClick: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BgCardColor,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { showDetailsDialog = true },
                    onLongClick = { showContextMenu = true }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = getCategoryDrawable(dish.category)),
                        contentDescription = dish.category,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(text = dish.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = dish.category, color = CategoryTextColor)
                    Text(text = "$${dish.price}", fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onShareClick, modifier = Modifier.size(width = 44.dp, height = 62.dp)) {
                    Text("📤", fontSize = 18.sp)
                }

                IconButton(onClick = onOrderClick, modifier = Modifier.size(width = 44.dp, height = 62.dp)) {
                    Text("🛒", fontSize = 18.sp)
                }
            }
        }

        DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }) {
            DropdownMenuItem(
                text = { Text("Remove from favourites 💔", color = Color.Red, fontWeight = FontWeight.Bold) },
                onClick = {
                    showContextMenu = false
                    onRemoveFavClick()
                }
            )
        }

        if (showDetailsDialog) {
            AlertDialog(
                onDismissRequest = { showDetailsDialog = false },
                title = { Text(text = dish.name, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = dish.description.ifEmpty { "No description available." }, fontStyle = FontStyle.Italic, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "🔥 Calories: ${dish.calories} kcal", fontWeight = FontWeight.Bold)
                        Text(text = "💪 Protein: ${dish.protein} g", fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showDetailsDialog = false
                        onOrderClick()
                    }) {
                        Text("Add to Cart")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDetailsDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}