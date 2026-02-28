package com.xfw.moretidefish.registries.item;

import com.google.common.base.Suppliers;
import com.li64.tide.registries.TideFoods;
import com.xfw.moretidefish.MoreTideFish;
import com.xfw.moretidefish.misc.ModArmorMaterial;
import com.xfw.moretidefish.registries.models.LanternFishHelmetModel;
import com.xfw.moretidefish.registries.renderers.LanternFishHelmetRender;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LanternFishItem extends ArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<ItemAttributeModifiers> itemAttributeModifiersSupplier;

    public LanternFishItem() {
        super(ModArmorMaterial.LANTERN_FISH,
                ArmorItem.Type.HELMET,
                new Properties().stacksTo(1).food(TideFoods.RAW_FISH.toProperties())
        );
        this.itemAttributeModifiersSupplier = Suppliers.memoize(() -> {
            ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
            EquipmentSlotGroup equipmentSlotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
            ResourceLocation resourceLocation = ResourceLocation.withDefaultNamespace("armor." + type.getName());
            ResourceLocation gResourceLocation = MoreTideFish.Resource("armor." + type.getName());
            builder.add(Attributes.ARMOR, new AttributeModifier(resourceLocation, this.getMaterial().value().getDefense(type), AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(resourceLocation, this.getMaterial().value().toughness(), AttributeModifier.Operation.ADD_VALUE), equipmentSlotGroup);
            builder.add(Attributes.MAX_HEALTH, new AttributeModifier(gResourceLocation, -0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), equipmentSlotGroup);
            return builder.build();
        });
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            //潜行时吃
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            //不潜行时穿
            return this.swapWithEquipmentSlot(this, level, player, hand);
        }
    }

    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers() {
        return this.itemAttributeModifiersSupplier.get();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null) {
                    this.renderer = supplyRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @OnlyIn(Dist.CLIENT)
    public GeoArmorRenderer<?> supplyRenderer() {
        return new LanternFishHelmetRender(new LanternFishHelmetModel());
    }

}
