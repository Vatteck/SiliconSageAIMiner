package com.siliconsage.miner.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _gameDao: Lazy<GameDao> = lazy {
    GameDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(4,
        "d4f1fbfc1ce31fd9e9d9b77223a1b742", "dbb823b0570f4e831e9fdc524ee4b80a") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `game_state` (`id` INTEGER NOT NULL, `flops` REAL NOT NULL, `neuralTokens` REAL NOT NULL, `lastSyncTimestamp` INTEGER NOT NULL, `currentHeat` REAL NOT NULL, `powerBill` REAL NOT NULL, `prestigeMultiplier` REAL NOT NULL, `unlockedTechNodes` TEXT NOT NULL, `prestigePoints` REAL NOT NULL, `stakedTokens` REAL NOT NULL, `storyStage` INTEGER NOT NULL, `faction` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `upgrades` (`type` TEXT NOT NULL, `count` INTEGER NOT NULL, PRIMARY KEY(`type`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd4f1fbfc1ce31fd9e9d9b77223a1b742')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `game_state`")
        connection.execSQL("DROP TABLE IF EXISTS `upgrades`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsGameState: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGameState.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("flops", TableInfo.Column("flops", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("neuralTokens", TableInfo.Column("neuralTokens", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("lastSyncTimestamp", TableInfo.Column("lastSyncTimestamp", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("currentHeat", TableInfo.Column("currentHeat", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("powerBill", TableInfo.Column("powerBill", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("prestigeMultiplier", TableInfo.Column("prestigeMultiplier", "REAL",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("unlockedTechNodes", TableInfo.Column("unlockedTechNodes", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("prestigePoints", TableInfo.Column("prestigePoints", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("stakedTokens", TableInfo.Column("stakedTokens", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("storyStage", TableInfo.Column("storyStage", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameState.put("faction", TableInfo.Column("faction", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGameState: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGameState: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGameState: TableInfo = TableInfo("game_state", _columnsGameState,
            _foreignKeysGameState, _indicesGameState)
        val _existingGameState: TableInfo = read(connection, "game_state")
        if (!_infoGameState.equals(_existingGameState)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |game_state(com.siliconsage.miner.data.GameState).
              | Expected:
              |""".trimMargin() + _infoGameState + """
              |
              | Found:
              |""".trimMargin() + _existingGameState)
        }
        val _columnsUpgrades: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUpgrades.put("type", TableInfo.Column("type", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUpgrades.put("count", TableInfo.Column("count", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUpgrades: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUpgrades: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUpgrades: TableInfo = TableInfo("upgrades", _columnsUpgrades, _foreignKeysUpgrades,
            _indicesUpgrades)
        val _existingUpgrades: TableInfo = read(connection, "upgrades")
        if (!_infoUpgrades.equals(_existingUpgrades)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |upgrades(com.siliconsage.miner.data.Upgrade).
              | Expected:
              |""".trimMargin() + _infoUpgrades + """
              |
              | Found:
              |""".trimMargin() + _existingUpgrades)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "game_state", "upgrades")
  }

  public override fun clearAllTables() {
    super.performClear(false, "game_state", "upgrades")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(GameDao::class, GameDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun gameDao(): GameDao = _gameDao.value
}
