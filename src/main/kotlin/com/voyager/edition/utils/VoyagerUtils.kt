package com.voyager.edition.utils

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.npc.configuration.NPCBattleConfiguration
import com.cobblemon.mod.common.api.npc.partyproviders.SimplePartyProvider
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.party.NPCPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.voyager.edition.VoyagerFlavor
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.level.biome.Biome
import kotlin.text.startsWith
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class VoyagerUtils {
    companion object {
        var centerPosition: BlockPos? = null
        fun spawnProfessor(level: ServerLevel, pos: BlockPos ) {
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

        fun runCommand(server: MinecraftServer,command: String) {
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

        fun sendPlayerMessage(player: ServerPlayer, message: String, color: ChatFormatting) {
            player.sendSystemMessage(
                Component.literal(message)
                    .withStyle(color)
            )
        }

        fun spawnVanceAt(player: ServerPlayer, pos: BlockPos) {
            val level = player.serverLevel()
            val server = level.server

            val command = "spawnnpcat ${pos.x} ${pos.y} ${pos.z} vance"
            VoyagerFlavor.LOGGER.info("[Voyager] Spawning Vance at ${pos.x} ${pos.y} ${pos.z}")

            try {
                server.commands.performPrefixedCommand(
                    server.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                    command
                )
                VoyagerFlavor.LOGGER.info("Cpt. Vance summoned ")
                player.sendSystemMessage(
                    Component
                        .literal("§aVance: Ei Recruta! Estou em ${pos.x} ${pos.y} ${pos.z}.")
                        .withStyle(ChatFormatting.DARK_AQUA)
                )

            } catch (e: Exception) {
                VoyagerFlavor.LOGGER.error("Failed to spawn Cap. Vance: ${e.message}")
            }
        }



        fun teleportPlayer(player: ServerPlayer, pos: BlockPos) {
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
            VoyagerFlavor.LOGGER.info("Added Tag: $coordTag to player ${player.name.string}")

            // 3. NEW: Apply Night Vision for 15 Minutes
            // 15 mins * 60 sec * 20 ticks = 18000 ticks
            // Amplifier 0 (Level 1), showParticles = false (cleaner view)
            player.addEffect(
                MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    18000,
                    2,
                    true,
                    true
                )
            )

            VoyagerFlavor.LOGGER.info("Teleported player and applied Night Vision.")
        }

        fun findSafeSurfaceSpot(level: ServerLevel, centerX: Int, centerZ: Int): BlockPos? {
            val radius = 128
            for (x in -radius..radius) {
                for (z in -radius..radius) {
                    val currentX = centerX + x
                    val currentZ = centerZ + z
                    for (y in 120 downTo 20) { // ALtura
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

        fun findBiome(level: ServerLevel, center: BlockPos, biomeKey: ResourceKey<Biome>): BlockPos? {
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

        fun findSafeSurfaceSpace(player: ServerPlayer, blockOffset: Int): BlockPos? {
            val level = player.serverLevel()
            val center = player.blockPosition()

            // Procuramos em 8 direções ao redor do jogador (em formato de círculo)
            // Assim, se houver um penhasco na frente, ele tenta achar lugar do lado ou atrás.
            val angles = listOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)

            for (angle in angles) {
                // Calcula as coordenadas X e Z no raio especificado
                val rad = Math.toRadians(angle)
                val targetX = center.x + (blockOffset * cos(rad)).toInt()
                val targetZ = center.z + (blockOffset * sin(rad)).toInt()

                // O segredo: getHeightmapPos encontra o bloco mais alto com acesso livre ao céu.
                // MOTION_BLOCKING_NO_LEAVES garante que ele vai atravessar a copa das árvores
                // e pegar a terra firme lá embaixo, em vez de spawnar o NPC em cima de uma árvore.
                val surfacePos = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos(targetX, 0, targetZ)
                )

                // Pegamos os blocos de interesse para análise
                val floorBlock = level.getBlockState(surfacePos.below()) // Chão
                val footBlock = level.getBlockState(surfacePos)          // Corpo/Pernas
                val headBlock = level.getBlockState(surfacePos.above())  // Cabeça

                // CHECAGENS DE SEGURANÇA:
                // 1. O chão não pode ser ar.
                // 2. O chão não pode ser um líquido (Água ou Lava).
                // 3. Os blocos do corpo e cabeça não podem ter colisão física (precisam ser ar, grama alta, flores, etc).
                if (!floorBlock.isAir && floorBlock.fluidState.isEmpty) {
                    if (footBlock.getCollisionShape(level, surfacePos).isEmpty &&
                        headBlock.getCollisionShape(level, surfacePos.above()).isEmpty) {

                        return surfacePos // Encontramos o bloco perfeito!
                    }
                }
            }

            // Se as 8 direções forem inválidas (ex: o jogador está num barco no meio do oceano de lava)
            return null
        }

        fun startAdventureEvent(player: ServerPlayer) {
            val level = player.serverLevel()

            val targetBiome = Biomes.LUSH_CAVES
            val biomePos = findBiome(level, player.blockPosition(), targetBiome)
            // Check if this is the player's first time
            if (!player.tags.contains("voyager.first_join") && !player.tags.contains("voyager.ready")) {
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
                        player.addTag("voyager.ready")
                    } else {
                        VoyagerFlavor.LOGGER.warn("Found cave biome but no safe air pocket!")
                    }
                } else {
                    VoyagerFlavor.LOGGER.warn("Could not find a $targetBiome nearby!")
                }
            }
        }


        fun configurarNpcDax(npc: NPCEntity, player: ServerPlayer, level: Int) {
            // 1. Prepara o NBT para as configurações estruturais
            VoyagerFlavor.LOGGER.info("Editando Dax")
            val nbt = CompoundTag()
            npc.saveWithoutId(nbt) // Pega os dados atuais para não quebrar nada

            // --- A. Hitbox e Persistência ---
            nbt.putString("hitbox", "player")
            nbt.putBoolean("canDespawn", false)
            nbt.putBoolean("PersistenceRequired", true) // Garante que o Minecraft Vanilla também não despawne

            // --- B. Nomes (Lista) ---
            val namesList = ListTag()
            namesList.add(StringTag.valueOf("npc.dax.name"))
            nbt.put("names", namesList)

            // --- C. Interação (Diálogo) ---
            val interaction = CompoundTag()
            interaction.putString("type", "dialogue")
            interaction.putString("dialogue", "cobblemon:dax_initial_chat")
            nbt.put("interaction", interaction)

            // --- D. Configuração de Batalha ---
            val battleConfig = CompoundTag()
            battleConfig.putBoolean("canChallenge", true)
            nbt.put("battleConfiguration", battleConfig)

            // --- E. Habilidade (Skill) ---
            nbt.putInt("skill", 5)

            VoyagerFlavor.LOGGER.info("Loading dax NBT")

            // Aplica as configurações estruturais (exceto a party)
            npc.load(nbt)

            VoyagerFlavor.LOGGER.info("Loaded dax NBT")


            // 2. Configura a Party (Time Pokémon) usando a API do Cobblemon
            // Isso é necessário porque o NBT bruto espera objetos serializados,
            // mas você tem Strings de propriedades.

            val npcParty = npc.party // Pega o objeto da Party do NPC

            if (npcParty == null) {
                VoyagerFlavor.LOGGER.warn("NPC party is null!")
                npc.party = NPCPartyStore(npc)
            }
            if (npc.party == null) {
                VoyagerFlavor.LOGGER.error("Error criando NPC party")
            }

            npc.party!!.clearParty() // Limpa qualquer Pokémon anterior

//            val counterPary: MutableList<String> = MutableList(6) { "" }
//            counterPary.removeFirst()

//            VoyagerFlavor.LOGGER.info("Adding calculating counter")
//            for (playerPokemon in player.party()) {
//                val counterSpecies = when {
//                    playerPokemon.primaryType.name.contains("Grass") -> "0006" //charizard
//                    playerPokemon.primaryType.name.contains("Fire") -> "0484" // FODENDO PALKIA
//                    playerPokemon.primaryType.name.contains("Water") -> "0025" // raichu
//                    playerPokemon.primaryType.name.contains("Ghost") -> "0302" // sableye
//                    playerPokemon.primaryType.name.contains("Dragon") -> "0700" //sylveon
//                    playerPokemon.primaryType.name.contains("Steel") -> "0214" // Heracross
//                    playerPokemon.primaryType.name.contains("Bug") -> "0398" // staraptor
//                    playerPokemon.primaryType.name.contains("Fairy") -> "0809" //melmetal
//                    playerPokemon.primaryType.name.contains("Normal") -> "0214"
//                    else -> "0133" // Eevee
//                }
//                VoyagerFlavor.LOGGER.info("Adding counter= $counterSpecies for ${playerPokemon.primaryType.name}")
//                counterPary.add(counterSpecies)
//            }
//
//            VoyagerFlavor.LOGGER.info("Counter Party = $counterPary")
//
//            VoyagerFlavor.LOGGER.info("Adding Pokemons To party")
//            for (counter in counterPary) {
//                val levelAdvantage = Math.clamp(Random(level).nextLong(), 0, level)
//                val humiliation = Random(1000).nextBoolean()
//                val pkmn = createCustomPokemon(counter, levelAdvantage, humiliation) ?: continue
//                VoyagerFlavor.LOGGER.info("Created pokemon $pkmn")
////                if (pkmn.species) {
//                npc.party?.add(pkmn)
//                VoyagerFlavor.LOGGER.info("Added pokemon to party $pkmn")
//
////                }
////                else{
////                    VoyagerFlavor.LOGGER.error("Could not create pokemon to party $pkmn")
////                }
//            }
        }

        fun spawnDaxPresetAndEdit(player: ServerPlayer, level: ServerLevel, pos: BlockPos, lvl: Int) {
            // 1. Cria a entidade NPC crua
            val npcId = ResourceLocation.parse("cobblemon:dax")
            val npc = CobblemonEntities.NPC.create(level)
            npc?.npc?.id = npcId


            if (npc == null) {
                VoyagerFlavor.LOGGER.error("Could not create dax")
            } else {
                VoyagerFlavor.LOGGER.info("Created dax with id ${npc.name.string}")
            }

            // 2. Posiciona
            npc!!.setPos(pos.x.toDouble() + 0.5, pos.y.toDouble(), pos.z.toDouble() + 0.5)

            // 3. Carrega o Preset "dax" e Edita ao mesmo tempo
            configurarNpcDax(npc, player, lvl)
            var legendary = Random(1000).nextBoolean()
            var shiny = Random(3).nextBoolean()
            var playerPkmnLevel = 0
            if (player.name.string.lowercase().contains("pw") ||
                player.name.string.lowercase().contains("pedabliw") ||
                player.name.string.lowercase().contains("momoleda")) {
                VoyagerFlavor.LOGGER.warn("[Voyager] Special Name Detected, HARDCORE RIVAL ENABLED!!!")
                legendary = true
                shiny = true
                playerPkmnLevel = lvl*2
            } else {
                playerPkmnLevel = lvl
            }
            gerarTimeRivalSeguro(
                npc,
                playerPkmnLevel,
                player,
                legendary,
                shiny
            )
            npc.battle = NPCBattleConfiguration()
            npc.battle!!.canChallenge = true


            VoyagerFlavor.LOGGER.info("Created dax at ${npc.blockPosition()}")
            // 5. Só agora adiciona ao mundo. Ele já nasce modificado!
            VoyagerFlavor.LOGGER.info("Created dax with party ${npc.party?.toGappyList()}")
            level.addFreshEntity(npc)

        }

        fun getCounterType(type: String): String {
            return when (type.lowercase()) {
                "fire" -> "water"
                "water" -> "electric"
                "grass" -> "fire"
                "electric" -> "ground"
                "ground" -> "ice"
                "ice" -> "fighting"
                "fighting" -> "flying"
                "flying" -> "electric"
                "psychic" -> "ghost"
                "ghost" -> "dark"
                "dark" -> "fairy"
                "fairy" -> "steel"
                "dragon" -> "fairy"
                "steel" -> "fire"
                "rock" -> "fighting"
                "bug" -> "fire"
                "poison" -> "psychic"
                "normal" -> "fighting"
                else -> "normal"
            }
        }

        fun isLegendary(pokemonName: String): Boolean {
            // Normaliza o nome: tudo minúsculo e remove espaços, hifens e underlines
            val normalizedName = pokemonName.lowercase()
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "")

            return when (normalizedName) {
                // --- 1ª Geração (Kanto) ---
                "articuno", "zapdos", "moltres", "mewtwo", "mew" -> true

                // --- 2ª Geração (Johto) ---
                "raikou", "entei", "suicune", "lugia", "hooh", "celebi" -> true

                // --- 3ª Geração (Hoenn) ---
                "regirock", "regice", "registeel", "latias", "latios",
                "kyogre", "groudon", "rayquaza", "jirachi", "deoxys" -> true

                // --- 4ª Geração (Sinnoh) ---
                "uxie", "mesprit", "azelf", "dialga", "palkia",
                "heatran", "regigigas", "giratina", "cresselia",
                "phione", "manaphy", "darkrai", "shaymin", "arceus" -> true

                // --- 5ª Geração (Unova) ---
                "victini", "cobalion", "terrakion", "virizion",
                "tornadus", "thundurus", "reshiram", "zekrom",
                "landorus", "kyurem", "keldeo", "meloetta", "genesect" -> true

                // --- 6ª Geração (Kalos) ---
                "xerneas", "yveltal", "zygarde", "diancie", "hoopa", "volcanion" -> true

                // --- 7ª Geração (Alola / Ultra Beasts / Meltan) ---
                "typenull", "silvally", "tapukoko", "tapulele", "tapubulu", "tapufini",
                "cosmog", "cosmoem", "solgaleo", "lunala", "necrozma", "magearna",
                "marshadow", "zeraora", "meltan", "melmetal",
                    // Ultra Beasts
                "nihilego", "buzzwole", "pheromosa", "xurkitree", "celesteela",
                "kartana", "guzzlord", "poipole", "naganadel", "stakataka", "blacephalon" -> true

                // --- 8ª Geração (Galar / Hisui) ---
                "zacian", "zamazenta", "eternatus", "kubfu", "urshifu", "zarude",
                "regieleki", "regidrago", "glastrier", "spectrier", "calyrex", "enamorus" -> true

                // --- 9ª Geração (Paldea) ---
                // (Nota: Pokémon Paradoxo não são tecnicamente lendários pelas regras oficiais,
                // então listamos apenas as Ruínas, os da DLC e os mascotes)
                "tinglu", "chienpao", "wochien", "chiyu",
                "koraidon", "miraidon", "okidogi", "munkidori", "fezandipiti",
                "ogerpon", "terapagos", "pecharunt" -> true

                // Se não bateu com nenhum dos nomes acima, não é lendário
                else -> false
            }
        }

        fun gerarTimeRivalSeguro(rival: NPCEntity, lvl: Int, player: ServerPlayer, useLegendary: Boolean = true, shiny: Boolean) {
            VoyagerFlavor.LOGGER.info("[Voyager] Gerar time Rival seguro Lendarios=$useLegendary e Shiny=$shiny")
            var rivalParty = rival.party
            if (rivalParty == null) {
                rival.party = SimplePartyProvider().provide(rival, lvl)
                rivalParty = rival.party
            }
            rivalParty!!.clearParty()

            val playerParty = Cobblemon.storage.getParty(player)
            if (playerParty.isEmpty()) {
                return // Aborta se o jogador não tiver time
            }

            val usedSpecies = mutableSetOf<String>()

            for (playerMon in playerParty) {
                val playerType = playerMon.primaryType.name.lowercase()
                val counterTypeStr = getCounterType(playerType)

                // 1. Filtra espécies implementadas que tenham vantagem de tipo
                var candidates = PokemonSpecies.species.filter { species ->
                    val pType = species.primaryType.name.lowercase()
                    val sType = species.secondaryType?.name?.lowercase()

                    (pType == counterTypeStr || sType == counterTypeStr) &&
                            species.name !in usedSpecies
                }

                // 2. Lógica para Lendários (opcional)
                if (useLegendary) {
                    val legendaryCandidates = candidates.filter {
                        isLegendary(it.name)
                    }
                    VoyagerFlavor.LOGGER.info("[Voyager] Legendary candidates=$legendaryCandidates")

                    if (legendaryCandidates.isNotEmpty()) {
                        candidates = legendaryCandidates
                    }
                } else {
                    // Remove lendários se não for permitido
                    candidates = candidates.filter { !it.features.contains("legend") }
                }

                // 3. Seleção de Espécie (com fallback se a lista for vazia)
                val selectedSpecies = if (candidates.isNotEmpty()) {
                    candidates.random()
                } else {
                    // Fallback: pega qualquer pokémon implementado que não seja lendário (se proibido)
                    PokemonSpecies.implemented.filter {
                        it.name !in usedSpecies && (useLegendary || !it.features.contains("legend"))
                    }.random()
                }

                usedSpecies.add(selectedSpecies.name)

                // 4. Definição de Nível
                val targetLevel = (playerMon.level + if (useLegendary) 5 else 1).coerceIn(1, 100)

                // 5. Construção da String de Propriedades (Usando a sintaxe correta do arquivo .kt)
                // Nota: PokemonProperties.kt usa "${stat}_iv", então devemos usar nomes completos ou abreviações padrão do mod.
                // Geralmente: hp_iv, attack_iv, defence_iv, special_attack_iv, special_defence_iv, speed_iv
                var isShiny = if (shiny) "yes" else "no"
                val propertiesString = buildString {
                    append("species=${selectedSpecies.resourceIdentifier}") // Usa ID completo para evitar erro
                    append(" level=$targetLevel") // "level" é válido segundo o código fonte
                    append(" hp_iv=31 attack_iv=31 defence_iv=31 special_attack_iv=31 special_defence_iv=31 speed_iv=31")
                    append(" nature=random")
                    append(" shiny=$isShiny")

                    if (useLegendary && selectedSpecies.features.contains("legend")) {
                        append(" held_item=cobblemon:life_orb") // "held_item" é válido
                    }
                }

                try {
                    // Parse seguro usando o código nativo do mod
                    val props = PokemonProperties.parse(propertiesString)
                    val newPokemon = props.create()

                    VoyagerFlavor.LOGGER.info("Added ${newPokemon.species.name} to Dax's party")
                    rivalParty.add(newPokemon)

                    // Log de Debug (Opcional)
                    println("[Dax] Criado: ${newPokemon.species.name} (Lv.$targetLevel) contra ${playerMon.species.name}")

                } catch (e: Exception) {
                    println("[Dax] Erro crítico ao criar ${selectedSpecies.name}: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        fun completeQuestReward(server: MinecraftServer, playerName: String, questTitle: String) {
            val command = "ftbquests change_progress $playerName complete $questTitle"
            runCommand(server, command)
        }

        fun spawnCenterAt(server: MinecraftServer, player: ServerPlayer) {
            val pos = findSafeSurfaceSpot(server.getLevel(player.serverLevel().dimension())!!, player.x.toInt(), player.z.toInt())
            VoyagerFlavor.LOGGER.info("[Voyager] Spawning center at ${pos!!.center}")
            centerPosition = pos
            val command = "place template bca:default/one_off/pokecenter ${pos.x} ${pos.y} ${pos.z}"
            runCommand(server, command)
        }

        fun placeTemplateOnSurface(level: ServerLevel, originPos: BlockPos, templateId: String): Boolean {
            // 1. Parse do ID (ex: "bca:default/one_off/pokecenter")
            val resourceLocation = ResourceLocation.tryParse(templateId)
            if (resourceLocation == null) {
                println("VoyagerUtils: ID inválido: $templateId")
                return false
            }

//             2. Busca o Template no Manager (Isso lê pastas data/namespace/structures/...)
            val templateManager = level.structureManager
            val templateOptional = templateManager.get(resourceLocation)

            if (templateOptional.isEmpty) {
                println("VoyagerUtils: Template NBT não encontrado: $resourceLocation")
                return false
            }

            val template = templateOptional.get()
            val size = template.size // Tamanho da estrutura (X, Y, Z)

            // 3. Calcula a Superfície (Ponto mais alto sólido)
            // Usamos WORLD_SURFACE para pegar o topo da grama/areia.
            val surfaceY = level.getHeightmapPos(
                Heightmap.Types.WORLD_SURFACE,
                originPos
            ).y

            // 4. Centralização Matemática
            // Se a estrutura tem 20 blocos de largura, queremos que ela comece 10 blocos "para trás"
            // para que o jogador fique exatamente no meio dela.
            val centeredPos = BlockPos(
                originPos.x - (size.x / 2),
                surfaceY,
                originPos.z - (size.z / 2)
            )

            // 5. Configurações de Colocação
            val settings = StructurePlaceSettings()
                .setRotation(Rotation.NONE) // Você pode mudar para Rotation.RANDOM se quiser variedade
                .setMirror(Mirror.NONE)
                .setIgnoreEntities(false) // Carrega os NPCs/Spawners salvos no NBT


            VoyagerFlavor.LOGGER.info("Place template $templateId at $centeredPos")
            // 6. Executa a colocação
            val success = template.placeInWorld(
                level,
                centeredPos,
                centeredPos,
                settings,
                level.random,
                2 // Flag 2 = Atualiza os clientes (evita blocos fantasmas)
            )

            return success
        }
        /**
         * Tenta gerar uma estrutura NBT no mundo.
         * @param server O Minecraft Server.
         * @param pos A posição central/inicial onde a estrutura será colada.
         * @param structureIdString O ID completo da estrutura (ex: "cobblemon:small_center").
         * @return True se sucesso, False se não achou a estrutura.
         */
        fun placeStructure(server: MinecraftServer, pos: BlockPos, structureIdString: String) {
            // 1. Tenta converter a string em ResourceLocation (Namespace + Path)
//            val resourceLocation = ResourceLocation.tryParse(structureIdString)
//            if (resourceLocation == null) {
//                println("VoyagerUtils: ID de estrutura inválido: $structureIdString")
//                return false
//            }
//
//            // 2. Acessa o Gerenciador de Templates do Minecraft
//            val templateManager = level.structureManager
//            val templateOptional = templateManager.get(resourceLocation)
//
//            if (templateOptional.isEmpty) {
//                println("VoyagerUtils: Estrutura não encontrada nos arquivos: $resourceLocation")
//                return false
//            }
//
//            val template = templateOptional.get()
//
//            // 3. Configurações de Colocação (Padrão: Sem rotação, sem espelhamento)
//            val settings = StructurePlaceSettings()
//                .setRotation(Rotation.NONE)
//                .setMirror(Mirror.NONE)
//                .setIgnoreEntities(false) // Se true, não spawna os NPCs/Mobs salvos na estrutura
//
//            // 4. Centralizar (Opcional): Tenta ajustar para o bloco ser o centro da estrutura
//            // Se preferir que o spawn seja na "esquina" da estrutura, remova o cálculo de centerPos
//            val size = template.size
//            val centerPos = pos.offset(-size.x / 2, 0, -size.z / 2)
//
//            // 5. Coloca no mundo
//            val success = template.placeInWorld(
//                level,
//                centerPos, // Use 'pos' aqui se quiser que nasça na esquina exata do player
//                centerPos,
//                settings,
//                level.random,
//                2 // Flag 2 envia atualização para os clientes verem os blocos aparecendo
//            )
            val command = "place template bca:default/one_off/pokecenter ${pos.x} ${pos.y} ${pos.z}"
            runCommand(server, command)
        }
    }



}