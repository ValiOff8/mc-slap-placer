package com.valithedev.slabplacer.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.valithedev.slabplacer.placement.SlabPlacementFilter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Unique
    private boolean slabplacer$blockedPlacement;

    @WrapMethod(method = "useItemOn")
    private InteractionResult slabplacer$scopePlacement(
            LocalPlayer player, InteractionHand hand, BlockHitResult hit,
            Operation<InteractionResult> original) {
        boolean previousBlockedPlacement = slabplacer$blockedPlacement;
        slabplacer$blockedPlacement = false;
        try {
            return original.call(player, hand, hit);
        } finally {
            slabplacer$blockedPlacement = previousBlockedPlacement;
        }
    }

    @WrapOperation(
            method = "performUseItemOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"),
            require = 2,
            allow = 2
    )
    private InteractionResult slabplacer$filterPlacement(
            ItemStack stack, UseOnContext context, Operation<InteractionResult> original) {
        // Vanilla has already handled block interactions, spectators, and cooldowns.
        if (SlabPlacementFilter.shouldBlock(stack, context)) {
            slabplacer$blockedPlacement = true;
            // FAIL also stops Minecraft from trying the other hand or using the item in air.
            return InteractionResult.FAIL;
        }
        return original.call(stack, context);
    }

    @WrapWithCondition(
            method = "startPrediction",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V")
    )
    private boolean slabplacer$sendAllowedPlacement(ClientPacketListener connection, Packet<?> packet) {
        // Returning FAIL locally alone still sends the placement packet in vanilla.
        return !slabplacer$blockedPlacement || !(packet instanceof ServerboundUseItemOnPacket);
    }
}
