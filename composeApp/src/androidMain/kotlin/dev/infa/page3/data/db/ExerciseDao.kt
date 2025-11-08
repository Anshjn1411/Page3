package dev.infa.page3.data.db


import kotlinx.coroutines.flow.Flow


interface ExerciseDao {

    suspend fun clear()
}


