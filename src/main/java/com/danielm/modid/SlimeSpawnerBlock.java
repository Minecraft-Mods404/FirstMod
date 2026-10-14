package com.danielm.modid;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SlimeSpawnerBlock extends BaseEntityBlock {

    public static final MapCodec<SlimeSpawnerBlock> CODEC = simpleCodec(SlimeSpawnerBlock::new);

    /** True when the spawner has Super Slime in it. */
    public static final BooleanProperty FUELLED = BooleanProperty.create("fuelled");

    public SlimeSpawnerBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FUELLED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FUELLED);
    }

    @Override
    protected MapCodec<SlimeSpawnerBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new SlimeSpawnerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, SlimeMod.SLIME_SPAWNER_ENTITY,
                SlimeSpawnerBlockEntity::serverTick);
    }

    // ─── right-click with Super Slime: add fuel ───

    @Override
    protected InteractionResult useItemOn(final ItemStack stack, final BlockState state,
                                          final Level level, final BlockPos pos, final Player player,
                                          final net.minecraft.world.InteractionHand hand,
                                          final BlockHitResult hitResult) {

        if (!stack.is(SlimeMod.SUPER_SLIME)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof SlimeSpawnerBlockEntity be) {
            if (be.addFuel(1)) {
                stack.consume(1, player);
                player.sendSystemMessage(
                        Component.literal("Slime: " + be.getFuel() + " / "
                                + SlimeSpawnerBlockEntity.MAX_FUEL));
            } else {
                player.sendSystemMessage(Component.literal("Full"));
            }
        }

        return InteractionResult.SUCCESS;
    }

    // ─── right-click empty handed: start or stop ───

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level,
                                               final BlockPos pos, final Player player,
                                               final BlockHitResult hitResult) {

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof SlimeSpawnerBlockEntity be) {
            boolean nowRunning = be.toggle();

            String message;
            if (nowRunning) {
                message = "Spawning";
            } else if (be.getFuel() <= 0) {
                message = "No Super Slime";
            } else {
                message = "Stopped";
            }
            player.sendSystemMessage(Component.literal(message));
        }

        return InteractionResult.SUCCESS;
    }
}