package com.voyager.edition.event

import com.voyager.edition.VoyagerFlavor
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import com.cobblemon.mod.common.util.server
import com.voyager.edition.registry.VoyagerTrainerData
import com.voyager.edition.utils.VoyagerUtils.Companion.findSafeSurfaceSpace
import com.voyager.edition.utils.VoyagerUtils.Companion.runCommand
import com.voyager.edition.utils.VoyagerUtils.Companion.sendPlayerMessage
import com.voyager.edition.utils.VoyagerUtils.Companion.spawnProfessor
import com.voyager.edition.utils.VoyagerUtils.Companion.spawnVanceAt
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block

object VoyagerEvents {
    var spawnedVance = false
    var spawnedRedwood = false

    fun register() {
        // ----------------------------------------------------------------
        // 1. JOIN EVENT: Teleports player and spawns NPC
        // ----------------------------------------------------------------
        lateinit var biome: ResourceKey<Biome>
        var bPos: BlockPos? = null

//        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
//            val player = handler.player
//        }
        // ----------------------------------------------------------------
        // 2. TICK EVENT: Prevents escaping the cave (Gatekeeper)
        // ----------------------------------------------------------------
        ServerTickEvents.START_SERVER_TICK.register { server ->
            if (server.tickCount % 20 == 0) { // Check every second
                for (player in server.playerList.players) {
                    val tickToSeconds = 20 * 20 // 20 seconds
                    if (player.tickCount < tickToSeconds) continue

                    if (!player.tags.contains("voyager_journey_started") && !spawnedRedwood) {
                        spawnProfessor(player.serverLevel(), player.blockPosition())
                        spawnedRedwood = true
                        player.tags.add("voyager_journey_started")
                    }
                    // Check if they are in the Tutorial Phase
                    if (player.tags.contains("voyager.first_join")) {

                        // Check party size
                        val party = Cobblemon.storage.getParty(player)

                        if (party.size() == 0) {
                            // Find the spawn tag we saved earlier
                            val spawnTag = player.tags.firstOrNull { it.startsWith("voyager_spawn:") }

                            // Debug Log (Now this should work!)
                            VoyagerFlavor.LOGGER.info("${player.name.string} has tag ${spawnTag}")

                            if (spawnTag != null) {
                                try {
                                    val rawCoords = spawnTag.removePrefix("voyager_spawn:").split(",")
                                    val spawnX = rawCoords[0].toDouble()
                                    val spawnY = rawCoords[1].toDouble()
                                    val spawnZ = rawCoords[2].toDouble()

                                    // Distance Check (15 blocks)
                                    if (player.distanceToSqr(spawnX, spawnY, spawnZ) > 225) {
                                        player.teleportTo(spawnX + 0.5, spawnY, spawnZ + 0.5)
                                        player.sendSystemMessage(
                                            Component.literal("Prof. Redwood: Wait! It's too dangerous without a Pokémon!")
                                                .withStyle(ChatFormatting.RED)
                                        )
                                    }
                                } catch (e: Exception) {
                                    VoyagerFlavor.LOGGER.error("Error parsing spawn tag: $spawnTag")
                                }
                            }
                        } else {
                            // Tutorial Complete!
                            player.removeTag("voyager.first_join")
                            player.tags.removeIf { it.startsWith("voyager_spawn:") }

                            player.sendSystemMessage(
                                Component.literal("Good luck out there, Voyager!")
                                    .withStyle(ChatFormatting.GREEN)
                            )
                        }
                    }


                    if (player.tags.contains("voyager_has_license") && player.tickCount > 600
                        && !spawnedVance
                        && !player.tags.contains("voyager_won_vance")) {

                        val spawnPos = findSafeSurfaceSpace(player, 150)!!
                        spawnVanceAt(player, spawnPos)
                        sendPlayerMessage(player, "Essa pressão...", ChatFormatting.GOLD)
                        sendPlayerMessage(player, "Capitão Vance deve estar por perto!", ChatFormatting.GOLD)
                        spawnedVance = true
                    }

                }
            }

            for (player in server.playerList.players) {
                if (VoyagerFlavor.VoyagerTrainersDatabank!!.trainersList.find
                    { it.name == player.name.string } == null) {
                    VoyagerFlavor.VoyagerTrainersDatabank!!.trainersList.add(
                        VoyagerTrainerData(
                            player.name.string,
                            player.party(),
                            player.server.tickCount.toLong()
                        )
                    )
                }
            }
        }

        ServerTickEvents.START_SERVER_TICK.register { server ->
            val tickToSeconds = 20 * 15 //

            if (server.tickCount % 20 == 0) {
                for (player in server.playerList.players) {
                    if (player.tags.contains("voyager_lyra_event")
                        && player.tickCount / 20 >= tickToSeconds) {
                        VoyagerFlavor.LOGGER.info("${player.name.string} has met conditions for ultra portal")
                        val command = "ultrabeasts wormhole spawn"
                        runCommand(server, command)
                        player.sendSystemMessage(
                            Component.literal("[ULTRA-SCANNER]-> ALERTA! ANOMALIA ESPACIAL DETECTADA PRÓXIMA A SUA LOCALIZAÇÃO!!!")
                                .withStyle(ChatFormatting.RED)
                        )
                        player.tags.removeIf { it.contains("voyager_lyra_event") }
                    }
                }
            }
        }

        CobblemonEvents.BATTLE_STARTED_POST.subscribe { battle ->
            val trainerA  = battle.battle.actors.first()
            val trainerB  = battle.battle.actors.last()

            if (trainerA.type.name.lowercase().contains("player") &&
                !trainerB.type.name.lowercase().contains("player")
                && !trainerA.pokemonList.first().originalPokemon.isWild()) {
                trainerB.pokemonList.forEach { pokemon ->
                    pokemon.originalPokemon.persistentData.putBoolean("voyager_catchable", true)
                    pokemon.entity?.pokemon?.persistentData?.putBoolean("voyager_catchable", true)
                }
                VoyagerFlavor.LOGGER.info("[Voyager]: Player X Npc Battle, able to catch")

            }
            else if (!trainerA.type.name.lowercase().contains("player") &&
                trainerB.type.name.lowercase().contains("player")) {
                trainerA.pokemonList.forEach { pokemon ->
                    pokemon.originalPokemon.persistentData.putBoolean("voyager_catchable", true)
                    pokemon.entity?.pokemon?.persistentData?.putBoolean("voyager_catchable", true)
                }
                VoyagerFlavor.LOGGER.info("[Voyager]: NPC X PLayer Battle, able to catch")

            } else {
                VoyagerFlavor.LOGGER.error("[Voyager]: Player X Player Battle, unable to catch")
            }
        }

        CobblemonEvents.THROWN_POKEBALL_HIT.subscribe { hitEvent ->
            val player: Player = hitEvent.pokeBall.effectSource as Player
            val pokemon = hitEvent.pokemon.pokemon

            if (player in hitEvent.pokemon.server!!.playerList.players) {
                if (pokemon.persistentData.getBoolean("voyager_catchable")) {
                    VoyagerFlavor.LOGGER.info("${player.name.string} is attempting to catch another trainer PKMN!")

                    sendPlayerMessage(
                        player as ServerPlayer,
                        "[${pokemon.originalTrainer}] BASTARD! DONT STEAL MY POKEMON!!!",
                        ChatFormatting.DARK_RED)

                    // TODO: Add some kind of punishment for player (Ex: add to a WantedList or decrease CatchRate calculator values or both!)
                }
            } else {
                VoyagerFlavor.LOGGER.error("[Voyager]-> Player not in player list????")
            }
        }
    }

    // ----------------------------------------------------------------
    // HELPER FUNCTIONS
    // ----------------------------------------------------------------


}