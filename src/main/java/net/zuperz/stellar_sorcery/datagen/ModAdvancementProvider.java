package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.world.item.Items;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.fluid.ModFluids;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zuperz.stellar_sorcery.util.ModTags;

import java.util.function.Consumer;

public class ModAdvancementProvider implements AdvancementProvider.AdvancementGenerator {

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
        AdvancementHolder rootAdvancement = Advancement.Builder.advancement()
                .display(
                        ModBlocks.CALENDULA,
                        Component.translatable("advancements.story.mine_stone.root.title"),
                        Component.translatable("advancements.story.mine_stone.root.description"),
                        ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/block/slate.png"),
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_flower", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_flower"), existingFileHelper);

        AdvancementHolder codexArcanum = Advancement.Builder.advancement()
                .parent(rootAdvancement)
                .display(
                        ModItems.CODEX_ARCANUM,
                        Component.translatable("advancements.codex_sorcery.codex_arcanum.title"),
                        Component.translatable("advancements.codex_sorcery.codex_arcanum.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_codex_arcanum", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CODEX_ARCANUM))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_codex_arcanum"), existingFileHelper);

        AdvancementHolder vitalStump = Advancement.Builder.advancement()
                .parent(rootAdvancement)
                .display(
                        ModBlocks.VITAL_STUMP,
                        Component.translatable("advancements.codex_sorcery.vital_stump.title"),
                        Component.translatable("advancements.codex_sorcery.vital_stump.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_vital_stump", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.VITAL_STUMP))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_vital_stump"), existingFileHelper);

        AdvancementHolder fritillariaMeleagrisSeeds = Advancement.Builder.advancement()
                .parent(vitalStump)
                .display(
                        ModItems.FRITILLARIA_MELEAGRIS_SEEDS,
                        Component.translatable("advancements.codex_sorcery.fritillaria_meleagris_seeds.title"),
                        Component.translatable("advancements.codex_sorcery.fritillaria_meleagris_seeds.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_fritillaria_meleagris_seeds", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FRITILLARIA_MELEAGRIS_SEEDS))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_fritillaria_meleagris_seeds"), existingFileHelper);

        AdvancementHolder moonshineCatalyst = Advancement.Builder.advancement()
                .parent(vitalStump)
                .display(
                        ModItems.MOONSHINE_CATALYST,
                        Component.translatable("advancements.codex_sorcery.moonshine_catalyst.title"),
                        Component.translatable("advancements.codex_sorcery.moonshine_catalyst.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_moonshine_catalyst", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOONSHINE_CATALYST))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_moonshine_catalyst"), existingFileHelper);

        AdvancementHolder clayJar = Advancement.Builder.advancement()
                .parent(rootAdvancement)
                .display(
                        ModItems.CLAY_JAR,
                        Component.translatable("advancements.codex_sorcery.clay_jar.title"),
                        Component.translatable("advancements.codex_sorcery.clay_jar.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_clay_jar", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CLAY_JAR))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_clay_jar"), existingFileHelper);

        AdvancementHolder fireClayJar = Advancement.Builder.advancement()
                .parent(clayJar)
                .display(
                        ModItems.FIRE_CLAY_JAR,
                        Component.translatable("advancements.codex_sorcery.fire_clay_jar.title"),
                        Component.translatable("advancements.codex_sorcery.fire_clay_jar.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_fire_clay_jar", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FIRE_CLAY_JAR))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_fire_clay_jar"), existingFileHelper);

        AdvancementHolder twigClayJar = Advancement.Builder.advancement()
                .parent(clayJar)
                .display(
                        ModItems.TWIG_CLAY_JAR,
                        Component.translatable("advancements.codex_sorcery.twig_clay_jar.title"),
                        Component.translatable("advancements.codex_sorcery.twig_clay_jar.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_twig_clay_jar", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.TWIG_CLAY_JAR))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_twig_clay_jar"), existingFileHelper);

        AdvancementHolder Moonshine = Advancement.Builder.advancement()
                .parent(moonshineCatalyst)
                .display(
                        ModBlocks.BUDDING_MOONSHINE,
                        Component.translatable("advancements.codex_sorcery.moonshine.title"),
                        Component.translatable("advancements.codex_sorcery.moonshine.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_moonshine", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOONSHINE_CATALYST))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_moonshine"), existingFileHelper);

        AdvancementHolder light_Infuser = Advancement.Builder.advancement()
                .parent(Moonshine)
                .display(
                        ModBlocks.LIGHT_INFUSER,
                        Component.translatable("advancements.codex_sorcery.light_infuser.title"),
                        Component.translatable("advancements.codex_sorcery.light_infuser.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_light_infuser", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIGHT_INFUSER))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_light_infuser"), existingFileHelper);

        AdvancementHolder light_beam_emitter = Advancement.Builder.advancement()
                .parent(light_Infuser)
                .display(
                        ModBlocks.LIGHT_BEAM_EMITTER,
                        Component.translatable("advancements.codex_sorcery.light_beam_emitter.title"),
                        Component.translatable("advancements.codex_sorcery.light_beam_emitter.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_light_beam_emitter", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIGHT_BEAM_EMITTER))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_light_beam_emitter"), existingFileHelper);

        AdvancementHolder itemEmitter = Advancement.Builder.advancement()
                .parent(light_beam_emitter)
                .display(
                        ModBlocks.ITEM_EMITTER,
                        Component.translatable("advancements.codex_sorcery.item_emitter.title"),
                        Component.translatable("advancements.codex_sorcery.item_emitter.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_item_emitter", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ITEM_EMITTER))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_item_emitter"), existingFileHelper);

        AdvancementHolder light_jar = Advancement.Builder.advancement()
                .parent(light_Infuser)
                .display(
                        ModBlocks.LIGHT_JAR,
                        Component.translatable("advancements.codex_sorcery.light_jar.title"),
                        Component.translatable("advancements.codex_sorcery.light_jar.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_light_jar", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIGHT_JAR))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_light_jar"), existingFileHelper);

        AdvancementHolder noctilume_bucket = Advancement.Builder.advancement()
                .parent(light_Infuser)
                .display(
                        ModFluids.NOCTILUME_BUCKET,
                        Component.translatable("advancements.codex_sorcery.noctilume_bucket.title"),
                        Component.translatable("advancements.codex_sorcery.noctilume_bucket.description"),
                        null,
                        AdvancementType.TASK,
                        true, //ShowToast
                        true, // AnnounceChat
                        false // Hidden
                )
                .addCriterion("get_noctilume_bucket", InventoryChangeTrigger.TriggerInstance.hasItems(ModFluids.NOCTILUME_BUCKET))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_noctilume_bucket"), existingFileHelper);

        AdvancementHolder soulCandle = Advancement.Builder.advancement()
                .parent(light_jar)
                .display(
                        ModBlocks.SOUL_CANDLE,
                        Component.translatable("advancements.codex_sorcery.soul_candle.title"),
                        Component.translatable("advancements.codex_sorcery.soul_candle.description"),
                        null,
                        AdvancementType.TASK,
                        true, true, false
                )
                .addCriterion("get_soul_candle", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.SOUL_CANDLE))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_soul_candle"), existingFileHelper);

        AdvancementHolder chalkCanister = Advancement.Builder.advancement()
                .parent(light_jar)
                .display(
                        ModItems.CHALK_CANISTER,
                        Component.translatable("advancements.codex_sorcery.chalk_canister.title"),
                        Component.translatable("advancements.codex_sorcery.chalk_canister.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        true
                )
                .addCriterion("get_schalk_canister", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CHALK_CANISTER))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_schalk_canister"), existingFileHelper);

        AdvancementHolder moonshineShard = Advancement.Builder.advancement()
                .parent(soulCandle)
                .display(
                        ModItems.MOONSHINE_SHARD,
                        Component.translatable("advancements.codex_sorcery.moonshine_shard.title"),
                        Component.translatable("advancements.codex_sorcery.moonshine_shard.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_moonshine_shard", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.MOONSHINE_SHARD))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_moonshine_shard"), existingFileHelper);

        AdvancementHolder whisperingFragment = Advancement.Builder.advancement()
                .parent(moonshineShard)
                .display(
                        ModItems.WHISPERING_FRAGMENT,
                        Component.translatable("advancements.codex_sorcery.whispering_fragment.title"),
                        Component.translatable("advancements.codex_sorcery.whispering_fragment.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_whispering_fragment", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.WHISPERING_FRAGMENT))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_whispering_fragment"), existingFileHelper);

        AdvancementHolder bluestoneDust = Advancement.Builder.advancement()
                .parent(moonshineShard)
                .display(
                        ModItems.BLUESTONE_DUST,
                        Component.translatable("advancements.codex_sorcery.bluestone_dust.title"),
                        Component.translatable("advancements.codex_sorcery.bluestone_dust.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_bluestone_dust", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.BLUESTONE_DUST))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_bluestone_dust"), existingFileHelper);

        AdvancementHolder lunarJar = Advancement.Builder.advancement()
                .parent(bluestoneDust)
                .display(
                        ModBlocks.LUNAR_JAR,
                        Component.translatable("advancements.codex_sorcery.lunar_jar.title"),
                        Component.translatable("advancements.codex_sorcery.lunar_jar.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_lunar_jar", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LUNAR_JAR))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_lunar_jar"), existingFileHelper);

        AdvancementHolder lunarInfuser = Advancement.Builder.advancement()
                .parent(lunarJar)
                .display(
                        ModBlocks.LUNAR_INFUSER,
                        Component.translatable("advancements.codex_sorcery.lunar_infuser.title"),
                        Component.translatable("advancements.codex_sorcery.lunar_infuser.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_lunar_infuser", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LUNAR_INFUSER))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_lunar_infuser"), existingFileHelper);

        AdvancementHolder lunarLightBeamEmitter = Advancement.Builder.advancement()
                .parent(lunarInfuser)
                .display(
                        ModBlocks.LUNAR_LIGHT_BEAM_EMITTER,
                        Component.translatable("advancements.codex_sorcery.lunar_light_beam_emitter.title"),
                        Component.translatable("advancements.codex_sorcery.lunar_light_beam_emitter.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_lunar_light_beam_emitter", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LUNAR_LIGHT_BEAM_EMITTER))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_lunar_light_beam_emitter"), existingFileHelper);

        AdvancementHolder astralAltar = Advancement.Builder.advancement()
                .parent(whisperingFragment)
                .display(
                        ModBlocks.ASTRAL_ALTAR,
                        Component.translatable("advancements.codex_sorcery.astral_altar.title"),
                        Component.translatable("advancements.codex_sorcery.astral_altar.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("get_astral_altar", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ASTRAL_ALTAR))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_astral_altar"), existingFileHelper);

        AdvancementHolder astralNexus = Advancement.Builder.advancement()
                .parent(astralAltar)
                .display(
                        ModBlocks.ASTRAL_NEXUS,
                        Component.translatable("advancements.codex_sorcery.astral_nexus.title"),
                        Component.translatable("advancements.codex_sorcery.astral_nexus.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("get_astral_nexus", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ASTRAL_NEXUS))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_astral_nexus"), existingFileHelper);

        AdvancementHolder arcforge = Advancement.Builder.advancement()
                .parent(astralNexus)
                .display(
                        ModBlocks.ARCFORGE,
                        Component.translatable("advancements.codex_sorcery.arcforge.title"),
                        Component.translatable("advancements.codex_sorcery.arcforge.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("get_arcforge", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ARCFORGE))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_arcforge"), existingFileHelper);

        AdvancementHolder augmentForge = Advancement.Builder.advancement()
                .parent(arcforge)
                .display(
                        ModBlocks.AUGMENT_FORGE,
                        Component.translatable("advancements.codex_sorcery.augment_forge.title"),
                        Component.translatable("advancements.codex_sorcery.augment_forge.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("get_augment_forge", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.AUGMENT_FORGE))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_augment_forge"), existingFileHelper);

        AdvancementHolder essenceBoiler = Advancement.Builder.advancement()
                .parent(rootAdvancement)
                .display(
                        ModBlocks.ESSENCE_BOILER,
                        Component.translatable("advancements.codex_sorcery.essence_boiler.title"),
                        Component.translatable("advancements.codex_sorcery.essence_boiler.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_essence_boiler", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.ESSENCE_BOILER))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_essence_boiler"), existingFileHelper);

        AdvancementHolder essenceBottle = Advancement.Builder.advancement()
                .parent(essenceBoiler)
                .display(
                        ModItems.ESSENCE_BOTTLE,
                        Component.translatable("advancements.codex_sorcery.essence_bottle.title"),
                        Component.translatable("advancements.codex_sorcery.essence_bottle.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_essence_bottle", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ESSENCE_BOTTLE))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_essence_bottle"), existingFileHelper);

        AdvancementHolder essenceAmulet = Advancement.Builder.advancement()
                .parent(essenceBoiler)
                .display(
                        ModItems.ESSENCE_AMULET,
                        Component.translatable("advancements.codex_sorcery.essence_amulet.title"),
                        Component.translatable("advancements.codex_sorcery.essence_amulet.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("get_essence_amulet", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ESSENCE_AMULET))
                .save(saver, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "get_essence_amulet"), existingFileHelper);
    }
}
