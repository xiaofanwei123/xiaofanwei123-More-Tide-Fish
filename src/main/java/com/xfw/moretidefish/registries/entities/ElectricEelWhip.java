package com.xfw.moretidefish.registries.entities;


import com.mojang.datafixers.util.Pair;
import com.xfw.moretidefish.registries.MTFDataAttachmentsRegistry;
import com.xfw.moretidefish.registries.MTFEntityRegistry;
import com.xfw.moretidefish.registries.MTFParticleRegistry;
import com.xfw.moretidefish.registries.item.ElectricEelItem;
import com.xfw.moretidefish.registries.MTFSoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ElectricEelWhip extends Entity {
    public final ConcurrentLinkedQueue<Pair<Integer, Vec3>> pendingParticlePositions = new ConcurrentLinkedQueue<>();


    protected static final EntityDataAccessor<Integer> SPAWNED_TICKS = SynchedEntityData.defineId(ElectricEelWhip.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(ElectricEelWhip.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FOIL = SynchedEntityData.defineId(ElectricEelWhip.class, EntityDataSerializers.BOOLEAN);

    private static final String TAG_SPAWNED_TICKS = "spawned_ticks";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_WEAPON = "weapon";


    public ElectricEelWhip(Level level, Player player, @Nullable ItemStack weapon) {
        this(MTFEntityRegistry.ELECTRIC_EEL_ITEM.get(), level, player, weapon);
    }

    public ElectricEelWhip(EntityType<? extends ElectricEelWhip> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    public ElectricEelWhip(EntityType<? extends ElectricEelWhip> entityType, Level level, Player player, @Nullable ItemStack weapon) {
        this(entityType, level);
        this.setOwner(player);
        this.setPos(player.getEyePosition());
        this.firedFromWeapon = weapon;
        if (firedFromWeapon != null) {
            this.entityData.set(FOIL, firedFromWeapon.hasFoil());
        }
    }

    public int getSpawnedTicks() {
        return this.getEntityData().get(SPAWNED_TICKS);
    }

    public void setSpawnedTicks(int spawnedTicks) {
        this.getEntityData().set(SPAWNED_TICKS, spawnedTicks);
    }


    private void setOwnerId(int ownerId) {
        this.getEntityData().set(OWNER_ID, ownerId);
    }

    public int getOwnerId() {
        return this.getEntityData().get(OWNER_ID);
    }


    @Nullable
    private ItemStack firedFromWeapon;

    @Nullable
    private Entity owner;
    @Nullable
    private UUID ownerId;

    public Entity getOwner() {
        return level().isClientSide ? level().getEntity(getOwnerId()) : owner;
    }

    public void setOwner(Entity owner) {
        if (owner != null) {
            this.ownerId = owner.getUUID();
        } else {
            this.ownerId = null;
        }
        this.owner = owner;
        this.updateOwnerInfo(this);
    }

    private int oldAnimationTicks, animationTicks;

    public float getAnimationTicks(float partialTicks) {
        return Mth.lerp(partialTicks, oldAnimationTicks, animationTicks);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPAWNED_TICKS, 0)
                .define(OWNER_ID, -1)
                .define(FOIL, false);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    public boolean isFoil() {
        return this.entityData.get(FOIL);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (owner == null && ownerId != null && level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(ownerId);
                if (entity != null) {
                    owner = entity;
                }
                if (owner == null) {
                    ownerId = null;
                }
            }
            setOwnerId(owner != null ? owner.getId() : -1);
            if (getSpawnedTicks() >= getLifespan()) {
                discard();
            }
            setSpawnedTicks(getSpawnedTicks() + 1);
            Player player = getPlayerOwner();
            if (!(player != null && !player.isRemoved() && player.isAlive() && firedFromWeapon != null && ItemStack.isSameItemSameComponents(player.getMainHandItem(), firedFromWeapon))) {
                discard();
            }
            if (player != null) {
                setPos(player.getEyePosition());
                if (getSpawnedTicks() == getLifespan() / 2) {
                    playSound(MTFSoundRegistry.ELECTRIC_EEL_RELEASE.get(), 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
                    Vec3 endPos = rotationToPosition(player.getEyePosition(), getWhipRange((float) player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)), -owner.getXRot(), owner.getYHeadRot() + 90);
                    BlockHitResult hitResult = level().clip(new ClipContext(player.getEyePosition(), endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.of(this)));
                    if (hitResult.getType() != HitResult.Type.MISS) {
                        endPos = hitResult.getLocation();
                    }
                    List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, new AABB(player.getEyePosition(), endPos).inflate(1));
                    for (LivingEntity entity : entities) {
                        AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius() + 1.5f);
                        if (shouldHarm(player, entity) && entity.isPickable() && (aabb.contains(player.getEyePosition()) || aabb.clip(player.getEyePosition(), endPos).isPresent())) {
                            DamageSource damageSource = damageSources().playerAttack(player);
                            float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                            float knockback = (float)player.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
                            if (level() instanceof ServerLevel serverLevel && this.getWeaponItem() != null) {
                                damage = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damageSource, damage);
                                knockback = EnchantmentHelper.modifyKnockback(serverLevel, this.getWeaponItem(), player, damageSource, knockback);
                            }
                            if (entity.hurt(damageSource, damage)) {
                                if (getWeaponItem() != null && getWeaponItem().getItem() instanceof ElectricEelItem whipItem) {
                                    whipItem.doPostHurtEffects(entity);
                                }
                                if (level() instanceof ServerLevel serverLevel) {
                                    EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damageSource, this.getWeaponItem());
                                }
                                entity.knockback(knockback * 0.5F, Mth.sin(player.getYRot() * Mth.DEG_TO_RAD), -Mth.cos(player.getYRot() * Mth.DEG_TO_RAD));
                            }
                        }
                    }
                }
            }

        } else {
            processParticleQueue();
            int currentOwnerId = owner != null ? owner.getId() : -1;
            if (currentOwnerId != getOwnerId()) {
                setOwner(level().getEntity(getOwnerId()));
            }
            oldAnimationTicks = animationTicks;
            if (animationTicks == 0) {
                animationTicks = getSpawnedTicks();
            } else {
                animationTicks++;
            }
        }
    }

    private void processParticleQueue() {
        if (getSpawnedTicks() < getLifespan() / 2) return;

        Pair<Integer, Vec3> entry;
        while ((entry = pendingParticlePositions.poll()) != null) {
            int segmentIndex = entry.getFirst();
            Vec3 pos = entry.getSecond();
            if (random.nextFloat() < 0.5f) {
                int particleCount = random.nextInt(3) + 1;
                for (int i = 0; i < particleCount; i++) {
                    level().addParticle(
                            MTFParticleRegistry.SPARK_PARTICLE.get(),
                            pos.x, pos.y, pos.z,
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1,
                            (random.nextDouble() - 0.5) * 0.1
                    );
                }
            }
        }
    }

    public static boolean shouldHarm(Entity attacker, Entity victim) {
        if (attacker == null || victim == null) {
            return true;
        }
        if (attacker == victim) {
            return false;
        }
        if (attacker.isAlliedTo(victim) || victim.isAlliedTo(attacker)) {
            return false;
        }
        if (attacker instanceof Player p1 && victim instanceof Player p2 && !p1.canHarmPlayer(p2)) {
            return false;
        }
        return victim.isAttackable();
    }

    public static Vec3 rotationToPosition(Vec3 startPos, float radius, float pitch, float yaw) {
        double endPosX = radius * Math.cos(yaw * Mth.DEG_TO_RAD) * Math.cos(pitch * Mth.DEG_TO_RAD);
        double endPosY = radius * Math.sin(pitch * Mth.DEG_TO_RAD);
        double endPosZ = radius * Math.sin(yaw * Mth.DEG_TO_RAD) * Math.cos(pitch * Mth.DEG_TO_RAD);
        return startPos.add(new Vec3(endPosX, endPosY, endPosZ));
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            discard();
            return true;
        }
        return false;
    }

    public int getLifespan(){
        return 12;
    };

    public float getWhipRange(float interactionRange){
        return interactionRange + 7;
    };

    @Nullable
    @Override
    public ItemStack getWeaponItem() {
        return firedFromWeapon;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt(TAG_SPAWNED_TICKS, getSpawnedTicks());
        if (ownerId != null) {
            compoundTag.putUUID(TAG_OWNER, owner.getUUID());
        }
        if (this.firedFromWeapon != null && !this.firedFromWeapon.isEmpty()) {
            compoundTag.put(TAG_WEAPON, firedFromWeapon.save(registryAccess(), new CompoundTag()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        setSpawnedTicks(compoundTag.getInt(TAG_SPAWNED_TICKS));
        if (compoundTag.hasUUID(TAG_OWNER)) {
            ownerId = compoundTag.getUUID(TAG_OWNER);
        }
        if (compoundTag.contains(TAG_WEAPON, CompoundTag.TAG_COMPOUND)) {
            firedFromWeapon = ItemStack.parse(registryAccess(), compoundTag.getCompound(TAG_WEAPON)).orElse(null);
            if (firedFromWeapon != null) {
                this.entityData.set(FOIL, firedFromWeapon.hasFoil());
            }
        } else {
            firedFromWeapon = null;
        }
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public void remove(RemovalReason removalReason) {
        this.updateOwnerInfo(null);
        super.remove(removalReason);
    }

    private void updateOwnerInfo(@Nullable ElectricEelWhip whip) {
        Player player = this.getPlayerOwner();
        if (player != null) {
            player.setData(MTFDataAttachmentsRegistry.WHIP, whip == null ? -1 : whip.getId());
        }
    }

    @Nullable
    public Player getPlayerOwner() {
        Entity entity = this.getOwner();
        return entity instanceof Player ? (Player) entity : null;
    }

    @Override
    public boolean canChangeDimensions(Level level, Level level2) {
        return false;
    }
}