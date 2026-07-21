package com.sp.mixin.rain;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.SPBRevampedClient;
import com.sp.init.BackroomsLevels;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LightningEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightningEntity.class)
public class LightningSkyLightingUpLightningEntity {
    @WrapMethod(method = "tick")
    public void spbrevamped$LightingUpSkyDuringLightningTick(Operation<Void> original) {
        original.call();

        LightningEntity thiz = ((LightningEntity) (Object) this);
        if (!thiz.getWorld().isClient()) {
            return;
        }


        if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.getY() > 20) {
            if (thiz.age == 1) {
                SPBRevampedClient.isLightning = true;
            }

            if (thiz.age >= 3) {
                SPBRevampedClient.isLightning = false;
            }
        } else {
            thiz.kill();
        }
    }

    @WrapMethod(method = "spawnFire")
    public void spbrevamped$LightingUpSkyDuringLightningSpawnFire(int spreadAttempts, Operation<Void> original) {
        LightningEntity thiz = ((LightningEntity) (Object) this);

        if (BackroomsLevels.isInBackroomsLevel(thiz.getWorld(), BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return;
        }

        original.call(spreadAttempts);
    }
}
