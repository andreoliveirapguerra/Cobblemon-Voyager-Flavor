package com.voyager.edition.networking

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.properties.AspectPropertyType
import com.cobblemon.mod.common.pokemon.properties.UnaspectPropertyType
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.server
import com.github.yajatkaul.mega_showdown.codec.Effect
import com.voyager.edition.VoyagerFlavor
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import com.github.yajatkaul.mega_showdown.gimmick.MaxGimmick
import com.github.yajatkaul.mega_showdown.sound.MegaShowdownSounds
import net.minecraft.sounds.SoundSource

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
                    VoyagerFlavor.LOGGER.info("[Voyager] Dynamax: Pokemon ${pokemon.species.name}is Dynamaxing")
                    pokemon.persistentData.remove("voyager_is_dynamax")
                    if (pokemon.gmaxFactor) {
                        MaxGimmick.startGradualScaling(pokemon, 2f)
                        Effect.getEffect(
                            "mega_showdown:dynamax").applyEffects(
                            pokemon,
                            listOf("dynamax_form=gmax"),
                            null
                        )

                    } else {
                        MaxGimmick.startGradualScaling(pokemon, 2f)
                        AspectPropertyType.fromString("msd:dmax")!!.apply(pokemon)
                        Effect.getEffect("mega_showdown:dynamax").applyEffects(
                            pokemon,
                            listOf(),
                            null
                        )

                    }
                    pokemon.persistentData.putBoolean("is_max", true);
                    player.serverLevel().playSound(
                        null, pokemon.entity!!.getX(), pokemon.entity!!.getY(), pokemon.entity!!.getZ(),
                        MegaShowdownSounds.DYNAMAX.get(),
                        SoundSource.PLAYERS, 0.2f, 0.8f
                    )
                    VoyagerFlavor.LOGGER.info("[Voyager] ${pokemon.species.name} Dynamax for ${player.name.string}")

                }
                else {
                    VoyagerFlavor.LOGGER.info("[Voyager] Dynamax: Pokemon ${pokemon.species.name}is UnDynamaxing")

                    pokemon.persistentData.putBoolean("voyager_is_dynamax", true)

                    if (pokemon.gmaxFactor) {
                        // TODO: ??
                    } else {
                        UnaspectPropertyType.fromString("msd:dmax")!!.apply(pokemon);
                    }
                    Effect.getEffect(
                        "mega_showdown:dynamax").revertEffects(
                        pokemon,
                        listOf("dynamax_form=eternamax"),
                            null
                        )

                    MaxGimmick.startGradualScalingDown(pokemon)
                    MaxGimmick.updateScalingAnimations(server())
                    VoyagerFlavor.LOGGER.info("[Voyager] ${pokemon.species.name} UnDynamaxed for ${player.name.string}")
                }
            }
        }
    }
}
