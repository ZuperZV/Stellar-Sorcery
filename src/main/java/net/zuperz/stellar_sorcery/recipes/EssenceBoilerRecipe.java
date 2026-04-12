package net.zuperz.stellar_sorcery.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.fluids.FluidStack;

import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.CodecFix;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EssenceBoilerRecipe implements Recipe<FluidRecipeInput> {

    public final Ingredient mainIngredient;

    public final List<Optional<Ingredient>> additionalIngredients;

    public final FluidStack inputFluid;
    public final FluidStack outputFluid;
    public final int recipeTime;

    public EssenceBoilerRecipe(
            Ingredient mainIngredient,
            List<Optional<Ingredient>> additionalIngredients,
            FluidStack inputFluid,
            FluidStack outputFluid,
            int recipeTime
    ) {
        this.mainIngredient = mainIngredient;
        this.additionalIngredients = additionalIngredients;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.recipeTime = recipeTime;
    }

    @Override
    public boolean matches(FluidRecipeInput input, Level level) {

        FluidStack tankFluid = input.getFluidType();

        if (!tankFluid.getFluid().isSame(inputFluid.getFluid())) {
            return false;
        }

        if (tankFluid.getAmount() < inputFluid.getAmount()) {
            return false;
        }

        boolean mainMatched = false;
        List<Ingredient> matchedExtras = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {

            ItemStack stack = input.getItem(i);

            if (stack.isEmpty()) continue;

            if (!mainMatched && mainIngredient.test(stack)) {
                mainMatched = true;
                continue;
            }

            for (Optional<Ingredient> opt : additionalIngredients) {

                if (opt.isPresent() && opt.get().test(stack) && !matchedExtras.contains(opt.get())) {
                    matchedExtras.add(opt.get());
                    break;
                }
            }
        }

        return mainMatched;
    }

    @Override
    public ItemStack assemble(FluidRecipeInput input, net.minecraft.core.HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static final class Type implements RecipeType<EssenceBoilerRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "essence_boiler";
    }

    public static final class Serializer implements RecipeSerializer<EssenceBoilerRecipe> {

        private Serializer() {}
        public static final Serializer INSTANCE = new Serializer();

        public static final ResourceLocation ID =
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "essence_boiler");

        private final MapCodec<EssenceBoilerRecipe> CODEC =
                RecordCodecBuilder.mapCodec(instance -> instance.group(

                        Ingredient.CODEC_NONEMPTY
                                .fieldOf("ingredient")
                                .forGetter(r -> r.mainIngredient),

                        Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(recipe ->
                                recipe.additionalIngredients.stream()
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())
                        ),

                        CodecFix.OPTIONAL_FLUID_STACK_CODEC.fieldOf("input_fluid").forGetter(recipe -> recipe.inputFluid),
                        CodecFix.OPTIONAL_FLUID_STACK_CODEC.fieldOf("output_fluid").forGetter(recipe -> recipe.outputFluid),

                        Codec.INT.fieldOf("time")
                                .forGetter(r -> r.recipeTime)

                ).apply(instance, (main, extras, in, out, time) -> {

                    List<Optional<Ingredient>> optExtras =
                            extras.stream().map(Optional::of).collect(Collectors.toList());

                    return new EssenceBoilerRecipe(
                            main,
                            optExtras,
                            in,
                            out,
                            time
                    );
                }));

        @Override
        public MapCodec<EssenceBoilerRecipe> codec() {
            return CODEC;
        }

        private static void write(RegistryFriendlyByteBuf buffer, EssenceBoilerRecipe recipe) {

            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.mainIngredient);

            buffer.writeVarInt(recipe.additionalIngredients.size());

            for (Optional<Ingredient> opt : recipe.additionalIngredients) {
                buffer.writeBoolean(opt.isPresent());
                opt.ifPresent(i -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, i));
            }
            
            FluidStack.STREAM_CODEC.encode(buffer, recipe.inputFluid);
            FluidStack.STREAM_CODEC.encode(buffer, recipe.outputFluid);

            buffer.writeVarInt(recipe.recipeTime);
        }

        private static EssenceBoilerRecipe read(RegistryFriendlyByteBuf buffer) {

            Ingredient main = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);

            int size = buffer.readVarInt();

            List<Optional<Ingredient>> extras = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                boolean present = buffer.readBoolean();

                if (present) {
                    extras.add(Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)));
                } else {
                    extras.add(Optional.empty());
                }
            }

            FluidStack input = FluidStack.STREAM_CODEC.decode(buffer);
            FluidStack output = FluidStack.STREAM_CODEC.decode(buffer);
            int time = buffer.readVarInt();

            return new EssenceBoilerRecipe(main, extras, input, output, time);
        }

        private final StreamCodec<RegistryFriendlyByteBuf, EssenceBoilerRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::write, Serializer::read);

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EssenceBoilerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}