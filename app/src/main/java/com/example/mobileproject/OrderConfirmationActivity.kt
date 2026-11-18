package com.example.mobileproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrderConfirmationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val orderId = intent.getLongExtra("orderId", -1L)
        val method = intent.getStringExtra("method") ?: "Cash"
        val intentUserId = intent.getLongExtra("userId", -1L)
        val userId = if (intentUserId != -1L) intentUserId else prefs.getLong("userId", -1L)
        val role = prefs.getString("role", "Customer") ?: "Customer"

        val tvOrderDetails = findViewById<TextView>(R.id.tvOrderDetails)
        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)

        tvOrderDetails.text = "Order ID: $orderId\nPayment Method: $method\nYour order has been successfully placed."

        btnBackToMenu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("role", role)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}