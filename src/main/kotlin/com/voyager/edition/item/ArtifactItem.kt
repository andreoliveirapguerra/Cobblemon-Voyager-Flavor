package com.voyager.edition.item

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

// We pass a 'tooltipKey' so each badge can have unique lore in the lang file
class ArtifactItem(properties: Properties, private val tooltipKey: String) : Item(properties) {

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltipComponents: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        // Add the standard lore text in Gray
        tooltipComponents.add(
            Component.translatable(tooltipKey)
                .withStyle(ChatFormatting.GRAY)
        )

        // Optional: Add "Gym Badge" subtitle in Gold
        tooltipComponents.add(
            Component.translatable("item.voyager.artifact_subtitle")
                .withStyle(ChatFormatting.GOLD)
        )
    }
}