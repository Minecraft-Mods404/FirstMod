package com.danielm.modid;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ExplosiveSlimeEntity extends Slime {

    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR =
            SynchedEntityData.defineId(ExplosiveSlimeEntity.class, EntityDataSerializers.INT);

    private int oldSwell;
    private int swell;
    private final int maxSwell = 30;

    public ExplosiveSlimeEntity(final EntityType<? extends ExplosiveSlimeEntity> type, final Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Slime.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 16.0)
            .add(Attributes.ATTACK_DAMAGE, 2.0)
            .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(2, new SlimeSwellGoal(this));
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1);
    }

    // ─── blast power scales with size ───

    /** 1 -> 2.0 (about half TNT), 2 -> 4.0 (TNT), 4 -> 20.0 (about 5x TNT). */
    private float blastPower() {
        return switch (this.getSize()) {
            case 1 -> 2.0F;
            case 2 -> 4.0F;
            default -> 20.0F;
        };
    }

    // ─── fuse ───

    @Override
    public void tick() {
        if (this.isAlive()) {
            this.oldSwell = this.swell;

            int dir = this.getSwellDir();
            if (dir > 0 && this.swell == 0) {
                this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
                this.gameEvent(net.minecraft.world.level.gameevent.GameEvent.PRIME_FUSE);
            }

            this.swell += dir;
            if (this.swell < 0) {
                this.swell = 0;
            }

            if (this.swell >= this.maxSwell) {
                this.swell = this.maxSwell;
                this.explode();
            }
        }
        super.tick();
    }

    private void explode() {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.dead = true;
            serverLevel.explode(this, this.getX(), this.getY(), this.getZ(),
                    this.blastPower(), Level.ExplosionInteraction.MOB);
            this.spawnLingeringCloudIfNeeded();
            this.kill(serverLevel);          // vanilla split still runs on death
        }
    }

    private void spawnLingeringCloudIfNeeded() {
        // hook for later - particles, effects, etc.
    }

    // ─── swell accessors (the renderer reads these) ───

    public float getSwelling(final float partialTicks) {
        return net.minecraft.util.Mth.lerp(partialTicks, (float) this.oldSwell, (float) this.swell)
                / (float) (this.maxSwell - 2);
    }

    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    public void setSwellDir(final int dir) {
        this.entityData.set(DATA_SWELL_DIR, dir);
    }

    @Override
    public void addAdditionalSaveData(final ValueOutput output) {
        super.addAdditionalSaveData(output);
        if (this.getSwellDir() > 0) {
            output.putBoolean("ignited", true);
        }
    }

    @Override
    public void readAdditionalSaveData(final ValueInput input) {
        super.readAdditionalSaveData(input);
        if (input.getBooleanOr("ignited", false)) {
            this.setSwellDir(1);
        }
    }
}