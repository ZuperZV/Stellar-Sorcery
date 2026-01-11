package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
import net.zuperz.stellar_sorcery.planet.PlanetCommandHandler;
import net.zuperz.stellar_sorcery.recipes.SoulCandleRecipe;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.zuperz.stellar_sorcery.block.custom.SoulCandleBlock.CRAFTING;
import static net.zuperz.stellar_sorcery.block.custom.SoulCandleBlock.LIT;


public class SoulCandleBlockEntity extends BlockEntity {
    public int progress = 0;
    public int maxProgress = 80;
    private int prevProgress = 0;
    private int itemsConsumed = 0;
    private List<ItemEntity> consumedEntities = new ArrayList<>();
    private final List<ItemStack> storedIngredients = new ArrayList<>();
    private Optional<SoulCandleRecipe> activeRecipe = Optional.empty();
    public EntityType<?> entityLastSacrificed = null;
    private float rotation;

    public SoulCandleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SoulCandleBlockEntity soulCandle) {
        soulCandle.prevProgress = soulCandle.progress;

        state = state.setValue(CRAFTING, soulCandle.progress > 0);
        level.setBlockAndUpdate(pos, state);

        if (state.getValue(LIT)) {

            if (soulCandle.activeRecipe.isEmpty()) {
                if (!soulCandle.tryStartCrafting(level)) {
                    soulCandle.progress = 0;
                    soulCandle.itemsConsumed = 0;
                    return;
                }
            }

            if (soulCandle.entityLastSacrificed != null) {
                takeEntityLastSacrificed(level, pos, soulCandle);
            }

            soulCandle.progress++;
            setCrafting(pos, level, true);

            SoulCandleRecipe recipe = soulCandle.activeRecipe.get();
            int totalItems = soulCandle.itemsConsumed + soulCandle.storedIngredients.size();

            if (totalItems > 0) {
                int interval = soulCandle.maxProgress / totalItems;

                if (soulCandle.itemsConsumed < totalItems &&
                        soulCandle.progress >= (soulCandle.itemsConsumed + 1) * interval) {

                    if (!soulCandle.consumedEntities.isEmpty()) {
                        ItemEntity target = soulCandle.consumedEntities.remove(0);

                        if (target == null || target.isRemoved()) {
                            soulCandle.abortCrafting(level, recipe);
                            return;
                        }

                        ItemStack stack = target.getItem();
                        stack.shrink(1);

                        level.playSound(null, target, SoundEvents.ITEM_PICKUP,
                                SoundSource.BLOCKS, 1f, 2f);

                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.ASH,
                                    target.getX() + 0.5,
                                    target.getY() + 0.1,
                                    target.getZ() + 0.5,
                                    5,
                                    0.2, 0.2, 0.2,
                                    0.01
                            );

                            serverLevel.sendParticles(ParticleTypes.CLOUD,
                                    target.getX() + 0.5,
                                    target.getY() + 0.1,
                                    target.getZ() + 0.5,
                                    20,
                                    0.3, 0.3, 0.3,
                                    0.01
                            );
                        }

                        if (stack.isEmpty()) {
                            target.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }

                    if (!soulCandle.storedIngredients.isEmpty()) {
                        soulCandle.storedIngredients.remove(0);
                    }

                    soulCandle.itemsConsumed++;

                    if (soulCandle.itemsConsumed >= totalItems) {
                        soulCandle.craftItem(recipe);
                        soulCandle.executeCommands(SoulCandleCommand.Trigger.ON_END, recipe);
                        soulCandle.resetCrafting();
                    }
                }
            }
        }

        if (soulCandle.activeRecipe.isPresent()) {
            SoulCandleRecipe recipe = soulCandle.activeRecipe.get();

            if (soulCandle.progress < soulCandle.maxProgress) {
                soulCandle.executeCommands(SoulCandleCommand.Trigger.EACH_TICK, recipe);

                if (soulCandle.progress == 1) {
                    soulCandle.executeCommands(SoulCandleCommand.Trigger.ON_START, recipe);
                }

                if (soulCandle.progress == soulCandle.maxProgress / 2) {
                    soulCandle.executeCommands(SoulCandleCommand.Trigger.AT_PROGRESS_HALF, recipe);
                }
            }
        }
    }

    private static void takeEntityLastSacrificed(Level level, BlockPos pos, SoulCandleBlockEntity soulCandle) {
        soulCandle.entityLastSacrificed = null;

        level.playSound(null, pos, SoundEvents.ITEM_PICKUP,
                SoundSource.BLOCKS, 1f, 2f);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ASH,
                    pos.getX() + 0.5,
                    pos.getY() + 0.6,
                    pos.getZ() + 0.5,
                    5,
                    0.2, 0.2, 0.2,
                    0.01
            );

            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5,
                    pos.getY() + 0.6,
                    pos.getZ() + 0.5,
                    20,
                    0.3, 0.3, 0.3,
                    0.01
            );
        }
    }

    private void executeCommands(SoulCandleCommand.Trigger trigger, SoulCandleRecipe recipe) {
        for (SoulCandleCommand cmd : recipe.commands) {
            if (cmd.getTrigger() != trigger) continue;

            String executedCommand = cmd.getCommand();

            switch (cmd.getTarget()) {
                case SOUL_CANDLE -> {
                    executedCommand = executedCommand
                            .replace("$posX", String.valueOf(getBlockPos().getX()))
                            .replace("$posY", String.valueOf(getBlockPos().getY()))
                            .replace("$posZ", String.valueOf(getBlockPos().getZ()))
                            .replace("$pos", getBlockPos().getX() + " " + getBlockPos().getY() + " " + getBlockPos().getZ());

                    System.out.println("SOUL_CANDLE command: " + executedCommand);
                }

                case PLAYER_CLOSEST -> {
                    var player = level.getNearestPlayer(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), 10, false);
                    if (player != null) {
                        String cmdForPlayer = executedCommand
                                .replace("$playerX", String.valueOf(player.getX()))
                                .replace("$playerY", String.valueOf(player.getY()))
                                .replace("$playerZ", String.valueOf(player.getZ()))
                                .replace("$player", player.getName().getString());

                        System.out.println("PLAYER_CLOSEST command: " + cmdForPlayer);
                    }
                }

                case ALL_PLAYERS -> {
                    List<Player> allPlayers = (List<Player>) level.players();
                    for (Player player : allPlayers) {
                        String cmdForPlayer = executedCommand
                                .replace("$playerX", String.valueOf(player.getX()))
                                .replace("$playerY", String.valueOf(player.getY()))
                                .replace("$playerZ", String.valueOf(player.getZ()))
                                .replace("$player", player.getName().getString());

                        System.out.println("ALL_PLAYERS command for " + player.getName().getString() + ": " + cmdForPlayer);
                    }
                }

                case PLAYERS_IN_5_BLOCKS -> {
                    AABB area = new AABB(getBlockPos()).inflate(5);
                    List<Player> playersInArea = level.getEntitiesOfClass(Player.class, area);
                    for (Player player : playersInArea) {
                        String cmdForPlayer = executedCommand
                                .replace("$player", player.getName().getString())
                                .replace("$playerX", String.valueOf(player.getX()))
                                .replace("$playerY", String.valueOf(player.getY()))
                                .replace("$playerZ", String.valueOf(player.getZ()));

                        System.out.println("PLAYERS_IN_5_BLOCKS command for " + player.getName().getString() + ": " + cmdForPlayer);
                    }
                }

                case PLANET -> {
                    if (!(level instanceof ServerLevel serverLevel)) return;
                    PlanetCommandHandler.execute(serverLevel, executedCommand);
                }
            }

            System.out.println("executedCommand: " + executedCommand);

            level.getServer().getCommands().performPrefixedCommand(
                    level.getServer().createCommandSourceStack(),
                    executedCommand );
        }
    }

    private void resetCrafting() {
        activeRecipe = Optional.empty();
        storedIngredients.clear();
        progress = 0;
        itemsConsumed = 0;
    }

    private void abortCrafting(Level level, SoulCandleRecipe recipe) {

        List<Ingredient> remainingIngredients = recipe.getIngredients().stream()
                .filter(ingredient ->
                        storedIngredients.stream().noneMatch(stack -> ingredient.test(stack))
                )
                .toList();

        for (Ingredient ingredient : remainingIngredients) {
            for (ItemStack stack : ingredient.getItems()) {
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(level,
                            worldPosition.getX() + 0.5,
                            worldPosition.getY() + 1,
                            worldPosition.getZ() + 0.5,
                            stack.copy());
                }
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ASH,
                    this.worldPosition.getX() + 0.5,
                    this.worldPosition.getY() + 0.1,
                    this.worldPosition.getZ() + 0.5,
                    5,
                    0.2, 0.2, 0.2,
                    0.01
            );
        }

        level.playSound(null, this.worldPosition, SoundEvents.ITEM_BREAK,
                SoundSource.BLOCKS, 1f, 2f);

        resetCrafting();
    }

    private boolean tryStartCrafting(Level level) {
        if (activeRecipe.isPresent()) return true;

        Optional<RecipeHolder<SoulCandleRecipe>> recipeOpt = getSoulCandleRecipeRecipeHolder(level);
        if (recipeOpt == null || recipeOpt.isEmpty()) return false;

        SoulCandleRecipe recipe = recipeOpt.get().value();

        List<ItemEntity> collectedEntities = new ArrayList<>();

        for (Ingredient ing : recipe.getIngredients()) {
            boolean found = false;

            for (int dx = -5; dx <= 5 && !found; dx++) {
                for (int dz = -2; dz <= 2 && !found; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos checkPos = this.worldPosition.offset(dx, 0, dz);
                    AABB checkArea = new AABB(checkPos).inflate(0.5);

                    List<ItemEntity> itemsOnGround = level.getEntitiesOfClass(ItemEntity.class, checkArea);
                    for (ItemEntity entity : itemsOnGround) {
                        ItemStack stack = entity.getItem();
                        if (ing.test(stack)) {
                            collectedEntities.add(entity);

                            found = true;
                            break;
                        }
                    }
                }
            }

            if (!found) {
                for (ItemEntity e : collectedEntities) {
                    Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), e.getItem());
                }
                return false;
            }
        }

        storedIngredients.clear();
        for (ItemEntity e : collectedEntities) {
            ItemStack copy = e.getItem().copy();
            copy.setCount(1);
            storedIngredients.add(copy);
        }

        consumedEntities = collectedEntities;
        activeRecipe = Optional.of(recipe);
        progress = 0;
        itemsConsumed = 0;
        maxProgress = recipe.recipeTime;


        return true;
    }

    public void craftItem(SoulCandleRecipe recipe) {
        Level level = this.level;
        if (level == null) return;

        ItemStack result = recipe.getResultItem(level.registryAccess()).copy();
        ItemEntity itemEntity = new ItemEntity(
                level,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                result
        );
        level.addFreshEntity(itemEntity);

        itemCraftingParticles(level);
    }

    private @Nullable Optional<RecipeHolder<SoulCandleRecipe>> getSoulCandleRecipeRecipeHolder(Level level) {
        Optional<RecipeHolder<SoulCandleRecipe>> recipeOpt = Optional.empty();

        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = this.worldPosition.offset(dx, 0, dz);
                AABB checkArea = new AABB(checkPos).inflate(0.5);
                List<ItemEntity> itemsOnGround = level.getEntitiesOfClass(ItemEntity.class, checkArea);

                for (ItemEntity itemEntity : itemsOnGround) {
                    ItemStack stack = itemEntity.getItem();
                    if (stack.isEmpty()) continue;

                    recipeOpt = level.getRecipeManager().getRecipeFor(
                            ModRecipes.ALTER_RECIPE_TYPE.get(),
                            new BlockRecipeInput(stack, worldPosition),
                            level
                    );

                    if (recipeOpt.isPresent()) break;
                }
            }
        }

        if (recipeOpt.isEmpty()) return null;
        return recipeOpt;
    }

    private void itemCraftingParticles(Level level) {
        if (level instanceof ServerLevel serverLevel) {

            spawnVisualLightningBolt(serverLevel, worldPosition);

            serverLevel.sendParticles(ParticleTypes.ASH,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.3,
                    worldPosition.getZ() + 0.5,
                    20, // antal partikler
                    0.1, 0.1, 0.1, // spread
                    0.01 // fart
            );

            serverLevel.sendParticles(ParticleTypes.CRIT,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.3,
                    worldPosition.getZ() + 0.5,
                    10, // antal partikler
                    0.1, 0.1, 0.1, // spread
                    0.01 // fart
            );
        }

        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 1f, 2f);

        level.playSound(null, worldPosition, SoundEvents.ALLAY_HURT,
                SoundSource.BLOCKS, 0.12f, 0.17f);

        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 0.3f, 0.2f);
    }

    public static void setCrafting(BlockPos altarPos, Level level, boolean boo) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = altarPos.offset(dx, 0, dz);
                BlockState state = level.getBlockState(checkPos);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (be instanceof AstralNexusBlockEntity nexus) {
                    if (boo) {
                        if (nexus.craftingStartTime == -1) {
                            nexus.craftingStartTime = level.getGameTime();
                            nexus.setChanged();
                        }
                    } else {
                        nexus.craftingStartTime = -1;
                        nexus.setChanged();
                    }
                }
            }
        }
    }

    // Sacrificed //

    public void setSacrificedEntity(EntityType<?> entityType) {
        this.entityLastSacrificed = entityType;
    }

    private void spawnVisualLightningBolt(ServerLevel level, BlockPos blockPos) {
        EntityType.LIGHTNING_BOLT.spawn(level, blockPos, MobSpawnType.TRIGGERED).setVisualOnly(true);
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360) {
            rotation = 0;
        }
        return rotation;
    }

    public static class BlockRecipeInput implements RecipeInput {
        private final ItemStack stack;
        private final BlockPos pos;

        public BlockRecipeInput(ItemStack stack, BlockPos pos) {
            this.stack = stack;
            this.pos = pos;
        }

        @Override
        public ItemStack getItem(int pIndex) {
            return stack;
        }

        @Override
        public int size() {
            return 1;
        }

        public ItemStack stack() {
            return stack;
        }

        public BlockPos pos() {
            return pos;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("progress", this.progress);
        tag.putInt("maxProgress", this.maxProgress);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("progress", progress);
        tag.putInt("maxProgress", maxProgress);
        tag.putInt("prevProgress", prevProgress);
        tag.putInt("itemsConsumed", itemsConsumed);

        if (entityLastSacrificed != null) {
            ResourceLocation id = EntityType.getKey(entityLastSacrificed);
            tag.putString("entityLastSacrificed", id.toString());
        }

        ListTag consumedList = new ListTag();
        for (ItemEntity entity : consumedEntities) {
            if (!entity.getItem().isEmpty()) {
                consumedList.add(entity.getItem().save(registries));
            }
        }
        tag.put("consumedEntities", consumedList);

        ListTag ingredientList = new ListTag();
        for (ItemStack stack : storedIngredients) {
            ingredientList.add(stack.save(registries));
        }
        tag.put("storedIngredients", ingredientList);

        activeRecipe.ifPresent(recipe -> {
            level.getRecipeManager().getAllRecipesFor(SoulCandleRecipe.Type.INSTANCE).stream()
                    .filter(holder -> holder.value() == recipe)
                    .findFirst()
                    .ifPresent(holder -> tag.putString("activeRecipe", holder.id().toString()));
        });
    }


    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        progress = tag.getInt("progress");
        maxProgress = tag.getInt("maxProgress");
        prevProgress = tag.getInt("prevProgress");
        itemsConsumed = tag.getInt("itemsConsumed");

        if (tag.contains("entityLastSacrificed")) {
            String id = tag.getString("entityLastSacrificed");
            ResourceLocation rl = ResourceLocation.parse(id);
            entityLastSacrificed = BuiltInRegistries.ENTITY_TYPE.get(rl);
        } else {
            entityLastSacrificed = null;
        }

        consumedEntities.clear();
        if (tag.contains("consumedEntities")) {
            ListTag consumedList = tag.getList("consumedEntities", 10);
            for (int i = 0; i < consumedList.size(); i++) {
                ItemStack stack = ItemStack.parseOptional(registries, consumedList.getCompound(i));
                if (!stack.isEmpty()) {
                    consumedEntities.add(new ItemEntity(level, 0, 0, 0, stack));
                }
            }
        }

        storedIngredients.clear();
        if (tag.contains("storedIngredients")) {
            ListTag ingredientList = tag.getList("storedIngredients", 10);
            for (int i = 0; i < ingredientList.size(); i++) {
                ItemStack stack = ItemStack.parseOptional(registries, ingredientList.getCompound(i));
                if (!stack.isEmpty()) {
                    storedIngredients.add(stack);
                }
            }
        }

        if (tag.contains("activeRecipe")) {
            ResourceLocation id = ResourceLocation.parse(tag.getString("activeRecipe"));
            activeRecipe = level.getRecipeManager()
                    .byKey(id)
                    .filter(r -> r.value() instanceof SoulCandleRecipe)
                    .map(r -> (SoulCandleRecipe) r.value());
        } else {
            activeRecipe = Optional.empty();
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}