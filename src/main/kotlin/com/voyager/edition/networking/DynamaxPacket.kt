package com.voyager.edition.networking

import com.voyager.edition.VoyagerFlavor
import io.netty.buffer.ByteBuf
import net.minecraft.core.UUIDUtil
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import java.util.UUID

/**
 * Client → Server packet that requests a Dynamax toggle for the given Pokemon UUID.
 * Sent when the player taps the Dynamax button in the Cobblemon interaction wheel.
 */
data class DynamaxPacket(val pokemonId: UUID) : CustomPacketPayload {

    companion object {
        val TYPE = CustomPacketPayload.Type<DynamaxPacket>(
            ResourceLocation.fromNamespaceAndPath(VoyagerFlavor.MOD_ID, "dynamax_toggle")
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, DynamaxPacket> = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            DynamaxPacket::pokemonId,
            ::DynamaxPacket
        )
    }

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}
