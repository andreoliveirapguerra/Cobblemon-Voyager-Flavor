package com.voyager.edition.story

import com.voyager.edition.VoyagerFlavor
import com.voyager.edition.utils.VoyagerUtils.Companion.runCommand
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource

/**
 * Manages Ultra Beast wormhole events.
 *
 * Each event is identified by an integer ID (1, 2, 3, …) and tagged on the
 * player so it can never fire twice.  Call [triggerUltraEvent] from Kotlin
 * (e.g. in a battle-victory handler) or via the `/voyager triggerUltraEvent`
 * command from a dialogue script.
 */
object UltraEventManager {

    private const val WORMHOLE_COMMAND = "ultrabeasts wormhole spawn"

    /**
     * Fires Ultra event [eventId] for [player]:
     *  1. Guards against re-triggering via a player tag.
     *  2. Runs the wormhole spawn command silently.
     *  3. Shows the ULTRA-SCANNER alert in chat.
     *  4. Plays a dramatic ambient sound at the player's position.
     *  5. Marks the event as consumed with tag `voyager_ultra_event_<id>`.
     */
    fun triggerUltraEvent(player: ServerPlayer, eventId: Int) {
        val tag = "voyager_ultra_event_$eventId"

        if (player.tags.contains(tag)) {
            player.sendSystemMessage(
                Component.literal("§8[ULTRA-SCANNER] §7Evento $eventId já foi ativado anteriormente.")
            )
            VoyagerFlavor.LOGGER.info("[UltraEventManager] Event $eventId already consumed for ${player.name.string}")
            return
        }

        runCommand(player.server, WORMHOLE_COMMAND)

        player.sendSystemMessage(
            Component.literal("[ULTRA-SCANNER]: ALERTA ULTRA RADIAÇÃO INTENSA NA ÁREA DETECTADA")
        )

        player.serverLevel().playSound(
            null,
            player.blockPosition(),
            SoundEvents.END_PORTAL_SPAWN,
            SoundSource.AMBIENT,
            1.2f,
            0.7f
        )

        player.addTag(tag)
        VoyagerFlavor.LOGGER.info("[UltraEventManager] Event $eventId triggered for ${player.name.string}")
    }
}
