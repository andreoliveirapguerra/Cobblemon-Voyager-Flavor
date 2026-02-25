package com.voyager.edition.event

import com.cobblemon.mod.common.CobblemonEntities
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.voyager.edition.VoyagerFlavor
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.storage.party.PartyStore
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.party
import com.sun.jna.platform.unix.Resource
import com.voyager.edition.registry.VoyagerTrainerData
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
        ServerTickEvents.START_SERVER_TICK.register { server ->
            thisServer = server
            checkDaxSpawnConditions(server)
        }

        CobblemonEvents.BATTLE_VICTORY.subscribe { handler ->
            val winner = handler.winners.first().getName()
            val loser = handler.losers.first().getName()

            if (loser.toString().lowercase().contains("dax")) {
                if (thisServer != null) {
                    val command = "give ${winner.string} mega_showdown:omni_ring"
                    runCommand(thisServer!!, command)
                }
            }
        }

        ServerEntityEvents.ENTITY_LOAD.register { entity, level ->
            // 1. Verifica se é um NPC do Cobblemon e se estamos no servidor
            if (entity is NPCEntity && !level.isClientSide) {

                // 2. Verifica se o nome dele é o que você acabou de spawnar
                // (O comando /spawnpc dax geralmente nomeia o NPC com o nome do preset)
                if (entity.name.string.equals("Dax", ignoreCase = true) ||
                    entity.customName?.string?.equals("Dax", ignoreCase = true) == true) {

                    VoyagerFlavor.LOGGER.info("Dax spawning!")
                    // === AQUI VOCÊ TEM A REFERÊNCIA DO NPC! ===
//                    editarNpcDax(entity)
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

    fun createCustomPokemon(species: String, level: Int, shiny: Boolean): Pokemon? {
        // 1. Buscamos a espécie no registro (ex: "charcadet")
        if (species == "" ) return null
        VoyagerFlavor.LOGGER.info("Creating custom pokemon from species $species")
        var species = PokemonSpecies.getByPokedexNumber(species.toInt())
        PokemonSpecies.species.find { it.name == species?.name }?.let { speciesPokemon ->
            species = speciesPokemon
        }

        var pokemon: Pokemon? = null

        if (species != null) {
            // 2. Criamos a instância do Pokémon em um nível específico
            val pokemon = species.create()
            pokemon.level = level // Definindo o nível

            // 3. Customizando atributos (usando as propriedades do seu stub)
            pokemon.shiny = shiny // Por que não um shiny para o teste?
        } else {
            VoyagerFlavor.LOGGER.error("Species not found! ")
        }



        // Você também pode definir IVs e EVs se quiser um Pokémon competitivo
        // pokemon.setIV(Stats.ATTACK, 31)

        return pokemon
    }

     fun triggerRivalDax(player: ServerPlayer, playerPartyStore: PlayerPartyStore, level: Int) {

        // 1. Determina o contra-ataque (Type Advantage)
        val counterPary: MutableList<String> = MutableList(7) { "" }
        for (playerPokemon in playerPartyStore) {
            val counterSpecies = when {
                playerPokemon.primaryType.name.contains("grass") -> "charizard"
                playerPokemon.primaryType.name.contains("fire") -> "blastoise"
                playerPokemon.primaryType.name.contains("water") -> "raichu"
                playerPokemon.primaryType.name.contains("ghost") -> "sableye"
                playerPokemon.primaryType.name.contains("dragon") -> "sylveon"
                playerPokemon.primaryType.name.contains("insect") -> "staraptor"
                playerPokemon.primaryType.name.contains("fairy") -> "melmetal"
                else -> "eevee"
            }
            counterPary.add(counterSpecies)
        }


        // 2. Criação do NPC Dax
        // Aqui assumimos que você está instanciando a classe de NPC do Cobblemon
        val dax = NPCEntity(player.serverLevel())


        dax.apply {
            customName = Component.literal("Dax").withStyle(ChatFormatting.RED)
            // Define o preset que criamos no JSON anteriormente

            // remove the PLACEHOLDER VAL USED TO INITIATE
            counterPary.removeFirst()
//            name = Component.literal("dax")
            // 3. Customize the Pokémon
            for (counter in counterPary) {
                val levelAdvantage = Math.clamp(Random(level).nextLong(), 0, level)
                val humiliation = Random(1000).nextBoolean()
                val pkmn = createCustomPokemon(counter, levelAdvantage, humiliation)
                if (pkmn != null) this.party?.add(pkmn)

            }
            val spawnPos = BlockPos((player.x + 1).toInt(), player.y.toInt(), player.z.toInt())
            // Posiciona o Dax perto do jogador
            VoyagerFlavor.LOGGER.info("[Voyager] Spawning Rival Dax at for ${player.name.string}")
            setPos(spawnPos.center)
            VoyagerFlavor.LOGGER.info("[Voyager] Dax at $spawnPos for ${player.name.string}")

        }


//        spawnDax(player.serverLevel(), player.blockPosition())
         player.level().addFreshEntity(dax)

        // 3. Feedback para o jogador
        player.sendSystemMessage(
            Component.literal("§c[!] Assinatura térmica detectada: Dax está se aproximando!")
                .withStyle(ChatFormatting.RED)
        )
    }

}