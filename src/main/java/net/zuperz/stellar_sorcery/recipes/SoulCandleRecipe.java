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
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
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
    public final Optional<TimeOfDay> timeOfDay;
    public final Optional<TimeOfDay> fakeTimeOfDay;
    public final List<SoulCandleCommand> commands;
    public final int recipeTime;

    public SoulCandleRecipe(ItemStack output, List<String> pattern, Map<String, Block> blockMapping, List<Optional<Ingredient>> additionalIngredients, Optional<EntityType<?>> entityType,
                            Optional<TimeOfDay> timeOfDay, Optional<TimeOfDay> fakeTimeOfDay, List<SoulCandleCommand> commands, int recipeTime) {
        this.pattern = pattern;
        this.blockMapping = blockMapping;
        this.entityType = entityType;
        this.timeOfDay = timeOfDay;
        this.fakeTimeOfDay = fakeTimeOfDay;
        this.commands = commands;
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

    private boolean HasEntitySacrificed(Level level, BlockPos center) {
        if (entityType.isPresent()) {
            BlockEntity be = level.getBlockEntity(center);
            if (!(be instanceof SoulCandleBlockEntity altar)) return true;

            if (altar.entityLastSacrificed == null) return true;

            if (!altar.entityLastSacrificed.equals(entityType.get())) return true;
        }
        return false;
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

                    TimeOfDay.CODEC.optionalFieldOf("time_of_day").forGetter(recipe -> recipe.timeOfDay),
                    TimeOfDay.CODEC.optionalFieldOf("fake_time_of_day").forGetter(recipe -> recipe.fakeTimeOfDay),

                    Codec.STRING.fieldOf("command").forGetter(r -> r.commands.isEmpty() ? "" : r.commands.get(0).getCommand()),
                    Codec.STRING.xmap(SoulCandleCommand.Target::valueOf, SoulCandleCommand.Target::name).fieldOf("target")
                            .forGetter(r -> r.commands.isEmpty() ? SoulCandleCommand.Target.SOUL_CANDLE : r.commands.get(0).getTarget()),
                    Codec.STRING.xmap(SoulCandleCommand.Trigger::valueOf, SoulCandleCommand.Trigger::name).fieldOf("trigger")
                            .forGetter(r -> r.commands.isEmpty() ? SoulCandleCommand.Trigger.ON_START : r.commands.get(0).getTrigger()),

                    Codec.INT.fieldOf("time").forGetter(r -> r.recipeTime)

            ).apply(instance, (output, pattern, blockMapping, additionalIngredients, entityType, timeOfDay, fakeTimeOfDay,
                               commandStr, target, trigger, recipeTime) ->
                    new SoulCandleRecipe(
                            output,
                            pattern,
                            blockMapping,
                            additionalIngredients,
                            entityType,
                            timeOfDay,
                            fakeTimeOfDay,
                            List.of(new SoulCandleCommand(commandStr, target, trigger)),
                            recipeTime
                    )
            );
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

            buffer.writeBoolean(recipe.timeOfDay.isPresent());
            recipe.timeOfDay.ifPresent(t -> buffer.writeUtf("BOTH")); // <--- do not lock at it. it is stil is datagenet

            buffer.writeBoolean(recipe.fakeTimeOfDay.isPresent());
            recipe.fakeTimeOfDay.ifPresent(t -> buffer.writeUtf(recipe.fakeTimeOfDay.get().toString()));

            buffer.writeVarInt(recipe.commands.size());
            for (SoulCandleCommand cmd : recipe.commands) {
                buffer.writeUtf(cmd.getCommand());
                buffer.writeUtf(cmd.getTarget().name());
                buffer.writeUtf(cmd.getTrigger().name());
            }

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

            int cmdCount = buffer.readVarInt();
            List<SoulCandleCommand> commands = new ArrayList<>();
            for (int i = 0; i < cmdCount; i++) {
                String command = buffer.readUtf();
                SoulCandleCommand.Target target = SoulCandleCommand.Target.valueOf(buffer.readUtf());
                SoulCandleCommand.Trigger trigger = SoulCandleCommand.Trigger.valueOf(buffer.readUtf());
                commands.add(new SoulCandleCommand(command, target, trigger));
            }

            int recipeTime = buffer.readVarInt();

            ItemStack output = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);

            return new SoulCandleRecipe(output, pattern, blockMapping, ingredients, entityType, timeOfDay, fakeTimeOfDay, commands, recipeTime);
        }

        private final StreamCodec<RegistryFriendlyByteBuf, SoulCandleRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::write, Serializer::read);

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SoulCandleRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}