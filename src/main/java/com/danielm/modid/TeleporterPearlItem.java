package com.danielm.modid;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;

public class TeleporterPearlItem extends Item {

    /** Which dimension this pearl sends you to. */
    private final ResourceKey<Level> target;

    /** Advancement path granted when this pearl is used, e.g. "use_slime_pearl". */
    private final String advancementPath;

    public TeleporterPearlItem(final Item.Properties properties,
                               final ResourceKey<Level> target,
                               final String advancementPath) {
        super(properties);
        this.target = target;
        this.advancementPath = advancementPath;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {

        // server side only
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel destination = serverLevel.getServer().getLevel(this.target);

        // no such dimension, or we are already there
        if (destination == null || destination == serverLevel) {
            return InteractionResult.FAIL;
        }

        // let the game pick a safe landing spot rather than a fixed height
        serverPlayer.teleport(new TeleportTransition(
                destination,
                new Vec3(serverPlayer.getX(), 100.0, serverPlayer.getZ()),
                Vec3.ZERO,
                serverPlayer.getYRot(),
                serverPlayer.getXRot(),
                TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)));

        SlimeMod.grantAdvancement(serverLevel.getServer(), serverPlayer, this.advancementPath, "used");

        ItemStack stack = player.getItemInHand(hand);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}