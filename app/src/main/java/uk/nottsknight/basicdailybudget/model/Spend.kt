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
data class Spend(
    @PrimaryKey val id: Int,
    val accountId: Int,
    val date: Instant,
    val amount: Int,
    val label: String
)

@Dao
interface SpendLocalDataStore {
    @Query("SELECT * FROM Spend WHERE id = :id")
    suspend fun selectById(id: Int): Spend?

    @Query("SELECT * FROM Spend WHERE accountId = :accountId ORDER BY date DESC")
    suspend fun selectAllByAccountId(accountId: Int): List<Spend>

    @Insert
    suspend fun insert(spend: Spend)

    @Update
    suspend fun update(spend: Spend)

    @Delete
    suspend fun delete(spend: Spend)
}

class SpendRepository(private val localData: SpendLocalDataStore) {
    suspend fun getAllByAccount(accountId: Int) = localData.selectAllByAccountId(accountId)
}