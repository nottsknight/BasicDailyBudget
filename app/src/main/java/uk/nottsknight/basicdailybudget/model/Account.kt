package uk.nottsknight.basicdailybudget.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

@Entity
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val dailyAllowance: Int,
    val nextPayday: Instant
)

data class AccountWithSpends(
    @Embedded val account: Account,
    @Relation(parentColumn = "id", entityColumn = "accountId")
    val spends: List<Spend>
)

@Dao
interface AccountLocalDataStore {
    @Query("SELECT * FROM Account WHERE id = :id")
    suspend fun selectById(id: Int): Account?

    @Transaction
    @Query("SELECT * FROM Account WHERE id = :id")
    suspend fun selectWithSpends(id: Int): AccountWithSpends?

    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)
}

class AccountRepository(private val localDataStore: AccountLocalDataStore) {
    suspend fun select(id: Int) = withContext(Dispatchers.IO) { localDataStore.selectById(id) }
    suspend fun insert(account: Account): Long =
        withContext(Dispatchers.IO) { localDataStore.insert(account) }

    suspend fun update(account: Account) =
        withContext(Dispatchers.IO) { localDataStore.update(account) }

    suspend fun delete(account: Account) =
        withContext(Dispatchers.IO) { localDataStore.delete(account) }
}