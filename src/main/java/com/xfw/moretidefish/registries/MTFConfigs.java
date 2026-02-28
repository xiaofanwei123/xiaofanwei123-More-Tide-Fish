package com.xfw.moretidefish.registries;

import com.li64.tide.registries.entities.TideSpawnConfigs;
import net.minecraft.tags.BiomeTags;

import java.util.function.Consumer;

//TODO生物生成条件
public class MTFConfigs extends TideSpawnConfigs {

    public static void onRegisterSpawnConfigs(Consumer<SpawnConfig> registry) {
        //灯笼鱼
        registry.accept(waterSpawnConfig("moretidefish_lantern_fish", 4, 1, 1, BiomeTags.IS_OCEAN));
        //忍者海星
        registry.accept(waterSpawnConfig("moretidefish_pink_starfish", 5, 1, 1, BiomeTags.IS_OCEAN));
        //掠夺鲨
        registry.accept(waterSpawnConfig("moretidefish_reaver_shark", 1, 1, 1, BiomeTags.IS_OCEAN));
        //爆炸河豚
        registry.accept(waterSpawnConfig("moretidefish_pufferfish_bomb", 4, 1, 1, BiomeTags.IS_OCEAN));
        //电鳗
        registry.accept(waterSpawnConfig("moretidefish_electric_eel", 1, 1, 1, BiomeTags.IS_DEEP_OCEAN));
    }
}
