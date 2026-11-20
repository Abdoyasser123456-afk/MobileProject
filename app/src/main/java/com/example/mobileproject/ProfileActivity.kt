package com.example.mobileproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.ui.theme.AppBackground
import com.example.mobileproject.ui.theme.MobileProjectTheme
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)

        val intentUserId = intent.getLongExtra("userId", -1L)
        val userId = if (intentUserId != -1L) intentUserId else prefs.getLong("userId", -1L)

        setContent {
            MobileProjectTheme {
                ProfileScreen(db = db, userId = userId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(db: AppDatabase, userId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<User?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != -1L) {
            user = db.userDao().getUserById(userId)
        }
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("My Profile", color = MaterialTheme.colorScheme.onPrimary) },
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
                            val intent = Intent(context, MenuActivity::class.java).apply {
                                putExtra("userId", userId)
                            }
                            context.startActivity(intent)
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = false,
                        onClick = {
                            val intent = Intent(context, FavouritesActivity::class.java).apply {
                                putExtra("userId", userId)
                            }
                            context.startActivity(intent)
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                        label = { Text("Cart") },
                        selected = false,
                        onClick = {
                            val intent = Intent(context, CartActivity::class.java).apply {
                                putExtra("userId", userId)
                            }
                            context.startActivity(intent)
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = true,
                        onClick = { Toast.makeText(context, "You are alraedy here", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                val isFemale = user?.gender.equals("Female", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = if (isFemale) Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFemale) "👩‍🦰" else "👨‍🦰",
                        fontSize = 50.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = user?.name ?: "Loading...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = user?.email ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    onClick = {
                        val intent = Intent(context, OrderHistoryActivity::class.java).apply {
                            putExtra("userId", userId)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Order History", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    onClick = { showLogoutDialog = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "Log Out", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Red)
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

                            val intent = Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
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