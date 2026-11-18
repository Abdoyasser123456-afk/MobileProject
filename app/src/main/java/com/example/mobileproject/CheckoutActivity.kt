package com.example.mobileproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCheckout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val initialMethod = intent.getStringExtra("method") ?: "Cash"
        val intentUserId = intent.getLongExtra("userId", -1L)
        val userId = if (intentUserId != -1L) intentUserId else prefs.getLong("userId", -1L)

        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val spinnerPayment = findViewById<Spinner>(R.id.spinnerPayment)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)

        val paymentOptions = arrayOf("Cash", "Card", "Wallet")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paymentOptions)
        spinnerPayment.adapter = adapter

        val spinnerPosition = adapter.getPosition(initialMethod)
        if (spinnerPosition >= 0) {
            spinnerPayment.setSelection(spinnerPosition)
        }

        btnConfirm.setOnClickListener {
            val address = etAddress.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val selectedMethod = spinnerPayment.selectedItem.toString()

            if (address.isBlank() || phone.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length != 11) {
                Toast.makeText(this, "Please enter a real number!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (userId == -1L) {
                Toast.makeText(this, "Session expired, please log in again", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(this@CheckoutActivity)
                    val cartItems = db.cartDao().getCartItemsList(userId)

                    if (cartItems.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CheckoutActivity, "Your cart is empty!", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val totalAmount = cartItems.sumOf { it.price * it.qty }

                    val order = Order(
                        userId = userId,
                        totalAmount = totalAmount,
                        deliveryAddress = address,
                        phoneNumber = phone,
                        paymentMethod = selectedMethod,
                        timestamp = System.currentTimeMillis()
                    )

                    val generatedOrderId = db.withTransaction {
                        val orderId = db.orderDao().insertOrder(order)

                        val orderItems = cartItems.map { cartItem ->
                            OrderItem(
                                orderId = orderId,
                                dishName = cartItem.name,
                                price = cartItem.price,
                                qty = cartItem.qty
                            )
                        }
                        db.orderDao().insertOrderItems(orderItems)
                        db.orderDao().clearUserCart(userId)

                        orderId
                    }

                    withContext(Dispatchers.Main) {
                        val i = Intent(this@CheckoutActivity, OrderConfirmationActivity::class.java).apply {
                            putExtra("orderId", generatedOrderId)
                            putExtra("method", selectedMethod)
                            putExtra("userId", userId)
                        }
                        startActivity(i)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CheckoutActivity, "Order failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}