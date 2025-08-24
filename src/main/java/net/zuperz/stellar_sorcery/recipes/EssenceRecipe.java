package net.zuperz.stellar_sorcery.recipes;

import com.mojang.serialization.Codec;
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

public class EssenceRecipe implements Recipe<RecipeInput> {
    private final ItemStack output;
    private final Ingredient input1;
    private final Ingredient input2;
    private final Ingredient input3;

    public EssenceRecipe(Ingredient input1, Ingredient input2, Ingredient input3, ItemStack output) {
        this.input1 = input1;
        this.input2 = input2;
        this.input3 = input3;
        this.output = output;
    }

    public Ingredient getInput1() { return input1; }
    public Ingredient getInput2() { return input2; }
    public Ingredient getInput3() { return input3; }
    public ItemStack getOutput() { return output.copy(); }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (input.size() < 3) return false;
        return input1.test(input.getItem(0)) &&
                input2.test(input.getItem(1)) &&
                input3.test(input.getItem(2));
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
        list.add(input1);
        list.add(input2);
        list.add(input3);
        return list;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return w * h >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<EssenceRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = "essence_recipe";
    }

    public static class Serializer implements RecipeSerializer<EssenceRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "essence_recipe");

        private static final MapCodec<EssenceRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("input1").forGetter(r -> r.input1),
                Ingredient.CODEC.fieldOf("input2").forGetter(r -> r.input2),
                Ingredient.CODEC.fieldOf("input3").forGetter(r -> r.input3),
                ItemStack.CODEC.fieldOf("output").forGetter(r -> r.output)
        ).apply(instance, EssenceRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, EssenceRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::write, Serializer::read);

        @Override
        public MapCodec<EssenceRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EssenceRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static void write(RegistryFriendlyByteBuf buf, EssenceRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input1);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input2);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.input3);
            ItemStack.STREAM_CODEC.encode(buf, recipe.output);
        }

        private static EssenceRecipe read(RegistryFriendlyByteBuf buf) {
            Ingredient in1 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient in2 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            Ingredient in3 = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            ItemStack out = ItemStack.STREAM_CODEC.decode(buf);
            return new EssenceRecipe(in1, in2, in3, out);
        }
    }
}