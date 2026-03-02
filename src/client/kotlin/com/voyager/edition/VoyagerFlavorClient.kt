package com.voyager.edition

import com.voyager.edition.client.CobbleEvent
import net.fabricmc.api.ClientModInitializer

object VoyagerFlavorClient : ClientModInitializer {
	override fun onInitializeClient() {
		CobbleEvent.register()
	}
}