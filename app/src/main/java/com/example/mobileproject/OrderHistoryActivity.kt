package com.example.mobileproject

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var rvOrders: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        val toolbar = findViewById<Toolbar>(R.id.toolbarOrderHistory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        val intentUserId = intent.getLongExtra("userId", -1L)
        val userId = if (intentUserId != -1L) intentUserId else prefs.getLong("userId", -1L)

        rvOrders = findViewById(R.id.rvOrders)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        rvOrders.layoutManager = LinearLayoutManager(this)

        orderAdapter = OrderAdapter(emptyList())
        rvOrders.adapter = orderAdapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                AppDatabase.getDatabase(this@OrderHistoryActivity)
                    .orderDao()
                    .getUserOrders(userId)
                    .collectLatest { ordersWithItems ->
                        orderAdapter.updateOrders(ordersWithItems)

                        if (ordersWithItems.isEmpty()) {
                            tvEmptyState.visibility = View.VISIBLE
                            rvOrders.visibility = View.GONE
                        } else {
                            tvEmptyState.visibility = View.GONE
                            rvOrders.visibility = View.VISIBLE
                        }
                    }
            }
        }
    }
}