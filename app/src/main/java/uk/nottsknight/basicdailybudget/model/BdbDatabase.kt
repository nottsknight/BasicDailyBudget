package uk.nottsknight.basicdailybudget.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

@Database(entities = [Account::class], version = 1)
@TypeConverters(Converters::class)
abstract class BdbDatabase : RoomDatabase() {
    abstract fun accounts(): AccountLocalDataStore
}

class Converters {
    @TypeConverter
    fun instantToLong(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun longToInstant(long: Long): Instant = Instant.ofEpochMilli(long)
}