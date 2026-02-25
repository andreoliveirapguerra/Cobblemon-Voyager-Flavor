package com.voyager.edition.registry

import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore
import net.minecraft.server.level.ServerPlayer

class VoyagerTrainerData {
    var partySnapshot: PlayerPartyStore? = null
    var name: String? = null
    var lastTickTimeRegistered: Long? = null

    constructor(name: String?, partySnapshot: PlayerPartyStore?, lastTickTimeRegistered: Long?) {
        this.name = name
        this.partySnapshot = partySnapshot
        this.lastTickTimeRegistered = lastTickTimeRegistered
    }
    constructor() {
        partySnapshot = null
        lastTickTimeRegistered = null
        name = "PLACEHOLDER"
    }
}