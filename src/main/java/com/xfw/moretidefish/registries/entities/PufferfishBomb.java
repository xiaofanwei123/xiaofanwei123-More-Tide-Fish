package com.xfw.moretidefish.registries.entities;

import com.li64.tide.registries.entities.fish.SmoothSwimmingFish;
import com.xfw.moretidefish.registries.MTFParticleRegistry;
import com.xfw.moretidefish.misc.WaterIgnoringDamageCalculator;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class PufferfishBomb extends SmoothSwimmingFish {

    private static final EntityDataAccessor<Integer> PUFF_STATE = SynchedEntityData.defineId(PufferfishBomb.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> INFLATE_COUNTER = SynchedEntityData.defineId(PufferfishBomb.class, EntityDataSerializers.INT);
    int deflateTimer;

    private static final Predicate<LivingEntity> SCARY_MOB = (livingEntity) -> {
        if (livingEntity instanceof Player player && player.isCreative()) {
            return false;
        }
        return !livingEntity.getType().is(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH);
    };
    static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat()
            .ignoreInvisibilityTesting().ignoreLineOfSight().selector(SCARY_MOB);

    public static final int STATE_SMALL = 0;
    public static final int STATE_MID = 1;
    public static final int STATE_FULL = 2;
    public static final int STATE_BOOM = 3;

    // 计时常量（单位：tick）
    public static final int MID_THRESHOLD = 20;        // 膨胀到中
    public static final int FULL_THRESHOLD = 60;       // 膨胀到大
    public static final int WAIT_BEFORE_CHECK = 40;    // 第三阶段等待2秒后检查条件
    public static final int BOOM_DELAY = 40;           // 第四阶段等待2秒再爆炸
    public static final int TOTAL_TIME_TO_BOOM = FULL_THRESHOLD + WAIT_BEFORE_CHECK + BOOM_DELAY; //总时间到爆炸

    public PufferfishBomb(EntityType<PufferfishBomb> pufferfishBombEntityEntityType, Level level) {
        super(pufferfishBombEntityEntityType, level);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(PUFF_STATE, 0);
        builder.define(INFLATE_COUNTER, 0);
    }

    public int getPuffState() { return this.entityData.get(PUFF_STATE); }
    public void setPuffState(int puffState) { this.entityData.set(PUFF_STATE, puffState); }
    public int getInflateCounter() { return this.entityData.get(INFLATE_COUNTER); }
    public void setInflateCounter(int counter) { this.entityData.set(INFLATE_COUNTER, counter); }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (PUFF_STATE.equals(key)) this.refreshDimensions();
        super.onSyncedDataUpdated(key);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("PuffState", this.getPuffState());
        compound.putInt("InflateCounter", this.getInflateCounter());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setPuffState(Math.min(compound.getInt("PuffState"), 3));
        this.setInflateCounter(compound.getInt("InflateCounter"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PufferfishPuffGoal(this));
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            int puffState = this.getPuffState();
            int counter = this.getInflateCounter();

            if (counter > 0) {
                //等待阶段
                if (puffState == STATE_SMALL) {
                    this.makeSound(SoundEvents.PUFFER_FISH_BLOW_UP);
                    this.setPuffState(STATE_MID);
                //第二阶段
                } else if (puffState == STATE_MID && counter > MID_THRESHOLD) {
                    this.makeSound(SoundEvents.PUFFER_FISH_BLOW_UP);
                    this.setPuffState(STATE_FULL);
                //第三阶段
                } else if (puffState == STATE_FULL) {
                    //等一段时间后检查条件
                    if (counter > FULL_THRESHOLD + WAIT_BEFORE_CHECK) {
                        if (shouldExplode()) {
                            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
                            //进入第四阶段，爆炸倒计时
                            this.setPuffState(STATE_BOOM);
                            //ParticleTypes.EXPLOSION_EMITTER
                            this.makeSound(SoundEvents.TNT_PRIMED);
                        } else {
                            //条件不满足，开始收缩
                            this.setInflateCounter(0);
                            this.deflateTimer = 0;
                        }
                    }
                } else if (puffState == STATE_BOOM) {
                    //进入第四阶段,播放音效
                    if (counter == FULL_THRESHOLD + WAIT_BEFORE_CHECK + 1) {
                        this.makeSound(SoundEvents.TNT_PRIMED);
                    }
                    //等待爆炸
                    if (counter > FULL_THRESHOLD + WAIT_BEFORE_CHECK + BOOM_DELAY) {
                        doExplode();
                        this.discard();
                    }
            }

                //只在非收缩状态（counter未置0）才递增计数器
                if (this.getInflateCounter() > 0) {
                    this.setInflateCounter(counter + 1);
                }
            } else {
            //收缩逻辑（counter == 0）
            if (puffState == STATE_MID && this.deflateTimer > 100) {
                this.makeSound(SoundEvents.PUFFER_FISH_BLOW_OUT);
                this.setPuffState(STATE_SMALL);
                this.deflateTimer = 0;
            } else if (puffState == STATE_FULL && this.deflateTimer > 100) {
                this.makeSound(SoundEvents.PUFFER_FISH_BLOW_OUT);
                this.setPuffState(STATE_MID);
                this.deflateTimer = 0;
            } else if (puffState == STATE_BOOM) {
                //第四阶段：等待后爆炸
                if (this.deflateTimer > BOOM_DELAY) {
                    doExplode();
                    this.discard();
                }
            }
            ++this.deflateTimer;
        }
        }

        super.tick();
    }

    //判断是否应该进入第四阶段准备爆炸
    private boolean shouldExplode() {
        Player nearestPlayer = this.level().getNearestPlayer(this, 10.0);
        if (nearestPlayer != null && !nearestPlayer.isCreative()) return true;

        LivingEntity lastAttacker = this.getLastHurtByMob();
        return lastAttacker != null && lastAttacker.isAlive() && this.distanceTo(lastAttacker) <= 10.0;
    }

    //执行爆炸
    //TODO:麻痹的力竭了，怎么样才能爆炸产生气泡
    private void doExplode() {
        DamageSource damageSource = this.damageSources().explosion(this, this);
        ExplosionDamageCalculator calculator = new WaterIgnoringDamageCalculator();
        this.level().explode(this, damageSource, calculator,
                this.getX(), this.getY(), this.getZ(), 5, false,
                Level.ExplosionInteraction.MOB,
                MTFParticleRegistry.AIR_BUBBLE.get(), MTFParticleRegistry.AIR_BUBBLE.get(),
                SoundEvents.GENERIC_EXPLODE);
    }

    public int getFuse() {
        if (this.getPuffState() == STATE_BOOM) {
            int total = FULL_THRESHOLD + WAIT_BEFORE_CHECK + BOOM_DELAY;
            int current = this.getInflateCounter();
            return Math.max(0, total - current);
        }
        return 0;
    }

    @Override
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

    @Override
    public int getAirSupply() {
        return this.getPuffState() == STATE_FULL ? 300 : super.getAirSupply();
    }

    public boolean finalTouch(LivingEntity livingEntity) {
        int i = this.getPuffState();
        if (i == STATE_BOOM) i = 3;
        boolean flag = livingEntity.hurt(this.damageSources().mobAttack(this), (float) (1 + i));
        if (flag) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * i, 0), this);
            this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
        }
        return flag;
    }

    @Override
    public void playerTouch(Player player) {
        int i = this.getPuffState();
        if (i == STATE_BOOM) i = 3;
        if (player instanceof ServerPlayer && i > 0 && this.finalTouch(player)) {
            if (!this.isSilent()) {
                ((ServerPlayer) player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.PUFFER_FISH_AMBIENT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.PUFFER_FISH_DEATH; }
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) { return SoundEvents.PUFFER_FISH_HURT; }
    @Override
    protected SoundEvent getFlopSound() { return SoundEvents.PUFFER_FISH_FLOP; }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(getScale(this.getPuffState()));
    }

    private static float getScale(int puffState) {
        switch (puffState) {
            case STATE_SMALL -> { return 0.5F; }
            case STATE_MID -> { return 0.7F; }
            case STATE_FULL -> { return 1.0F; }
            case STATE_BOOM -> { return 1.2F; }
            default -> { return 1.0F; }
        }
    }

    static class PufferfishPuffGoal extends Goal {
        private final PufferfishBomb fish;

        public PufferfishPuffGoal(PufferfishBomb fish) {
            this.fish = fish;
        }

        @Override
        public boolean canUse() {
            List<LivingEntity> list = this.fish.level().getEntitiesOfClass(LivingEntity.class,
                    this.fish.getBoundingBox().inflate(2.0),
                    (target) -> PufferfishBomb.targetingConditions.test(this.fish, target));
            return !list.isEmpty();
        }

        @Override
        public void start() {
            this.fish.setInflateCounter(1);
            this.fish.deflateTimer = 0;
        }

        @Override
        public void stop() {
            int state = this.fish.getPuffState();
            if (state < PufferfishBomb.STATE_FULL) {
                this.fish.setInflateCounter(0);
            }
        }
    }
}