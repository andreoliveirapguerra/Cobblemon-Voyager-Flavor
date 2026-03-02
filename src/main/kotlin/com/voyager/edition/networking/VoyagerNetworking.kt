package com.voyager.edition.networking

import com.cobblemon.mod.common.Cobblemon
import com.voyager.edition.VoyagerFlavor
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object VoyagerNetworking {

    fun register() {
        // Register the payload type on both sides (required before any connection)
        PayloadTypeRegistry.playC2S().register(DynamaxPacket.TYPE, DynamaxPacket.STREAM_CODEC)

        // Handle the packet on the server main thread
        ServerPlayNetworking.registerGlobalReceiver(DynamaxPacket.TYPE) { payload, context ->
            context.server().execute {
                val player = context.player()
                val party = Cobblemon.storage.getParty(player)

                // Find the Pokemon — try by Pokemon UUID first, then by entity UUID
                val pokemon = (0 until 6)
                    .mapNotNull { party.get(it) }
                    .firstOrNull { it.uuid == payload.pokemonId }
                    ?: (0 until 6)
                        .mapNotNull { party.get(it) }
                        .firstOrNull { it.entity?.uuid == payload.pokemonId }

                if (pokemon == null) {
                    VoyagerFlavor.LOGGER.warn("[Voyager] Dynamax: Pokemon not found for UUID ${payload.pokemonId}")
                    return@execute
                }

                val isDynamaxed = pokemon.persistentData.getBoolean("voyager_is_dynamax")

                if (isDynamaxed) {
                    pokemon.persistentData.remove("voyager_is_dynamax")
                    pokemon.aspects = pokemon.aspects - "dynamax"
                    VoyagerFlavor.LOGGER.info("[Voyager] ${pokemon.species.name} reverted from Dynamax for ${player.name.string}")
                } else {
                    pokemon.persistentData.putBoolean("voyager_is_dynamax", true)
                    pokemon.aspects = pokemon.aspects + "dynamax"
                    VoyagerFlavor.LOGGER.info("[Voyager] ${pokemon.species.name} Dynamaxed for ${player.name.string}")
                }
            }
        }
    }
}
