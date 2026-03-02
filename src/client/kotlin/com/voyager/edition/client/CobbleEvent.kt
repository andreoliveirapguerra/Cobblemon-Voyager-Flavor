package com.voyager.edition.client

import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.gui.interact.wheel.InteractWheelOption
import com.voyager.edition.VoyagerFlavor
import com.voyager.edition.networking.DynamaxPacket
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import org.joml.Vector3f
import java.util.Objects

@Environment(EnvType.CLIENT)
object CobbleEvent {

    fun register() {
        CobblemonEvents.POKEMON_INTERACTION_GUI_CREATION.subscribe(Priority.NORMAL) { event ->
            // Find the Pokemon whose world entity UUID matches the interaction event
            val pokemon = CobblemonClient.storage.party.slots
                .filter { Objects.nonNull(it) }
                .firstOrNull { slot -> slot.entity != null && slot.entity!!.uuid == event.pokemonID }
                ?: return@subscribe

            val isDynamaxed = pokemon.persistentData.getBoolean("voyager_is_dynamax")
            val label = if (isDynamaxed) "voyager.ui.undynamax" else "voyager.ui.dynamax"

            val wheelOption = InteractWheelOption(
                ResourceLocation.fromNamespaceAndPath(VoyagerFlavor.MOD_ID, "textures/gui/interact/dynamax_wheel.png"),
                null,
                true,
                label,
                { Vector3f(0.8f, 0.2f, 1.0f) },  // purple tint for Dynamax
                {
                    ClientPlayNetworking.send(DynamaxPacket(event.pokemonID))
                    Minecraft.getInstance().setScreen(null)
                }
            )

            event.addFillingOption(wheelOption)
        }
    }
}
