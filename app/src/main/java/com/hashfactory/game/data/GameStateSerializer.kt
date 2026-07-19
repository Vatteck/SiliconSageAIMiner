package com.hashfactory.game.data

import androidx.datastore.core.Serializer
import com.hashfactory.core.model.GameState
import com.hashfactory.core.persistence.SaveCodec
import java.io.InputStream
import java.io.OutputStream

/**
 * DataStore adapter over the pure SaveCodec in :core. SaveCodec.decode never
 * throws — unrecoverable input degrades to a default state instead of crashing
 * or wiping (CLAUDE.md rule 4).
 */
object GameStateSerializer : Serializer<GameState> {
    override val defaultValue: GameState = GameState()

    override suspend fun readFrom(input: InputStream): GameState =
        SaveCodec.decode(input.readBytes().decodeToString())

    override suspend fun writeTo(t: GameState, output: OutputStream) {
        output.write(SaveCodec.encode(t).encodeToByteArray())
    }
}
