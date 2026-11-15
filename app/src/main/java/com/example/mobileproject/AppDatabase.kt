package com.example.mobileproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Dish::class,
        CartItem::class,
        FavouriteItem::class,
        Order::class,
        OrderItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun dishDao(): DishDao
    abstract fun cartDao(): CartDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "restaurant_database"
                )
                    .fallbackToDestructiveMigration() // Safely handles schema changes during development by recreating tables
                    .addCallback(DatabaseCallback())
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    INSTANCE?.let {
                        seedDatabase(it)
                    }
                }
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            if (db.userDao().getUsersCount() == 0) {
                db.userDao().insertUser(
                    User(name = "Admin User", email = "admin@rest.com", password = "123", gender = "Male", role = "Admin")
                )
            }

            if (db.dishDao().getDishesCount() == 0) {
                val initialDishes = listOf(
                    Dish(name = "Margherita", category = "Pizza", price = 8.99, description = "Classic delight with 100% real mozzarella cheese and fresh tomato sauce.", calories = 800.0, protein = 32.0),
                    Dish(name = "Pepperoni", category = "Pizza", price = 9.99, description = "Topped with premium pepperoni, mozzarella, and our signature sauce.", calories = 950.0, protein = 40.0),
                    Dish(name = "Classic Burger", category = "Burger", price = 7.49, description = "Juicy beef patty with fresh lettuce, tomatoes, and cheddar cheese.", calories = 650.0, protein = 35.0),
                    Dish(name = "Veggie Pasta", category = "Pasta", price = 6.99, description = "Penne pasta mixed with seasonal roasted vegetables and rich marinara.", calories = 450.0, protein = 14.0),
                    Dish(name = "Greek Salad", category = "Salad", price = 5.49, description = "Fresh cucumbers, tomatoes, feta cheese, and olives with olive oil dressing.", calories = 300.0, protein = 8.0)
                )

                initialDishes.forEach {
                    db.dishDao().insertDish(it)
                }
            }
        }
    }
}