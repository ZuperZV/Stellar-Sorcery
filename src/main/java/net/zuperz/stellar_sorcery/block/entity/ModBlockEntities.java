package net.zuperz.stellar_sorcery.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.entity.custom.*;

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

    public static final Supplier<BlockEntityType<VitalStumpBlockEntity>> VITAL_STUMP_BE =
            BLOCK_ENTITIES.register("vital_stump_be", () -> BlockEntityType.Builder.of(
                    VitalStumpBlockEntity::new, ModBlocks.VITAL_STUMP.get()).build(null));

    public static final Supplier<BlockEntityType<StumpBlockEntity>> STUMP_BE =
            BLOCK_ENTITIES.register("stump_be", () -> BlockEntityType.Builder.of(
                    StumpBlockEntity::new, ModBlocks.STUMP.get()).build(null));

    public static final Supplier<BlockEntityType<SoulCandleBlockEntity>> ALTER_BE =
            BLOCK_ENTITIES.register("alter_be", () -> BlockEntityType.Builder.of(
                    SoulCandleBlockEntity::new, ModBlocks.SOUL_CANDLE.get()).build(null));

    public static final Supplier<BlockEntityType<EssenceBoilerBlockEntity>> ESSENCE_BOILER_BE =
            BLOCK_ENTITIES.register("essence_boiler_be", () -> BlockEntityType.Builder.of(
                    EssenceBoilerBlockEntity::new, ModBlocks.ESSENCE_BOILER.get()).build(null));

    public static final Supplier<BlockEntityType<ArcForgeBlockEntity>> ARCFORGE_BE =
            BLOCK_ENTITIES.register("arcforge_be", () -> BlockEntityType.Builder.of(
                    ArcForgeBlockEntity::new, ModBlocks.ARCFORGE.get()).build(null));

    public static final Supplier<BlockEntityType<LunarInfuserBlockEntity>> LUNAR_INFUSER_BE =
            BLOCK_ENTITIES.register("lunar_infuser_be", () -> BlockEntityType.Builder.of(
                    LunarInfuserBlockEntity::new, ModBlocks.LUNAR_INFUSER.get()).build(null));

    public static final Supplier<BlockEntityType<LunarInfuserBlockEntity>> LIGHT_INFUSER_BE =
            BLOCK_ENTITIES.register("light_infuser_be", () -> BlockEntityType.Builder.of(
                    LunarInfuserBlockEntity::new, ModBlocks.LIGHT_INFUSER.get()).build(null));

    public static final Supplier<BlockEntityType<LunarJarBlockEntity>> LUNAR_JAR_BE =
            BLOCK_ENTITIES.register("lunar_jar_be", () -> BlockEntityType.Builder.of(
                    LunarJarBlockEntity::new, ModBlocks.LUNAR_JAR.get()).build(null));

    public static final Supplier<BlockEntityType<LunarJarBlockEntity>> LIGHT_JAR_BE =
            BLOCK_ENTITIES.register("light_jar_be", () -> BlockEntityType.Builder.of(
                    LunarJarBlockEntity::new, ModBlocks.LIGHT_JAR.get()).build(null));

    public static final Supplier<BlockEntityType<LightBeamEmitterBlockEntity>> LIGHT_BEAM_EMITTER_BE =
            BLOCK_ENTITIES.register("light_beam_emitter_be", () -> BlockEntityType.Builder.of(
                    LightBeamEmitterBlockEntity::new, ModBlocks.LIGHT_BEAM_EMITTER.get()).build(null));

    public static final Supplier<BlockEntityType<LightBeamEmitterBlockEntity>> LUNAR_LIGHT_BEAM_EMITTER_BE =
            BLOCK_ENTITIES.register("lunar_light_beam_emitter_be", () -> BlockEntityType.Builder.of(
                    LightBeamEmitterBlockEntity::new, ModBlocks.LUNAR_LIGHT_BEAM_EMITTER.get()).build(null));

    public static final Supplier<BlockEntityType<AugmentForgeBlockEntity>> AUGMENT_FORGE_BE =
            BLOCK_ENTITIES.register("augment_forge_be", () -> BlockEntityType.Builder.of(
                    AugmentForgeBlockEntity::new, ModBlocks.AUGMENT_FORGE.get()).build(null));



    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}