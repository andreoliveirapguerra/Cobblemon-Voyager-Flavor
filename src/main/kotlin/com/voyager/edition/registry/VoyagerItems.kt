package com.voyager.edition.registry

import com.voyager.edition.VoyagerFlavor // Import your main class
import com.voyager.edition.item.ArtifactItem
import com.voyager.edition.item.BadgeItem
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity

object VoyagerItems {

    // Define the badges
    val PION_BADGE = registerBadge("pion_badge", "item.voyager.pion.desc")
    val GEMINI_BADGE = registerBadge("gemini_badge", "item.voyager.gemini.desc")
    val ANTARES_BADGE = registerBadge("antares_badge", "item.voyager.antares.desc")
    val ANDROMEDA_BADGE = registerBadge("andromeda_badge", "item.voyager.andromeda.desc")
    val ARTIFACT = registerArtifact("artifact", "item.voyager.artifact.desc")

    private fun registerBadge(name: String, tooltipKey: String): Item {
        val properties = Item.Properties().rarity(Rarity.RARE)
        val item = BadgeItem(properties, tooltipKey)

        return Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(VoyagerFlavor.MOD_ID, name),
            item
        )
    }

    private fun registerArtifact(name: String, tooltipKey: String): Item {
        val properties = Item.Properties().rarity(Rarity.RARE)
        val item = ArtifactItem(properties, tooltipKey)

        return Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(VoyagerFlavor.MOD_ID, name),
            item
        )
    }

    fun registerModItems() {
        // ERROR FIX: Access the logger via the object name
        VoyagerFlavor.LOGGER.info("Registering Voyager Gym Badges")
    }
}