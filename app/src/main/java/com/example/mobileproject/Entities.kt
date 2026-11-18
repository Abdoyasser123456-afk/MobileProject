package com.example.mobileproject

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// User Entity
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val email: String,
    val password: String,
    val gender: String,
    val role: String
)

// Dish Entity
@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val price: Double,
    val description: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0
)

// Cart Entity
@Entity(
    tableName = "cart_items",
    indices = [Index(value = ["userId", "dishId"], unique = true)]
)
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val dishId: Long,
    val name: String,
    val price: Double,
    var qty: Int
)

// Favourite Entity
@Entity(
    tableName = "favourite_items",
    indices = [Index(value = ["userId", "dishId"], unique = true)]
)
data class FavouriteItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val dishId: Long
)

// Order Entity
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val totalAmount: Double,
    val deliveryAddress: String,
    val phoneNumber: String,
    val paymentMethod: String,
    val status: String = "Confirmed",
    val timestamp: Long = System.currentTimeMillis()
)

// OrderItem Entity
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = Order::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val dishName: String,
    val price: Double,
    val qty: Int
)

// Relation Class
data class OrderWithItems(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItem>
)