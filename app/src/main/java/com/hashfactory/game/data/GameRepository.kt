package com.hashfactory.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.hashfactory.core.model.GameState
import kotlinx.coroutines.flow.first

private val Context.gameStateStore: DataStore<GameState> by dataStore(
    fileName = "game_state.json",
    serializer = GameStateSerializer,
)

class GameRepository(private val context: Context) {

    suspend fun load(): GameState = context.gameStateStore.data.first()

    suspend fun save(state: GameState) {
        context.gameStateStore.updateData { state }
    }
}
