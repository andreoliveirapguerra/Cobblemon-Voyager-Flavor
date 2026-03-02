package com.voyager.edition.story

import com.voyager.edition.VoyagerFlavor
import com.voyager.edition.book.LorebookRegistry
import com.voyager.edition.utils.VoyagerUtils.Companion.findSafeSurfaceSpace
import com.voyager.edition.utils.VoyagerUtils.Companion.runCommand
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Central manager for Keitai's narrative progression.
 *
 * Phases:
 *  Phase 3 — The Void Calls       (after player beats Vance + meets Lyra)
 *  Phase 4 — Sector Zero          (stub: Harpy dialogue arc, biome unlock)
 *  Phase 5 — Fenda Final          (stub: moral choice — Voyager vs Hypnos)
 */
object StoryPhaseManager {

    // ─── Phase 3: The Void Calls ──────────────────────────────────────────────

    fun startPhase3_TheVoidCalls(player: ServerPlayer) {
        player.addTag("voyager_phase3_started")
        spawnHarpyNearPlayer(player)
        giveBookToPlayer(player, LorebookRegistry.VANCE_BOOK.copy())
        player.sendSystemMessage(
            Component.literal("[ULTRA-SCANNER] Sinal anômalo detectado no Setor Zero...")
                .withStyle(ChatFormatting.DARK_GRAY)
        )
        player.addTag("voyager_met_harpy")
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Phase 3 started for ${player.name.string}")
    }

    // ─── Phase 4 (stub) ───────────────────────────────────────────────────────

    fun startPhase4_SectorZeroExploration(player: ServerPlayer) {
        // TODO: unlock Sector Zero biome access, begin Harpy dialogue arc
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Phase 4 stub called for ${player.name.string}")
    }

    // ─── Phase 5 (stub) ───────────────────────────────────────────────────────

    fun startPhase5_MoralChoice_FendaFinal(player: ServerPlayer) {
        // TODO: final moral choice — join Voyager or Hypnos
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Phase 5 stub called for ${player.name.string}")
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun spawnHarpyNearPlayer(player: ServerPlayer) {
        val safePos = findSafeSurfaceSpace(player, 80) ?: player.blockPosition()
        runCommand(player.server, "spawnnpcat ${safePos.x} ${safePos.y} ${safePos.z} harpy")
        player.sendSystemMessage(
            Component.literal("Uma presença estranha se aproxima das sombras...")
                .withStyle(ChatFormatting.DARK_PURPLE)
        )
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Harpy spawned at $safePos for ${player.name.string}")
    }

    private fun giveBookToPlayer(player: ServerPlayer, book: ItemStack) {
        if (!player.inventory.add(book)) {
            // Inventory full — drop the book at the player's feet
            player.drop(book, false)
        }
    }
}
