package com.example.codequest.api

class LeaderboardRepository(private val leaderboardDao: LeaderboardDao) {

    suspend fun insertScore(username: String, score: Int, timestamp: Long) {
        val leaderboardEntity = LeaderboardEntity(username = username, score = score, timestamp = timestamp)
        leaderboardDao.insertScore(leaderboardEntity)
    }

    suspend fun getLeaderboard(): List<LeaderboardEntity> {
        return leaderboardDao.getLeaderboard()
    }

    suspend fun clearLeaderboard() {
        leaderboardDao.clearLeaderboard()
    }
}
