package net.zuperz.stellar_sorcery.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.entity.custom.SigilOrbEntity;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, StellarSorcery.MOD_ID);

    public static final Supplier<EntityType<SigilOrbEntity>> SIGIL_ORB =
            ENTITY_TYPES.register("sigil_orb", () -> EntityType.Builder.of(SigilOrbEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f).build("sigil_orb"));


    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}