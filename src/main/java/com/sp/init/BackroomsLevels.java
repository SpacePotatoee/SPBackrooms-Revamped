package com.sp.init;

import com.sp.SPBRevamped;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class BackroomsLevels {
    public static final RegistryKey<World> LEVEL0_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, new Identifier(SPBRevamped.MOD_ID, "level0"));
    public static final RegistryKey<DimensionType> LEVEL0_DIM_TYPE = RegistryKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier(SPBRevamped.MOD_ID, "level0_type"));

    public static final RegistryKey<World> LEVEL1_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, new Identifier(SPBRevamped.MOD_ID, "level1"));

    public static final RegistryKey<World> LEVEL2_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, new Identifier(SPBRevamped.MOD_ID, "level2"));

    public static final RegistryKey<World> POOLROOMS_WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, new Identifier(SPBRevamped.MOD_ID, "poolrooms"));

    public static boolean isInBackrooms(RegistryKey<World> world){
        return world == LEVEL0_WORLD_KEY || world == LEVEL1_WORLD_KEY || world == LEVEL2_WORLD_KEY || world == POOLROOMS_WORLD_KEY;
    }

    public static BlockPos getCurrentLevelsOrigin(RegistryKey<World> world){
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        if(world == LEVEL0_WORLD_KEY){
            return mutable.set(1,22,1);

        } else if(world == LEVEL1_WORLD_KEY){
            return mutable.set(6,22,3);

        } else if(world == LEVEL2_WORLD_KEY){
            return mutable.set(0,21,8);

        } else if(world == POOLROOMS_WORLD_KEY){
            return mutable.set(15,104,16);

        }

        return null;
    }


}
