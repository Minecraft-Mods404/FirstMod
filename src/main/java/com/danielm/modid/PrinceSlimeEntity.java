package com.danielm.modid;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.UUID;

public class PrinceSlimeEntity extends Slime {

    /** One tier above a large slime (vanilla goes 1, 2, 4). */
    public static final int PRINCE_SIZE = 8;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.firstmod.prince_slime"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS);

    public PrinceSlimeEntity(final EntityType<? extends PrinceSlimeEntity> type, final Level level) {
        super(type, level);
        this.setSize(PRINCE_SIZE, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Slime.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    // ─── stop vanilla from randomizing our size on spawn ───

    @Override
    protected void setSpawnSize(final ServerLevelAccessor level, final DifficultyInstance difficulty) {
        this.setSize(PRINCE_SIZE, true);
    }

    /** Vanilla sets max health to size*size (64 at size 8). We want a flat 100. */
    @Override
    protected void setcubeMobHealth(final int actualSize) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0);
    }

    /** Zero split children, so vanilla's remove() loop spawns nothing. */
    @Override
    protected int getSplitCount() {
        return 0;
    }

    // ─── boss bar ───

    @Override
    public void startSeenByPlayer(final ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(final ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    // ─── death: 3 large slimes, 5-10 super slime ───

    @Override
    public void remove(final Entity.RemovalReason reason) {
        if (!this.level().isClientSide() && this.isDeadOrDying()) {

            for (int i = 0; i < 3; i++) {
                Slime child = EntityTypes.SLIME.create(this.level(), EntitySpawnReason.TRIGGERED);
                if (child != null) {
                    child.setSize(4, true);              // large slime
                    child.snapTo(this.getX() + (i - 1) * 2.0,
                            this.getY() + 0.5,
                            this.getZ(),
                            this.random.nextFloat() * 360.0F, 0.0F);
                    this.level().addFreshEntity(child);
                }
            }

            int amount = 5 + this.random.nextInt(6);     // 5-10
            this.spawnAtLocation((ServerLevel) this.level(),
                    new ItemStack(FirstMod.SUPER_SLIME, amount));
        }
        super.remove(reason);
    }
}