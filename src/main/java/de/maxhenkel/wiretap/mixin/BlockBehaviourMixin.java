package de.maxhenkel.wiretap.mixin;

import de.maxhenkel.wiretap.utils.HeadUtils;
import de.maxhenkel.wiretap.wiretap.DimensionLocation;
import de.maxhenkel.wiretap.wiretap.WiretapManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    public void useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        this.use(blockState, level, blockPos, player, interactionHand, cir);
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    public void useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        this.use(blockState, level, blockPos, player, InteractionHand.MAIN_HAND, cir);
    }

    @Unique
    private void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide()) {
            return;
        }
        if (!interactionHand.equals(InteractionHand.MAIN_HAND)) {
            return;
        }
        if (!blockState.getBlock().equals(Blocks.PLAYER_HEAD) && !blockState.getBlock().equals(Blocks.PLAYER_WALL_HEAD)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        if (!(blockEntity instanceof SkullBlockEntity skullBlockEntity)) {
            return;
        }

        ResolvableProfile profile = skullBlockEntity.getOwnerProfile();
        UUID speaker = profile == null ? null : HeadUtils.getSpeaker(profile.gameProfile());
        if (speaker == null) {
            return;
        }

        DimensionLocation microphoneLocation = WiretapManager.getInstance().getMicrophoneLocation(speaker);

        boolean verified = WiretapManager.getInstance().verifyMicrophoneLocation(speaker, microphoneLocation);

        if (verified) {
            player.displayClientMessage(Component.literal("Currently connected to %s".formatted(microphoneLocation)), false);
        } else {
            if (microphoneLocation != null && !microphoneLocation.isLoaded()) {
                player.displayClientMessage(Component.literal("Microphone is currently not in a loaded chunk"), false);
            } else {
                player.displayClientMessage(Component.literal("Microphone is currently not in a loaded chunk or not connected to a microphone"), false);
            }
        }

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

}
