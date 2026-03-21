package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseSpawner.class)
public interface BaseSpawnerAccessor {
    @Accessor("spin")
    double getSpin();

    @Accessor("spin")
    void setSpin(double spin);

    @Accessor("oSpin")
    double getOSpin();

    @Accessor("oSpin")
    void setOSpin(double oSpin);

    @Accessor("displayEntity")
    Entity getDisplayEntity();

    @Accessor("spawnDelay")
    int getSpawnDelay();

    @Accessor("spawnDelay")
    void setSpawnDelay(int delay);

    @Invoker("isNearPlayer")
    boolean callIsNearPlayer(Level level, BlockPos pos);
}