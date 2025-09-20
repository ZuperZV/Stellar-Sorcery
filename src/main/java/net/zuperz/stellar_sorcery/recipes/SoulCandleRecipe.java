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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.SoulCandleBlockEntity;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.CodecFix;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.TimeOfDay;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class SoulCandleRecipe implements Recipe<RecipeInput> {

    public final ItemStack output;
    public final List<String> pattern;
    public final Map<String, Block> blockMapping;
    public final List<Optional<Ingredient>> additionalIngredients;
    public final Optional<EntityType<?>> entityType;
    public final Optional<String> requiredEssenceType;
    public final Optional<Block> additionalBlock;
    public final Optional<Map<String, String>> blockState;
    public final Optional<Boolean> needsBlock;
    public final Optional<Block> blockOutput;
    public final Optional<TimeOfDay> timeOfDay;
    public final Optional<TimeOfDay> fakeTimeOfDay;
    public final int recipeTime;

    public SoulCandleRecipe(ItemStack output, List<String> pattern, Map<String, Block> blockMapping, List<Optional<Ingredient>> additionalIngredients, Optional<EntityType<?>> entityType, Optional<String> requiredEssenceType,
                            Optional<Block> additionalBlock, Optional<Map<String, String>> blockState, Optional<Boolean> needsBlock, Optional<Block> blockOutput,
                            Optional<TimeOfDay> timeOfDay, Optional<TimeOfDay> fakeTimeOfDay, int recipeTime) {
        this.pattern = pattern;
        this.blockMapping = blockMapping;
        this.entityType = entityType;
        this.requiredEssenceType = requiredEssenceType;
        this.additionalBlock = additionalBlock;
        this.blockState = blockState;
        this.needsBlock = needsBlock;
        this.blockOutput = blockOutput;
        this.timeOfDay = timeOfDay;
        this.fakeTimeOfDay = fakeTimeOfDay;
        this.recipeTime = recipeTime;

        this.additionalIngredients = new ArrayList<>(additionalIngredients);
        while (this.additionalIngredients.size() < 8) {
            this.additionalIngredients.add(Optional.empty());
        }

        this.output = output;
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
        if (!(input instanceof SoulCandleBlockEntity.BlockRecipeInput blockInput)) return false;
        BlockPos center = blockInput.pos();

        if (HasEntitySacrificed(level, center)) return false;

        if (extractedAdditionalBlock(level, center)) return false;

        if (IsTimeOfDay(level)) return false;

        Set<Ingredient> unmatched = getIngredients(level, center);
        if (unmatched == null) return true;

        if (!checkPattern(level, center)) return false;

        return unmatched.isEmpty();
    }

    private boolean checkPattern(Level level, BlockPos center) {
        int startY = center.getY();
        int startZ = center.getZ() - pattern.size() / 2;

        for (int row = 0; row < pattern.size(); row++) {
            String line = pattern.get(row);
            int startX = center.getX() - line.length() / 2;

            for (int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
                if (symbol == '_') continue;

                Block expected = blockMapping.get(symbol);
                if (expected == null) continue;

                BlockPos checkPos = new BlockPos(startX + col, startY, startZ + row);
                if (!level.getBlockState(checkPos).is(expected)) {
                    System.out.println("false");
                    return false;
                }
            }
        }
        System.out.println("true");
        return true;
    }

    private @Nullable Set<Ingredient> getIngredients(Level level, BlockPos center) {
        List<Ingredient> ingredientsToMatch = additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Set<Ingredient> unmatched = new HashSet<>(ingredientsToMatch);

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = center.offset(dx, 0, dz);

                AABB checkArea = new AABB(checkPos).inflate(0.5);
                List<ItemEntity> itemsOnGround = level.getEntitiesOfClass(ItemEntity.class, checkArea);

                for (ItemEntity itemEntity : itemsOnGround) {
                    ItemStack stack = itemEntity.getItem();
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

                    if (unmatched.isEmpty()) return null;
                }
            }
        }
        return unmatched;
    }

    private boolean IsTimeOfDay(Level level) {
        if (fakeTimeOfDay.isPresent()) {
            boolean isDay = level.isDay();
            switch (fakeTimeOfDay.get()) {
                case DAY -> {
                    if (!isDay) return true;
                }
                case NIGHT -> {
                    if (isDay) return true;
                }
                case BOTH -> {
                }
            }
        }
        return false;
    }

    private boolean extractedAdditionalBlock(Level level, BlockPos center) {
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
                return true;
            }
        }
        return false;
    }

    private boolean HasEntitySacrificed(Level level, BlockPos center) {
        if (entityType.isPresent()) {
            BlockEntity be = level.getBlockEntity(center);
            if (!(be instanceof SoulCandleBlockEntity altar)) return true;

            if (altar.entityLastSacrificed == null) return true;

            if (!altar.entityLastSacrificed.equals(entityType.get())) return true;
        }
        return false;
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
        return "altar";
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static final class Type implements RecipeType<SoulCandleRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "altar";
    }

    public static final class Serializer implements RecipeSerializer<SoulCandleRecipe> {
        private Serializer() {}
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "altar");

        private final MapCodec<SoulCandleRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(
                    CodecFix.ITEM_STACK_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),

                    Codec.STRING.listOf().fieldOf("pattern").forGetter(recipe -> recipe.pattern),
                    Codec.unboundedMap(Codec.STRING, BuiltInRegistries.BLOCK.byNameCodec())
                            .fieldOf("blockMapping")
                            .forGetter(recipe -> recipe.blockMapping),

                    Ingredient.CODEC.listOf().xmap(
                                   list -> list.stream().map(Optional::ofNullable).toList(),
                                    list -> list.stream().filter(Optional::isPresent).map(Optional::get).toList()
                            ).optionalFieldOf("ingredients", List.of())
                            .forGetter(recipe -> recipe.additionalIngredients),

                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("entityType").forGetter(recipe -> recipe.entityType),

                    Codec.STRING.optionalFieldOf("essence_type").forGetter(recipe -> recipe.requiredEssenceType),

                    BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block").forGetter(recipe -> recipe.additionalBlock),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("block_state").forGetter(recipe -> recipe.blockState),

                    Codec.BOOL.optionalFieldOf("needs_block").forGetter(recipe -> recipe.needsBlock),
                    BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block_output").forGetter(recipe -> recipe.blockOutput),

                    TimeOfDay.CODEC.optionalFieldOf("time_of_day").forGetter(recipe -> recipe.timeOfDay),
                    TimeOfDay.CODEC.optionalFieldOf("fake_time_of_day").forGetter(recipe -> recipe.fakeTimeOfDay),

                    Codec.INT.fieldOf("time").forGetter(recipe -> recipe.recipeTime)

            ).apply(instance, SoulCandleRecipe::new);
        });

        @Override
        public MapCodec<SoulCandleRecipe> codec() {
            return CODEC;
        }

        private static void write(RegistryFriendlyByteBuf buffer, SoulCandleRecipe recipe) {
            buffer.writeVarInt(recipe.pattern.size());
            for (String row : recipe.pattern) {
                buffer.writeUtf(row);
            }

            buffer.writeVarInt(recipe.blockMapping.size());
            for (Map.Entry<String, Block> entry : recipe.blockMapping.entrySet()) {
                buffer.writeUtf(String.valueOf(entry.getKey()));
                buffer.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(entry.getValue()));
            }

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

            buffer.writeBoolean(recipe.fakeTimeOfDay.isPresent());
            recipe.fakeTimeOfDay.ifPresent(t -> buffer.writeUtf(recipe.fakeTimeOfDay.get().toString()));

            buffer.writeVarInt(recipe.recipeTime);

            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.output);
        }

        private static SoulCandleRecipe read(RegistryFriendlyByteBuf buffer) {

            int patternRows = buffer.readVarInt();
            List<String> pattern = new ArrayList<>();
            for (int i = 0; i < patternRows; i++) {
                pattern.add(buffer.readUtf());
            }

            int mappingSize = buffer.readVarInt();
            Map<String, Block> blockMapping = new HashMap<>();
            for (int i = 0; i < mappingSize; i++) {
                String key = buffer.readUtf();
                ResourceLocation blockId = buffer.readResourceLocation();
                blockMapping.put(key, BuiltInRegistries.BLOCK.get(blockId));
            }

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
                System.out.println("timeOfDay: " + timeOfDay);
            }

            Optional<TimeOfDay> fakeTimeOfDay = Optional.empty();
            if (buffer.readBoolean()) {
                fakeTimeOfDay = Optional.of(TimeOfDay.valueOf(buffer.readUtf().toUpperCase()));
                System.out.println("fakeTimeOfDay: " + fakeTimeOfDay);
            }

            int recipeTime = buffer.readVarInt();

            ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);

            return new SoulCandleRecipe(output, pattern, blockMapping, ingredients, entityType, requiredEssenceType, Optional.ofNullable(additionalBlock), blockState, needsBlock, Optional.ofNullable(blockOutput), fakeTimeOfDay, timeOfDay, recipeTime);
        }

        private final StreamCodec<RegistryFriendlyByteBuf, SoulCandleRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::write, Serializer::read);

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SoulCandleRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}