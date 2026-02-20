package com.voyager.edition.registry

import com.voyager.edition.VoyagerFlavor
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack

object VoyagerGroup {
    // 1. Create the Tab Identifier
    val VOYAGER_TAB_KEY = Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        ResourceLocation.fromNamespaceAndPath(VoyagerFlavor.MOD_ID, "voyager_items"),
        FabricItemGroup.builder()
            .icon { ItemStack(VoyagerItems.PION_BADGE) } // The icon on the tab
            .title(Component.translatable("itemGroup.voyager-flavor.general")) // The title (needs lang file)
            .displayItems { context, entries ->
                // Add your items here
                entries.accept(VoyagerItems.PION_BADGE)
                entries.accept(VoyagerItems.GEMINI_BADGE)
                entries.accept(VoyagerItems.ANTARES_BADGE)
                entries.accept(VoyagerItems.ANDROMEDA_BADGE)
                entries.accept(VoyagerItems.ARTIFACT)
            }
            .build()
    )

    fun registerGroup() {
        VoyagerFlavor.LOGGER.info("Registering Voyager Creative Tab")
    }
}