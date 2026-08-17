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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.UUID;

public class KingSlimeEntity extends Slime {

    /** One tier above a Prince (vanilla goes 1, 2, 4; Prince is 8). */
    public static final int KING_SIZE = 12;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.firstmod.king_slime"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS);

    public KingSlimeEntity(final EntityType<? extends KingSlimeEntity> type, final Level level) {
        super(type, level);
        this.setSize(KING_SIZE, true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Slime.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    // ─── stop vanilla overriding our size and health ───

    @Override
    protected void setSpawnSize(final ServerLevelAccessor level, final DifficultyInstance difficulty) {
        this.setSize(KING_SIZE, true);
    }

    /** Vanilla sets max health to size*size (144 at size 12). We want a flat 400. */
    @Override
    protected void setcubeMobHealth(final int actualSize) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0);
    }

    /** Zero split children, so vanilla's remove() loop spawns nothing of its own. */
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

    // ─── death: 3 Prince Slimes, plus a pile of loot ───

    @Override
    public void remove(final Entity.RemovalReason reason) {
        if (!this.level().isClientSide() && this.isDeadOrDying()) {

            ServerLevel serverLevel = (ServerLevel) this.level();

            // 3 Prince Slimes
            for (int i = 0; i < 3; i++) {
                PrinceSlimeEntity child = FirstMod.PRINCE_SLIME.create(
                        this.level(), EntitySpawnReason.TRIGGERED);
                if (child != null) {
                    child.snapTo(this.getX() + (i - 1) * 3.0,
                            this.getY() + 0.5,
                            this.getZ(),
                            this.random.nextFloat() * 360.0F, 0.0F);
                    this.level().addFreshEntity(child);
                }
            }

            // 10-50 super slime
            int superSlime = 10 + this.random.nextInt(41);
            this.spawnAtLocation(serverLevel,
                    new ItemStack(FirstMod.SUPER_SLIME, superSlime));

            // 0-10 explosive slime
            int explosive = this.random.nextInt(11);
            if (explosive > 0) {
                this.spawnAtLocation(serverLevel,
                        new ItemStack(FirstMod.EXPLOSIVE_SLIME, explosive));
            }

            // 10% chance of 1 slime grenade
            if (this.random.nextFloat() < 0.10f) {
                this.spawnAtLocation(serverLevel,
                        new ItemStack(FirstMod.SLIME_GRENADE, 1));
            }

            // 1% chance of 1 mega slime grenade
            if (this.random.nextFloat() < 0.01f) {
                this.spawnAtLocation(serverLevel,
                        new ItemStack(FirstMod.SLIME_MEGA_GRENADE, 1));
            }
        }
        super.remove(reason);
    }
}