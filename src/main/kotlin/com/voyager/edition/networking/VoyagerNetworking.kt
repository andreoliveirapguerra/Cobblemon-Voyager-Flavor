package com.voyager.edition.networking

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.pokemon.properties.AspectPropertyType
import com.cobblemon.mod.common.pokemon.properties.UnaspectPropertyType
import com.cobblemon.mod.common.util.server
import com.github.yajatkaul.mega_showdown.codec.Effect
import com.voyager.edition.VoyagerFlavor
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import com.github.yajatkaul.mega_showdown.gimmick.MaxGimmick
import com.github.yajatkaul.mega_showdown.sound.MegaShowdownSounds
import com.voyager.edition.utils.VoyagerUtils.Companion.isPokemonAlpha
import net.minecraft.sounds.SoundSource

object VoyagerNetworking {

    fun register() {
        PayloadTypeRegistry.playC2S().register(DynamaxPacket.TYPE, DynamaxPacket.STREAM_CODEC)

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
                    // ------------------ UN-DYNAMAX ------------------
                    VoyagerFlavor.LOGGER.info("[Voyager] UnDynamax: ${pokemon.species.name} for ${player.name.string}")
                    pokemon.persistentData.remove("voyager_is_dynamax")

                    if (pokemon.gmaxFactor) {
                        UnaspectPropertyType.fromString("msd:dmax")?.apply(pokemon)
                        Effect.getEffect("mega_showdown:dynamax")?.revertEffects(
                            pokemon, listOf("dynamax_form=gmax"), null
                        )
                        Effect.getEffect("mega_showdown:dynamax")?.applyEffects(
                            pokemon, listOf("dynamax_form=eternamax"), null
                        )
                        VoyagerFlavor.LOGGER.info("[VOyager] UnDynamax -> Pokemon is Gmax capable!")
                    } else {
                        UnaspectPropertyType.fromString("msd:dmax")?.apply(pokemon)
                        Effect.getEffect("mega_showdown:dynamax")?.revertEffects(
                            pokemon, listOf(), null
                        )
                    }



//                    MaxGimmick.startGradualScalingDown(pokemon)

//                    if (isPokemonAlpha(pokemon)) {
//                        MaxGimmick.startGradualScaling(pokemon, 2f)
//                    } else {
//                        MaxGimmick.startGradualScaling(pokemon, 1f)
//                    }


                    VoyagerFlavor.LOGGER.info("[Voyager] ${pokemon.species.name} UnDynamaxed for ${player.name.string}")

                } else {
                    // ------------------ DYNAMAX / G-MAX ------------------
                    VoyagerFlavor.LOGGER.info("[Voyager] Dynamax: ${pokemon.species.name} for ${player.name.string}")

                    if (isPokemonAlpha(pokemon)) {
                        pokemon.persistentData.putInt("orignal_size", 2)
                    }

                    pokemon.persistentData.putBoolean("voyager_is_dynamax", true)

                    if (pokemon.gmaxFactor) {
                        MaxGimmick.startGradualScaling(pokemon, 2f)
                        Effect.getEffect("mega_showdown:dynamax")?.applyEffects(
                            pokemon, listOf("dynamax_form=gmax"), null
                        )
                    } else {
                        MaxGimmick.startGradualScaling(pokemon, 2f)
                        AspectPropertyType.fromString("msd:dmax")?.apply(pokemon)
                        Effect.getEffect("mega_showdown:dynamax")?.applyEffects(
                            pokemon, listOf(), null
                        )
                    }

                    pokemon.entity?.let { entity ->
                        player.serverLevel().playSound(
                            null, entity.x, entity.y, entity.z,
                            MegaShowdownSounds.DYNAMAX.get(),
                            SoundSource.PLAYERS, 0.2f, 0.8f
                        )
                    }

                    VoyagerFlavor.LOGGER.info("[Voyager] ${pokemon.species.name} Dynamaxed for ${player.name.string}")
                }

                MaxGimmick.updateScalingAnimations(server())
            }
        }
    }

}
