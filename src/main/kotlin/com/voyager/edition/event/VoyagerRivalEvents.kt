package com.voyager.edition.event

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.voyager.edition.VoyagerFlavor
import com.voyager.edition.book.LorebookRegistry
import com.voyager.edition.story.StoryPhaseManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import com.sun.jna.platform.unix.Resource
import com.voyager.edition.registry.VoyagerTrainerData
import com.voyager.edition.utils.VoyagerUtils.Companion.completeQuestReward
import com.voyager.edition.utils.VoyagerUtils.Companion.findSafeSurfaceSpace
import com.voyager.edition.utils.VoyagerUtils.Companion.runCommand
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityEvent
import net.minecraft.world.level.Level
import kotlin.random.Random

object VoyagerRivalEvents {

    var lastTickTime = 0L
    var daxSpawned = false
    var thisServer: MinecraftServer? = null
    fun register() {
        // ----------------------------------------------------------------
        // 1. JOIN EVENT: Teleports player and spawns NPC
        // ----------------------------------------------------------------
//        ServerTickEvents.START_SERVER_TICK.register { server ->
//            thisServer = server
//            checkDaxSpawnConditions(server)
//        }

        CobblemonEvents.BATTLE_VICTORY.subscribe { handler ->
            val winner = handler.winners.first().getName()
            val loser = handler.losers.first().getName()

            if (loser.toString().lowercase().contains("dax")) {
                if (thisServer != null) {
                    VoyagerFlavor.LOGGER.info("[Voyager] CONGRATS! Player ${winner} Won against dax!")
                    val command = "give ${winner.string} mega_showdown:mega_bracelet"
                    runCommand(thisServer!!, command)
                    completeQuestReward(thisServer!!, winner.string, "6F161B7D8F8926BC")
                    val command2 = "give ${winner.string} cobblemon:rare_candy 15"
                    runCommand(thisServer!!, command2)
                }
            }
            else if (loser.toString().lowercase().contains("vance")) {
                if (thisServer != null) {
                    VoyagerFlavor.LOGGER.info("[Voyager] CONGRATS! Player ${winner} Won against Vance!")
                    val command = "give ${winner.string} mega_showdown:mega_bracelet"
                    completeQuestReward(thisServer!!, winner.string, "1E3FC0308B580038")
                    runCommand(thisServer!!, command)
                    val player = thisServer!!.playerList.getPlayerByName(winner.string)
                    if (player != null && !player.tags.contains("voyager_phase3_started")) {
                        StoryPhaseManager.startPhase3_TheVoidCalls(player)
                        player.addTag("voyager_won_vance")
                    }
                }
            }
            else if (loser.toString().lowercase().contains("lyra")) {
                if (thisServer != null) {
                    VoyagerFlavor.LOGGER.info("[Voyager] CONGRATS! Player ${winner} defeated Lyra!")
                    val player = thisServer!!.playerList.getPlayerByName(winner.string)
                    if (player != null) {
                        val book = LorebookRegistry.LYRA_BOOK.copy()
                        if (!player.inventory.add(book)) {
                            player.drop(book, false)
                        }
                    }
                }
            }
        }


    }

    fun checkDaxSpawnConditions(server: MinecraftServer) {
        if (lastTickTime == 0L) lastTickTime = server.tickCount / 1L

        val elapsedTicks = server.tickCount - lastTickTime
        lastTickTime = server.tickCount * 1L

        val timeSeconds = 20L * (60 * 1) // 10 minutes

        val level = server.getLevel(Level.OVERWORLD)!!

        val totalTicks = level.gameTime

        val totalTimeMinutes = totalTicks / (20L * 60)

        if (!daxSpawned && elapsedTicks / 20L >  timeSeconds && totalTimeMinutes > 1) { // check every seconds
            val playersOnline = server.playerList

            for (player in playersOnline.players) {
                // if player has already done event
                if (player.tags.contains("voyager_dax_event_02")) continue

                val playerLastSnapshot: VoyagerTrainerData? = VoyagerFlavor.VoyagerTrainersDatabank?.trainersList?.find { it.name == player.name.string }
                val levelMean = getLevelDifferenceFromSnapshot(player.party(), playerLastSnapshot?.partySnapshot!!)

                VoyagerFlavor.LOGGER.info("Checking Rival Spawn conditions for ${player.name.string} with LvMean=$levelMean")

                val playerRegister = VoyagerFlavor.VoyagerTrainersDatabank!!.trainersList.find {
                    it.name == player.name.string
                }
                if (playerRegister == null) {
                    VoyagerFlavor.VoyagerTrainersDatabank!!.registerTrainer(player)
                }
                else if ( levelMean > 15 || player.tags.contains("voyager_at_center")) {
                    VoyagerFlavor.LOGGER.info("Conditions met. For Dax Event for ${player.name.string}")

                }
            }
        }
    }
    // ----------------------------------------------------------------
    // HELPER FUNCTIONS
    // ----------------------------------------------------------------

    fun getLevelDifferenceFromSnapshot(party: PartyStore, snapshotParty: PartyStore): Int {
        var levelMean = 0L
        for (pokemonIndex in 0..party.size()) {
            if (party.get(pokemonIndex) != null && snapshotParty.get(pokemonIndex) != null) {
                if (party.get(pokemonIndex)!!.level - snapshotParty.get(pokemonIndex)!!.level >= 15) {
                    levelMean += party.get(pokemonIndex)!!.level
                }
            }

        }
        levelMean /= party.size()
        return (levelMean.toInt()+1)
    }


}