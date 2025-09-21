package net.zuperz.stellar_sorcery.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.StellarSorcery;

public class AmuletEssenceRecipe implements Recipe<RecipeInput> {
    private final ItemStack output;
    private final ItemStack input1;
    private final Ingredient input2;
    private final Ingredient amuletEssence1;
    private final Ingredient amuletEssence2;
    private final Ingredient amuletEssence3;

    public AmuletEssenceRecipe(ItemStack input1, Ingredient input2,
                               Ingredient amuletEssence1, Ingredient amuletEssence2, Ingredient amuletEssence3,
                               ItemStack output) {
        this.input1 = input1;
        this.input2 = input2;
        this.amuletEssence1 = amuletEssence1;
        this.amuletEssence2 = amuletEssence2;
        this.amuletEssence3 = amuletEssence3;
        this.output = output;
    }

    public ItemStack getInput1() { return input1; }
    public Ingredient getInput2() { return input2; }
    public ItemStack getOutput() { return output.copy(); }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (input.size() < 5) return false;
        return input1.is(input.getItem(0).getItem()) &&
                input2.test(input.getItem(1)) &&
                amuletEssence1.test(input.getItem(2)) &&
                amuletEssence2.test(input.getItem(3)) &&
                amuletEssence3.test(input.getItem(4));
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(input1));
        list.add(input2);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 5;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<AmuletEssenceRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "amulet_essence_recipe";
    }

    public static class Serializer implements RecipeSerializer<AmuletEssenceRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "amulet_essence_recipe");

        private static final MapCodec<AmuletEssenceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("input1").forGetter(r -> r.input1),
                Ingredient.CODEC.fieldOf("input2").forGetter(r -> r.input2),
                Ingredient.CODEC.fieldOf("amuletEssence1").forGetter(r -> r.amuletEssence1),
                Ingredient.CODEC.fieldOf("amuletEssence2").forGetter(r -> r.amuletEssence2),
                Ingredient.CODEC.fieldOf("amuletEssence3").forGetter(r -> r.amuletEssence3),
                ItemStack.CODEC.fieldOf("output").forGetter(r -> r.output)
        ).apply(instance, AmuletEssenceRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AmuletEssenceRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::write, Serializer::read);

        @Override
        public MapCodec<AmuletEssenceRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AmuletEssenceRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static void write(RegistryFriendlyByteBuf buf, AmuletEssenceRecipe recipe) {
            ItemStack.STREAM_CODEC.encode(buf, recipe.input1);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input2);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.amuletEssence1);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.amuletEssence2);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.amuletEssence3);
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
        }

        private static AmuletEssenceRecipe read(RegistryFriendlyByteBuf buf) {
            ItemStack in1 = ItemStack.STREAM_CODEC.decode(buf);
            Ingredient in2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient ae1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient ae2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient ae3 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack out = ItemStack.STREAM_CODEC.decode(buf);
            return new AmuletEssenceRecipe(in1, in2, ae1, ae2, ae3, out);
        }
    }
}