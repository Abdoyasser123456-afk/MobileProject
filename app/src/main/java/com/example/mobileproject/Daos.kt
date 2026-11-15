package com.example.mobileproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// User DAO
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email AND password = :pass LIMIT 1")
    suspend fun checkLogin(email: String, pass: String): User?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCount(): Int
}

// Dish DAO
@Dao
interface DishDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDish(dish: Dish): Long

    @Query("SELECT * FROM dishes ORDER BY category, name")
    fun getAllDishes(): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE name LIKE '%' || :query || '%' ORDER BY category, name")
    fun searchDishes(query: String): Flow<List<Dish>>

    @Query("SELECT * FROM dishes WHERE id = :id LIMIT 1")
    suspend fun getDishById(id: Long): Dish?

    @Update
    suspend fun updateDish(dish: Dish)

    @Delete
    suspend fun deleteDish(dish: Dish)

    @Query("SELECT COUNT(*) FROM dishes")
    suspend fun getDishesCount(): Int
}

// Cart DAO
@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: Long): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    suspend fun getCartItemsList(userId: Long): List<CartItem>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND dishId = :dishId LIMIT 1")
    suspend fun getCartItem(userId: Long, dishId: Long): CartItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("UPDATE cart_items SET qty = qty + :additionalQty WHERE id = :itemId")
    suspend fun increaseCartItemQty(itemId: Long, additionalQty: Int)

    @Transaction
    suspend fun addToCart(item: CartItem) {
        val existingItem = getCartItem(item.userId, item.dishId)
        if (existingItem != null) {
            increaseCartItemQty(existingItem.id, item.qty)
        } else {
            insertCartItem(item)
        }
    }

    @Query("UPDATE cart_items SET qty = :newQty WHERE id = :itemId")
    suspend fun updateQty(itemId: Long, newQty: Int)

    @Query("DELETE FROM cart_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)
}

// Favourite DAO
@Dao
interface FavouriteDao {

    @Query("SELECT * FROM favourite_items WHERE userId = :userId")
    fun getFavourites(userId: Long): Flow<List<FavouriteItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavourite(item: FavouriteItem)

    @Query("DELETE FROM favourite_items WHERE dishId = :dishId AND userId = :userId")
    suspend fun removeFavourite(dishId: Long, userId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_items WHERE dishId = :dishId AND userId = :userId)")
    suspend fun isFavourite(dishId: Long, userId: Long): Boolean
}

// Order DAO
@Dao
interface OrderDao {

    @Insert
    suspend fun insertOrder(order: Order): Long

    @Insert
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearUserCart(userId: Long)

    // Retrieve all past orders for a specific user along with their dish details
    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserOrders(userId: Long): Flow<List<OrderWithItems>>

    // Retrieve all orders (for admin management)
    @Transaction
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderWithItems>>
}