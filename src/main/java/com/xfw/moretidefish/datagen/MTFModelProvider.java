package com.xfw.moretidefish.datagen;

import com.li64.tide.Tide;
import com.li64.tide.registries.TideFish;
import com.xfw.moretidefish.MoreTideFish;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


//TODO灯笼鱼模型
public class MTFModelProvider extends ItemModelProvider {

    public MTFModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MoreTideFish.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // 生成所有符合条件的鱼物品
        TideFish.ORDERED.stream()
                .filter(this::isTideMoreTideFishItem) //仅限 tide:moretidefish_ 前缀
                .forEach(this::generateFishItem);

        //生成生成物品（如鱼桶、刷怪蛋等）—— 不要求路径前缀，但确保属于 tide 命名空间
        TideFish.SPAWNING_ITEMS.stream()
                .filter(this::isTideMoreTideFishItem)
                .forEach(this::basicItem);  //平面物品模型
    }

    private boolean isTideMoreTideFishItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key.getNamespace().equals(Tide.MOD_ID) && key.getPath().startsWith("moretidefish_");
    }

    private void generateFishItem(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        String path = key.getPath();

        if (item instanceof SwordItem) {
            //鱼剑：使用手持模型，纹理指向 item/ 下的同名 png
            withExistingParent(path, mcLoc("item/handheld"))
                    .texture("layer0", modLoc("item/" + path));
        } else {
            //普通鱼：平面模型
            basicItem(item);
        }
    }
}