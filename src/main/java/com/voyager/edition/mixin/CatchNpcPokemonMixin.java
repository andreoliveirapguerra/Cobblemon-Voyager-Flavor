package com.voyager.edition.mixin;

import com.cobblemon.mod.common.pokemon.Pokemon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows catching NPC Pokémon that have been explicitly marked as catchable by Voyager story events.
 *
 * Usage (Kotlin):
 *   pokemon.persistentData.putBoolean("voyager_catchable", true)
 *
 * This overrides isWild() to return true, which bypasses the two hard-blocks inside
 * EmptyPokeBallEntity:
 *   1. onHitEntity – "capture.not_wild" rejection
 *   2. shakeBall   – successful capture condition
 */
@Mixin(value = Pokemon.class, remap = false)
public abstract class CatchNpcPokemonMixin {

//    @Inject(method = "isUncatchable", at = @At("RETURN"), cancellable = true, remap = true)
//    private void voyager_overrideIsUncatchable(CallbackInfoReturnable<Boolean> cir) {
//        Pokemon self = (Pokemon) (Object) this;
//        if (self.getPersistentData().getBoolean("voyager_catchable")) {
//            cir.setReturnValue(false);
//        }
//    }

//    @Inject(method = "isWild", at = @At("RETURN"), cancellable = true, remap = true)
//    private void voyager_overrideIsWild(CallbackInfoReturnable<Boolean> cir) {
//        Pokemon self = (Pokemon) (Object) this;
//        if (self.getPersistentData().getBoolean("voyager_catchable")) {
//            cir.setReturnValue(true);
//        }
//    }
//
//    @Inject(method = "isNPCOwned", at = @At("RETURN"), cancellable = true, remap = true)
//    private void voyager_overrideIsNPCOwned(CallbackInfoReturnable<Boolean> cir) {
//        Pokemon self = (Pokemon) (Object) this;
//        if (self.getPersistentData().getBoolean("voyager_catchable")) {
//            cir.setReturnValue(false);
//        }
//    }

//    @Inject(method = "is", at = @At("RETURN"), cancellable = true, remap = false)
//    private void voyager_overrideIsWild(CallbackInfoReturnable<Boolean> cir) {
//        Pokemon self = (Pokemon) (Object) this;
//        if (self.getPersistentData().getBoolean("voyager_catchable")) {
//            cir.setReturnValue(false);
//        }
//    }
}
