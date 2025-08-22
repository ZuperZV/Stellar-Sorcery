package net.zuperz.stellar_sorcery.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.*;
import java.util.stream.Collectors;

public class AstralAltarRecipe implements Recipe<RecipeInput> {

    public final ItemStack output;
    public final Ingredient moldIngredient;
    public final List<Optional<Ingredient>> additionalIngredients;
    public final Optional<EntityType<?>> entityType;
    public final Optional<String> requiredEssenceType;
    public final Optional<Block> additionalBlock;
    public final Optional<Map<String, String>> blockState;
    public final Optional<Boolean> needsBlock;
    public final Optional<Block> blockOutput;
    public final Optional<TimeOfDay> timeOfDay;
    public final int recipeTime;

    public AstralAltarRecipe(ItemStack output, Ingredient moldIngredient, List<Optional<Ingredient>> additionalIngredients, Optional<EntityType<?>> entityType, Optional<String> requiredEssenceType,
                             Optional<Block> additionalBlock, Optional<Map<String, String>> blockState, Optional<Boolean> needsBlock, Optional<Block> blockOutput,
                             Optional<TimeOfDay> timeOfDay, int recipeTime) {
        this.entityType = entityType;
        this.requiredEssenceType = requiredEssenceType;
        this.additionalBlock = additionalBlock;
        this.blockState = blockState;
        this.needsBlock = needsBlock;
        this.blockOutput = blockOutput;
        this.timeOfDay = timeOfDay;
        this.recipeTime = recipeTime;

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

        if (entityType.isPresent()) {
            BlockEntity be = level.getBlockEntity(center);
            if (!(be instanceof AstralAltarBlockEntity altar)) return false;

            if (altar.entityLastSacrificed == null) return false;

            System.out.println("entityLastSacrificed: " + altar.entityLastSacrificed);
            System.out.println("entityType: " + entityType);
            System.out.println("equals: " + (!altar.entityLastSacrificed.equals(entityType)));

            if (!altar.entityLastSacrificed.equals(entityType.get())) return false;
        }

        if (additionalBlock.isPresent() && needsBlock.orElse(false)) {
            boolean found = false;

            for (int dx = -2; dx <= 2 && !found; dx++) {
                for (int dz = -2; dz <= 2 && !found; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos checkPos = center.offset(dx, 0, dz);
                    BlockState stateAt = level.getBlockState(checkPos);
                    if (stateAt.getBlock().equals(additionalBlock.get())) {
                        if (blockState.isPresent()) {
                            Map<String, String> requiredStates = blockState.get();
                            boolean allMatch = true;
                            for (Map.Entry<String, String> entry : requiredStates.entrySet()) {
                                Property<?> property = stateAt.getBlock().getStateDefinition().getProperty(entry.getKey());
                                if (property == null) {
                                    allMatch = false;
                                    break;
                                }

                                Optional<? extends Comparable<?>> parsed = property.getValue(entry.getValue());
                                if (parsed.isEmpty() || !stateAt.getValue(property).equals(parsed.get())) {
                                    allMatch = false;
                                    break;
                                }
                            }

                            if (allMatch) {
                                found = true;
                            }
                        } else {
                            found = true;
                        }
                    }
                }
            }
            if (!found) {
                return false;
            }
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

                    if (stack.is(ModItems.ESSENCE_BOTTLE.get()) && requiredEssenceType.isPresent()) {
                        if (!stack.has(ModDataComponentTypes.ESSENCE_BOTTLE)) continue;

                        EssenceBottleData data = stack.get(ModDataComponentTypes.ESSENCE_BOTTLE.get());
                        boolean matches = checkEssenceItems(
                                requiredEssenceType.get(),
                                String.valueOf(data.getEmbeddedItem().getItem()),
                                String.valueOf(data.getEmbeddedItem1().getItem()),
                                String.valueOf(data.getEmbeddedItem2().getItem())
                        );

                        if (!matches) continue;
                    }


                    Ingredient matched = null;
                    for (Ingredient ing : unmatched) {
                        if (ing.test(stack)) {
                            matched = ing;
                            break;
                        }
                    }

                    if (matched != null) {
                        unmatched.remove(matched);
                    }

                    if (unmatched.isEmpty()) return true;
                }
            }
        }

        return unmatched.isEmpty();
    }

    public boolean checkEssenceItems(String input, String actualEssence, String actualEssence1, String actualEssence2) {
        String[] expectedItems = input.split(",");
        Arrays.sort(expectedItems);

        String[] actualItems = new String[] { actualEssence, actualEssence1, actualEssence2 };
        Arrays.sort(actualItems);

        return Arrays.equals(expectedItems, actualItems);
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

        private final MapCodec<AstralAltarRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(
                    CodecFix.ITEM_STACK_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),

                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(recipe -> recipe.moldIngredient),
                    Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(recipe ->
                            recipe.additionalIngredients.stream()
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .collect(Collectors.toList())
                    ),

                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("entityType").forGetter(recipe -> recipe.entityType),

                    Codec.STRING.optionalFieldOf("essence_type").forGetter(recipe -> recipe.requiredEssenceType),

                    BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block").forGetter(recipe -> recipe.additionalBlock),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("block_state").forGetter(recipe -> recipe.blockState),

                    Codec.BOOL.optionalFieldOf("needs_block").forGetter(recipe -> recipe.needsBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block_output").forGetter(recipe -> recipe.blockOutput),

                    TimeOfDay.CODEC.optionalFieldOf("time_of_day").forGetter(recipe -> recipe.timeOfDay),
                    Codec.INT.fieldOf("time").forGetter(recipe -> recipe.recipeTime)

            ).apply(instance, (output, mold, ingredients, entityType, essenceType, block, blockState, needsBlock, blockOutput, timeOfDay, recipeTime) -> {
                List<Optional<Ingredient>> optionalIngredients = ingredients.stream()
                        .map(Optional::ofNullable)
                        .collect(Collectors.toList());

                return new AstralAltarRecipe(output, mold, optionalIngredients, entityType, essenceType, block, blockState, needsBlock, blockOutput, timeOfDay, recipeTime);
            });
        });

        @Override
        public MapCodec<AstralAltarRecipe> codec() {
            return CODEC;
        }

        private static void write(RegistryFriendlyByteBuf buffer, AstralAltarRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.moldIngredient);

            buffer.writeVarInt(recipe.additionalIngredients.size());

            for (Optional<Ingredient> optional : recipe.additionalIngredients) {
                buffer.writeBoolean(optional.isPresent());
                optional.ifPresent(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient));
            }

            if (recipe.entityType.isPresent()) {
                buffer.writeBoolean(true);
                ByteBufCodecs.registry(Registries.ENTITY_TYPE).encode(buffer, recipe.entityType.get());
            } else {
                buffer.writeBoolean(false);
            }

            if (recipe.requiredEssenceType.isPresent()) {
                buffer.writeBoolean(true);
                buffer.writeUtf(recipe.requiredEssenceType.get());
            } else {
                buffer.writeBoolean(false);
            }

            if (recipe.additionalBlock.isPresent()) {
                buffer.writeBoolean(true);
                Block block = recipe.additionalBlock.get();
                buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
            } else {
                buffer.writeBoolean(false);
            }

            if (recipe.blockState.isPresent()) {
                buffer.writeBoolean(true);
                Map<String, String> stateMap = recipe.blockState.get();
                buffer.writeVarInt(stateMap.size());
                for (Map.Entry<String, String> entry : stateMap.entrySet()) {
                    buffer.writeUtf(entry.getKey());
                    buffer.writeUtf(entry.getValue());
                }
            } else {
                buffer.writeBoolean(false);
            }

            buffer.writeBoolean(recipe.needsBlock.isPresent());
            recipe.needsBlock.ifPresent(buffer::writeBoolean);

            if (recipe.blockOutput.isPresent()) {
                buffer.writeBoolean(true);
                Block block = recipe.blockOutput.get();
                buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(block));
            } else {
                buffer.writeBoolean(false);
            }

            buffer.writeBoolean(recipe.timeOfDay.isPresent());
            recipe.timeOfDay.ifPresent(t -> buffer.writeUtf("BOTH")); // <--- do not lock at it. it is stil is datagenet

            buffer.writeVarInt(recipe.recipeTime);

            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.output);
        }

        private static AstralAltarRecipe read(RegistryFriendlyByteBuf buffer) {
            Ingredient mold = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);

            int ingredientCount = buffer.readVarInt();

            List<Optional<Ingredient>> ingredients = new ArrayList<>();

            for (int i = 0; i < ingredientCount; i++) {
                boolean present = buffer.readBoolean();
                if (present) {
                    ingredients.add(Optional.of(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer)));
                } else {
                    ingredients.add(Optional.empty());
                }
            }

            Optional<EntityType<?>> entityType = Optional.empty();
            if (buffer.readBoolean()) {
                entityType = Optional.of(ByteBufCodecs.registry(Registries.ENTITY_TYPE).decode(buffer));
            }

            Optional<String> requiredEssenceType = Optional.empty();
            if (buffer.readBoolean()) {
                requiredEssenceType = Optional.of(buffer.readUtf());
            }

            Block additionalBlock = null;
            if (buffer.readBoolean()) {
                ResourceLocation loc = buffer.readResourceLocation();
                additionalBlock = BuiltInRegistries.BLOCK.get(loc);
            }

            Optional<Map<String, String>> blockState = Optional.empty();
            if (buffer.readBoolean()) {
                int size = buffer.readVarInt();
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    String key = buffer.readUtf();
                    String value = buffer.readUtf();
                    map.put(key, value);
                }
                blockState = Optional.of(map);
            }

            Optional<Boolean> needsBlock = Optional.empty();
            if (buffer.readBoolean()) {
                needsBlock = Optional.of(buffer.readBoolean());
            }

            Block blockOutput = null;
            if (buffer.readBoolean()) {
                ResourceLocation loc = buffer.readResourceLocation();
                blockOutput = BuiltInRegistries.BLOCK.get(loc);
            }

            Optional<TimeOfDay> timeOfDay = Optional.empty();
            if (buffer.readBoolean()) {
                timeOfDay = Optional.of(TimeOfDay.valueOf(buffer.readUtf().toUpperCase()));
            }

            int recipeTime = buffer.readVarInt();

            ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);

            return new AstralAltarRecipe(output, mold, ingredients, entityType, requiredEssenceType, Optional.ofNullable(additionalBlock), blockState, needsBlock, Optional.ofNullable(blockOutput), timeOfDay, recipeTime);
        }

        private final StreamCodec<RegistryFriendlyByteBuf, AstralAltarRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::write, Serializer::read);

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AstralAltarRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}