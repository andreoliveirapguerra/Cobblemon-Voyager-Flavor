package com.voyager.edition

import com.voyager.edition.command.VoyagerCommands
import com.voyager.edition.event.VoyagerEvents
import com.voyager.edition.registry.VoyagerGroup
import com.voyager.edition.registry.VoyagerItems
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.slf4j.LoggerFactory

object VoyagerFlavor : ModInitializer {
    public val LOGGER = LoggerFactory.getLogger("voyager-flavor")

	const val MOD_ID = "voyager-flavor"


	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!")

		VoyagerItems.registerModItems()
		VoyagerGroup.registerGroup()
		VoyagerEvents.register()

		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			VoyagerCommands.register(dispatcher)
		}
	}
}