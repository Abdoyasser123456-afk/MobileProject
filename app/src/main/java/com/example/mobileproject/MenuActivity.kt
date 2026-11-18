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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.ui.theme.AppBackground
import com.example.mobileproject.ui.theme.BgCardColor
import com.example.mobileproject.ui.theme.CategoryTextColor
import com.example.mobileproject.ui.theme.MobileProjectTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontStyle

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        val intentUserId = intent.getLongExtra("userId", -1L)
        val userId = if (intentUserId != -1L) intentUserId else prefs.getLong("userId", -1L)
        val userRole = intent.getStringExtra("role") ?: prefs.getString("role", "Customer") ?: "Customer"

        setContent {
            MobileProjectTheme {
                MenuScreen(
                    db = db,
                    userId = userId,
                    userRole = userRole,
                    onAddDishClick = {
                        val i = Intent(this, AddDishActivity::class.java)
                        i.putExtra("userId", userId)
                        startActivity(i)
                    },
                    onEditDishClick = { dishId ->
                        val i = Intent(this, AddDishActivity::class.java)
                        i.putExtra("editId", dishId)
                        startActivity(i)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    db: AppDatabase,
    userId: Long,
    userRole: String,
    onAddDishClick: () -> Unit,
    onEditDishClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dishes by db.dishDao().getAllDishes().collectAsState(initial = emptyList())
    val isAdmin = userRole.equals("Admin", ignoreCase = true)
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedNavItem by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Pizza", "Burger", "Pasta", "Salad", "Sushi", "Drink")

    androidx.activity.compose.BackHandler(enabled = true) {
        showLogoutDialog = true
    }

    val allCategory = "All"
    val filteredDishes = dishes.filter { dish ->
        val matchesSearch = dish.name.contains(searchQuery, ignoreCase = true) ||
                dish.category.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory.equals(allCategory, ignoreCase = true) ||
                dish.category.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Restaurant Menu",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    actions = {
                        if (isAdmin) {
                            IconButton(onClick = onAddDishClick) {
                                Text("➕", fontSize = 20.sp)
                            }
                        }
                        TextButton(onClick = { showLogoutDialog = true }) {
                            Text("Log out", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                if (!isAdmin) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            selected = selectedNavItem == 0,
                            onClick = { Toast.makeText(context, "You are already here", Toast.LENGTH_SHORT).show() }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                            label = { Text("Favorites") },
                            selected = selectedNavItem == 1,
                            onClick = {
                                selectedNavItem = 1
                                val intent = Intent(context, FavouritesActivity::class.java).apply {
                                    putExtra("userId", userId)
                                }
                                context.startActivity(intent)
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                            label = { Text("Cart") },
                            selected = selectedNavItem == 2,
                            onClick = {
                                selectedNavItem = 2
                                val intent = Intent(context, CartActivity::class.java).apply {
                                    putExtra("userId", userId)
                                }
                                context.startActivity(intent)
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile") },
                            selected = selectedNavItem == 3,
                            onClick = {
                                selectedNavItem = 3
                                val intent = Intent(context, ProfileActivity::class.java).apply { putExtra("userId", userId) }
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search dishes or categories.") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White.copy(alpha = 0.9f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.9f)
                    )
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory.equals(category, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.8f),
                                labelColor = Color.DarkGray
                            )
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(1.dp)
                ) {
                    items(filteredDishes, key = { it.id }) { dish ->
                        DishRowItem(
                            dish = dish,
                            isAdmin = isAdmin,
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
                            onFavClick = {
                                scope.launch {
                                    db.favouriteDao().addFavourite(
                                        FavouriteItem(userId = userId, dishId = dish.id)
                                    )
                                    Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onOrderClick = {
                                scope.launch {
                                    db.cartDao().addToCart(
                                        CartItem(userId = userId, dishId = dish.id, name = dish.name, price = dish.price, qty = 1)
                                    )
                                    Toast.makeText(context, "${dish.name} added to cart", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEditClick = { onEditDishClick(dish.id) },
                            onDeleteClick = {
                                scope.launch {
                                    db.dishDao().deleteDish(dish)
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE)
                                .edit().clear().apply()

                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Yes", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("No")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DishRowItem(
    dish: Dish,
    isAdmin: Boolean,
    onShareClick: () -> Unit,
    onFavClick: () -> Unit,
    onOrderClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BgCardColor,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { showDetailsDialog = true },
                    onLongClick = {
                        if (isAdmin) showContextMenu = true
                    }
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
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

                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(text = dish.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = dish.category, color = CategoryTextColor)
                    Text(text = "$${dish.price}", fontWeight = FontWeight.Bold)
                }

                if (!isAdmin) {
                    IconButton(onClick = onShareClick, modifier = Modifier.size(width = 44.dp, height = 62.dp)) {
                        Text("📤", fontSize = 18.sp)
                    }
                    IconButton(onClick = onFavClick, modifier = Modifier.size(width = 44.dp, height = 62.dp)) {
                        Text("❤️", fontSize = 18.sp)
                    }
                    IconButton(onClick = onOrderClick, modifier = Modifier.size(width = 44.dp, height = 62.dp)) {
                        Text("🛒", fontSize = 18.sp)
                    }
                }
            }
        }

        DropdownMenu(expanded = showContextMenu, onDismissRequest = { showContextMenu = false }) {
            DropdownMenuItem(text = { Text("Edit") }, onClick = { showContextMenu = false; onEditClick() })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { showContextMenu = false; showDeleteDialog = true })
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Dish", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete \"${dish.name}\"?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Yes", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("No")
                    }
                }
            )
        }

        if (showDetailsDialog) {
            AlertDialog(
                onDismissRequest = { showDetailsDialog = false },
                title = { Text(text = dish.name, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = dish.description.ifEmpty { "No description." }, fontStyle = FontStyle.Italic, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "🔥 Calories: ${dish.calories} kcal", fontWeight = FontWeight.Bold)
                        Text(text = "💪 Protein: ${dish.protein} g", fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    if (!isAdmin) {
                        Button(onClick = { showDetailsDialog = false; onOrderClick() }) { Text("Add to Cart") }
                    }
                },
                dismissButton = { TextButton(onClick = { showDetailsDialog = false }) { Text("Close") } }
            )
        }
    }
}

fun getCategoryDrawable(category: String): Int {
    return when (category.lowercase().trim()) {
        "pizza" -> R.drawable.img_pizza
        "burger" -> R.drawable.img_burger
        "pasta" -> R.drawable.img_pasta
        "salad" -> R.drawable.img_salad
        "sushi" -> R.drawable.img_sushi
        "drink"-> R.drawable.img_drink
        else -> R.drawable.img_pizza
    }
}