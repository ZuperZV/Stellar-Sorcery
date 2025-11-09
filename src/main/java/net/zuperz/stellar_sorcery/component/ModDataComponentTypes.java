package net.zuperz.stellar_sorcery.component;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.util.function.UnaryOperator;

public class ModDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(StellarSorcery.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> COORDINATES = register("coordinates",
            builder -> builder.persistent(BlockPos.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StarDustData>> STAR_DUST = register("star_dust",
            builder -> builder.persistent(StarDustData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CodexTierData>> CODEX_TIER = register("codex_tier",
            builder -> builder.persistent(CodexTierData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CelestialData>> CELESTIAL = register("celestial",
            builder -> builder.persistent(CelestialData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EssenceBottleData>> ESSENCE_BOTTLE = register("essence_bottle",
            builder -> builder.persistent(EssenceBottleData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlayerData>> PLAYER_DATA = register("player_data",
            builder -> builder.persistent(PlayerData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockStorageData>> BLOCK_STORAGE_DATA = register("block_storage_data",
            builder -> builder.persistent(BlockStorageData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ActiveData>> ACTIVE = register("active",
            builder -> builder.persistent(ActiveData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SigilData>> SIGIL = register("sigil",
            builder -> builder.persistent(SigilData.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SigilNameData>> SIGIL_NAME = register("sigil_name",
            builder -> builder.persistent(SigilNameData.CODEC));


    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}