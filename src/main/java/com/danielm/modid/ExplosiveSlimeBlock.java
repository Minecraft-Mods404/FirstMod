package com.danielm.modid;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ExplosiveSlimeBlock extends TntBlock {

    public static final MapCodec<TntBlock> CODEC = simpleCodec(ExplosiveSlimeBlock::new);

    public ExplosiveSlimeBlock(final BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TntBlock> codec() {
        return CODEC;
    }
}