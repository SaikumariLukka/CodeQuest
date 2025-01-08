package com.example.codequest.api

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LeaderboardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(leaderboardEntity: LeaderboardEntity)

    @Query("SELECT * FROM leaderboard ORDER BY score DESC")
    suspend fun getLeaderboard(): List<LeaderboardEntity>

    @Query("DELETE FROM leaderboard")
    suspend fun clearLeaderboard()
}
