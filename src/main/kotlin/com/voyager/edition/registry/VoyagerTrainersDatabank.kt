package com.voyager.edition.registry

import com.cobblemon.mod.common.util.party
import net.minecraft.server.level.ServerPlayer

class VoyagerTrainersDatabank {
    val trainersList: MutableList<VoyagerTrainerData> = MutableList(10) { VoyagerTrainerData() }
    fun registerTrainer(player: ServerPlayer) {
        if (trainersList.get(0).name == "PLACEHOLDER") {
            trainersList.removeAt(0)
        }
        this.trainersList.add(VoyagerTrainerData(player.name.string, player.party(), player.server.tickCount.toLong()))
    }

    fun unregisterTrainer(player: ServerPlayer) {
        trainersList.find { it.name == player.name.string }?.let { trainersList.remove(it) }
    }
}