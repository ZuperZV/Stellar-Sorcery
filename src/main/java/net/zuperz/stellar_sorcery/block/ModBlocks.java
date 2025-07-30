package net.zuperz.stellar_sorcery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.custom.*;
import net.zuperz.stellar_sorcery.block.entity.custom.StumpBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.VitalStumpBlockEntity;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(StellarSorcery.MOD_ID);

    public static final DeferredBlock<Block> ASTRAL_ALTAR = registerBlock("astral_altar",
            () -> new AstralAltarBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE_TILES).noOcclusion()));

    public static final DeferredBlock<Block> ASTRAL_NEXUS = registerBlock("astral_nexus",
            () -> new AstralNexusBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
                    .sound(SoundType.TUFF_BRICKS).noOcclusion()));

    public static final DeferredBlock<Block> VITAL_STUMP = registerBlock("vital_stump",
            () -> new VitalStumpBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE_TILES).noOcclusion()));

    public static final DeferredBlock<Block> STUMP = registerBlock("stump",
            () -> new StumpBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
                    .sound(SoundType.TUFF_BRICKS).noOcclusion()));

    public static final DeferredBlock<Block> ESSENCE_BOILER = registerBlock("essence_boiler",
            () -> new EssenceBoilerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAMPFIRE).requiresCorrectToolForDrops().strength(5.0F, 6.0F)
                    .sound(SoundType.DEEPSLATE_TILES).noOcclusion().lightLevel(litBlockEmission(15)).ignitedByLava()));


    public static final DeferredBlock<Block> ARCFORGE = registerBlock("arcforge",
            () -> new ArcForgeBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
                    .sound(SoundType.TUFF_BRICKS).noOcclusion()));

    public static final DeferredBlock<Block> STAR_LIGHT = registerBlock("star_light",
            () -> new StarLightBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5.0F, 6.0F)
                    .sound(new SoundType(1.0F, 1.0F, SoundEvents.ALLAY_DEATH, SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundEvents.ALLAY_ITEM_GIVEN, SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundEvents.ALLAY_THROW)).noOcclusion()));

    public static final DeferredBlock<Block> FRITILLARIA_MELEAGRIS_CROP = BLOCKS.register("fritillaria_meleagris_crop",
            () -> new FritillariaMeleagrisCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));

    public static final DeferredBlock<Block> RED_CAMPION = registerBlock("red_campion",
            () -> new FlowerBlock(MobEffects.HARM, 2, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
    public static final DeferredBlock<Block> POTTED_RED_CAMPION = BLOCKS.register("potted_red_campion",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), RED_CAMPION, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)));

    public static final DeferredBlock<Block> CALENDULA = registerBlock("calendula",
            () -> new FlowerBlock(MobEffects.LUCK, 8, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
    public static final DeferredBlock<Block> POTTED_CALENDULA = BLOCKS.register("potted_calendula",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), CALENDULA, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)));

    public static final DeferredBlock<Block> NIGELLA_DAMASCENA = registerBlock("nigella_damascena",
            () -> new FlowerBlock(MobEffects.DARKNESS, 8, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY)));
    public static final DeferredBlock<Block> POTTED_NIGELLA_DAMASCENA = BLOCKS.register("potted_nigella_damascena",
            () -> new FlowerPotBlock(() -> ((FlowerPotBlock) Blocks.FLOWER_POT), NIGELLA_DAMASCENA, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY)));



    private static boolean always(BlockState p_50775_, BlockGetter p_50776_, BlockPos p_50777_) {
        return true;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static ToIntFunction<BlockState> litBlockEmission(int p_50760_) {
        return p_50763_ -> p_50763_.getValue(BlockStateProperties.LIT) ? p_50760_ : 0;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}