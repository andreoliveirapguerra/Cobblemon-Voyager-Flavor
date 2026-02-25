package com.voyager.edition.command

import com.cobblemon.mod.common.util.party
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.voyager.edition.VoyagerFlavor
import com.voyager.edition.event.VoyagerRivalEvents.getLevelDifferenceFromSnapshot
import com.voyager.edition.utils.VoyagerUtils
import com.voyager.edition.utils.VoyagerUtils.Companion.findSafeSurfaceSpace
import com.voyager.edition.utils.VoyagerUtils.Companion.findSafeSurfaceSpot
import com.voyager.edition.utils.VoyagerUtils.Companion.spawnDaxPresetAndEdit
import com.voyager.edition.utils.VoyagerUtils.Companion.spawnVanceAt
import com.voyager.edition.utils.VoyagerUtils.Companion.startAdventureEvent
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import kotlin.text.startsWith

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
                                    rescuePlayerAlt(target)
                                    1
                                }
                        )
                )
        )
        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("pokecentertp")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    rescuePlayerAlt(target)
                                    spawnNurseJoy(target.serverLevel(), target.blockPosition())
                                    1
                                }
                        )
                )
        )

        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("tpToForest")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    tpPlayerToForest(target)
                                    1
                                }
                        )
                )
        )

        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("spawnRival")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    val targetSnapshot = VoyagerFlavor.VoyagerTrainersDatabank!!.trainersList.find { it.name == target.name.string }
                                    val levelMean = getLevelDifferenceFromSnapshot(target.party(), targetSnapshot!!.partySnapshot!!)
                                    spawnDaxPresetAndEdit(
                                        target,
                                        target.serverLevel(),
                                        target.blockPosition(),
                                        levelMean)
                                    1
                                }
                        )
                )
        )

        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("spawnVance")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    spawnVanceAt(target.serverLevel(), target.blockPosition())
                                    1
                                }
                        )
                )
        )

        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("startGame")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    startAdventureEvent(target)
                                    1
                                }
                        )
                )
        )

        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("startcavechallenge")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    spawnLowLevelWilds(target)
                                    1
                                }
                        )
                )
        )

        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("spawnRedwood")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    spawnProfessor(target.serverLevel(), target.blockPosition())
                                    1
                                }
                        )
                )
        )

        dispatcher.register(
            Commands.literal("voyager")
                .requires { it.hasPermission(2) } // Only server/operators can run this directly
                .then(
                    Commands.literal("spawnLyra")
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes { context ->
                                    val target = EntityArgument.getPlayer(context, "target")
                                    val targetSnapshot = VoyagerFlavor.VoyagerTrainersDatabank!!.trainersList.find { it.name == target.name.string }
                                    val levelMean = getLevelDifferenceFromSnapshot(target.party(), targetSnapshot!!.partySnapshot!!)

                                    1
                                }
                        )
                )
        )

        dispatcher.register(
                Commands.literal("voyager")
                    // ... seus outros subcomandos (ex: spawnboss, heal) ...

                    // Novo comando: /voyager gen_structure <namespace:nome>
                    .then(Commands.literal("gen_structure")
                        .requires { it.hasPermission(2) } // Requer OP
                        .then(Commands.argument("structure_id", StringArgumentType.string())
                            .executes { context -> generateStructureCommand(context) }
                        )
                    )
            )

        dispatcher.register(
                Commands.literal("voyager")
                    .then(Commands.literal("gen_template") // Mudei o nome para ser mais técnico
                        .requires { it.hasPermission(2) }
                        .then(Commands.argument("template_id", StringArgumentType.greedyString()) // greedyString permite espaços se houver, mas IDs não costumam ter
                            .executes { context -> generateTemplateCommand(context) }
                        )
                    )
            )

        }


    fun generateStructureCommand(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val player = source.playerOrException // Garante que foi um player que digitou
        val level = source.level

        // Pega o argumento digitado
        val structureId = "bca:default/one_off/pokecenter"

        source.sendSystemMessage(Component.literal("§eTentando gerar estrutura: $structureId..."))

        // Chama a função da Utils
        VoyagerUtils.placeStructure(player.server, player.blockPosition(), structureId)

//        if (success) {
        source.sendSystemMessage(Component.literal("§aSucesso! Estrutura gerada."))
//        } else {
//            source.sendSystemMessage(Component.literal("§cFalha: Estrutura não encontrada ou ID inválido."))
//        }

        return 1
    }


    private fun spawnLowLevelWilds(player: ServerPlayer) {
        val server = player.serverLevel().server

        // Usamos o comando "pokespawn" sem coordenadas.
        // Como a fonte do comando é o jogador, o Cobblemon vai espalhá-los de forma segura à sua volta!
        val SPAWN_ZUBAT = "pokespawn zubat lvl=3"
        val SPAWN_WOOBAT = "pokespawn woobat lvl=4"

        player.tags.add("voyager_cave_cleared")

        val cmdList = arrayOf(
            SPAWN_WOOBAT, SPAWN_WOOBAT, SPAWN_WOOBAT,
            SPAWN_ZUBAT, SPAWN_ZUBAT, SPAWN_ZUBAT,
        )

        for (cmd in cmdList) {
            try {
                server.commands.performPrefixedCommand(
                    player.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                    cmd
                )
            } catch (e: Exception) {
                VoyagerFlavor.LOGGER.error("Falha ao rodar $cmd: ${e.message}")
            }
        }

        player.sendSystemMessage(
            Component.literal("!! POKÉMON SELVAGENS BLOQUEIAM O CAMINHO !!")
                .withStyle(ChatFormatting.RED)
        )
    }

    private fun spawnNurseJoy(level: ServerLevel, pos: BlockPos) {
        val server = level.server

        val command = "spawnnpcat ${pos.x} ${pos.y} ${pos.z} joy"

        try {
            server.commands.performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                command
            )
            VoyagerFlavor.LOGGER.info("Nurse Joy summoned ")
        } catch (e: Exception) {
            VoyagerFlavor.LOGGER.error("Failed to spawn nurse Joy: ${e.message}")
        }
    }

    private fun spawnLyraAt(level: ServerLevel, pos: BlockPos) {
        val server = level.server

        var command = "spawnnpcat ${pos.x} ${pos.y} ${pos.z} lyra"

        val safePos: BlockPos? = findSafeSurfaceSpot(level, pos.x, pos.z)

        if (safePos != null) {
            command = "spawnnpcat ${safePos.x} ${safePos.y} ${safePos.z} lyra"
        } else {
            VoyagerFlavor.LOGGER.warn("Could not find surface spot")
        }

        try {
            server.commands.performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                command
            )
            VoyagerFlavor.LOGGER.info("Lyra summoned at ${pos.x} ${pos.y} ${pos.z}")
        } catch (e: Exception) {
            VoyagerFlavor.LOGGER.error("Failed to spawn Lyra: ${e.message}")
        }
    }

    private fun findSpecificBiome(level: ServerLevel, center: BlockPos, biomeId: String, radius: Int = 6400): BlockPos? {
        // 1. Transforma a String (ex: "minecraft:cherry_grove") em uma chave de registro do jogo
        val biomeResourceLocation = ResourceLocation.tryParse(biomeId) ?: return null
        val biomeKey = ResourceKey.create(Registries.BIOME, biomeResourceLocation)

        // 2. Manda o servidor buscar o bioma mais próximo
        val resultPair = level.findClosestBiome3d(
            { holder -> holder.`is`(biomeKey) }, // Filtro: Estamos procurando este bioma específico
            center,                              // Posição inicial (onde o jogador/NPC está)
            radius,                              // Raio máximo de busca (6400 é o padrão do comando /locate do Vanilla)
            32,                                  // Pulo Horizontal: Checa a cada 32 blocos (deixa a busca hiper rápida)
            64                                   // Pulo Vertical: Checa a cada 64 blocos de altura
        )

        // O resultPair retorna um Pair<BlockPos, Holder<Biome>>. Nós só queremos a coordenada (o primeiro item)
        return resultPair?.first
    }

    fun findSafeAirPocket(level: ServerLevel, centerX: Int, centerZ: Int): BlockPos? {
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


    private fun generateTemplateCommand(context: CommandContext<CommandSourceStack>): Int {
        val source = context.source
        val player = source.playerOrException
        val level = source.level

        // ID ex: "bca:default/one_off/pokecenter"
        val templateId = StringArgumentType.getString(context, "template_id")

        source.sendSystemMessage(Component.literal("§eBuscando superfície para template: $templateId..."))

        val success = VoyagerUtils.placeTemplateOnSurface(
            level,
            player.blockPosition(),
            templateId
        )

        if (success) {
            source.sendSystemMessage(Component.literal("§aSucesso! Estrutura gerada logo acima/abaixo de você."))
        } else {
            source.sendSystemMessage(Component.literal("§cFalha: Template não encontrado. Verifique se o ID começa com 'bca:'"))
        }

        return 1
    }


    private fun tpPlayerToForest(player: ServerPlayer) {
        val coords: BlockPos? = findSpecificBiome(player.serverLevel(), player.blockPosition(), "minecraft:forest", 30000)
        val server = player.server
        val safeCoord = findSafeAirPocket(player.serverLevel(), coords!!.x, coords.z)
        if (safeCoord != null) {
            val command = "tp ${player.name.string} ${safeCoord.x} ${safeCoord.y} ${safeCoord.z}"

            server.commands.performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                command
            )
        }
    }

    private fun rescuePlayerAlt(player: ServerPlayer) {
        // First send player to forest

        val level = player.serverLevel()
        val server = level.server
        val pos = player.blockPosition()

        player.sendSystemMessage(Component.literal("§eProf. Redwood: Escaneando frequências de máquinas de cura... (Isso pode levar um segundo)"))

        player.tags.add("voyager_at_center")
        // Busca pela máquina de cura num raio de 600 blocos
        val healingMachinePos = findHealingMachine(level, pos, 30000)


        if (healingMachinePos != null) {

            player.teleportTo(healingMachinePos.x.toDouble() , healingMachinePos.y.toDouble(), healingMachinePos.y.toDouble())
            val safePos = findSafeSurfaceSpace(player, 2)
            player.teleportTo(safePos!!.x.toDouble(), player.y, player.z)

            // Encontrou! Vamos jogar o player bem na frente da máquina
            val targetX = healingMachinePos.x.toDouble() + 0.5
            val targetY = healingMachinePos.y.toDouble()
            val targetZ = healingMachinePos.z.toDouble() + 1.5 // +1.5 Z para não nascer DENTRO da máquina

            // 1. TELEPORTAR O PROFESSOR PRIMEIRO
            val npcTpCommand = "execute as ${player.scoreboardName} at @s run tp @e[type=cobblemon:npc,distance=..10,limit=1,sort=nearest] ${targetX + 1.5} $targetY $targetZ"
            server.commands.performPrefixedCommand(
                server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                npcTpCommand
            )

            // 2. LIMPAR AS TAGS E EFEITOS DO JOGADOR
            player.tags.removeIf { it.startsWith("voyager_spawn:") }
            player.tags.remove("voyager_cave_cleared")
            player.removeEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION)

            // 3. TELEPORTAR O JOGADOR
//            player.teleportTo(
//                level,
//                targetX,
//                targetY,
//                targetZ,
//                player.yRot,
//                player.xRot
//            )

            spawnProfessor(level, healingMachinePos)

            player.sendSystemMessage(Component.literal("§aProf. Redwood: Chegamos! A Estação de Cura está logo aqui."))
        } else {
            // Caso falhe
            player.sendSystemMessage(Component.literal("§cRedwood: O scanner falhou! Nenhuma Estação de Cura encontrada num raio de 600 blocos."))
        }
    }

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


    private fun findHealingMachine(level: ServerLevel, center: BlockPos, radius: Int): BlockPos? {
        val targetId = ResourceLocation.parse("cobblemon:healing_machine")
        val targetBlock = BuiltInRegistries.BLOCK.get(targetId)

        val centerChunkX = center.x shr 4
        val centerChunkZ = center.z shr 4
        val chunkRadius = radius / 16

        // Busca em espiral (começa da chunk do jogador e vai se afastando)
        for (r in 0..chunkRadius) {
            for (x in -r..r) {
                for (z in -r..r) {
                    // Ignora o miolo do quadrado, processando apenas a borda do raio atual
                    if (Math.abs(x) != r && Math.abs(z) != r) continue

                    val chunkX = centerChunkX + x
                    val chunkZ = centerChunkZ + z

                    // Segurança: Só vasculha se a chunk já existir no mapa para não crashar o servidor gerando mundo
                    if (!level.hasChunk(chunkX, chunkZ)) continue
                    val chunk = level.getChunk(chunkX, chunkZ)

                    // Limita a busca vertical: Centros Pokémon normalmente spawnam na superfície (Y 60 a 100)
                    for (by in -60..300) {
                        for (bx in 0..15) {
                            for (bz in 0..15) {
                                val pos = BlockPos(chunkX * 16 + bx, by, chunkZ * 16 + bz)
                                val state = chunk.getBlockState(pos)

                                if (state.`is`(targetBlock)) {
                                    return pos // Retorna a coordenada exata da máquina
                                }
                            }
                        }
                    }
                }
            }
        }
        return null // Não encontrou nada no raio
    }
}