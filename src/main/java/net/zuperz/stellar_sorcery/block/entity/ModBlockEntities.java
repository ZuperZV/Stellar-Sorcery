package net.zuperz.stellar_sorcery.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.entity.custom.ArcForgeBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, StellarSorcery.MOD_ID);

    public static final Supplier<BlockEntityType<AstralAltarBlockEntity>> ASTRAL_ALTAR_BE =
            BLOCK_ENTITIES.register("astral_altar_be", () -> BlockEntityType.Builder.of(
                    AstralAltarBlockEntity::new, ModBlocks.ASTRAL_ALTAR.get()).build(null));

    public static final Supplier<BlockEntityType<AstralNexusBlockEntity>> ASTRAL_NEXUS_BE =
            BLOCK_ENTITIES.register("astral_nexus_be", () -> BlockEntityType.Builder.of(
                    AstralNexusBlockEntity::new, ModBlocks.ASTRAL_NEXUS.get()).build(null));

    public static final Supplier<BlockEntityType<ArcForgeBlockEntity>> ARCFORGE_BE =
            BLOCK_ENTITIES.register("arcforge_be", () -> BlockEntityType.Builder.of(
                    ArcForgeBlockEntity::new, ModBlocks.ARCFORGE.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}