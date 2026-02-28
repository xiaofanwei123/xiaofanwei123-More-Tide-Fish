package com.xfw.moretidefish.registries.entities;

import com.li64.tide.registries.entities.fish.AbstractTideFish;
import com.xfw.moretidefish.registries.MTFMobEffectRegistry;
import com.xfw.moretidefish.registries.particles.ZapParticleOption;
import com.xfw.moretidefish.registries.MTFSoundRegistry;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class ElectricEel extends AbstractTideFish {
    public ElectricEel(EntityType<? extends ElectricEel> entityType, Level level) {
        super(entityType, level);
    }

    public int electricCooldown = 20;//电击冷却

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new ElectricEel.ElectricEelGoal(this));
    }


    private static final Predicate<LivingEntity> TARGET_SELECTOR = (livingEntity) -> {
        //排除自身 同类 创造/旁观者
        if (livingEntity instanceof ElectricEel) return false;
        if (livingEntity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return livingEntity.isAlive();
    };

    static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat()
            .ignoreInvisibilityTesting()
            .ignoreLineOfSight()
            .selector(TARGET_SELECTOR);


    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && !this.level().isClientSide && electricCooldown-- <= 0) {
            float range = this.isInWaterRainOrBubble() ? 1 : 0.3f;
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(range),
                    (livingEntity) -> targetingConditions.test(this, livingEntity))) {
                if (livingEntity.isAlive()) {
                    this.finalTouch(livingEntity);

                }
            }
        }
    }

    public boolean finalTouch(LivingEntity livingEntity) {
        boolean flag = livingEntity.hurt(this.damageSources().mobAttack(this), 1f);
        if (flag) {
            livingEntity.addEffect(new MobEffectInstance(MTFMobEffectRegistry.ELECTRIFIED, 60, 0,false,false), this);
            this.playSound(MTFSoundRegistry.ELECTRIC_EEL_RELEASE.get(), 1.0F, 1.0F);
            Vec3 start = this.getBoundingBox().getCenter();
            Vec3 end = livingEntity.getBoundingBox().getCenter();
            ((ServerLevel)this.level()).sendParticles(new ZapParticleOption(end),
                    start.x, start.y, start.z, 1, 0, 0, 0, 0);
        }
        return flag;
    }

    public void playerTouch(Player player) {
        if (player instanceof ServerPlayer && this.finalTouch(player)) {
            if (!this.isSilent()) {
                ((ServerPlayer)player).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
            }
        }
    }

    static class ElectricEelGoal extends Goal {
        private final ElectricEel fish;

        public ElectricEelGoal(ElectricEel fish) {
            this.fish = fish;
        }

        public boolean canUse() {
            List<LivingEntity> list = this.fish.level().getEntitiesOfClass(
                    LivingEntity.class,
                    this.fish.getBoundingBox().inflate(2.0),
                    entity -> targetingConditions.test(this.fish, entity)
            );
            return !list.isEmpty();
        }

//        public void start() {
//
//        }
//
//        public void stop() {
//
//        }
    }

}
