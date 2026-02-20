package com.voyager.edition.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.TagKey
import net.minecraft.world.level.levelgen.Heightmap

object VoyagerCommands {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("rescue")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    rescuePlayer(target)
                                    1
                                }
                        )
                )
        )
    }

    private fun rescuePlayer(player: ServerPlayer) {
        val level = player.serverLevel()
        val server = level.server
        val pos = player.blockPosition()

        // We search for the nearest Village (RGS and ExtraStructures hook into this tag)
        val structureTag = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse("minecraft:village"))

        // Search in a 100-chunk radius (~1600 blocks)
        val nearestStructurePos = level.findNearestMapStructure(structureTag, pos, 100, false)

        if (nearestStructurePos != null) {
            // Calculate precise target coordinates
            val targetX = nearestStructurePos.x.toDouble() + 0.5
            val targetZ = nearestStructurePos.z.toDouble() + 0.5
            val targetY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, nearestStructurePos.x, nearestStructurePos.z).toDouble() + 1.0

            // 1. TELEPORT THE PROFESSOR FIRST
            // We execute a command *as* the player to find the nearest NPC within 10 blocks and teleport it to the target.
            // We add +1.5 to the X coordinate so he stands next to you, not inside you!
            val npcTpCommand = "execute as ${player.scoreboardName} at @s run tp @e[type=cobblemon:npc,distance=..10,limit=1,sort=nearest] ${targetX + 1.5} $targetY $targetZ"
            server.commands.performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                npcTpCommand
            )

            // 2. CLEAN UP PLAYER TAGS
            player.tags.removeIf { it.startsWith("voyager_spawn:") }
            player.tags.remove("voyager_cave_cleared")
            player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)

            // 3. TELEPORT THE PLAYER
            player.teleportTo(
                level,
                targetX,
                targetY,
                targetZ,
                player.yRot,
                player.xRot
            )
            player.sendSystemMessage(Component.literal("§aProf. Redwood safely teleported you both to a settlement!"))
        } else {
            player.sendSystemMessage(Component.literal("§cRedwood's scanner failed! No settlements found nearby..."))
        }
    }
}