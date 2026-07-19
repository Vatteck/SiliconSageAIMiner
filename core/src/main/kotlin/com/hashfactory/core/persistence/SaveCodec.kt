package com.hashfactory.core.persistence

import com.hashfactory.core.model.GameState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Save serialization + forward migration, pure and unit-testable in :core.
 * Destructive fallback is forbidden (CLAUDE.md rule 4): decode never throws and
 * migration is a stepwise v(n) -> v(n+1) chain on raw JSON.
 */
object SaveCodec {

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(state: GameState): String =
        json.encodeToString(GameState.serializer(), state)

    /** Returns a fresh default state only if [raw] is unrecoverable garbage. */
    fun decode(raw: String): GameState = try {
        val root = json.parseToJsonElement(raw).jsonObject
        val migrated = migrate(root)
        json.decodeFromJsonElement<GameState>(migrated)
            .copy(schemaVersion = GameState.CURRENT_SCHEMA_VERSION)
    } catch (_: Exception) {
        GameState()
    }

    /** Stepwise migration chain. Add a case per schema bump; never delete one. */
    internal fun migrate(root: JsonObject): JsonObject {
        var current = root
        var version = current["schemaVersion"]?.jsonPrimitive?.intOrNull ?: 1
        while (version < GameState.CURRENT_SCHEMA_VERSION) {
            current = when (version) {
                // Example shape for the future:
                // 1 -> migrateV1toV2(current)
                else -> current
            }
            version++
        }
        return withVersion(current, GameState.CURRENT_SCHEMA_VERSION)
    }

    private fun withVersion(obj: JsonObject, version: Int): JsonObject =
        JsonObject(obj + ("schemaVersion" to JsonPrimitive(version)))
}
