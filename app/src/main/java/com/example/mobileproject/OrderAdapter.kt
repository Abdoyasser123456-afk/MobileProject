package com.example.mobileproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(private var ordersList: List<OrderWithItems>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvOrderId)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvOrderStatus)
        val tvOrderItems: TextView = itemView.findViewById(R.id.tvOrderItems)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)
        val tvOrderTotal: TextView = itemView.findViewById(R.id.tvOrderTotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orderWithItems = ordersList[position]
        val order = orderWithItems.order
        val items = orderWithItems.items

        // Set Order ID
        holder.tvOrderId.text = "Order #${order.id}"

        // Set Status (You can customize status based on your logic)
        holder.tvOrderStatus.text = "Confirmed"

        // Format and combine items names and quantities (e.g., "2x Burger, 1x Pizza")
        val itemsDescription = items.joinToString(separator = ", ") { "${it.qty}x ${it.dishName}" }
        holder.tvOrderItems.text = itemsDescription

        // Format Timestamp to readable date
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateString = sdf.format(Date(order.timestamp))
        holder.tvOrderDate.text = dateString

        // Set Total Amount
        holder.tvOrderTotal.text = "Total: $${String.format(Locale.getDefault(), "%.2f", order.totalAmount)}"
    }

    override fun getItemCount(): Int = ordersList.size

    // Function to update adapter data dynamically
    fun updateOrders(newOrders: List<OrderWithItems>) {
        ordersList = newOrders
        notifyDataSetChanged()
    }
}