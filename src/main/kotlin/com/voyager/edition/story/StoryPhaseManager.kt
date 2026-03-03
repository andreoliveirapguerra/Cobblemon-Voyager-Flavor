package com.voyager.edition.story

import com.voyager.edition.VoyagerFlavor
import com.voyager.edition.book.LorebookRegistry
import com.voyager.edition.registry.VoyagerItems
import com.voyager.edition.utils.VoyagerUtils.Companion.findSafeSurfaceSpace
import com.voyager.edition.utils.VoyagerUtils.Companion.runCommand
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Central manager for Keitai's narrative progression.
 *
 *  Event 11 — Harpy First Contact    (spawn Harpy + wormhole + Link de Tunelamento)
 *  Event 12 — Elias Meeting          (spawn Elias no Setor Zero)
 *  Event 13 — Fenda do Equilíbrio    (moral choice: Ordem vs Caos)
 */
object StoryPhaseManager {

    // ─── Event 11: Harpy First Contact ────────────────────────────────────────

    fun startEvent11_HarpyFirstContact(player: ServerPlayer) {
        spawnHarpyNearPlayer(player)
        UltraEventManager.triggerUltraEvent(player, 1)
        giveItemToPlayer(player, ItemStack(VoyagerItems.LINK_TUNELAMENTO))
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Event 11 started for ${player.name.string}")
    }

    // ─── Event 12: Elias Meeting ──────────────────────────────────────────────

    fun startEvent12_EliasMeeting(player: ServerPlayer) {
        spawnEliasNearPlayer(player)
        player.sendSystemMessage(
            Component.literal("[ULTRA-SCANNER] Assinatura de calor detectada no Setor Zero. Investigar.")
                .withStyle(ChatFormatting.DARK_GRAY)
        )
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Event 12 started for ${player.name.string}")
    }

    // ─── Event 13: Fenda do Equilíbrio ────────────────────────────────────────

    fun startEvent13_MoralChoice(player: ServerPlayer) {
        UltraEventManager.triggerUltraEvent(player, 13)
        spawnHarpyNearPlayer(player)
        player.sendSystemMessage(
            Component.literal("[ULTRA-SCANNER] ANOMALIA MÁXIMA DETECTADA. A Fenda do Equilíbrio se abriu.")
                .withStyle(ChatFormatting.DARK_RED)
        )
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Event 13 started for ${player.name.string}")
    }

    // ─── Phase 3 alias (called by VoyagerRivalEvents on Vance victory) ────────

    fun startPhase3_TheVoidCalls(player: ServerPlayer) {
        player.addTag("voyager_phase3_started")
        spawnHarpyNearPlayer(player)
        giveItemToPlayer(player, LorebookRegistry.VANCE_BOOK.copy())
        player.sendSystemMessage(
            Component.literal("[ULTRA-SCANNER] Sinal anômalo detectado no Setor Zero...")
                .withStyle(ChatFormatting.DARK_GRAY)
        )
        player.addTag("voyager_met_harpy")
        startEvent11_HarpyFirstContact(player)
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

    private fun spawnEliasNearPlayer(player: ServerPlayer) {
        val safePos = findSafeSurfaceSpace(player, 60) ?: player.blockPosition()
        runCommand(player.server, "spawnnpcat ${safePos.x} ${safePos.y} ${safePos.z} elias")
        player.sendSystemMessage(
            Component.literal("Alguém está aqui embaixo... você não está sozinho.")
                .withStyle(ChatFormatting.YELLOW)
        )
        VoyagerFlavor.LOGGER.info("[StoryPhaseManager] Elias spawned at $safePos for ${player.name.string}")
    }

    private fun giveItemToPlayer(player: ServerPlayer, item: ItemStack) {
        if (!player.inventory.add(item)) {
            player.drop(item, false)
        }
    }
}
