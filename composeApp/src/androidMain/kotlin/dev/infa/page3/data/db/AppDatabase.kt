//package dev.infa.page3.data.db
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//
//@Database(
//    entities = [ExerciseEntity::class],
//    version = 1,
//    exportSchema = false
//)
//abstract class AppDatabase : RoomDatabase() {
//    abstract fun exerciseDao(): ExerciseDao
//
//    companion object {
//        @Volatile private var INSTANCE: AppDatabase? = null
//        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
//            INSTANCE ?: Room.databaseBuilder(
//                context.applicationContext,
//                AppDatabase::class.java,
//                "page3.db"
//            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
//        }
//    }
//}
//
//
