package net.zuperz.stellar_sorcery.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModCodexProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public ModCodexProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "codex_entries");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> writes = new ArrayList<>();

        for (GeneratedEntry entry : buildEntries()) {
            Path path = this.pathProvider.json(ResourceLocation.fromNamespaceAndPath(
                    StellarSorcery.MOD_ID,
                    entry.categoryFolder + "/tier_" + entry.tier + "/" + entry.fileName
            ));
            writes.add(DataProvider.saveStable(cachedOutput, entry.json, path));
        }

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Stellar Sorcery Codex Entries";
    }

    private List<GeneratedEntry> buildEntries() {
        List<GeneratedEntry> entries = new ArrayList<>();

        String codex = "codex_stellar_sorcery-codex_arcanum";
        String flora = "flora_stellar_sorcery-fritillaria_meleagris";
        String rituals = "rituals_stellar_sorcery-soul_candle";
        String lunar = "lunar_stellar_sorcery-moonshine_catalyst";
        String astral = "astral_stellar_sorcery-astral_altar";

        entries.add(entry(codex, 1, 1, "codex_arcanum", "stellar_sorcery:codex_arcanum",
                List.of("stellar_sorcery:codex_arcanum", "codex", "wiki", "book", "guide"),
                List.of("stump", "light_infuser", "soul_candle"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.codex_arcanum.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.codex_arcanum.text.2")
                ),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.codex_arcanum.text.3")
                )));

        entries.add(entry(codex, 1, 2, "stump", "stellar_sorcery:stump",
                List.of("stellar_sorcery:stump", "stump", "ritual", "garden", "altar"),
                List.of("vital_stump", "fritillaria_meleagris_seeds", "root"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.stump.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.stump.text.2")
                )));

        entries.add(entry(codex, 1, 3, "vital_stump", "stellar_sorcery:vital_stump",
                List.of("stellar_sorcery:vital_stump", "vital stump", "stump", "ritual", "flora"),
                List.of("stump", "moonshine_catalyst", "calendula"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.vital_stump.text.1")
                ),
                page(
                        recipe(
                                List.of("DCD", "BAB"),
                                Map.of(
                                        "A", "stellar_sorcery:stump",
                                        "B", "stellar_sorcery:calendula",
                                        "C", "stellar_sorcery:red_campion",
                                        "D", "minecraft:gold_ingot"
                                ),
                                "stellar_sorcery:vital_stump"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.vital_stump.text.2")
                )));

        entries.add(entry(codex, 1, 4, "clay_jar", "stellar_sorcery:clay_jar",
                List.of("stellar_sorcery:clay_jar", "clay jar", "jar", "smelt", "vessel"),
                List.of("light_jar", "light_infuser"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.clay_jar.text.1")
                ),
                page(
                        furnace("stellar_sorcery:soft_clay_jar", "stellar_sorcery:clay_jar", 0.15F, 200),
                        text("codex_arcanum.stellar_sorcery.entry.clay_jar.text.2")
                )));

        entries.add(entry(codex, 1, 5, "light_jar", "stellar_sorcery:light_jar",
                List.of("stellar_sorcery:light_jar", "light jar", "jar", "noctilume", "storage"),
                List.of("light_infuser", "light_beam_emitter", "noctilume_bucket"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.light_jar.text.1")
                ),
                page(
                        recipe(
                                List.of(" A ", "CBC", "ADA"),
                                Map.of(
                                        "A", "minecraft:oak_log",
                                        "B", "minecraft:glass",
                                        "C", "stellar_sorcery:calendula",
                                        "D", "minecraft:spider_eye"
                                ),
                                "stellar_sorcery:light_jar"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.light_jar.text.2")
                )));

        entries.add(entry(codex, 1, 6, "light_infuser", "stellar_sorcery:light_infuser",
                List.of("stellar_sorcery:light_infuser", "light infuser", "machine", "noctilume", "infuse"),
                List.of("light_jar", "light_beam_emitter", "moonshine_catalyst"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.light_infuser.text.1")
                ),
                page(
                        recipe(
                                List.of(" B ", "CEC", "ADA"),
                                Map.of(
                                        "A", "minecraft:oak_log",
                                        "B", "stellar_sorcery:wind_clay_jar",
                                        "C", "stellar_sorcery:calendula",
                                        "D", "stellar_sorcery:nigella_damascena",
                                        "E", "minecraft:spider_eye"
                                ),
                                "stellar_sorcery:light_infuser"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.light_infuser.text.2")
                )));

        entries.add(entry(codex, 1, 7, "light_beam_emitter", "stellar_sorcery:light_beam_emitter",
                List.of("stellar_sorcery:light_beam_emitter", "beam emitter", "pipe", "transfer", "noctilume"),
                List.of("light_jar", "light_infuser", "item_emitter"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.light_beam_emitter.text.1")
                ),
                page(
                        recipe(
                                List.of(" A ", " D ", "BCB"),
                                Map.of(
                                        "A", "minecraft:spider_eye",
                                        "B", "minecraft:amethyst_shard",
                                        "C", "minecraft:oak_log",
                                        "D", "minecraft:bucket"
                                ),
                                "stellar_sorcery:light_beam_emitter"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.light_beam_emitter.text.2")
                )));

        entries.add(entry(codex, 2, 8, "item_emitter", "stellar_sorcery:item_emitter",
                List.of("stellar_sorcery:item_emitter", "item emitter", "automation", "beam", "transport"),
                List.of("light_beam_emitter", "astral_altar"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.item_emitter.text.1")
                ),
                page(
                        recipe(
                                List.of(" A ", "BCB"),
                                Map.of(
                                        "A", "minecraft:spider_eye",
                                        "B", "minecraft:amethyst_shard",
                                        "C", "minecraft:oak_log"
                                ),
                                "stellar_sorcery:item_emitter"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.item_emitter.text.2")
                )));

        entries.add(entry(codex, 1, 9, "essence_boiler", "stellar_sorcery:essence_boiler",
                List.of("stellar_sorcery:essence_boiler", "essence boiler", "infusion", "brew", "alchemy"),
                List.of("root", "moonshine_catalyst", "whispering_fragment"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.essence_boiler.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.essence_boiler.text.2")
                ),
                page(
                        recipe(
                                List.of("A A", "AAA", "ABA"),
                                Map.of(
                                        "A", "minecraft:iron_ingot",
                                        "B", "minecraft:campfire"
                                ),
                                "stellar_sorcery:essence_boiler"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.essence_boiler.text.3")
                )));

        entries.add(entry(flora, 1, 1, "fritillaria_meleagris_seeds", "stellar_sorcery:fritillaria_meleagris_seeds",
                List.of("stellar_sorcery:fritillaria_meleagris_seeds", "fritillaria", "seeds", "crop", "flower"),
                List.of("fritillaria_meleagris", "vital_stump"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.fritillaria_meleagris_seeds.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.fritillaria_meleagris_seeds.text.2")
                )));

        entries.add(entry(flora, 1, 2, "fritillaria_meleagris", "stellar_sorcery:fritillaria_meleagris",
                List.of("stellar_sorcery:fritillaria_meleagris", "fritillaria", "flower", "nature", "essence"),
                List.of("fritillaria_meleagris_seeds", "calendula", "red_campion"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.fritillaria_meleagris.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.fritillaria_meleagris.text.2")
                )));

        entries.add(entry(flora, 1, 3, "calendula", "stellar_sorcery:calendula",
                List.of("stellar_sorcery:calendula", "calendula", "flower", "luck", "day"),
                List.of("fritillaria_meleagris", "root"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.calendula.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.calendula.text.2")
                )));

        entries.add(entry(flora, 1, 4, "red_campion", "stellar_sorcery:red_campion",
                List.of("stellar_sorcery:red_campion", "red campion", "flower", "blood", "stump"),
                List.of("root", "vital_stump", "blood_vial"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.red_campion.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.red_campion.text.2")
                )));

        entries.add(entry(flora, 1, 5, "nigella_damascena", "stellar_sorcery:nigella_damascena",
                List.of("stellar_sorcery:nigella_damascena", "nigella", "night bloom", "flower", "shadow"),
                List.of("moonshine_catalyst", "light_infuser"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.nigella_damascena.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.nigella_damascena.text.2")
                )));

        entries.add(entry(flora, 1, 6, "soul_bloom_seeds", "stellar_sorcery:soul_bloom_seeds",
                List.of("stellar_sorcery:soul_bloom_seeds", "soul bloom", "seeds", "ritual", "crop"),
                List.of("soul_blooms", "soul_candle"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.soul_bloom_seeds.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.soul_bloom_seeds.text.2")
                )));

        entries.add(entry(flora, 1, 7, "soul_blooms", "stellar_sorcery:soul_blooms",
                List.of("stellar_sorcery:soul_blooms", "soul blooms", "ritual flower", "soul", "dark"),
                List.of("soul_bloom_seeds", "soul_candle", "whispering_fragment"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.soul_blooms.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.soul_blooms.text.2")
                )));

        entries.add(entry(rituals, 1, 1, "soul_candle", "stellar_sorcery:soul_candle",
                List.of("stellar_sorcery:soul_candle", "soul candle", "ritual", "chalk", "night"),
                List.of("white_chalk_stick", "blood_vial", "moonshine_shard"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.soul_candle.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.soul_candle.text.2")
                ),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.soul_candle.text.3")
                )));

        entries.add(entry(rituals, 1, 2, "white_chalk_stick", "stellar_sorcery:white_chalk_stick",
                List.of("stellar_sorcery:white_chalk_stick", "chalk", "white chalk", "ritual", "draw"),
                List.of("soul_candle", "chalk_canister"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.white_chalk_stick.text.1")
                ),
                page(
                        recipe(
                                List.of(" B ", "DAD", "CAC"),
                                Map.of(
                                        "A", "minecraft:calcite",
                                        "B", "stellar_sorcery:extracter_clay_jar",
                                        "C", "stellar_sorcery:twig_clay_jar",
                                        "D", "minecraft:bone"
                                ),
                                "stellar_sorcery:white_chalk_stick"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.white_chalk_stick.text.2")
                )));

        entries.add(entry(rituals, 1, 3, "chalk_canister", "stellar_sorcery:chalk_canister",
                List.of("stellar_sorcery:chalk_canister", "chalk canister", "circle", "ritual", "chalk"),
                List.of("white_chalk_stick", "soul_candle"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.chalk_canister.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.chalk_canister.text.2")
                )));

        entries.add(entry(rituals, 1, 4, "ritual_dagger", "stellar_sorcery:ritual_dagger",
                List.of("stellar_sorcery:ritual_dagger", "dagger", "blood", "ritual", "knife"),
                List.of("blood_vial", "soul_candle"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.ritual_dagger.text.1")
                ),
                page(
                        recipe(
                                List.of("A", "B", "C"),
                                Map.of(
                                        "A", "minecraft:iron_ingot",
                                        "B", "stellar_sorcery:blood_vial",
                                        "C", "minecraft:stick"
                                ),
                                "stellar_sorcery:ritual_dagger"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.ritual_dagger.text.2")
                )));

        entries.add(entry(rituals, 1, 5, "blood_vial", "stellar_sorcery:blood_vial",
                List.of("stellar_sorcery:blood_vial", "blood vial", "ritual", "blood", "self"),
                List.of("ritual_dagger", "voodoo_doll", "soul_candle"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.blood_vial.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.blood_vial.text.2")
                )));

        entries.add(entry(rituals, 1, 6, "voodoo_doll", "stellar_sorcery:voodoo_doll",
                List.of("stellar_sorcery:voodoo_doll", "voodoo doll", "effigy", "curse", "ritual"),
                List.of("blood_vial", "soul_candle"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.voodoo_doll.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.voodoo_doll.text.2")
                )));

        entries.add(entry(lunar, 1, 1, "root", "stellar_sorcery:root",
                List.of("stellar_sorcery:root", "root", "reagent", "wood", "stump"),
                List.of("moonshine_catalyst", "twig_clay_jar", "red_campion"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.root.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.root.text.2")
                )));

        entries.add(entry(lunar, 1, 2, "moonshine_catalyst", "stellar_sorcery:moonshine_catalyst",
                List.of("stellar_sorcery:moonshine_catalyst", "moonshine catalyst", "amethyst", "moon", "crystal"),
                List.of("budding_moonshine", "moonshine_shard", "root"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.moonshine_catalyst.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.moonshine_catalyst.text.2")
                )));

        entries.add(entry(lunar, 1, 3, "budding_moonshine", "stellar_sorcery:budding_moonshine",
                List.of("stellar_sorcery:budding_moonshine", "budding moonshine", "amethyst", "moonshine", "cluster"),
                List.of("moonshine_catalyst", "moonshine_shard", "whispering_fragment"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.budding_moonshine.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.budding_moonshine.text.2")
                )));

        entries.add(entry(lunar, 1, 4, "noctilume_bucket", "stellar_sorcery:noctilume_bucket",
                List.of("stellar_sorcery:noctilume_bucket", "noctilume", "bucket", "fluid", "light"),
                List.of("light_infuser", "light_jar", "lunar_infuser"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.noctilume_bucket.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.noctilume_bucket.text.2")
                )));

        entries.add(entry(lunar, 2, 5, "moonshine_shard", "stellar_sorcery:moonshine_shard",
                List.of("stellar_sorcery:moonshine_shard", "moonshine shard", "crystal", "ritual", "lunar"),
                List.of("budding_moonshine", "bluestone_dust", "whispering_fragment"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.moonshine_shard.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.moonshine_shard.text.2")
                )));

        entries.add(entry(lunar, 2, 6, "bluestone_dust", "stellar_sorcery:bluestone_dust",
                List.of("stellar_sorcery:bluestone_dust", "bluestone dust", "powder", "lunar", "upgrade"),
                List.of("lunar_jar", "lunar_infuser", "arcforge"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.bluestone_dust.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.bluestone_dust.text.2")
                )));

        entries.add(entry(lunar, 2, 7, "lunar_jar", "stellar_sorcery:lunar_jar",
                List.of("stellar_sorcery:lunar_jar", "lunar jar", "jar", "noctilume", "storage"),
                List.of("lunar_infuser", "lunar_light_beam_emitter"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.lunar_jar.text.1")
                ),
                page(
                        recipe(
                                List.of("ABA", "CDC", "AEA"),
                                Map.of(
                                        "A", "minecraft:amethyst_shard",
                                        "B", "stellar_sorcery:moonshine_shard",
                                        "C", "minecraft:copper_ingot",
                                        "D", "stellar_sorcery:light_jar",
                                        "E", "stellar_sorcery:bluestone_dust"
                                ),
                                "stellar_sorcery:lunar_jar"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.lunar_jar.text.2")
                )));

        entries.add(entry(lunar, 2, 8, "lunar_infuser", "stellar_sorcery:lunar_infuser",
                List.of("stellar_sorcery:lunar_infuser", "lunar infuser", "machine", "moon", "noctilume"),
                List.of("lunar_jar", "lunar_light_beam_emitter", "astral_altar"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.lunar_infuser.text.1")
                ),
                page(
                        recipe(
                                List.of("ABA", "CDC", "AEA"),
                                Map.of(
                                        "A", "minecraft:amethyst_shard",
                                        "B", "stellar_sorcery:moonshine_shard",
                                        "C", "minecraft:copper_ingot",
                                        "D", "stellar_sorcery:light_infuser",
                                        "E", "stellar_sorcery:lunar_jar"
                                ),
                                "stellar_sorcery:lunar_infuser"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.lunar_infuser.text.2")
                )));

        entries.add(entry(lunar, 2, 9, "lunar_light_beam_emitter", "stellar_sorcery:lunar_light_beam_emitter",
                List.of("stellar_sorcery:lunar_light_beam_emitter", "lunar beam emitter", "beam", "pipe", "moon"),
                List.of("lunar_infuser", "astral_altar"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.lunar_light_beam_emitter.text.1")
                ),
                page(
                        recipe(
                                List.of("ABA", "CDC", "AEA"),
                                Map.of(
                                        "A", "minecraft:amethyst_shard",
                                        "B", "stellar_sorcery:moonshine_shard",
                                        "C", "minecraft:copper_ingot",
                                        "D", "stellar_sorcery:light_beam_emitter",
                                        "E", "stellar_sorcery:lunar_jar"
                                ),
                                "stellar_sorcery:lunar_light_beam_emitter"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.lunar_light_beam_emitter.text.2")
                )));

        entries.add(entry(lunar, 2, 10, "whispering_fragment", "stellar_sorcery:whispering_fragment",
                List.of("stellar_sorcery:whispering_fragment", "whispering fragment", "astral", "shard", "ritual"),
                List.of("astral_altar", "budding_moonshine", "moonshine_shard"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.whispering_fragment.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.whispering_fragment.text.2")
                )));

        entries.add(entry(astral, 2, 1, "astral_altar", "stellar_sorcery:astral_altar",
                List.of("stellar_sorcery:astral_altar", "astral altar", "altar", "astral", "crafting"),
                List.of("astral_nexus", "lunar_infuser", "whispering_fragment"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.astral_altar.text.1")
                ),
                page(
                        recipe(
                                List.of("ABA", "CDC", "EFE"),
                                Map.of(
                                        "A", "minecraft:polished_deepslate",
                                        "B", "stellar_sorcery:whispering_fragment",
                                        "C", "stellar_sorcery:lunar_jar",
                                        "D", "stellar_sorcery:vital_stump",
                                        "E", "stellar_sorcery:soul_candle",
                                        "F", "stellar_sorcery:lunar_infuser"
                                ),
                                "stellar_sorcery:astral_altar"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.astral_altar.text.2")
                )));

        entries.add(entry(astral, 2, 2, "astral_nexus", "stellar_sorcery:astral_nexus",
                List.of("stellar_sorcery:astral_nexus", "astral nexus", "nexus", "astral", "automation"),
                List.of("astral_altar", "arcforge"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.astral_nexus.text.1")
                ),
                page(
                        recipe(
                                List.of("ABA", "CDC", "AEA"),
                                Map.of(
                                        "A", "minecraft:echo_shard",
                                        "B", "stellar_sorcery:whispering_fragment",
                                        "C", "minecraft:ender_eye",
                                        "D", "stellar_sorcery:astral_altar",
                                        "E", "stellar_sorcery:moonshine_shard"
                                ),
                                "stellar_sorcery:astral_nexus"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.astral_nexus.text.2")
                )));

        entries.add(entry(astral, 3, 3, "arcforge", "stellar_sorcery:arcforge",
                List.of("stellar_sorcery:arcforge", "arcforge", "forge", "astral", "late game"),
                List.of("augment_forge", "celestial_blade"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.arcforge.text.1")
                ),
                page(
                        recipe(
                                List.of("ABA", "CDC", "EFE"),
                                Map.of(
                                        "A", "minecraft:netherite_ingot",
                                        "B", "stellar_sorcery:moonshine_shard",
                                        "C", "stellar_sorcery:lunar_light_beam_emitter",
                                        "D", "stellar_sorcery:astral_altar",
                                        "E", "minecraft:blaze_rod",
                                        "F", "stellar_sorcery:bluestone_dust"
                                ),
                                "stellar_sorcery:arcforge"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.arcforge.text.2")
                )));

        entries.add(entry(astral, 3, 4, "augment_forge", "stellar_sorcery:augment_forge",
                List.of("stellar_sorcery:augment_forge", "augment forge", "forge", "upgrade", "astral"),
                List.of("arcforge", "smart_upgrade_template"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.augment_forge.text.1")
                ),
                page(
                        recipe(
                                List.of("ABA", "CDC", "AEA"),
                                Map.of(
                                        "A", "minecraft:obsidian",
                                        "B", "stellar_sorcery:essence_amulet",
                                        "C", "stellar_sorcery:bluestone_dust",
                                        "D", "stellar_sorcery:arcforge",
                                        "E", "stellar_sorcery:lunar_infuser"
                                ),
                                "stellar_sorcery:augment_forge"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.augment_forge.text.2")
                )));

        entries.add(entry(astral, 3, 5, "smart_upgrade_template", "stellar_sorcery:smart_upgrade_template",
                List.of("stellar_sorcery:smart_upgrade_template", "template", "smart spawner", "nullite", "upgrade"),
                List.of("smart_spawner", "arcforge"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.smart_upgrade_template.text.1")
                ),
                page(
                        recipe(
                                List.of("ACA", "ABA", "AAA"),
                                Map.of(
                                        "A", "minecraft:diamond",
                                        "B", "stellar_sorcery:nullite_block",
                                        "C", "stellar_sorcery:smart_upgrade_template"
                                ),
                                "stellar_sorcery:smart_upgrade_template"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.smart_upgrade_template.text.2")
                )));

        entries.add(entry(astral, 3, 6, "smart_spawner", "stellar_sorcery:smart_spawner",
                List.of("stellar_sorcery:smart_spawner", "smart spawner", "spawner", "template", "mob"),
                List.of("smart_upgrade_template", "astral_nexus"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.smart_spawner.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.smart_spawner.text.2")
                )));

        entries.add(entry(astral, 3, 7, "celestial_blade", "stellar_sorcery:celestial_blade",
                List.of("stellar_sorcery:celestial_blade", "celestial blade", "sword", "relic", "moon"),
                List.of("arcforge", "moonshine_catalyst"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.celestial_blade.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.celestial_blade.text.2")
                )));

        entries.add(entry(astral, 3, 8, "wraith_cloak", "stellar_sorcery:wraith_cloak",
                List.of("stellar_sorcery:wraith_cloak", "wraith cloak", "cloak", "wraith", "invisible"),
                List.of("soul_candle", "arcforge"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.wraith_cloak.text.1"),
                        text("codex_arcanum.stellar_sorcery.entry.wraith_cloak.text.2")
                )));

        return entries;
    }

    private GeneratedEntry entry(
            String categoryFolder,
            int tier,
            int order,
            String id,
            String icon,
            List<String> searchItems,
            List<String> related,
            JsonObject... pages
    ) {
        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("title_key", "codex_arcanum.stellar_sorcery.guide." + id + ".title");
        root.addProperty("type", "item");
        root.addProperty("icon", icon);
        root.add("search_items", toArray(searchItems));
        root.add("right_side", toArray(List.of(pages)));
        root.add("related", toArray(related));

        return new GeneratedEntry(categoryFolder, tier, String.format("%02d_%s", order, id), root);
    }

    private JsonObject page(JsonObject... modules) {
        JsonObject page = new JsonObject();
        page.add("modules", toArray(List.of(modules)));
        return page;
    }

    private JsonObject text(String key) {
        JsonObject module = new JsonObject();
        module.addProperty("module_type", "text");
        module.addProperty("text_key", key.replace(".entry.", ".guide."));
        return module;
    }

    private JsonObject recipe(List<String> pattern, Map<String, String> key, String result) {
        JsonObject module = new JsonObject();
        module.addProperty("module_type", "recipe");
        module.addProperty("recipe_type", "crafting_table");
        module.add("pattern", toArray(pattern));

        JsonObject keyObject = new JsonObject();
        key.forEach(keyObject::addProperty);
        module.add("key", keyObject);
        module.addProperty("result", result);
        return module;
    }

    private JsonObject furnace(String input, String output, float experience, int cookingTime) {
        JsonObject module = new JsonObject();
        module.addProperty("module_type", "furnace_recipe");
        module.addProperty("input", input);
        module.addProperty("output", output);
        module.addProperty("experience", experience);
        module.addProperty("cooking_time", cookingTime);
        return module;
    }

    private JsonArray toArray(List<?> values) {
        JsonArray array = new JsonArray();
        for (Object value : values) {
            if (value instanceof String stringValue) {
                array.add(stringValue);
            } else if (value instanceof JsonElement jsonElement) {
                array.add(jsonElement);
            }
        }
        return array;
    }

    private record GeneratedEntry(String categoryFolder, int tier, String fileName, JsonObject json) {
    }
}
