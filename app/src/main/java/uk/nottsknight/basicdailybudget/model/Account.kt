package uk.nottsknight.basicdailybudget.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import java.time.Instant

@Entity
data class Account(
    @PrimaryKey val id: Int,
    val dailyAllowance: Int,
    val nextPayday: Instant
)

@Dao
interface AccountLocalDataStore {
    @Query("SELECT * FROM Account WHERE id = :id")
    suspend fun selectById(id: Int): Account?

    @Insert
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)
}

class AccountRepository(private val localDataStore: AccountLocalDataStore) {
    suspend fun select(id: Int) = localDataStore.selectById(id)
    suspend fun insert(account: Account) = localDataStore.insert(account)
    suspend fun update(account: Account) = localDataStore.update(account)
    suspend fun delete(account: Account) = localDataStore.delete(account)
}