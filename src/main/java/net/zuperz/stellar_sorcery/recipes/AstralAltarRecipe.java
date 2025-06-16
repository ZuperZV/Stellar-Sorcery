package net.zuperz.stellar_sorcery.recipes;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;

import java.util.*;
import java.util.stream.Collectors;

public class AstralAltarRecipe implements Recipe<RecipeInput> {

    public final ItemStack output;
    public final Ingredient moldIngredient;
    public final List<Optional<Ingredient>> additionalIngredients;

    public AstralAltarRecipe(ItemStack output, Ingredient moldIngredient, List<Optional<Ingredient>> additionalIngredients) {
        while (additionalIngredients.size() < 8) {
            additionalIngredients.add(Optional.empty());
        }
        this.output = output;
        this.moldIngredient = moldIngredient;
        this.additionalIngredients = additionalIngredients;
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        return output;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    public ItemStack getResultEmi() {
        return output.copy();
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (!(input instanceof AstralAltarBlockEntity.BlockRecipeInput blockInput)) return false;

        ItemStack moldStack = blockInput.stack();
        BlockPos center = blockInput.pos();

        if (!moldIngredient.test(moldStack)) return false;

        List<Ingredient> ingredientsToMatch = additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Set<Ingredient> unmatched = new HashSet<>(ingredientsToMatch);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = center.offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (!(be instanceof AstralNexusBlockEntity nexus)) continue;

                for (int slot = 0; slot < nexus.inventory.getSlots(); slot++) {
                    ItemStack stack = nexus.inventory.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;

                    for (Ingredient ing : unmatched) {
                        if (ing.test(stack)) {
                            unmatched.remove(ing);
                            break;
                        }
                    }

                    if (unmatched.isEmpty()) return true;
                }
            }
        }
        return unmatched.isEmpty();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(moldIngredient);
        for (Optional<Ingredient> optional : additionalIngredients) {
            optional.ifPresent(ingredients::add);
        }
        return ingredients;
    }

    public List<Ingredient> getAdditionalIngredients() {
        List<Ingredient> result = new ArrayList<>();
        for (Optional<Ingredient> optional : additionalIngredients) {
            optional.ifPresent(result::add);
        }
        return result;
    }

    public int getCountForIngredient(int index) {
        if (index == 0) return moldIngredient.getItems().length;
        int i = index - 1;
        if (i >= 0 && i < additionalIngredients.size()) {
            return additionalIngredients.get(i).map(ing -> ing.getItems().length).orElse(0);
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public String getGroup() {
        return "astral_altar";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static final class Type implements RecipeType<AstralAltarRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "astral_altar";
    }

    public static final class Serializer implements RecipeSerializer<AstralAltarRecipe> {
        private Serializer() {}
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "astral_altar");

        private final MapCodec<AstralAltarRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(
                    CodecFix.ITEM_STACK_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.moldIngredient),
                    Ingredient.CODEC.optionalFieldOf("ingredient2").forGetter(recipe -> recipe.additionalIngredients.get(0)),
                    Ingredient.CODEC.optionalFieldOf("ingredient3").forGetter(recipe -> recipe.additionalIngredients.get(1)),
                    Ingredient.CODEC.optionalFieldOf("ingredient4").forGetter(recipe -> recipe.additionalIngredients.get(2)),
                    Ingredient.CODEC.optionalFieldOf("ingredient5").forGetter(recipe -> recipe.additionalIngredients.get(3)),
                    Ingredient.CODEC.optionalFieldOf("ingredient6").forGetter(recipe -> recipe.additionalIngredients.get(4)),
                    Ingredient.CODEC.optionalFieldOf("ingredient7").forGetter(recipe -> recipe.additionalIngredients.get(5)),
                    Ingredient.CODEC.optionalFieldOf("ingredient8").forGetter(recipe -> recipe.additionalIngredients.get(6)),
                    Ingredient.CODEC.optionalFieldOf("ingredient9").forGetter(recipe -> recipe.additionalIngredients.get(7))
            ).apply(instance, (output, mold, ing2, ing3, ing4, ing5, ing6, ing7, ing8, ing9) -> new AstralAltarRecipe(output, mold, List.of(ing2, ing3, ing4, ing5, ing6, ing7, ing8, ing9)));
        });

        @Override
        public MapCodec<AstralAltarRecipe> codec() {
            return CODEC;
        }

        private static void write(RegistryFriendlyByteBuf buffer, AstralAltarRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.moldIngredient);
            for (Optional<Ingredient> optional : recipe.additionalIngredients) {
                buffer.writeBoolean(optional.isPresent());
                optional.ifPresent(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient));
            }
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.output);
        }

        private static AstralAltarRecipe read(RegistryFriendlyByteBuf buffer) {
            Ingredient mold = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            List<Optional<Ingredient>> ingredients = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                boolean present = buffer.readBoolean();
                if (present) {
                    ingredients.add(Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)));
                } else {
                    ingredients.add(Optional.empty());
                }
            }
            ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            return new AstralAltarRecipe(output, mold, ingredients);
        }

        private final StreamCodec<RegistryFriendlyByteBuf, AstralAltarRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::write, Serializer::read);

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AstralAltarRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}