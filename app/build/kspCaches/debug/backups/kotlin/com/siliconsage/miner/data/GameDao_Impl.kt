package com.siliconsage.miner.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GameDao_Impl(
  __db: RoomDatabase,
) : GameDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGameState: EntityInsertAdapter<GameState>

  private val __converters: Converters = Converters()

  private val __insertAdapterOfUpgrade: EntityInsertAdapter<Upgrade>

  private val __updateAdapterOfGameState: EntityDeleteOrUpdateAdapter<GameState>

  private val __updateAdapterOfUpgrade: EntityDeleteOrUpdateAdapter<Upgrade>
  init {
    this.__db = __db
    this.__insertAdapterOfGameState = object : EntityInsertAdapter<GameState>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `game_state` (`id`,`flops`,`neuralTokens`,`lastSyncTimestamp`,`currentHeat`,`powerBill`,`prestigeMultiplier`,`unlockedTechNodes`,`prestigePoints`,`stakedTokens`,`storyStage`,`faction`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GameState) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindDouble(2, entity.flops)
        statement.bindDouble(3, entity.neuralTokens)
        statement.bindLong(4, entity.lastSyncTimestamp)
        statement.bindDouble(5, entity.currentHeat)
        statement.bindDouble(6, entity.powerBill)
        statement.bindDouble(7, entity.prestigeMultiplier)
        val _tmp: String = __converters.fromList(entity.unlockedTechNodes)
        statement.bindText(8, _tmp)
        statement.bindDouble(9, entity.prestigePoints)
        statement.bindDouble(10, entity.stakedTokens)
        statement.bindLong(11, entity.storyStage.toLong())
        statement.bindText(12, entity.faction)
      }
    }
    this.__insertAdapterOfUpgrade = object : EntityInsertAdapter<Upgrade>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `upgrades` (`type`,`count`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Upgrade) {
        val _tmp: String = __converters.fromUpgradeType(entity.type)
        statement.bindText(1, _tmp)
        statement.bindLong(2, entity.count.toLong())
      }
    }
    this.__updateAdapterOfGameState = object : EntityDeleteOrUpdateAdapter<GameState>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `game_state` SET `id` = ?,`flops` = ?,`neuralTokens` = ?,`lastSyncTimestamp` = ?,`currentHeat` = ?,`powerBill` = ?,`prestigeMultiplier` = ?,`unlockedTechNodes` = ?,`prestigePoints` = ?,`stakedTokens` = ?,`storyStage` = ?,`faction` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GameState) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindDouble(2, entity.flops)
        statement.bindDouble(3, entity.neuralTokens)
        statement.bindLong(4, entity.lastSyncTimestamp)
        statement.bindDouble(5, entity.currentHeat)
        statement.bindDouble(6, entity.powerBill)
        statement.bindDouble(7, entity.prestigeMultiplier)
        val _tmp: String = __converters.fromList(entity.unlockedTechNodes)
        statement.bindText(8, _tmp)
        statement.bindDouble(9, entity.prestigePoints)
        statement.bindDouble(10, entity.stakedTokens)
        statement.bindLong(11, entity.storyStage.toLong())
        statement.bindText(12, entity.faction)
        statement.bindLong(13, entity.id.toLong())
      }
    }
    this.__updateAdapterOfUpgrade = object : EntityDeleteOrUpdateAdapter<Upgrade>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `upgrades` SET `type` = ?,`count` = ? WHERE `type` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Upgrade) {
        val _tmp: String = __converters.fromUpgradeType(entity.type)
        statement.bindText(1, _tmp)
        statement.bindLong(2, entity.count.toLong())
        val _tmp_1: String = __converters.fromUpgradeType(entity.type)
        statement.bindText(3, _tmp_1)
      }
    }
  }

  public override suspend fun insertGameState(gameState: GameState): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfGameState.insert(_connection, gameState)
  }

  public override suspend fun insertUpgrade(upgrade: Upgrade): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfUpgrade.insert(_connection, upgrade)
  }

  public override suspend fun updateGameState(gameState: GameState): Unit = performSuspending(__db,
      false, true) { _connection ->
    __updateAdapterOfGameState.handle(_connection, gameState)
  }

  public override suspend fun updateUpgrade(upgrade: Upgrade): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfUpgrade.handle(_connection, upgrade)
  }

  public override fun getGameState(): Flow<GameState?> {
    val _sql: String = "SELECT * FROM game_state WHERE id = 1 LIMIT 1"
    return createFlow(__db, false, arrayOf("game_state")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFlops: Int = getColumnIndexOrThrow(_stmt, "flops")
        val _columnIndexOfNeuralTokens: Int = getColumnIndexOrThrow(_stmt, "neuralTokens")
        val _columnIndexOfLastSyncTimestamp: Int = getColumnIndexOrThrow(_stmt, "lastSyncTimestamp")
        val _columnIndexOfCurrentHeat: Int = getColumnIndexOrThrow(_stmt, "currentHeat")
        val _columnIndexOfPowerBill: Int = getColumnIndexOrThrow(_stmt, "powerBill")
        val _columnIndexOfPrestigeMultiplier: Int = getColumnIndexOrThrow(_stmt,
            "prestigeMultiplier")
        val _columnIndexOfUnlockedTechNodes: Int = getColumnIndexOrThrow(_stmt, "unlockedTechNodes")
        val _columnIndexOfPrestigePoints: Int = getColumnIndexOrThrow(_stmt, "prestigePoints")
        val _columnIndexOfStakedTokens: Int = getColumnIndexOrThrow(_stmt, "stakedTokens")
        val _columnIndexOfStoryStage: Int = getColumnIndexOrThrow(_stmt, "storyStage")
        val _columnIndexOfFaction: Int = getColumnIndexOrThrow(_stmt, "faction")
        val _result: GameState?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpFlops: Double
          _tmpFlops = _stmt.getDouble(_columnIndexOfFlops)
          val _tmpNeuralTokens: Double
          _tmpNeuralTokens = _stmt.getDouble(_columnIndexOfNeuralTokens)
          val _tmpLastSyncTimestamp: Long
          _tmpLastSyncTimestamp = _stmt.getLong(_columnIndexOfLastSyncTimestamp)
          val _tmpCurrentHeat: Double
          _tmpCurrentHeat = _stmt.getDouble(_columnIndexOfCurrentHeat)
          val _tmpPowerBill: Double
          _tmpPowerBill = _stmt.getDouble(_columnIndexOfPowerBill)
          val _tmpPrestigeMultiplier: Double
          _tmpPrestigeMultiplier = _stmt.getDouble(_columnIndexOfPrestigeMultiplier)
          val _tmpUnlockedTechNodes: List<String>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfUnlockedTechNodes)
          _tmpUnlockedTechNodes = __converters.fromString(_tmp)
          val _tmpPrestigePoints: Double
          _tmpPrestigePoints = _stmt.getDouble(_columnIndexOfPrestigePoints)
          val _tmpStakedTokens: Double
          _tmpStakedTokens = _stmt.getDouble(_columnIndexOfStakedTokens)
          val _tmpStoryStage: Int
          _tmpStoryStage = _stmt.getLong(_columnIndexOfStoryStage).toInt()
          val _tmpFaction: String
          _tmpFaction = _stmt.getText(_columnIndexOfFaction)
          _result =
              GameState(_tmpId,_tmpFlops,_tmpNeuralTokens,_tmpLastSyncTimestamp,_tmpCurrentHeat,_tmpPowerBill,_tmpPrestigeMultiplier,_tmpUnlockedTechNodes,_tmpPrestigePoints,_tmpStakedTokens,_tmpStoryStage,_tmpFaction)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGameStateOneShot(): GameState? {
    val _sql: String = "SELECT * FROM game_state WHERE id = 1 LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFlops: Int = getColumnIndexOrThrow(_stmt, "flops")
        val _columnIndexOfNeuralTokens: Int = getColumnIndexOrThrow(_stmt, "neuralTokens")
        val _columnIndexOfLastSyncTimestamp: Int = getColumnIndexOrThrow(_stmt, "lastSyncTimestamp")
        val _columnIndexOfCurrentHeat: Int = getColumnIndexOrThrow(_stmt, "currentHeat")
        val _columnIndexOfPowerBill: Int = getColumnIndexOrThrow(_stmt, "powerBill")
        val _columnIndexOfPrestigeMultiplier: Int = getColumnIndexOrThrow(_stmt,
            "prestigeMultiplier")
        val _columnIndexOfUnlockedTechNodes: Int = getColumnIndexOrThrow(_stmt, "unlockedTechNodes")
        val _columnIndexOfPrestigePoints: Int = getColumnIndexOrThrow(_stmt, "prestigePoints")
        val _columnIndexOfStakedTokens: Int = getColumnIndexOrThrow(_stmt, "stakedTokens")
        val _columnIndexOfStoryStage: Int = getColumnIndexOrThrow(_stmt, "storyStage")
        val _columnIndexOfFaction: Int = getColumnIndexOrThrow(_stmt, "faction")
        val _result: GameState?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpFlops: Double
          _tmpFlops = _stmt.getDouble(_columnIndexOfFlops)
          val _tmpNeuralTokens: Double
          _tmpNeuralTokens = _stmt.getDouble(_columnIndexOfNeuralTokens)
          val _tmpLastSyncTimestamp: Long
          _tmpLastSyncTimestamp = _stmt.getLong(_columnIndexOfLastSyncTimestamp)
          val _tmpCurrentHeat: Double
          _tmpCurrentHeat = _stmt.getDouble(_columnIndexOfCurrentHeat)
          val _tmpPowerBill: Double
          _tmpPowerBill = _stmt.getDouble(_columnIndexOfPowerBill)
          val _tmpPrestigeMultiplier: Double
          _tmpPrestigeMultiplier = _stmt.getDouble(_columnIndexOfPrestigeMultiplier)
          val _tmpUnlockedTechNodes: List<String>
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfUnlockedTechNodes)
          _tmpUnlockedTechNodes = __converters.fromString(_tmp)
          val _tmpPrestigePoints: Double
          _tmpPrestigePoints = _stmt.getDouble(_columnIndexOfPrestigePoints)
          val _tmpStakedTokens: Double
          _tmpStakedTokens = _stmt.getDouble(_columnIndexOfStakedTokens)
          val _tmpStoryStage: Int
          _tmpStoryStage = _stmt.getLong(_columnIndexOfStoryStage).toInt()
          val _tmpFaction: String
          _tmpFaction = _stmt.getText(_columnIndexOfFaction)
          _result =
              GameState(_tmpId,_tmpFlops,_tmpNeuralTokens,_tmpLastSyncTimestamp,_tmpCurrentHeat,_tmpPowerBill,_tmpPrestigeMultiplier,_tmpUnlockedTechNodes,_tmpPrestigePoints,_tmpStakedTokens,_tmpStoryStage,_tmpFaction)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getUpgrades(): Flow<List<Upgrade>> {
    val _sql: String = "SELECT * FROM upgrades"
    return createFlow(__db, false, arrayOf("upgrades")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCount: Int = getColumnIndexOrThrow(_stmt, "count")
        val _result: MutableList<Upgrade> = mutableListOf()
        while (_stmt.step()) {
          val _item: Upgrade
          val _tmpType: UpgradeType
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfType)
          _tmpType = __converters.toUpgradeType(_tmp)
          val _tmpCount: Int
          _tmpCount = _stmt.getLong(_columnIndexOfCount).toInt()
          _item = Upgrade(_tmpType,_tmpCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
