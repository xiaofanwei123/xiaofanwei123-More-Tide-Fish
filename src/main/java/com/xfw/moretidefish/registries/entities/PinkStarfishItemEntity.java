package com.xfw.moretidefish.registries.entities;

import com.xfw.moretidefish.registries.MTFRegistry;
import com.xfw.moretidefish.registries.MTFEntityRegistry;
import com.xfw.moretidefish.registries.MTFParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class PinkStarfishItemEntity extends AbstractArrow {

    public ItemStack weapon = ItemStack.EMPTY;
    public int randomRotation;
    private float lastFlightXRot;
    private float lastFlightZRot;

    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK =
            SynchedEntityData.defineId(PinkStarfishItemEntity.class, EntityDataSerializers.ITEM_STACK);

    public PinkStarfishItemEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.randomRotation = this.random.nextInt(100);
        this.noCulling = true;
    }

    public PinkStarfishItemEntity(LivingEntity owner, Level level, ItemStack weapon) {
        this(MTFEntityRegistry.PINK_STARFISH_ITEM_ENTITY.get(), level);
        this.setOwner(owner);
        this.weapon = weapon.copy();

        Vec3 pos = owner.getEyePosition().add(owner.getLookAngle().scale(0.5));
        this.setPos(pos.x, pos.y, pos.z);
        this.entityData.set(DATA_ITEM_STACK, weapon.copy());
        this.setNoGravity(false);
        this.noPhysics = false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_ITEM_STACK.equals(key)) {
            this.weapon = this.entityData.get(DATA_ITEM_STACK);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (!weapon.isEmpty()) {
            compound.put("RenderWeapon", weapon.save(this.registryAccess()));
        }
        compound.putInt("RandomRotation", this.randomRotation);
        compound.putFloat("LastFlightXRot", this.lastFlightXRot);
        compound.putFloat("LastFlightZRot", this.lastFlightZRot);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("RenderWeapon", 10)) { //10
            ItemStack.parse(this.registryAccess(), compound.getCompound("RenderWeapon"))
                    .ifPresent(stack -> this.weapon = stack);
            this.entityData.set(DATA_ITEM_STACK, this.weapon.copy());
        }
        this.randomRotation = compound.getInt("RandomRotation");
        this.lastFlightXRot = compound.getFloat("LastFlightXRot");
        this.lastFlightZRot = compound.getFloat("LastFlightZRot");
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) return;
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
            //造成伤害
            DamageSource damagesource = this.damageSources().thrown(this, this.getOwner());
            if(livingEntity.hurt(damagesource, 10.0F)){
                this.doKnockback(livingEntity, damagesource);
            }
            this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(MTFRegistry.PINK_STARFISH);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }


    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (!this.inGround) {
                this.lastFlightXRot = (float) (90 - 20 * Math.cos((this.tickCount + this.randomRotation) / 10.0));
                float pPartialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
                this.lastFlightZRot = this.tickCount + pPartialTicks;
                //产生向上的泡泡
                this.level().addParticle(MTFParticleRegistry.AIR_BUBBLE.get(), this.getX(), this.getY(), this.getZ(), 0, 0.1, 0);
            }
        }
        if (this.tickCount > 400) {
            this.discard();
        }
    }

    public void playerTouch(Player entity) {
        if (this.tickCount > 20) {
            super.playerTouch(entity);
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return weapon.copyWithCount(1); //返回保存的物品副本
    }

    @Override
    public void shootFromRotation(Entity shooter, float x, float y, float z, float velocity, float inaccuracy) {
        float f = -Mth.sin(y * 0.017453292F) * Mth.cos(x * 0.017453292F);
        float f1 = -Mth.sin((x + z) * 0.017453292F);
        float f2 = Mth.cos(y * 0.017453292F) * Mth.cos(x * 0.017453292F);
        Vec3 vec3 = new Vec3(f, f1, f2).normalize()
                .add(this.random.nextGaussian() * 0.0075F * inaccuracy,
                        this.random.nextGaussian() * 0.0075F * inaccuracy,
                        this.random.nextGaussian() * 0.0075F * inaccuracy)
                .scale(velocity);
        this.setDeltaMovement(vec3);

        //设置旋转
        this.setYRot(y * (180F / (float)Math.PI));
        this.setXRot(x * (180F / (float)Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public boolean inGround() {
        return this.inGround;
    }

    public float getLastFlightXRot() {
        return this.lastFlightXRot;
    }

    public float getLastFlightZRot() {
        return this.lastFlightZRot;
    }

    protected float getWaterInertia() {
        return 2.5f;
    }
}