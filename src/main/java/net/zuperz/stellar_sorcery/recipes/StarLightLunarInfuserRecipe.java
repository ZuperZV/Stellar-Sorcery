package net.zuperz.stellar_sorcery.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.LunarInfuserBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.VitalStumpBlockEntity;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.*;
import java.util.stream.Collectors;

public class StarLightLunarInfuserRecipe implements Recipe<FluidRecipeInput> {

    public final FluidStack output;
    public final Ingredient ingredient;
    public final Optional<TimeOfDay> timeOfDay;
    public final int recipeTime;

    public StarLightLunarInfuserRecipe(FluidStack output, Ingredient Ingredient, Optional<TimeOfDay> timeOfDay, int recipeTime) {
        this.timeOfDay = timeOfDay;
        this.recipeTime = recipeTime;
        this.ingredient = Ingredient;

        this.output = output;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return Items.DIAMOND.getDefaultInstance();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(2);
        ingredients.add(0, ingredient);
        return ingredients;
    }

    @Override
    public boolean matches(FluidRecipeInput input, Level level) {
        if(level.isClientSide()) {
            return false;
        }

        if (timeOfDay.isPresent()) {
            boolean isDay = level.isDay();
            switch (timeOfDay.get()) {
                case DAY -> {
                    if (!isDay) return false;
                }
                case NIGHT -> {
                    if (isDay) return false;
                }
                case BOTH -> {
                }
            }
        }
        return ingredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(FluidRecipeInput p_345149_, HolderLookup.Provider p_346030_) {
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public String getGroup() {
        return "star_light_lunar_infuser";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static final class Type implements RecipeType<StarLightLunarInfuserRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "star_light_lunar_infuser";
    }

    public static final class Serializer implements RecipeSerializer<StarLightLunarInfuserRecipe> {
        private Serializer() {}
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "star_light_lunar_infuser");

        private final MapCodec<StarLightLunarInfuserRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(
                    CodecFix.FLUID_STACK_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.ingredient),

                    TimeOfDay.CODEC.optionalFieldOf("time_of_day").forGetter(recipe -> recipe.timeOfDay),
                    Codec.INT.fieldOf("time").forGetter(recipe -> recipe.recipeTime)

            ).apply(instance, StarLightLunarInfuserRecipe::new);
        });

        @Override
        public MapCodec<StarLightLunarInfuserRecipe> codec() {
            return CODEC;
        }

        private static void write(RegistryFriendlyByteBuf buffer, StarLightLunarInfuserRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);

            buffer.writeBoolean(recipe.timeOfDay.isPresent());
            recipe.timeOfDay.ifPresent(t -> buffer.writeUtf("BOTH")); // <--- do not lock at it. it is stil is datagenet

            buffer.writeVarInt(recipe.recipeTime);

            FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.output);
        }

        private static StarLightLunarInfuserRecipe read(RegistryFriendlyByteBuf buffer) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);

            Optional<TimeOfDay> timeOfDay = Optional.empty();
            if (buffer.readBoolean()) {
                timeOfDay = Optional.of(TimeOfDay.valueOf(buffer.readUtf().toUpperCase()));
            }
            int recipeTime = buffer.readVarInt();

            FluidStack output = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);

            return new StarLightLunarInfuserRecipe(output, ingredient, timeOfDay, recipeTime);
        }

        private final StreamCodec<RegistryFriendlyByteBuf, StarLightLunarInfuserRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::write, Serializer::read);

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StarLightLunarInfuserRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}