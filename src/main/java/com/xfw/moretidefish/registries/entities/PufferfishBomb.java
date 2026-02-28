package com.xfw.moretidefish.registries.entities;

import com.li64.tide.registries.entities.fish.AbstractTideFish;
import com.xfw.moretidefish.registries.MTFParticleRegistry;
import com.xfw.moretidefish.misc.WaterIgnoringDamageCalculator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class PufferfishBomb extends AbstractTideFish {

    private static final EntityDataAccessor<Integer> PUFF_STATE = SynchedEntityData.defineId(PufferfishBomb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INFLATE_COUNTER = SynchedEntityData.defineId(PufferfishBomb.class, EntityDataSerializers.INT);;
    int deflateTimer;
    private static final Predicate<LivingEntity> SCARY_MOB = (livingEntity) -> {
        if (livingEntity instanceof Player player && player.isCreative()) {
            return false;
        }
        return !livingEntity.getType().is(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH);
    };
    static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat().ignoreInvisibilityTesting().ignoreLineOfSight().selector(SCARY_MOB);
    public static final int STATE_SMALL = 0;
    public static final int STATE_MID = 1;
    public static final int STATE_FULL = 2;

    public PufferfishBomb(EntityType<PufferfishBomb> pufferfishBombEntityEntityType, Level level) {
        super(pufferfishBombEntityEntityType, level);
        this.refreshDimensions();
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PUFF_STATE, 0);
        builder.define(INFLATE_COUNTER, 0);
    }

    public int getPuffState() {
        return this.entityData.get(PUFF_STATE);
    }

    public void setPuffState(int puffState) {
        this.entityData.set(PUFF_STATE, puffState);
    }

    public int getInflateCounter() {
        return this.entityData.get(INFLATE_COUNTER);
    }

    public void setInflateCounter(int inflateCounter) {
        this.entityData.set(INFLATE_COUNTER, inflateCounter);
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (PUFF_STATE.equals(key)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("PuffState", this.getPuffState());
        compound.putInt("InflateCounter", this.getInflateCounter());
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setPuffState(Math.min(compound.getInt("PuffState"), 2));
        this.setInflateCounter(compound.getInt("InflateCounter"));

    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PufferfishPuffGoal(this));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            //膨胀逻辑
            if (this.getInflateCounter() > 0) {
                if (this.getPuffState() == 0) {
                    this.makeSound(SoundEvents.PUFFER_FISH_BLOW_UP);
                    this.setPuffState(1);
                } else if (this.getInflateCounter() > 20 && this.getPuffState() == 1) {
                    this.makeSound(SoundEvents.PUFFER_FISH_BLOW_UP);
                    this.setPuffState(2);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                else if (this.getInflateCounter() > 60 && this.getPuffState() == 2) {
                    DamageSource damageSource = this.damageSources().explosion(this, this);
                    ExplosionDamageCalculator calculator = new WaterIgnoringDamageCalculator();
                    this.level().explode(this, damageSource, calculator,
                            this.getX(), this.getY(), this.getZ(), 5, false,
                            Level.ExplosionInteraction.MOB,
                            MTFParticleRegistry.AIR_BUBBLE.get(), MTFParticleRegistry.AIR_BUBBLE.get(),
                            SoundEvents.GENERIC_EXPLODE);
                    this.discard();
                }
                this.setInflateCounter(this.getInflateCounter()+1);
            } else if (this.getPuffState() != 0) {
                //收缩逻辑（只在未完全膨胀时收缩）
                if (this.deflateTimer > 100 && this.getPuffState() == 1) {
                    this.makeSound(SoundEvents.PUFFER_FISH_BLOW_OUT);
                    this.setPuffState(0);
                }
                ++this.deflateTimer;
            }
        }

        super.tick();
    }
    public int getFuse() {
        return this.getPuffState() == 2 ? 60 - this.getInflateCounter() : 0;
    }

    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.getPuffState() > 0) {
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(
                    Mob.class,
                    this.getBoundingBox().inflate(0.3),
                    (p_149013_) -> targetingConditions.test(this, p_149013_))) {
                if (livingEntity.isAlive()) {
                    this.finalTouch(livingEntity);
                }
            }
        }
    }

    public int getAirSupply() {
        return this.getPuffState() == 2 ? 300: super.getAirSupply();
    }

    public boolean finalTouch(LivingEntity livingEntity) {
        int i = this.getPuffState();
        boolean flag = livingEntity.hurt(this.damageSources().mobAttack(this), (float)(1 + i));
        if (flag) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * i, 0), this);
            this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
        }
        return flag;
    }

    public void playerTouch(Player player) {
        int i = this.getPuffState();
        if (player instanceof ServerPlayer && i > 0 && this.finalTouch(player)) {
            if (!this.isSilent()) {
                ((ServerPlayer)player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
            }
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.PUFFER_FISH_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PUFFER_FISH_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PUFFER_FISH_HURT;
    }

    protected SoundEvent getFlopSound() {
        return SoundEvents.PUFFER_FISH_FLOP;
    }

    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(getScale(this.getPuffState()));
    }

    private static float getScale(int puffState) {
        switch (puffState) {
            case 0 -> {
                return 0.5F;
            }
            case 1 -> {
                return 0.7F;
            }
            default -> {
                return 1.0F;
            }
        }
    }

    static class PufferfishPuffGoal extends Goal {
        private final PufferfishBomb fish;

        public PufferfishPuffGoal(PufferfishBomb fish) {
            this.fish = fish;
        }

        public boolean canUse() {
            List<LivingEntity> list = this.fish.level().getEntitiesOfClass(LivingEntity.class, this.fish.getBoundingBox().inflate(2.0), (target) -> {
                return PufferfishBomb.targetingConditions.test(this.fish, target);
            });
            return !list.isEmpty();
        }

        public void start() {
            this.fish.setInflateCounter(1);
            this.fish.deflateTimer = 0;
        }

        public void stop() {
            //只有在不是最大状态时才停止膨胀
            if (this.fish.getPuffState() < 2) {
                this.fish.setInflateCounter(0);
            }
        }
    }
}
