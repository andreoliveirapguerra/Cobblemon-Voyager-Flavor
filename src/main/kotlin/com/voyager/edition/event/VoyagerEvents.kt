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
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.level.Level

object VoyagerEvents {

    fun register() {
        // ----------------------------------------------------------------
        // 1. JOIN EVENT: Teleports player and spawns NPC
        // ----------------------------------------------------------------
        lateinit var biome: ResourceKey<Biome>
        var bPos: BlockPos? = null

        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            val player = handler.player
            val level = player.serverLevel()

            val targetBiome = Biomes.LUSH_CAVES
            biome = targetBiome
            val biomePos = findBiome(level, player.blockPosition(), targetBiome)
            bPos = biomePos
            // Check if this is the player's first time
            if (!player.tags.contains("voyager.first_join")) {
                player.addTag("voyager.first_join")
                VoyagerFlavor.LOGGER.info("First join detected for ${player.name.string}")


                if (biomePos != null) {
                    val safePos = findSafeAirPocket(level, biomePos.x, biomePos.z)

                    if (safePos != null) {
                        // CRITICAL FIX: Call your custom helper function, not the vanilla one!
                        // This ensures the tag is saved.
                        teleportPlayer(player, safePos)

                        // CRITICAL FIX: Actually call the spawn function!
                        spawnProfessor(level, safePos)

                    } else {
                        VoyagerFlavor.LOGGER.warn("Found cave biome but no safe air pocket!")
                    }
                } else {
                    VoyagerFlavor.LOGGER.warn("Could not find a $targetBiome nearby!")
                }
            }
        }

        // ----------------------------------------------------------------
        // 2. TICK EVENT: Prevents escaping the cave (Gatekeeper)
        // ----------------------------------------------------------------
        ServerTickEvents.START_SERVER_TICK.register { server ->
            if (server.tickCount % 20 == 0) { // Check every second
                for (player in server.playerList.players) {

                    if (player.tickCount < 220) continue

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

                    try {
                        VoyagerFlavor.LOGGER.info("Trying to Spawn Low level wilds")
                        if (player.tickCount > 301) {
                            val safePos = findSafeAirPocket(player.serverLevel(), bPos!!.x, bPos!!.z)

                            spawnLowLevelWilds(
                                player,
                                safePos!!
                            )
                        }
                    } catch (e: Exception) {
                        VoyagerFlavor.LOGGER.error("Error trying to spawn low level wilds: ${e.message}")
                    }

                }
            }
        }
    }

    // ----------------------------------------------------------------
    // HELPER FUNCTIONS
    // ----------------------------------------------------------------

    private fun spawnProfessor(level: ServerLevel, pos: BlockPos ) {
        val server = level.server

        // We define the JSON on a single line to avoid NBT parsing errors.
        // careful with quotes: We use single quotes ' inside the string where possible for cleanliness,
        // or escaped double quotes \" for strict JSON compliance.


        // The Command: summon cobblemon:npc <x> <y> <z> <nbt>
        val command = "spawnnpcat ${pos.x} ${pos.y} ${pos.z} redwood"

        try {
            server.commands.performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                command
            )
            VoyagerFlavor.LOGGER.info("Professor Redwood summoned via Hardcoded NBT.")
        } catch (e: Exception) {
            VoyagerFlavor.LOGGER.error("Failed to summon professor: ${e.message}")
        }
    }

    private fun spawnLowLevelWilds(player: ServerPlayer, pos: BlockPos) {
        val server = player.serverLevel().server

        val SPAWN_ZUBAT_5 = "pokespawn cobblemon:zubat 5"
        val SPAWN_GEODUDE_3 = "pokespawn cobblemon:geodude 3"
        val SPAWN_MACHOK_3 = "pokespawn cobblemon:machop 3"
        val cmdList = arrayOf(SPAWN_GEODUDE_3, SPAWN_MACHOK_3, SPAWN_ZUBAT_5)

        for (cmd in cmdList) {
            try {
                server.commands.performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                    cmd
                )
                VoyagerFlavor.LOGGER.info("Wild Pokemons Spawned")
            } catch (e: Exception) {
                VoyagerFlavor.LOGGER.error("Failed to run $cmd: ${e.message}")
            }
        }

        player.sendSystemMessage(
            Component.literal("!!WILD POKEMON BLOCKING YOUR WAY HAVE APPEARED!!")
                .withStyle(ChatFormatting.RED)
        )
    }

    private fun teleportPlayer(player: ServerPlayer, pos: BlockPos) {
        // 1. Teleport
        player.teleportTo(
            player.serverLevel(),
            pos.x.toDouble() + 0.5,
            pos.y.toDouble(),
            pos.z.toDouble() + 0.5,
            player.yRot,
            player.xRot
        )

        // 2. Clean old tags & Save new spawn location
        player.tags.removeIf { it.startsWith("voyager_spawn:") }
        val coordTag = "voyager_spawn:${pos.x},${pos.y},${pos.z}"
        player.addTag(coordTag)

        // 3. NEW: Apply Night Vision for 15 Minutes
        // 15 mins * 60 sec * 20 ticks = 18000 ticks
        // Amplifier 0 (Level 1), showParticles = false (cleaner view)
        player.addEffect(
            MobEffectInstance(
                MobEffects.NIGHT_VISION,
                18000,
                0,
                false,
                false
            )
        )

        VoyagerFlavor.LOGGER.info("Teleported player and applied Night Vision.")
    }

    private fun findBiome(level: ServerLevel, center: BlockPos, biomeKey: ResourceKey<Biome>): BlockPos? {
        val pair = level.findClosestBiome3d(
            { holder: Holder<Biome> -> holder.`is`(biomeKey) },
            center,
            6400,
            32,
            64
        )
        if (pair != null) {
            VoyagerFlavor.LOGGER.info("Found cave biome $biomeKey")
        } else {
            VoyagerFlavor.LOGGER.warn("Could not find cave biome!")
        }
        return pair?.first
    }

    private fun findSafeAirPocket(level: ServerLevel, centerX: Int, centerZ: Int): BlockPos? {
        val radius = 128
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                val currentX = centerX + x
                val currentZ = centerZ + z
                for (y in 60 downTo -50) {
                    val pos = BlockPos(currentX, y, currentZ)
                    val floor = pos.below()
                    val ceiling = pos.above()

                    if (level.getBlockState(floor).isSolidRender(level, floor) &&
                        !level.getBlockState(pos).isSolidRender(level, pos) &&
                        !level.getBlockState(ceiling).isSolidRender(level, ceiling)
                    ) {
                        if (level.getFluidState(pos).isEmpty && level.getFluidState(ceiling).isEmpty) {
                            return pos
                        }
                    }
                }
            }
        }
        return null
    }
}